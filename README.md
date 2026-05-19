# Supermarket Product Radar 🛒

### Desarrollado por **SavageNeoIdea**

Bienvenido a **Supermarket Product Radar**, una solución de software empresarial impulsada enteramente por eventos (**EDA - Event-Driven Architecture**) bajo una **Arquitectura Lambda** para la monitorización, trazabilidad histórica y optimización en tiempo real del presupuesto de la cesta de la compra.

Este sistema automatiza la recopilación de productos de las cadenas Mercadona e Hiperdino, procesa la información mediante un bróker de mensajería, almacena el histórico en un *Event Store* inmutable y consolida los datos en una unidad de negocio (*Business Unit*). Al inicializarse, reconstruye el estado completo del mercado y lo actualiza continuamente de forma reactiva para calcular de forma inteligente la combinación de compra óptima mediante un **Motor de Búsqueda Semántica basado en Inteligencia Artificial**, exponiendo el denominado **"Impuesto de la pereza"**.

---

## 1. Descripción del Proyecto y Propuesta de Valor

### Descripción General

**Supermarket Product Radar** es un ecosistema multimódulo distribuido que elimina la dependencia directa de APIs HTTP externas en caliente, apoyándose en su lugar en una red de publicación/suscripción de eventos. El sistema está compuesto por 4 módulos principales:

1. **Scraper Mercadona:** Módulo con planificador integrado (*Scheduler*) configurado por horas específicas para extraer productos de la plataforma de Mercadona y publicar eventos de actualización de precios.
2. **Scraper Hiperdino:** Módulo homólogo que extrae productos de Hiperdino mediante técnicas de web scraping distribuidas (Playwright), publicando sus eventos según la localización regional y la hora programada.
3. **EventStoreBuilder:** Componente encargado de escuchar de forma persistentemente los eventos de productos y almacenarlos cronológicamente en un almacén de eventos inmutable (*Event Store*).
4. **Business Unit:** El núcleo de inteligencia de negocio. Implementa una **Arquitectura Lambda** para la gestión de datos: al arrancar, reconstruye su estado leyendo el histórico completo para inicializar su base de datos local (Datamart) impulsada por Embeddings de IA y, posteriormente, procesa eventos en tiempo real para mantener el catálogo fresco, exponiendo un motor interactivo de optimización de cestas.

### Propuesta de Valor: El "Impuesto de la Pereza" (*Laziness Tax*)

El sistema permite procesar una lista de la compra en formato de texto libre y genera automáticamente tres proyecciones financieras:

* **Cesta Optimizada:** La combinación ideal y más barata del mercado, cruzando dinámicamente los productos de ambos supermercados mediante coincidencia semántica conceptual (venciendo las diferencias de nombres en el catálogo).
* **Cesta Solo Mercadona:** Coste total si el usuario decide comprar exclusivamente en este establecimiento.
* **Cesta Solo Hiperdino:** Coste total si el usuario decide comprar exclusivamente en este establecimiento.

> 💡 **Métrica Clave:** El sistema calcula y muestra explícitamente el **Análisis de Pérdidas**, que representa la cantidad exacta de dinero que el usuario pierde por la comodidad de no diversificar su compra entre ambos establecimientos (ir a uno solo en lugar de acudir a ambos cuando toca).

---

## 2. El Motor de Inteligencia Artificial Semántica

El problema de las búsquedas tradicionales por cadenas de texto cruzadas (como el operador `LIKE` de SQL) es su rigidez: si un usuario busca *"aceite de oliva"*, el sistema fallará en indexar eficazmente términos como *"óleo de oliva"* o formatos abreviados específicos de la industria. Para solucionar esto, el sistema migró a un motor de **Recuperación Semántica Basada en Vectores**.

### Modelado de Embeddings

El sistema integra de manera nativa la clase `IAService` acoplada al modelo preentrenado de última generación `all-MiniLM-L6-v2`. Este modelo transforma cualquier descripción textual de un producto (p. ej., *"Salmón fresco en rodajas 250g"*) en un vector denso de punto flotante en un espacio de 384 dimensiones.

