# LoganAlyzr

**LoganAlyzr** es un agente de monitoreo y análisis de logs en tiempo real, diseñado para desacoplar la ingesta de datos de la lógica de negocio mediante una arquitectura modular. Su objetivo es transformar flujos de datos crudos (logs) en información accionable a través de reglas de configuración dinámicas.

---

## 🚀 Visión del Proyecto

El problema con los sistemas de logs tradicionales es el ruido: gigabytes de texto donde es difícil encontrar errores críticos. Desarrollé **LoganAlyzr** para solucionar esto mediante un enfoque de "escucha activa". No se trata solo de leer archivos, sino de entender su estructura (ya sea JSON o Texto plano) y aplicar un motor de reglas flexible que detecte anomalías, errores o patrones específicos de negocio sin necesidad de recompilar el código.

## ✨ Características Principales

* **Ingesta Polimórfica:** Capacidad para procesar múltiples formatos de logs simultáneamente gracias a una arquitectura basada en interfaces (`LogSource`).
    * Soporte nativo para **JSON Logs** (NDJSON) utilizando la librería Jackson para un parsing de alto rendimiento.
    * Soporte para **Logs de Texto** tradicionales mediante expresiones regulares (Regex) configurables.
* **Monitoreo en Tiempo Real:** Implementación eficiente de lectura tipo "tail" (`RandomAccessFile`), procesando solo los nuevos bytes escritos en el archivo sin recargar el historial completo.
* **Motor de Reglas (En desarrollo):** Configuración externa vía JSON para definir criterios de alerta (Niveles de severidad, Keywords, Rangos de fecha).
* **Arquitectura Hexagonal:** El núcleo del dominio está aislado de la infraestructura, permitiendo cambiar el origen de los datos o el destino de las alertas sin afectar la lógica de negocio.

## 🛠️ Stack Tecnológico

* **Lenguaje:** Java 17+
* **Core:** Diseño Orientado a Objetos (OOP), SOLID Principles.
* **Librerías:**
    * `com.fasterxml.jackson.core`: Para el procesamiento robusto de estructuras JSON.
    * `java.util.regex`: Para el análisis de patrones en texto no estructurado.
* **Gestión de Dependencias:** Maven/Gradle (Ajustar según corresponda).

## ⚙️ Arquitectura y Diseño

El proyecto sigue una estructura de **Puertos y Adaptadores**:

1.  **Domain (Core):** Define qué es un `LogEvent` y las reglas de negocio.
2.  **Ports:** Interfaces como `LogSource` que definen contratos de entrada.
3.  **Adapters (Infrastructure):** Implementaciones concretas como `JsonLogReader` y `SmartFileReader`.

```text
[ File System ] --> [ JsonLogReader ] --( implements )--> [ LogSource ] --> [ Agent ]

## 📋 Configuración (Preview)
El sistema utiliza un archivo rules.json para definir dinámicamente qué buscar. Ejemplo de la estructura soportada:

JSON

{
  "matchMode": "ANY",
  "levels": ["ERROR", "FATAL"],
  "keywords": [
    {
      "keyword": "database connection",
      "caseSensitive": false
    }
  ]
}
