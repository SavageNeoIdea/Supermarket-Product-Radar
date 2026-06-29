# Supermarket Product Radar 🛒

### Desarrollado por **SavageNeoIdea**

Bienvenido a **Supermarket Product Radar**, una solución de software empresarial impulsada enteramente por eventos (**EDA - Event-Driven Architecture**) bajo una **Arquitectura Lambda** para la optimización en tiempo real del presupuesto de la cesta de la compra.

Este sistema automatiza la recopilación de productos de las cadenas Mercadona e Hiperdino, procesa la información mediante un bróker de mensajería, almacena el histórico en un *Event Store* inmutable y consolida los datos en una unidad de negocio (*Business Unit*). Al inicializarse, reconstruye el estado completo del mercado y lo actualiza continuamente de forma reactiva para calcular de forma inteligente la combinación de compra óptima mediante un **Motor de Búsqueda Semántica basado en Inteligencia Artificial**, exponiendo el denominado **"Impuesto de la pereza"**.

---

## 1. Descripción del Proyecto y Propuesta de Valor

### Descripción General

**Supermarket Product Radar** es un ecosistema multimódulo distribuido que elimina la dependencia directa de APIs HTTP externas en caliente, apoyándose en su lugar en una red de publicación/suscripción de eventos. El sistema está compuesto por 4 módulos principales:

1. **Scraper Mercadona:** Módulo con planificador integrado (*Scheduler*) configurado por horas específicas para extraer productos de la plataforma de Mercadona y publicar eventos de actualización de precios, los extrae a través de su api interno, por lo que no se pueden obtener productos locales como productos Tirma en el caso de canarias, pero existen la mayoría de productos.
2. **Scraper Hiperdino:** Módulo homólogo que extrae productos de Hiperdino mediante técnicas de web scraping distribuidas (Playwright) haciendo uso de un código postal, publicando sus eventos según la localización regional y la hora programada.
3. **EventStoreBuilder:** Componente encargado de escuchar de forma persistentemente los eventos de productos y almacenarlos cronológicamente en un almacén de eventos inmutable (*Event Store*).
4. **Business Unit:** El núcleo de inteligencia de negocio. Implementa una **Arquitectura Lambda** para la gestión de datos: al arrancar, reconstruye su estado leyendo el histórico completo para inicializar su base de datos local (Datamart) impulsada por Embeddings de IA y, posteriormente, procesa eventos en tiempo real para mantener el catálogo fresco, exponiendo un motor interactivo de optimización de cestas. Cabe destacar que este proceso de actualización a tiempo real solo funciona cuando se hace uso de un horario de scraping, ya que el sistema está preparado para actualizar su catálogo cada X tiempo teniendo en cuenta los horarios de scrapping.

### Propuesta de Valor: El "Impuesto de la Pereza" (*Laziness Tax*)

El sistema permite procesar una lista de la compra en formato de texto libre y genera automáticamente tres proyecciones financieras:

* **Cesta Optimizada:** La combinación ideal y más barata del mercado, cruzando dinámicamente los productos de ambos supermercados mediante coincidencia semántica conceptual (venciendo las diferencias de nombres en el catálogo).
* **Cesta Solo Mercadona:** Productos añadidos y coste total si el usuario decide comprar exclusivamente en este establecimiento.
* **Cesta Solo Hiperdino:** Productos añadidos y coste total si el usuario decide comprar exclusivamente en este establecimiento.

> 💡 **Métrica Clave:** El sistema calcula y muestra explícitamente el **Análisis de Pérdidas**, que representa la cantidad exacta de dinero que el usuario pierde por la comodidad de no diversificar su compra entre ambos establecimientos (ir a uno solo en lugar de acudir a ambos cuando toca).

## 2. El Motor de Inteligencia Artificial Semántica e Híbrida

En el Modulo de BusinessUnit se hace uso de un motor de semántica como bien se mencionó antes.

El sistema cuenta con un motor de **Búsqueda Híbrida** que combina la potencia de la recuperación semántica basada en vectores con el ajuste fino de la coincidencia léxica tradicional. No es el mejor modelo del mundo pero es lo suficientemente decente como para funcionar, premia las coincidencias exactas o de raíces de palabras críticas para el negocio alimentario ya que compara lo que escribas con lo que existe en la base de datos.