Estas dimensiones capturan las relaciones conceptuales, las categorías de alimentos y las propiedades físicas del producto de manera abstracta.

### El Modelo Matemático de Puntuación (Scoring Function)

Para determinar cuál es el producto óptimo que se debe emparejar con la petición en texto libre del usuario, la aplicación no solo evalúa lo bien que se describe el producto, sino también su impacto económico en la cesta. Implementamos una **función de puntuación compuesta no lineal** descrita por la siguiente fórmula matemática:

$$Score = \frac{\text{Price per SI Unit}}{\text{Similarity}^3}$$

Donde los componentes se definen de la siguiente manera:

* **$\text{Price per SI Unit}$:** Representa el precio normalizado del producto calculado matemáticamente respecto a su unidad estándar del Sistema Internacional (Euros por Kilogramo o Euros por Litro). Esto garantiza una comparación justa entre un envase de 500g y uno de 1kg.
* **$\text{Similarity}$:** Es la similitud de coseno ($\cos(\theta)$) calculada en un rango cerrado de $[0, 1]$ entre el vector de la consulta del usuario ($A$) y el vector del producto en el Datamart ($B$):

$$\text{Similarity} = \frac{A \cdot B}{\|A\| \|B\|} = \frac{\sum_{i=1}^{n} A_i B_i}{\sqrt{\sum_{i=1}^{n} A_i^2} \sqrt{\sum_{i=1}^{n} B_i^2}}$$

* **El Factor Cúbico ($\text{Similarity}^3$):** Al elevar exponencialmente la similitud al cubo, penalizamos severamente cualquier desviación semántica. Si la similitud disminuye ligeramente (p. ej., de $0.95$ a $0.70$), el denominador se reduce de forma drástica ($0.95^3 \approx 0.857$ frente a $0.70^3 \approx 0.343$). Como resultado, el $Score$ final se dispara positivamente. Debido a que el sistema busca **minimizar** el $Score$, los productos con baja coincidencia conceptual o un precio unitario desorbitado quedan descartados de inmediato.

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

```
[ Scraper Mercadona ] --------(Publica en: "product")--------> [ Bróker Mensajería ]
[ Scraper Hiperdino ] --------(Publica en: "product")--------> [   tcp://61616    ]
                                                                      |
       +--------------------------------------------------------------+
       | (SPEED LAYER: Suscripción en tiempo real)                    | (BATCH LAYER: Almacén Histórico)
       v                                                              v
[ Business Unit ]                                              [ EventStoreBuilder ]
 (Topic: "product")                                             (Topic: "product")
       ^
       | [BATCH LAYER: Replay inicial con Vectorización y cálculo de Embeddings]
       + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +

```

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
* **Principio de Responsabilidad Única (SRP):** Prestamos especial atención al diseño modular; el procesamiento de archivos y la infraestructura de acceso a datos (DAOs) se mantienen completamente aislados de los componentes de análisis de Inteligencia Artificial.
* **Inyección de Dependencias y Desacoplamiento:** Uso sistemático de **interfaces** y almacenamiento (`Store`, `WebScraper`), facilitando la sustitución de componentes y los entornos de pruebas unitarias.

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

* **Evitar Interbloqueos (Deadlocks) en la Base de Datos:** En la propiedad `datamartUrl`, se recomienda encarecidamente mantener todos los parámetros incluidos tras el signo de interrogación (`?journal_mode=WAL&busy_timeout=5000`). Esta configuración activa el modo de escritura adelantada (*Write-Ahead Logging*) y define un tiempo de espera para evitar conflictos concurrentes (interbloqueos) cuando el motor de optimización consulta la base de datos al mismo tiempo que el bróker escribe nuevos eventos en tiempo real. Modifica esto únicamente si vas a migrar la persistencia a otro motor de base de datos.
* **Consistencia de Red y Seguridad:** Todos los bloques de configuración deben compartir exactamente los mismos valores para las propiedades `topicName`, `brokerUrl`, `username` y `password` para garantizar que scrapers, almacén y unidad de negocio operen bajo el mismo bus de comunicación masiva.
* **Identificadores de Conexión:** Los campos `clientId` y `subscriptionName` son de libre elección para el administrador, asegurando que cada cliente mantenga una sesión única e identificable dentro del bróker de mensajería.
* **Planificación Horaria del Scraping:** Las propiedades `ScheduleTimeHour` y `ScheduleTimeMinutes` determinan la hora exacta en la que se disparará el proceso automático de extracción de datos para cada supermercado de forma diaria. El planificador toma como referencia la **hora local** del dispositivo o servidor donde se esté ejecutando el módulo.
---

