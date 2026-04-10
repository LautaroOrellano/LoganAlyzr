package com.loganalyzr.infrastructure.persistence;

import com.loganalyzr.core.ports.LogSource;

/**
 * Crea el LogSource correcto según el formato detectado en el archivo.
 *
 * Flujo:
 *   1. LogFormatDetector inspecciona las primeras líneas del archivo.
 *   2. LogSourceFactory instancia el reader correspondiente.
 *   3. Main usa esta factory en lugar de hardcodear JsonLogReader.
 *
 * Agregar soporte para un nuevo formato:
 *   1. Agregar el valor al enum LogFormatDetector.LogFormat.
 *   2. Agregar el regex en LogFormatDetector.matchLine().
 *   3. Crear el reader en infrastructure/persistence/.
 *   4. Agregar el case aquí.
 */
public class LogSourceFactory {

    private final LogFormatDetector detector = new LogFormatDetector();

    /**
     * Detecta el formato del archivo y retorna el LogSource apropiado.
     *
     * @param filePath Ruta al archivo de logs.
     * @return LogSource configurado para el formato detectado.
     */
    public LogSource create(String filePath) {
        LogFormatDetector.LogFormat format = detector.detect(filePath);

        return switch (format) {
            case JSONL    -> new JsonLogReader(filePath);
            case LOGBACK  -> new LogbackLogReader(filePath);
            case SPRING   -> new LogbackLogReader(filePath); // mismo parser, Spring es compatible
            case LOG4J    -> new SmartFileReader(filePath,
                                "(ERROR|WARN|INFO|DEBUG|TRACE|FATAL)\\s+\\[(\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2})\\]\\s+-\\s+(.+)",
                                "yyyy-MM-dd HH:mm:ss");
            case NGINX    -> new NginxLogReader(filePath);
            case PLAIN    -> new PlainTextLogReader(filePath);
        };
    }
}