### Arquitectura del Componente e Ingesta

El motor está gobernado por la clase `EmbeddingService` bajo la abstracción de LangChain4j. El flujo de procesamiento se divide en tres etapas críticas:

1. **Sanitización del Texto (`sanitizeText`):** Antes de cualquier cálculo, el texto se normaliza bajo el estándar Unicode NFD. Se eliminan diacríticos (acentos, tildes), caracteres especiales y mayúsculas, colapsando espacios duplicados para homogeneizar las cadenas.
2. **Generación de Embeddings Cuantizados:** Se integra de manera nativa el modelo **`all-MiniLM-L6-v2` cuantizado** en formato ONNX. Este transforma el texto sanitizado en un vector denso de punto flotante de **384 dimensiones**. La cuantización optimiza el uso de CPU y memoria en entornos productivos.
3. **Cálculo de Similitud:** Delegamos en funciones matemáticas nativas de LangChain4j para evitar desbordamientos o errores de precisión manuales.

---

### El Modelo Matemático del Score Híbrido

Para determinar la relevancia final de un producto frente a la consulta del usuario, el sistema calcula una puntuación compuesta (Maximización de Score).

Si la similitud semántica inicial no supera el umbral crítico de seguridad ($0.45$), el producto es **descartado de inmediato** (retornando `null`). Si lo supera, se aplica la siguiente fórmula:

$$Score_{Hybrid} = Score_{Semantic} + Bonus_{Lexical}$$

Donde los componentes matemáticos y lógicos se definen así:

#### 1. Similitud Semántica ($Score_{Semantic}$)

Es el mapeo de la similitud de coseno ($\cos(\theta)$) escalado al rango cerrado de $[0, 1]$ a través de la utilidad `RelevanceScore`:

$$Score_{Semantic} = \text{RelevanceScore}(\cos(\theta))$$

$$\cos(\theta) = \frac{\vec{A} \cdot \vec{B}}{\|\vec{A}\| \|\vec{B}\|}$$

#### Bono Léxico Dinámico ($Bonus_{Lexical}$)

Para asegurar que los términos clave introducidos por el usuario tengan un peso específico, el sistema aplica una bonificación según el nivel de coincidencia textual:

* **Coincidencia Exacta ($+0.30$):** Si la cadena de la consulta está contenida por completo dentro del nombre del producto (o viceversa).
* **Coincidencia Parcial por Lematización Heurística ($+0.15 + 0.02 \cdot m$):** Si no hay coincidencia exacta, el sistema descompone la consulta en palabras (mayores a 3 caracteres) y extrae su raíz matemática mediante un algoritmo de lematización secuencial para el idioma español (remoción de plurales `es`/`s` y sufijos de género `o`/`a`). Por cada raíz coincidente ($m$), se incrementa el bono.

A nivel de funciones, el comportamiento del bono se resume en:

$$Bonus_{Lexical} = \begin{cases} 0.30 & \text{si existe coincidencia exacta} \\ 0.15 + (0.02 \cdot m) & \text{si existen } m \text{ raíces del usuario en el producto} \\ 0 & \text{si no hay coincidencias léxicas} \end{cases}$$

---

### Umbrales y Control de Calidad

* **`SIMILARITY_THRESHOLD = 0.45`:** Actúa como un cortafuegos semántico. Previene que productos con nula relación conceptual con la búsqueda contaminen los resultados del usuario, cancelando el procesamiento de bonos léxicos si no se cumple.

Las variables SIMILARITY_THRESHOLD, EXACT_MATCH_BONUS y PARTIAL_MATCH_BONUS se encuentran en el módulo BusinessUnit en el paquete org.sni.businessUnit.embedding en la única implementación EmbeddingService, puedes modificarla si quieres juguetear con el modelo.
---

## 3. Arquitectura del Sistema e Infraestructura Concurrente

### 1. Vectorización Asíncrona en Lote (Batch Ingestion)

Durante la fase de *Replay* de la Arquitectura Lambda, el sistema debe procesar miles de registros históricos de precios. Calcular los embeddings uno a uno degradaría el tiempo de arranque.