Aquí tienes el bloque completo y unificado para la sección **6. Instrucciones de Compilación y Ejecución**.

Respecto a tu duda sobre Maven: **sí, es una idea fantástica añadir un comando**. En proyectos multimódulo de Java, en lugar de compilar o descargar dependencias módulo por módulo, puedes ejecutar un único comando desde la raíz del proyecto. Esto le descarga la vida al usuario y asegura que todas las dependencias (incluido Playwright, las librerías de IA y los drivers de SQLite) se descarguen de golpe y de forma limpia en todo el ecosistema.

He unificado todos los puntos (requisitos, configuración en IntelliJ, trucos del scheduler y el orden de encendido) en una sola guía fluida y súper profesional:

---

## 6. Instrucciones de Compilación y Ejecución

Para garantizar la integridad de la arquitectura Lambda, evitar la pérdida de mensajes y asegurar que el Motor de IA y los Scrapers arranquen sin conflictos de dependencias, **sigue estrictamente el orden de configuración y encendido detallado a continuación**:

### Requisitos Previos y Configuración del Entorno

* **Bróker de Mensajería:** Tener instalado y en ejecución un bróker de mensajería (como Apache ActiveMQ o Artemis) escuchando en el puerto configurado (por defecto, `tcp://localhost:61616`).
* **Fichero de Configuración:** Asegurar que el archivo `config.json` esté correctamente ubicado en la raíz del directorio principal que engloba todos los módulos del proyecto.
* **Entorno de Desarrollo (IDE Recomendado):** Se requiere el uso de **IntelliJ IDEA**. El proyecto delega en este IDE la gestión automática del ciclo de vida y las dependencias de herramientas del sistema (como la instalación interna y ejecución transparente de Chromium para el módulo de Web Scraping automatizado por Playwright), garantizando un despliegue sin configuraciones adicionales en el sistema operativo.
* **Versión de Java y SDK:** El ecosistema está desarrollado bajo **Microsoft OpenJDK 21** y requiere un nivel de lenguaje (**Language Level**) fijado estrictamente en **21** para soportar las características modernas de concurrencia y estructuras de datos utilizadas.

---

### Configuración del Proyecto en IntelliJ IDEA

Al abrir el proyecto por primera vez, realiza estos dos pasos para que el entorno entienda la disposición de los módulos y use el compilador correcto:

#### Paso 1: Configurar el JDK y Language Level 21

Para evitar errores de compilación por discrepancias de versiones, configura el SDK global del proyecto:

1. **Abrir Project Structure:** Paso 1.1.
En la barra de menús superior de IntelliJ IDEA, dirígete a `File` (Archivo) y selecciona `Project Structure...` (Estructura del proyecto), o utiliza el atajo de teclado `Ctrl + Alt + Shift + S` (`Cmd + ;` en macOS).


2. **Configurar el SDK del Proyecto:** Paso 1.2.
En el menú lateral izquierdo, haz clic en la sección `Project`. Dentro del apartado **SDK**, selecciona **Microsoft OpenJDK 21**. En caso de no tenerlo instalado, despliega las opciones, selecciona *Download JDK*, elige el proveedor *Microsoft* y descarga la versión 21.


