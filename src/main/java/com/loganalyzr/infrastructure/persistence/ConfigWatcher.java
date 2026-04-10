package com.loganalyzr.infrastructure.persistence;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;

/**
 * Monitorea cambios en archivos de configuración usando el WatchService nativo de Java.
 *
 * Qué observa:
 *   - Directorio configurable (por defecto: directorio de trabajo actual).
 *   - Eventos ENTRY_MODIFY en los archivos declarados en watchedFileNames.
 *   - Cuando detecta un cambio, espera debounceMs y luego ejecuta el callback.
 *
 * Debounce:
 *   Muchos editores generan múltiples eventos al guardar un archivo (escritura
 *   parcial + rename, etc.). El debounce agrupa eventos cercanos en el tiempo
 *   y ejecuta el callback solo una vez por "ráfaga" de cambios.
 *
 * Thread model:
 *   Corre en un daemon thread ("config-watcher"). No bloquea el shutdown de la JVM.
 *
 * Limitaciones conocidas:
 *   - WatchService en Linux usa inotify (eficiente). En macOS usa polling (menos preciso).
 *   - No detecta cambios en subdirectorios.
 *   - Solo ENTRY_MODIFY; creación/borrado de config no se trata.
 */
public class ConfigWatcher {

    private static final int DEFAULT_DEBOUNCE_MS = 500;

    private final Path watchDirectory;
    private final Set<String> watchedFileNames;
    private final Runnable onChangeCallback;
    private final int debounceMs;

    private Thread watchThread;
    private volatile boolean running = false;

    public ConfigWatcher(Path watchDirectory,
                         Set<String> watchedFileNames,
                         Runnable onChangeCallback) {
        this(watchDirectory, watchedFileNames, onChangeCallback, DEFAULT_DEBOUNCE_MS);
    }

    public ConfigWatcher(Path watchDirectory,
                         Set<String> watchedFileNames,
                         Runnable onChangeCallback,
                         int debounceMs) {
        this.watchDirectory   = watchDirectory;
        this.watchedFileNames = watchedFileNames;
        this.onChangeCallback = onChangeCallback;
        this.debounceMs       = debounceMs;
    }

    public void start() {
        running = true;
        watchThread = new Thread(this::watch, "config-watcher");
        watchThread.setDaemon(true);
        watchThread.start();
        System.out.printf("[ConfigWatcher] Monitoreando %s — archivos: %s%n",
                watchDirectory.toAbsolutePath(), watchedFileNames);
    }

    public void stop() {
        running = false;
        if (watchThread != null) {
            watchThread.interrupt();
        }
    }

    private void watch() {
        try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
            watchDirectory.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);

            long lastTriggeredAt = 0;

            while (running && !Thread.currentThread().isInterrupted()) {

                // poll con timeout para poder chequear 'running' periódicamente.
                WatchKey key = watcher.poll(1, java.util.concurrent.TimeUnit.SECONDS);
                if (key == null) continue;

                boolean relevantChange = false;

                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.OVERFLOW) continue;

                    Path changedFile = (Path) event.context();
                    if (watchedFileNames.contains(changedFile.getFileName().toString())) {
                        relevantChange = true;
                        System.out.printf("[ConfigWatcher] Cambio detectado en: %s%n",
                                changedFile.getFileName());
                    }
                }

                key.reset();

                if (!relevantChange) continue;

                // Debounce: ignorar eventos dentro del ventana de tiempo.
                long now = System.currentTimeMillis();
                if (now - lastTriggeredAt < debounceMs) continue;

                // Esperar a que el editor termine de escribir el archivo.
                Thread.sleep(debounceMs);
                lastTriggeredAt = System.currentTimeMillis();

                // Ejecutar el callback en el mismo hilo del watcher.
                // El callback (ConfigReloader.reload) es thread-safe.
                try {
                    onChangeCallback.run();
                } catch (Exception e) {
                    System.err.println("[ConfigWatcher] Error en callback de recarga: " + e.getMessage());
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[ConfigWatcher] Detenido.");
        } catch (IOException e) {
            System.err.println("[ConfigWatcher] Error iniciando WatchService: " + e.getMessage());
        }
    }
}