Se implementó un pool de hilos que usa todos los hilos disponibles (`ExecutorService` con hilos asignados según los núcleos de la máquina) que procesa las filas del *Event Store* en paralelo. Esto distribuye de forma balanceada el cálculo matricial de los embeddings en la CPU, reduciendo la latencia de inicialización en aproximadamente un **80%**. Eso si, espera que la cpu llega a porcentages de hasta el 100% ya que va a usar durante unos segundos toda su capacidad.

### 2. Motor de Búsqueda Concurrente (Producer-Consumer)

Al realizar la optimización de la lista de la compra de un usuario, el motor de consultas en `SQLiteQuery` divide el trabajo mediante un patrón de productor-consumidor:

* **Productor:** El hilo principal realiza un barrido secuencial de baja latencia a través del cursor nativo del `ResultSet` de SQLite.
* **Consumidores (Workers):** Una matriz de hilos secundarios toma los datos de los productos extraídos del flujo de la base de datos y computa de forma simultánea la fórmula de similitud de coseno y la puntuación compuesta en paralelo.

---

### Arquitectura de Mensajería y Flujo Lambda

El sistema hace uso de una arquitectura Lambda.

El flujo de información se distribuye a través de tópicos mediante el bróker de mensajería parametrizado en el archivo `config.json`:

<img width="1032" height="587" alt="{F3135A59-46BC-4000-B5E6-9A47F3D371DE}" src="https://github.com/user-attachments/assets/259b749c-ceec-463e-82c0-f0c1c847f00f" />

### Arquitectura de la Aplicación (Módulos Internos)

```
+---------------------------------------------------------------------------------+
|                                   config.json                                   |
| (Centraliza variables de brokers, bases de datos, tiempos de scraping y topics)  |
+---------------------------------------------------------------------------------+
                                       |
       +-------------------------------+-------------------------------+
       |                               |                               |
       v                               v                               v
[ Módulos Scrapers ]         [ EventStoreBuilder ]             [ Business Unit ]
 - Planificación Horaria      - Subscriber dedicado             - Replay Engine (Multi-Threaded)
 - Algoritmo Extracción       - Persistencia Inmutable          - AI Semantic Engine (all-MiniLM)
 - Publisher ("product")      - Registro cronológico            - Datamart SQLite + Vectors

```

---

## 4. Principios y Patrones de Diseño Aplicados

Para garantizar un ecosistema mantenible, escalable y robusto, el desarrollo se ha guiado por las mejores prácticas de la ingeniería de software:

* **Patrón Publish-Subscriber (Pub/Sub):** Desacoplamiento total entre los productores de datos (scrapers) y los consumidores finales.
* **Event Sourcing:** Salvaguarda el ciclo de vida del producto como una serie de eventos mutativos, impidiendo la sobrescritura directa de datos de precios.
* **Capa de Persistencia Idempotente:** La base de datos SQLite aprovecha un mecanismo de `UPSERT` para garantizar la consistencia e idempotencia de los datos semánticos, evitando duplicar registros si el lote de eventos vuelve a ser emitido.
* **Principio de Responsabilidad Única (SRP):** Prestamos especial atención al diseño modular.
* **Inyección de Dependencias y Desacoplamiento:** Uso sistemático de **interfaces** y almacenamiento (`Store`, `WebScraper`, ...), facilitando la sustitución de componentes y los entornos de pruebas unitarias.

---

## 5. Configuración del Sistema (`config.json`)

Toda la infraestructura de comunicación, almacenamiento local de consultas, regionalización y planificación horaria de los scrapers se parametriza mediante un archivo centralizado ubicado en la raíz del proyecto:

```json
{
  "subscribers": {
    "eventStoreSubscriber": {
      "brokerUrl": "tcp://localhost:61616",
      "topicName": "product",
      "clientId": "EventStoreBuilder_Subscriber",
      "subscriptionName": "MainEventStoreSub",
      "username": "admin",
      "password": "admin"
    },
    "businessUnitSubscriber": {
      "datamartUrl": "jdbc:sqlite:datamart.db?journal_mode=WAL&busy_timeout=5000",
      "brokerUrl": "tcp://localhost:61616",
      "topicName": "product",
      "clientId": "BusinessUnitSubscriber",
      "subscriptionName": "realtimeDatamartUpdate",
      "username": "admin",
      "password": "admin"
    }
  },
  "publishers": {
    "hiperdino": {
      "brokerUrl": "tcp://localhost:61616",
      "topicName": "product",
      "username": "admin",
      "password": "admin",
      "postalCode": "35010",
      "ScheduleTimeHour": "11",
      "ScheduleTimeMinutes": "30"
    },
    "mercadona": {
      "brokerUrl": "tcp://localhost:61616",
      "topicName": "product",
      "username": "admin",
      "password": "admin",
      "ScheduleTimeHour": "11",
      "ScheduleTimeMinutes": "30"
    }
  }
}

```