3. **Ajustar el Language Level:** Paso 1.3.
Justo debajo de la selección del SDK, localiza el desplegable **Project language level** y asegúrate de marcar la opción **21 - Record patterns, pattern matching for switch**. Haz clic en `Apply` y luego en `OK`.


#### Paso 2: Vinculación de Módulos y Descarga Global de Dependencias

Para no tener que ir uno a uno haciendo clic derecho en cada `pom.xml`, puedes importar y descargar absolutamente todas las dependencias del ecosistema a la vez.
Abre una terminal integrada en IntelliJ IDEA (asegúrate de estar en la **raíz del proyecto principal**) y ejecuta el siguiente comando de Maven:

```bash
mvn clean install -DskipTests
```

> 💡 **¿Qué hace este comando?** Borra residuos antiguos (`clean`), descarga todas las librerías necesarias para cada uno de los 4 módulos de golpe (`install`), compila el código bajo el JDK 21 y salta las pruebas de entorno (`-DskipTests`) para que el proceso sea inmediato. Tras esto, IntelliJ sincronizará automáticamente la jerarquía de carpetas.
> *Si por algún motivo un módulo específico no se iluminara en verde como proyecto Java, puedes ir de forma manual a su `pom.xml`, hacer clic derecho y seleccionar **Add as Maven Project**.*

---

### Modo de Ejecución Inmediata (Omitir el Programador / Schedulers)

Por defecto, los scrapers esperan a la hora configurada en el `config.json`. Si deseas **forzar una ejecución inmediata** para probar el sistema sin esperar a la hora programada, realiza la siguiente modificación en los puntos de entrada:

* **Módulo Hiperdino:** Dirígete al archivo `Main.java`, localiza la línea 19, sustituye la lógica del *scheduler* posterior a la instancia del controlador e invoca directamente al método de inicialización:
Vamos, borra la linea 19 y escribe eso:
```java
controller.init();
```


* **Módulo Mercadona:** Realiza el mismo procedimiento en su respectivo `Main.java` (línea 19), reemplazando el código del planificador por la ejecución directa del flujo pasando la URL del sitemap:
Vamos, borra la linea 19 y escribe eso:
```java
controller.run(sitemapUrl);
```
---

### Pasos para el Arranque Secuencial del Sistema

Una vez compilado todo limpiamente, ejecuta las clases `Main.java` de los componentes en este orden exacto para permitir la correcta sincronización de las capas de datos distribuidas:

1. **Levantar el Bróker de Mensajería:** Fase de Infraestructura.
Asegúrate de que la instancia de tu bróker (ActiveMQ/Artemis) esté operativa y aceptando conexiones de red en el puerto configurado (`61616`).


2. **Iniciar el Event Store (EventStoreBuilder):** Fase de Almacenamiento.
Ejecuta la clase `Main.java` dentro del módulo `EventStoreBuilder`. Este componente se mantendrá en escucha persistente y comenzará a registrar de forma inmutable en el disco cualquier flujo de datos entrante.


3. **Desplegar la Unidad de Negocio (Business Unit):** Fase de Inteligencia.
Ejecuta la clase `Main.java` del módulo `BusinessUnit`. Al arrancar, el sistema se conectará a la base de datos local SQLite, activará el pool de hilos concurrente para procesar el **Replay de la Capa Batch** (traduciendo los datos históricos del paso anterior a vectores de IA) y, al finalizar, dejará abierta la escucha en la **Capa Speed** para capturar actualizaciones en tiempo real.


4. **Activar los Scrapers de Origen:** Fase de Alimentación.
Ejecuta las clases `Main.java` de los módulos `Hiperdino` y `Mercadona`. Estos activarán sus tareas programadas basadas en el archivo de configuración (o se ejecutarán de inmediato si aplicaste el cambio opcional en las líneas 19) para comenzar a nutrir de datos al radar.

## 7. Ejemplos de Uso e Interacción

Al iniciar el módulo **Business Unit**, se desplegará la interfaz interactiva por consola basada en comandos de teclado. Puedes introducir tus términos de búsqueda de forma completamente natural gracias a la capa semántica de la IA.

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
