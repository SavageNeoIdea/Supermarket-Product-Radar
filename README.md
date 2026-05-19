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

## Requisitos Previos y Configuración del Entorno

* **Bróker de Mensajería:** Tener instalado y en ejecución un bróker de mensajería (como Apache ActiveMQ o Artemis) escuchando en el puerto configurado (por defecto, `tcp://localhost:61616`).
* **Fichero de Configuración:** Asegurar que el archivo `config.json` esté correctamente ubicado en la raíz del directorio principal que engloba todos los módulos del proyecto.
* **Entorno de Desarrollo (IDE Recomendado):** Se requiere el uso de **IntelliJ IDEA**. El proyecto delega en este IDE la gestión automática del ciclo de vida y las dependencias de herramientas del sistema (como la instalación interna y ejecución transparente de Chromium para el módulo de Web Scraping automatizado por Playwright), garantizando un despliegue sin configuraciones adicionales en el sistema operativo.
* **Versión de Java y SDK:** El ecosistema está desarrollado bajo **Microsoft OpenJDK 21** y requiere un nivel de lenguaje (**Language Level**) fijado estrictamente en **21** para soportar las características modernas de concurrencia y estructuras de datos utilizadas.

#### Ajuste de la Estructura del Proyecto en IntelliJ IDEA

Para evitar errores de compilación por discrepancias de versiones, debes configurar el SDK global del proyecto siguiendo estos pasos tras abrir el entorno:

1. **Abrir Project Structure:** Paso 1.
En la barra de menús superior de IntelliJ IDEA, dirígete a `File` (Archivo) y selecciona `Project Structure...` (Estructura del proyecto), o utiliza el atajo de teclado `Ctrl + Alt + Shift + S` (`Cmd + ;` en macOS).


2. **Configurar el SDK del Proyecto:** Paso 2.
En el menú lateral izquierdo, haz clic en la sección `Project`. Dentro del apartado **SDK**, selecciona **Microsoft OpenJDK 21**. En caso de no tenerlo instalado, despliega las opciones, selecciona *Download JDK*, elige el proveedor *Microsoft* y descarga la versión 21.


3. **Ajustar el Language Level:** Paso 3.
Justo debajo de la selección del SDK, localiza el desplegable **Project language level** y asegúrate de marcar la opción **21 - Record patterns, pattern matching for switch**.


4. **Aplicar y Sincronizar:** Paso 4.
Haz clic en el botón `Apply` y luego en `OK` en la esquina inferior derecha. Si el IDE lo solicita, permite que se realice la indexación y recarga de los módulos de Maven para heredar la nueva configuración del compilador.

### Requisitos Previos y Configuración del Entorno

* **Bróker de Mensajería:** Tener instalado y en ejecución un bróker de mensajería (como Apache ActiveMQ o Artemis) escuchando en el puerto configurado (por defecto, `tcp://localhost:61616`).
* **Fichero de Configuración:** Asegurar que el archivo `config.json` esté correctamente ubicado en la raíz del directorio principal que engloba todos los módulos del proyecto.
* **Entorno de Desarrollo (IDE Recomendado):** Se requiere el uso de **IntelliJ IDEA**. El proyecto delega en este IDE la gestión automática del ciclo de vida y las dependencias de herramientas del sistema (como la instalación interna y ejecución transparente de Chromium para el módulo de Web Scraping automatizado por Playwright), garantizando un despliegue sin configuraciones adicionales en el sistema operativo.
* **Versión de Java y SDK:** El ecosistema está desarrollado bajo **Microsoft OpenJDK 21** y requiere un nivel de lenguaje (**Language Level**) fijado estrictamente en **21** para soportar las características modernas de concurrencia y estructuras de datos utilizadas.

### Pasos para el Arranque del Sistema

1. **Levantar el Bróker:** Asegúrate de que ActiveMQ esté operativo.
2. **Levantar el Event Store (`EventStoreBuilder`):** Ejecuta `Main.java` en este módulo. Comenzará a escuchar y persistir de manera inmediata cualquier evento entrante para salvaguardar el histórico.
3. **Levantar la Unidad de Negocio (`Business Unit`):** Ejecuta `Main.java` en el módulo `BusinessUnit`. Se conectará a la *Serving Layer* SQLite, inicializará el pool de hilos distribuidos para ejecutar el **Replay de la Capa Batch** traduciendo los textos a vectores densos, y finalmente mantendrá abierta la **Capa Speed** reactiva.
4. **Iniciar Scrapers (La fuente de datos):** Ejecuta los archivos `Main.java` de los módulos `Hiperdino` y `Mercadona`. Estos iniciarán sus respectivos *Schedulers* internos basándose en las propiedades de planificación configuradas.

---

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