### Guía de Configuración y Restricciones del Sistema (`config.json`)

Para garantizar la estabilidad del sistema y evitar fallos de ejecución, es fundamental comprender qué elementos son estructurales y cuáles son parametrizables:

#### Elementos Estructurales (Inmutables)

**No se deben borrar ni renombrar** las claves principales del archivo: `publishers`, `subscribers`, `hiperdino`, `mercadona`, `eventStoreSubscriber` y `businessUnitSubscriber`. Modificar la estructura jerárquica o el nombre de estos bloques provocará errores críticos en el parseo de la configuración de la aplicación.

#### Parámetros Modificables y Buenas Prácticas

* **Consistencia de Red y Seguridad:** Todos los bloques de configuración deben compartir exactamente los mismos valores para las propiedades `topicName`, `brokerUrl`, `username` y `password` para garantizar que scrapers, almacén y unidad de negocio operen bajo el mismo bus de comunicación masiva.
* **Identificadores de Conexión:** Los campos `clientId` y `subscriptionName` son de libre elección para el administrador, asegurando que cada cliente mantenga una sesión única e identificable dentro del bróker de mensajería.
* **Planificación Horaria del Scraping:** Las propiedades `ScheduleTimeHour` y `ScheduleTimeMinutes` determinan la hora exacta en la que se disparará el proceso automático de extracción de datos para cada supermercado de forma diaria. El planificador toma como referencia la **hora local** del dispositivo o servidor donde se esté ejecutando el módulo.
---

## 6. Instrucciones de Compilación y Ejecución

Para garantizar la integridad de la arquitectura Lambda, evitar la pérdida de mensajes y asegurar que el Motor de IA y los Scrapers arranquen sin conflictos de dependencias, **sigue estrictamente el orden de configuración y encendido detallado a continuación**:

### Requisitos Previos y Configuración del Entorno

* **Bróker de Mensajería:** Tener instalado y en ejecución un bróker de mensajería (Actualmente, el único implementado es **Apache ActiveMQ**) escuchando en el puerto configurado (por defecto, `tcp://localhost:61616`, lo puedes cambiar en el `config.json`, cambiando el atributo `brokerUrl` de los 4 módulos).
* **Fichero de Configuración:** Asegurar que el archivo `config.json` esté correctamente ubicado en la raíz del directorio principal que engloba todos los módulos del proyecto.
* **Entorno de Desarrollo (IDE Recomendado):** Se requiere el uso de **IntelliJ IDEA**. El proyecto delega en este IDE la gestión automática del ciclo de vida y las dependencias de herramientas del sistema (como la instalación interna y ejecución transparente de Chromium para el módulo de Web Scraping automatizado por Playwright), garantizando un despliegue sin configuraciones adicionales en el sistema operativo.
* **Versión de Java y SDK:** El ecosistema está desarrollado bajo **Microsoft OpenJDK 21** y requiere un nivel de lenguaje (**Language Level**) fijado estrictamente en **21 - Record patterns, pattern matching for switch**.

---

### Configuración del Proyecto en IntelliJ IDEA

Al abrir el proyecto por primera vez, encontrarás los 4 módulos, el readme, el config.json y una carpeta de eventstore/product con eventos de prueba (estan organizados por fecha en formato YYYYMMDD, el sistema cogerá la fecha mas reciente):

<img width="1918" height="1003" alt="{466B208E-4F36-4294-8201-96B5FEF37286}" src="https://github.com/user-attachments/assets/aa908bc0-661c-4537-b8e4-6bbc7107811d" />

Realiza estos dos pasos para que el entorno entienda la disposición de los módulos y use el compilador correcto:

#### Paso 1: Configurar el JDK y Language Level 21

Para evitar errores de compilación por discrepancias de versiones, configura el SDK global del proyecto:

1. **Abrir Project Structure:** Paso 1.1.
En la barra de menús superior de IntelliJ IDEA, dirígete a `File` (Archivo) y selecciona `Project Structure...` (Estructura del proyecto), o utiliza el atajo de teclado `Ctrl + Alt + Shift + S` (`Cmd + ;` en macOS).

2. **Configurar el SDK del Proyecto:** Paso 1.2.
En el menú lateral izquierdo, haz clic en la sección `Project`. Dentro del apartado **SDK**, selecciona **Microsoft OpenJDK 21**. En caso de no tenerlo instalado, despliega las opciones, selecciona *Download JDK*, elige el proveedor *Microsoft* y descarga la versión 21.

3. **Ajustar el Language Level:** Paso 1.3.
Justo debajo de la selección del SDK, localiza el desplegable **Project language level** y asegúrate de marcar la opción **21 - Record patterns, pattern matching for switch**. Haz clic en `Apply` y luego en `OK`.

#### Paso 2: Vinculación de Módulos y Descarga Global de Dependencias

Como los módulos están distribuidos en subcarpetas independientes y no existe un archivo `pom.xml` unificado en la raíz, puedes forzar a Maven a que recorra recursivamente todas las subcarpetas del directorio principal para descargar las librerías y compilar todo el ecosistema de una sola pasada.

Para abrir la **Terminal de IntelliJ**, utiliza el atajo de teclado `Alt + F12` (`⌥ + F12` en macOS) o haz clic en la pestaña **Terminal** situada en la barra de herramientas inferior del IDE. Al abrirse, ya estará situada automáticamente en la raíz de tu proyecto principal (donde se encuentra tu archivo `config.json`).

Copia y pega en la terminal el comando correspondiente al sistema operativo en el que esté corriendo tu IntelliJ:

* **Si estás en Windows (PowerShell por defecto en IntelliJ):**
```powershell
Get-ChildItem -Recurse -Filter pom.xml | ForEach-Object { mvn -f $_.FullName clean install -DskipTests }
```

* **Si estás en Linux / macOS (Bash / Zsh):**
```bash
find . -name "pom.xml" -exec mvn -f {} clean install -DskipTests \;
```

> 💡 **¿Qué hace este comando?** Escanea automáticamente todo el árbol de directorios, localiza cada archivo `pom.xml` individual (de cada supermercado, el almacén y la unidad de negocio) y le aplica el ciclo de vida de Maven (`clean install`) de manera secuencial utilizando el parámetro `-f` para forzar el apuntado.
> Esto limpia residuos antiguos, compila bajo el JDK 21, salta las pruebas de entorno (`-DskipTests`) e instala absolutamente todas las dependencias pesadas (Playwright, controladores SQLite, librerías del modelo de IA, etc.) de un solo golpe.
> *Tras finalizar el proceso, IntelliJ IDEA sincronizará automáticamente la jerarquía de carpetas. Si por algún motivo un módulo específico no se iluminara en verde como proyecto Java activo, puedes ir de forma manual a su `pom.xml`, hacer clic derecho y seleccionar **Add as Maven Project**.*
---

### Modo de Ejecución Inmediata (Omitir el Programador / Schedulers)

Por defecto, los scrapers esperan a la hora configurada en el `config.json`. Si deseas **forzar una ejecución inmediata** para probar el sistema sin esperar a la hora programada, realiza la siguiente modificación en los puntos de entrada:

* **Módulo Hiperdino:** Dirígete al archivo `Main.java`, localiza la línea 23, sustituye la lógica del *scheduler* posterior a la instancia del controlador e invoca directamente al método de inicialización:
Vamos, borra la linea 23 y copia tal cual lo siguiente (executionTime se saca automáticamente):
```java
controller.startScheduler(executionTime);
```
En caso de querer volver a la versión con sheduler:
```java

```

* **Módulo Mercadona:** Realiza el mismo procedimiento en su respectivo `Main.java` (línea 23), reemplazando el código del planificador por la ejecución directa del flujo pasando la URL del sitemap:
Vamos, borra la linea 23 y copia tal cual lo siguiente (sitemapUrl y executionTime se sacan automáticamente, no tienes que hacer nada mas)
```java
controller.run(sitemapUrl);
```
En caso de querer volver a la versión con sheduler:
```java
controller.scheduleDailyRun(sitemapUrl, executionTime);
```
---

### Paso 3: Pasos para el Arranque Secuencial del Sistema

Una vez compilado todo limpiamente, ejecuta las clases `Main.java` de los componentes en este orden exacto para permitir la correcta sincronización de las capas de datos distribuidas:

1. **Levantar el Bróker de Mensajería:** Fase de Infraestructura.
Asegúrate de que la instancia de tu bróker (ActiveMQ o cualquier nueva implementación que quieras) esté operativa y aceptando conexiones de red en el puerto configurado (`61616`).

2. **Iniciar el Event Store (EventStoreBuilder):** Fase de Almacenamiento.
Ejecuta la clase `Main.java` dentro del módulo `EventStoreBuilder`. Este componente se mantendrá en escucha persistente y comenzará a registrar de forma inmutable en el disco cualquier flujo de datos entrante.

3. **Desplegar la Unidad de Negocio (Business Unit):** Fase de Inteligencia.
Ejecuta la clase `Main.java` del módulo `BusinessUnit`. Al arrancar, el sistema se conectará a la base de datos local SQLite, activará el pool de hilos concurrente para procesar el **Replay de la Capa Batch** (traduciendo los datos históricos del paso anterior a vectores de IA) y, al finalizar, dejará abierta la escucha en la **Capa Speed** para capturar actualizaciones en tiempo real.

4. **Activar los Scrapers de Origen:** Fase de Alimentación.
Ejecuta las clases `Main.java` de los módulos `Hiperdino` y `Mercadona`. Estos activarán sus tareas programadas basadas en el archivo de configuración (o se ejecutarán de inmediato si aplicaste el cambio opcional en las líneas 23) para comenzar a nutrir de datos al radar.

## 7. Ejemplos de Uso e Interacción

Al iniciar el módulo **Business Unit**, después de un breve lapso de tiempo donde se reconstruye el último día de cada topic/source del eventsotre como un datamart y se inicie la conexión con el activeMQ (si está encendido), se desplegará la interfaz interactiva por consola basada en comandos de teclado. Puedes introducir tus términos de búsqueda y la IA tratará de buscar un top 5 de aquellos resultados que mas se adecuen, a veces puede fallar, hemos implementado un top 5 para que puedas elegir o saltartelo en caso de no encontrar una respuesta satisfactoria.

### Flujo en la Consola de Comandos

```text
Bienvenido a la lista de compra automatica de Hiperdino!: elige una opción:
1. Crear lista de la compra
2. Observar la lista creada
3. Salir del programa:
Responde seleccionando uno de los números del teclado:
>
```

1. Introduce `1` para iniciar el asistente de entrada de datos (`Creando lista...`). El sistema te pedirá los artículos línea por línea hasta que escribas `fin` o dejes una línea vacía. Puedes escribir libremente sin preocuparte por marcas o palabras exactas (p. ej., *"aceite para freír"* mapeará correctamente a *"Aceite de Girasol"*).
2. Introduce `2` (`Cargando tu lista actual...`) para invocar al motor analítico de Inteligencia Artificial. Este ejecutará la función de coste y puntuación vectorial en paralelo y generará el reporte de ahorro comparativo.

#### Ejemplo Real de Salida del Reporte Analítico Semántico (`buildShopList`)

```text
====================================================================
🛒              LISTA DE LA COMPRA CONJUNTA OPTIMIZADA               
====================================================================
- [MERCADONA] Aceite Oliva Suave 1L: 7.20€ | Cantidad: 1 (unidad)
- [HIPERDINO] Salmon Fresco Rodajas: 4.50€ | Cantidad: 1 (pack)
- [HIPERDINO] Leche Entera Clásica: 5.10€ | Cantidad: 6 (litros)
--------------------------------------------------------------------
💰 COSTE TOTAL OPTIMIZADO (Yendo a ambos): 16.80€
====================================================================

====================================================================
📊                 ANÁLISIS DE PÉRDIDAS POR COMPRAR EN UN SOLO SITIO   
====================================================================
▶️ SI COMPRAS TODO EN MERCADONA:
   • Coste Total: 19.45€
   • ❌ Dinero que pierdes por no ir a Hiperdino cuando toca: 2.65€

▶️ SI COMPRAS TODO EN HIPERDINO:
   • Coste Total: 18.20€
   • ❌ Dinero que pierdes por no ir a Mercadona cuando toca: 1.40€
====================================================================

```
