# Supermarket Product Radar 🛒

### Desarrollado por **SavageNeoIdea**

Bienvenido a **Supermarket Product Radar**, una solución de software empresarial impulsada enteramente por eventos (**EDA - Event-Driven Architecture**) bajo una **Arquitectura Lambda** para la optimización en tiempo real del presupuesto de la cesta de la compra.

Este sistema automatiza la recopilación de productos de las cadenas Mercadona e Hiperdino, procesa la información mediante un bróker de mensajería, almacena el histórico en un *Event Store* inmutable y consolida los datos en una unidad de negocio (*Business Unit*). Al inicializarse, reconstruye el estado completo del mercado y lo actualiza continuamente de forma reactiva para calcular de forma inteligente la combinación de compra óptima mediante un **Motor de Búsqueda Semántica basado en Inteligencia Artificial**, exponiendo el denominado **"Impuesto de la pereza"**.

---

## 1. Descripción del Proyecto y Propuesta de Valor

### Descripción General

**Supermarket Product Radar** es un ecosistema multimódulo distribuido que se apoya en una red de publicación/suscripción de eventos. El sistema está compuesto por 4 módulos principales:

1. **Mercadona:** Módulo con planificador integrado (*Scheduler*) configurado por horas específicas para extraer productos de la plataforma de Mercadona y publicar eventos de actualización de precios, los extrae a través de su api interna haciendo uso de un sitemap, por lo que no se pueden obtener productos locales como productos Tirma en el caso de canarias, pero existen la mayoría de productos.
2. **Hiperdino:** Módulo homólogo que extrae productos de Hiperdino mediante técnicas de web scraping distribuidas (Playwright) haciendo uso de un código postal, publicando sus eventos según la localización regional y la hora programada.
3. **EventStoreBuilder:** Componente encargado de escuchar de forma persistentemente los eventos de productos y almacenarlos cronológicamente en un almacén de eventos inmutable (*Event Store*).
4. **Business Unit:** El núcleo de inteligencia de negocio. Al arrancar, reconstruye su estado leyendo el histórico completo para inicializar su base de datos local (Datamart) impulsada por Embeddings de IA y, posteriormente, procesa eventos en tiempo real para mantener el catálogo fresco, exponiendo un motor interactivo de optimización de cestas.

### Propuesta de Valor: El "Impuesto de la Pereza" (*Laziness Tax*)

El sistema permite procesar una lista de la compra en formato de texto libre y genera automáticamente tres proyecciones financieras:

* **Cesta Optimizada:** La combinación ideal y más barata de los productos que has seleccionado en el proceso de creación.
* **Cesta Solo Mercadona:** Productos añadidos por el usuario y coste total si el usuario decide comprar exclusivamente en este establecimiento.
* **Cesta Solo Hiperdino:** Misma idea que en Mercadona pero para Hiperdino.

> 💡 **Métrica Clave:** El sistema calcula y muestra explícitamente el **Análisis de Pérdidas**, que representa la cantidad exacta de dinero que el usuario pierde por la comodidad de no diversificar su compra entre ambos establecimientos (ir a uno solo en lugar de acudir a ambos cuando toca).

---

## 2. El Motor de Inteligencia Artificial Semántica e Híbrida

El módulo `BusinessUnit` integra un motor de **Búsqueda Híbrida** que combina la recuperación semántica vectorial con un refinamiento léxico estadístico basado en texto claro. El sistema está diseñado para contrastar las consultas del usuario en tiempo real contra un catálogo de productos previamente vectorizado.

Para ello se hace uso del modelo: `paraphrase-multilingual-MiniLM-L12-v2`: https://huggingface.co/Xenova/paraphrase-multilingual-MiniLM-L12-v2

### Arquitectura del Componente y Pipeline de Procesamiento

El motor está gobernado por la clase `EmbeddingService` bajo la abstracción de LangChain4j. El ciclo de vida del modelo es **100% local y autónomo**, instanciándose a través de `OnnxEmbeddingModel` con pooling de tipo `MEAN` mediante `model_quantized.onnx` y `tokenizer.json`.

Cuando un usuario realiza una búsqueda, el ciclo de vida del procesamiento sigue estrictamente este flujo:

```text
[Input de Usuario] 
       │
       ▼
(Sanitización del Input) 
       │
       ▼
(Vectorización del Input ➔ Comparación vs Vectores del Datamart)
       │
       ├─► [¿Supera SIMILARITY_THRESHOLD (0.75)?] ── NO ──► (Producto Descartado / null)
       │
       └─► SÍ
            │
            ▼
     (Tokenización de Ambos Textos ➔ Metodología Jaccard)
            │
            ▼
     [Generación de Puntuación Final Híbrida]

```

1. **Sanitización del Input (`sanitizeText`):** El texto introducido por el usuario se normaliza en formato Unicode NFD para separar los diacríticos de las letras base. Se eliminan tildes (`\\p{M}`), caracteres especiales (`[^\\p{L}\\p{N}\\s]`), se fuerza a minúsculas y se colapsan los espacios duplicados.
2. **Vectorización y Comparación Semántica:** El input sanitizado se transforma en un vector denso de **384 dimensiones**. Este vector se compara en tiempo real mediante *Similitud de Coseno* contra los vectores de los productos, los cuales **fueron generados previamente durante la construcción del datamart** para evitar sobrecostes de cómputo.
3. **Filtro de Umbral Crítico (`SIMILARITY_THRESHOLD`):** Actúa como un cortafuegos. Si la similitud semántica pura entre el input y un producto del datamart no supera el **0.75**, el producto se descarta inmediatamente (`null`).
4. **Bono Léxico Dinámico (Fase Jaccard):** **Solo a los productos que superan el umbral** se les aplica el análisis léxico. En esta fase, tanto el input del usuario como el nombre del producto se tokenizan, se limpian de conectores (`STOPWORDS`) y se extraen sus raíces (`extractRoot`). Finalmente, se aplica la metodología Jaccard y de cobertura para calcular el impacto del bono.

---

### El Modelo Matemático del Score Híbrido

Para determinar la relevancia final de los productos aprobados por el filtro semántico, se aplica la siguiente fórmula de maximización de score:

$$Score_{Hybrid} = Score_{Semantic} + Bonus_{Lexical}$$

#### 1. Similitud Semántica ($Score_{Semantic}$)

El modelo mide la proximidad conceptual pura en el hiperespacio vectorial a través de la similitud de coseno, mapeada a un rango cerrado de $[0, 1]$ mediante la utilidad `RelevanceScore`:

$$Score_{Semantic} = \text{RelevanceScore}(\cos(\theta))$$

$$\cos(\theta) = \frac{\vec{A} \cdot \vec{B}}{\|\vec{A}\| \|\vec{B}\|}$$

#### 2. Bono Léxico por Metodología Jaccard ($Bonus_{Lexical}$)

Sean $Q$ el conjunto de tokens del input sanitizado del usuario y $P$ el conjunto de tokens del nombre del producto mapeado, el sistema calcula la coincidencia matemática exacta de sus raíces:

* **Cobertura de la Consulta ($Query\>Coverage$):** Porcentaje de términos del usuario hallados en el producto.
$$QC = \frac{|Q \cap P|}{|Q|}$$


* **Índice de Jaccard ($Jaccard\>Index$):** Similitud estructural que penaliza el ruido o exceso de palabras irrelevantes en el nombre del producto.
$$JI = \frac{|Q \cap P|}{|Q \cup P|}$$

El impacto de los tokens se unifica mediante pesos estáticos ($70\%$ Cobertura, $30\%$ Jaccard) y se escala por el límite máximo del bono configurado:

$$Score_{Lexical} = (QC \cdot 0.70) + (JI \cdot 0.30)$$

$$Bonus_{Lexical} = Score_{Lexical} \cdot MaxLexicalBonus$$

---

### Hiperparámetros y Configuración del Motor

El comportamiento del motor semántico puede calibrarse modificando las constantes estáticas en la cabecera de `EmbeddingService`:

| Constante | Valor por Defecto | Propósito Técnico |
| --- | --- | --- |
| `SIMILARITY_THRESHOLD` | `0.75` | Filtro mínimo de confianza semántica para acceder a la fase de bono léxico. |
| `MAX_LEXICAL_BONUS` | `0.12` | Techo máximo que la metodología Jaccard/Cobertura puede sumar al score final. |
| `QUERY_COVERAGE_WEIGHT` | `0.70` | Peso asignado a la presencia de las palabras del usuario en el producto. |
| `JACCARD_WEIGHT` | `0.30` | Peso asignado a la similitud estructural global de los conjuntos de palabras. |

---
## 3. Arquitectura del Sistema e Infraestructura Concurrente

### 1. Vectorización Asíncrona en Lote (Batch Ingestion)

Durante la fase de *Replay* de la Arquitectura Lambda, el sistema debe procesar miles de registros históricos de precios. Calcular los embeddings uno a uno degradaría el tiempo de arranque.

Se implementó un pool de hilos que usa todos los hilos disponibles (`ExecutorService` con hilos asignados según los núcleos de la máquina) que procesa las filas del *Event Store* en paralelo. Esto distribuye de forma balanceada el cálculo matricial de los embeddings en la CPU, reduciendo la latencia de inicialización en aproximadamente un **80%**. Eso si, espera que la cpu llega a porcentajes de hasta el 100% ya que va a usar durante unos segundos toda su capacidad.

### 2. Motor de Búsqueda Concurrente (Producer-Consumer)

Al realizar la optimización de la lista de la compra de un usuario, el motor de consultas en `SQLiteQuery` divide el trabajo mediante un patrón de productor-consumidor:

* **Productor:** El hilo principal realiza un barrido secuencial de baja latencia a través del cursor nativo del `ResultSet` de SQLite.
* **Consumidores (Workers):** Una matriz de hilos secundarios toma los datos de los productos extraídos del flujo de la base de datos y computa de forma simultánea la fórmula de similitud de coseno y la puntuación compuesta en paralelo.
---

### Arquitectura de Mensajería y Flujo Lambda

El sistema hace uso de una arquitectura Lambda.

El flujo de información se distribuye a través de tópicos mediante el bróker de mensajería parametrizado en el archivo `config.json`:

<img width="1139" height="642" alt="{C71C14AB-E9FE-4C44-BE40-17B1B8B8DC11}" src="https://github.com/user-attachments/assets/9286c0d7-5d34-4c51-a9cc-47c64270673f" />

### Arquitectura de la Aplicación

#### Desglose a modo de resumen de los Módulos Internos

##### 1. Módulo Scraper: Mercadona
Encargado de la extracción masiva de productos e instrucciones de precios desde la plataforma de Mercadona de manera eficiente.
* **`controller`**: Gestiona el ciclo de vida de la extracción mediante `HttpClientManager` y `MercadonaHttpClient`. Convierte las respuestas crudas de la API mediante (`MercadonaProductService`).
* **`model`**: Define el dominio de origen (`Category`, `Product`, `Details`, `PriceInstructions`).
* **`store`**: Envia eventos limpios hacia la cola distribuidora mediante `ActiveMQStorer`.

##### 2. Módulo Scraper: Hiperdino
Especializado en la extracción web automatizada simulando el comportamiento de un usuario real para evadir bloqueos de navegación.
* **`controller/scraper`**: Dirige la automatización del navegador web utilizando Playwright (`HiperdinoPlaywrightManager`). Incorpora `HumanBehaviorSimulator` para hacer los movimientos y las esperas mas "humanas".
* **`controller/feeder/parser`**: Procesa el árbol DOM y respuestas JSON crudas extraídas de la web para limpiarlas y estructurarlas (`HiperdinoJsonProductParser`).
* **`store`**: Despacha de forma asíncrona la información procesada directamente hacia la red de mensajería en `ActivemqStore`.

##### 3. Módulo: EventStoreBuilder
Actúa como la **Fuente de la Verdad (Single Source of Truth)**. Guarda un registro histórico, inmutable y ordenado cronológicamente de cada evento emitido en el ecosistema.
* **`controller`**: Consume de manera dedicada los eventos procedentes de las colas de ActiveMQ (`ActivemqEventConsumer`).
* **`store`**: Implementa el patrón *Append-Only Log* a través de `EventStore`, aislando los datos en bruto en una estructura de carpetas persistente y segura (`eventstore/`), ideal para auditorías o reajustes semánticos del modelo AI.

##### 4. Módulo Principal: Business Unit
Es el núcleo analítico de la solución. Consolida las lecturas históricas y en tiempo real, calcula similitudes vectoriales y expone la aplicación al usuario.
* **`controller/reader`**: Al iniciar la aplicación, un motor de *Replay* (`EventReader`) sincroniza el estado actual leyendo los datos históricos almacenados en el disco correspondientes a las últimas 24 horas.
* **`controller/activemq`**: Un suscriptor en tiempo real (`ActiveMqSubscriptor`) escucha el broker de forma ininterrumpida para capturar de inmediato variaciones de precios o nuevos artículos emitidos en directo por los scrapers.
* **`store/sqlite`**: Centraliza los datos que van a ser usados por el cliente, estos datos ya poseen los embeddings del modelo gracias a ProductFeeder así que están preparados para ser consumidos.
* **`controller/embedding`**: El motor semántico (`EmbeddingService`) ejecuta inferencia local de Inteligencia Artificial empleando un modelo lingüístico cuantizado (`model_quantized.onnx`) y un tokenizador nativo. Calcula la distancia coseno e inyecta un algoritmo propio de *Lexical Bonus* basado en coeficientes Jaccard y cobertura de términos de búsqueda.
* **`controller/shoppinglist`**: Resuelve consultas semánticas complejas usando flujos paralelos (`parallelStream`) evaluando rápidamente miles de productos candidatos con su algoritmo de coincidencia.
* **`view`**: La capa de presentación (`AppCli`) implementa una CLI interactiva completa para orquestar la lista de la compra, permitiendo seleccionar productos de ambos supermercados y proporcionando un cuadro de mando financiero detallado sobre la rentabilidad semántica y la tasa de pereza de la compra.

---

##### Flujo de Trabajo del Ciclo de Vida del Dato

1.  **Captura**: Los módulos independientes **Mercadona** e **Hiperdino** extraen de forma asíncrona los datos de la web.
2.  **Transporte**: Los scrapers emiten mensajes estructurados con topic `product` hacia **ActiveMQ**.
3.  **Persistencia Inmutable**: **EventStoreBuilder** captura el mensaje y lo almacena inmediatamente en el eventstore con el formato eventstore/{topic}/{source}
4.  **Sincronización Semántica**: **Business Unit** consume los flujos y actualiza su Datamart relacional en **SQLite**. Al buscar, convierte el texto ingresado por el usuario en vectores matemáticos usando Inteligencia Artificial, compara las proximidades espaciales contra la base de datos de productos precargada y devuelve las 7 opciones más rentables al usuario a través del menú de la CLI de manera inmediata.
---

## 4. Principios y Patrones de Diseño Aplicados

Para garantizar un ecosistema mantenible, escalable y robusto, el desarrollo se ha guiado por las mejores prácticas de la ingeniería de software:

* **Patrón Publisher-Subscriber (Pub/Sub):** Desacoplamiento total entre los productores de datos (scrapers) y los consumidores finales.
* **Event Sourcing:** Salvaguarda el ciclo de vida del producto como una serie de eventos, impidiendo la sobrescritura directa de datos de precios.
* **Capa de Persistencia Idempotente:** La base de datos SQLite usada en businessUnit aprovecha un mecanismo de `UPSERT` para garantizar la consistencia e idempotencia de los datos semánticos haciendo uso del ean y source como claves para esta query, evitando duplicar registros si el lote de eventos vuelve a ser emitido.
* **Principio de Responsabilidad Única (SRP):** Prestamos especial atención al diseño modular.
* **Inyección de Dependencias y Desacoplamiento:** Uso sistemático de **interfaces** y almacenamiento (`Store`, `WebScraper`, ...), facilitando la sustitución de componentes y los entornos de pruebas unitarias.
* **Buenos nombres:** Hemos tratado de poner los mejores nombres para que todo este lo mas claro posible.
* **Uso de MVC:** Hacemos uso del patrón de arquitectura model-view-controller respetando las reglas de visibilidad.

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

#### Parámetros Modificables
* **Consistencia de Red y Seguridad:** Todos los bloques de configuración deben compartir exactamente los mismos valores para las propiedades `topicName`, `brokerUrl`, `username` y `password` para garantizar que scrapers, almacén y unidad de negocio operen bajo el mismo bus de comunicación masiva.
* **Identificadores de Conexión:** Los campos `clientId` y `subscriptionName` son de libre elección para el administrador, asegurando que cada cliente mantenga una sesión única e identificable dentro del bróker de mensajería.
* **Planificación Horaria del Scraping:** Las propiedades `ScheduleTimeHour` y `ScheduleTimeMinutes` determinan la hora exacta en la que se disparará el proceso automático de extracción de datos para cada supermercado de forma diaria. El planificador toma como referencia la **hora local** del dispositivo o servidor donde se esté ejecutando el módulo.
---

## 6. Instrucciones de uso

Para garantizar la integridad de la arquitectura Lambda, evitar la pérdida de mensajes y asegurar que el Motor de IA y los Scrapers arranquen sin conflictos de dependencias, **sigue estrictamente el orden de configuración y encendido detallado a continuación**:

### Requisitos Previos y Configuración del Entorno

* **Bróker de Mensajería:** Tener instalado y en ejecución un bróker de mensajería (Actualmente, el único implementado es **Apache ActiveMQ**) escuchando en el puerto configurado (por defecto, `tcp://localhost:61616`, lo puedes cambiar en el `config.json`, cambiando el atributo `brokerUrl` de los 4 módulos).
* **Fichero de Configuración:** Asegurar que el archivo `config.json` esté correctamente ubicado en la raíz del directorio principal que engloba todos los módulos del proyecto.
* **Entorno de Desarrollo (IDE Recomendado):** Se requiere el uso de **IntelliJ IDEA**. El proyecto delega en este IDE la gestión automática del ciclo de vida y las dependencias de herramientas del sistema (como la instalación interna y ejecución transparente de Chromium para el módulo de Web Scraping automatizado por Playwright), garantizando un despliegue sin configuraciones adicionales en el sistema operativo.
* **Versión de Java y SDK:** El ecosistema está desarrollado bajo **Microsoft OpenJDK 21** y requiere un nivel de lenguaje (**Language Level**) fijado estrictamente en **21 - Record patterns, pattern matching for switch**.

#### Obtención del modelo paraphrase-multilingual-MiniLM-L12-v2:
Debido al peso de mas de 100mb del modelo, se deberá descargar y posteriormanete colocar en un lugar específico del BusinessUnit:

- Descarga directa: https://huggingface.co/Xenova/paraphrase-multilingual-MiniLM-L12-v2/resolve/main/onnx/model_quantized.onnx?download=true
- Si la forma automatica no funciona, aquí tienes el proceso manual: https://huggingface.co/Xenova/paraphrase-multilingual-MiniLM-L12-v2 -> "Files and Versions" -> seleccionas la carpeta "onnx" (que significa Open Neural Network Exchange, un formato open-source y ecoistema diseñado para la estandarización de representación de modelos de machine o deep learning) -> Localiza model_quantized.onnx y lo descargas:
<img width="1600" height="829" alt="{276FB0F2-5F94-45A7-8B6E-5E3E0B86E38E}" src="https://github.com/user-attachments/assets/bb943299-552e-4e87-98f9-287a234c1070" />

- Mantenlo localizado ya que más adelante tendrás colocar el fichero donde se te indique.

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

#### Paso 3: Colocación del modelo:
Ahora que tienes el proyecto abierto simplemente accede al módulo BusinessUnit, luego accede sistemáticamente a src/main dentro del main observarás dos carpetas, java (donde se encuentra el código fuente) y resources, coloca el modelo model_quantized.onnx dentro de este último.
<img width="528" height="256" alt="{A80386BF-A7C6-4AD2-8529-93E6E410EA07}" src="https://github.com/user-attachments/assets/b5204d84-e3bf-414e-b801-f96a893fa60b" />

### Modo de Ejecución Inmediata (Omitir el Programador / Schedulers)

Por defecto, los scrapers esperan a la hora configurada en el `config.json`. Si deseas **forzar una ejecución inmediata** para probar el sistema sin esperar a la hora programada, realiza la siguiente modificación en los puntos de entrada:

* **Módulo Hiperdino:** Dirígete al archivo `Main.java`, localiza la línea 23, sustituye la lógica del *scheduler* posterior a la instancia del controlador e invoca directamente al método de inicialización:
Vamos, borra la linea 23 y copia tal cual lo siguiente (executionTime se saca automáticamente):
```java
controller.init();
```
En caso de querer volver a la versión con sheduler:
```java
controller.startScheduler(executionTime);
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

Al iniciar el módulo **Business Unit**, después de un breve lapso de tiempo donde se reconstruye el último día de cada topic/source del eventsotre como un datamart y se inicie la conexión con el activeMQ (si está encendido), se desplegará la interfaz interactiva por consola basada en comandos de teclado. Puedes introducir tus términos de búsqueda y la IA tratará de buscar un top 7 de aquellos resultados que mas se adecuen, a veces puede fallar ya que es un modelo bastante ligero, hemos implementado un top 7 para que puedas elegir o saltártelo en caso de no encontrar una respuesta satisfactoria.

### Flujo en la Consola de Comandos

```text
🛒 ¡Bienvenido al Optimizador de la Lista de la Compra (Mercadona & Hiperdino)! 🛒
Elige una opción:
1. Crear lista de la compra
2. Ver análisis de listas (Conjunta, Exclusivas y 'Tasa de Pereza')
3. Salir del programa
Responde seleccionando uno de los números del teclado: 
```

1. Introduce `1` para iniciar el asistente de entrada de datos (`Preparando una nueva lista...`). El sistema te pedirá los artículos línea por línea hasta que escribas `fin` o dejes una línea vacía. Trata de ser específico, a menos especifico menos probabilidad hay de que encuentres lo que buscas exactamente, encontrarás un ejemplo mas adelante para que te hagas una idea:
  
3. Introduce `2` (`Cargando tu análisis de compra (Conjunta vs Individual)...`). Este ejecutará la función que genera el reporte de ahorro comparativo. TEN EN CUENTA QUE CADA VEZ QUE CREES UNA LISTA, LA ANTERIOR SERÁ BORRADA
#### Ejemplo de procedimiento:
```text
🛒 ¡Bienvenido al Optimizador de la Lista de la Compra (Mercadona & Hiperdino)! 🛒
Elige una opción:
1. Crear lista de la compra
2. Ver análisis de listas (Conjunta, Exclusivas y 'Tasa de Pereza')
3. Salir del programa
Responde seleccionando uno de los números del teclado: 
1
Preparando una nueva lista...
===========================================
   🛒 ASISTENTE DE LISTA DE COMPRAS 🛒    
===========================================
Introduce los productos uno por uno.
Escribe 'fin' o pulsa Enter en una línea vacía para terminar.
-------------------------------------------

> ¿Qué producto buscas?: cerveza estrella galicia
INFO: Cargando catálogo de productos en memoria...

--- 🛒 Opciones en MERCADONA ---
Nº   | Producto                                           | Formato      | Precio   | Prec. Est.  
------------------------------------------------------------------------------------------------------------
1   | cerveza especial estrella galicia                                                | 0,25l        | 0,71  € | 2,84€/l
2   | cerveza especial estrella galicia                                                | 0,33l        | 0,90  € | 2,73€/l
3   | cerveza especial estrella galicia                                                | 10uds, 3,30l | 7,40  € | 2,24€/l
4   | cerveza especial estrella galicia                                                | 12uds, 3,00l | 7,32  € | 2,44€/l
5   | cerveza 00 sin alcohol estrella galicia                                          | 6uds, 1,50l  | 3,96  € | 2,64€/l
6   | cerveza 00 sin alcohol estrella galicia                                          | 0,25l        | 0,69  € | 2,76€/l
7   | cerveza amstel                                                                   | 0,25l        | 0,53  € | 2,12€/l

👉 Selecciona número (1-7) o [Enter] para omitir: 2

--- 🛒 Opciones en HIPERDINO ---
Nº   | Producto                                           | Formato      | Precio   | Prec. Est.  
------------------------------------------------------------------------------------------------------------
1   | estrella galicia cerveza especial                                                | 12uds, 2,40l | 7,39  € | 3,08€/l
2   | estrella galicia cerveza especial                                                | 12uds, 2,40l | 7,39  € | 3,08€/l
3   | estrella galicia cerveza especial                                                | 12uds, 2,40l | 7,39  € | 3,08€/l
4   | estrella galicia cerveza especial                                                | 10uds, 3,30l | 7,36  € | 2,23€/l
5   | estrella galicia cerveza especial                                                | 0,33l        | 0,76  € | 2,30€/l
6   | estrella galicia cerveza especial                                                | 6uds, 1,50l  | 3,56  € | 2,37€/l
7   | estrella galicia cerveza especial                                                | 0,33l        | 0,76  € | 2,30€/l

👉 Selecciona número (1-7) o [Enter] para omitir: 5
✅ Guardado. (Mejor opción global asignada por rentabilidad: estrella galicia cerveza especial por 0.76€)

> ¿Qué producto buscas?: datiles desecados

--- 🛒 Opciones en MERCADONA ---
Nº   | Producto                                           | Formato      | Precio   | Prec. Est.  
------------------------------------------------------------------------------------------------------------
1   | datiles en rama hacendado                                                        | 0,40kg       | 3,45  € | 8,63€/kg
2   | datiles desecados sin hueso hacendado                                            | 0,25kg       | 1,65  € | 6,60€/kg
3   | aguacate en dados hacendado congelado                                            | 0,50kg       | 3,50  € | 7,00€/kg
4   | almendra laminada hacendado                                                      | 0,13kg       | 2,30  € | 18,40€/kg
5   | embutido surtido oreado                                                          | 0,50kg       | 3,88  € | 7,76€/kg

👉 Selecciona número (1-5) o [Enter] para omitir: 2

--- 🛒 Opciones en HIPERDINO ---
Nº   | Producto                                           | Formato      | Precio   | Prec. Est.  
------------------------------------------------------------------------------------------------------------
1   | isola datiles extra                                                              | 0,20kg       | 1,80  € | 9,00€/kg
2   | isola datiles extra                                                              | 0,20kg       | 1,80  € | 9,00€/kg
3   | emicela datiles con hueso                                                        | 0,25kg       | 1,95  € | 7,80€/kg
4   | emicela datiles con hueso                                                        | 0,25kg       | 1,95  € | 7,80€/kg
5   | emicela datiles con hueso                                                        | 0,25kg       | 1,95  € | 7,80€/kg
6   | hiperdino datiles sin hueso                                                      | 0,25kg       | 1,63  € | 6,52€/kg
7   | tigex cambiadores desechables                                                    | 20,00ud      | 13,15 € | 0,66€/ud

👉 Selecciona número (1-7) o [Enter] para omitir: 6
✅ Guardado. (Mejor opción global asignada por rentabilidad: hiperdino datiles sin hueso por 1.63€)

> ¿Qué producto buscas?: leche entera asturiana

--- 🛒 Opciones en MERCADONA ---
Nº   | Producto                                           | Formato      | Precio   | Prec. Est.  
------------------------------------------------------------------------------------------------------------
1   | leche entera asturiana                                                           | 6uds, 9,00l  | 11,82 € | 1,31€/l
2   | leche entera asturiana                                                           | 1,50l        | 1,97  € | 1,31€/l
3   | leche entera asturiana                                                           | 6uds, 6,00l  | 7,74  € | 1,29€/l
4   | leche entera asturiana                                                           | 1,00l        | 1,29  € | 1,29€/l
5   | leche desnatada asturiana                                                        | 6uds, 9,00l  | 10,38 € | 1,15€/l
6   | leche desnatada asturiana                                                        | 6uds, 6,00l  | 6,78  € | 1,13€/l
7   | leche desnatada asturiana                                                        | 1,50l        | 1,73  € | 1,15€/l

👉 Selecciona número (1-7) o [Enter] para omitir: 1

--- 🛒 Opciones en HIPERDINO ---
Nº   | Producto                                           | Formato      | Precio   | Prec. Est.  
------------------------------------------------------------------------------------------------------------
1   | asturiana leche entera 15 l                                                      | 1,00ud       | 2,02  € | 2,02€/ud
2   | asturiana leche entera 15 l                                                      | 1,00ud       | 2,02  € | 2,02€/ud
3   | asturiana leche entera 15 l                                                      | 1,00ud       | 2,02  € | 2,02€/ud
4   | asturiana leche entera 15 l                                                      | 1,00ud       | 2,02  € | 2,02€/ud
5   | asturiana leche entera 15 l                                                      | 1,00ud       | 2,02  € | 2,02€/ud
6   | asturiana leche entera 15 l                                                      | 1,00ud       | 2,02  € | 2,02€/ud
7   | asturiana leche entera 15 l                                                      | 1,00ud       | 2,02  € | 2,02€/ud

👉 Selecciona número (1-7) o [Enter] para omitir: 1
✅ Guardado. (Mejor opción global asignada por rentabilidad: leche entera asturiana por 11.82€)

> ¿Qué producto buscas?: salmón ahumado envasado

--- 🛒 Opciones en MERCADONA ---
Nº   | Producto                                           | Formato      | Precio   | Prec. Est.  
------------------------------------------------------------------------------------------------------------
1   | salmon ahumado hacendado                                                         | 0,10kg       | 3,70  € | 37,00€/kg
2   | salmon ahumado hacendado                                                         | 2uds, 0,30kg | 10,80 € | 36,00€/kg
3   | taquitos de salmon ahumado hacendado                                             | 0,10kg       | 3,80  € | 38,00€/kg
4   | pate de salmon hacendado                                                         | 0,16kg       | 1,10  € | 6,88€/kg
5   | filete de salmon                                                                 | 0,35kg       | 8,23  € | 23,51€/kg
6   | salmon marinado hacendado                                                        | 0,08kg       | 3,50  € | 43,75€/kg
7   | escalopin de salmon                                                              | 0,35kg       | 8,75  € | 25,00€/kg

👉 Selecciona número (1-7) o [Enter] para omitir: 1

--- 🛒 Opciones en HIPERDINO ---
Nº   | Producto                                           | Formato      | Precio   | Prec. Est.  
------------------------------------------------------------------------------------------------------------
1   | skandia dados de salmon ahumado                                                  | 0,10kg       | 6,42  € | 64,20€/kg
2   | royal salmon ahumado                                                             | 0,08kg       | 5,39  € | 71,87€/kg
3   | royal salmon ahumado                                                             | 0,08kg       | 5,39  € | 71,87€/kg
4   | royal salmon ahumado intenso                                                     | 0,08kg       | 6,25  € | 78,13€/kg
5   | royal salmon ahumado marinado                                                    | 0,08kg       | 4,70  € | 58,75€/kg
6   | skandia salmon ahumado basics                                                    | 0,08kg       | 3,99  € | 49,88€/kg
7   | royal salmon ahumado menos 25 de sal                                             | 0,08kg       | 5,69  € | 71,13€/kg

👉 Selecciona número (1-7) o [Enter] para omitir: 2
✅ Guardado. (Mejor opción global asignada por rentabilidad: salmon ahumado hacendado por 3.7€)

> ¿Qué producto buscas?: galletas de chocolate

--- 🛒 Opciones en MERCADONA ---
Nº   | Producto                                           | Formato      | Precio   | Prec. Est.  
------------------------------------------------------------------------------------------------------------
1   | galletas rebuenas hacendado rellenas de chocolate                                | 0,50kg       | 1,30  € | 2,60€/kg
2   | galletas chocominis sabor chocolate hacendado                                    | 0,50kg       | 1,60  € | 3,20€/kg
3   | galletas con chocolate blanco hacendado                                          | 6uds, 0,24kg | 1,30  € | 5,51€/kg
4   | galletas divertidas chocolate con leche hacendado                                | 6uds, 0,25kg | 1,80  € | 7,17€/kg
5   | galletas de cacao caocream hacendado rellena de crema chocolate                  | 5uds, 0,22kg | 1,00  € | 4,55€/kg
6   | galletas con chocolate y crema de leche hacendado                                | 8uds, 0,27kg | 2,65  € | 9,89€/kg
7   | galletas digestive chocolate con leche hacendado                                 | 0,30kg       | 1,35  € | 4,50€/kg

👉 Selecciona número (1-7) o [Enter] para omitir: 1

--- 🛒 Opciones en HIPERDINO ---
Nº   | Producto                                           | Formato      | Precio   | Prec. Est.  
------------------------------------------------------------------------------------------------------------
1   | bandama galletas de chocolate                                                    | 0,15kg       | 1,65  € | 11,00€/kg
2   | schar galletas con chocolate                                                     | 0,15kg       | 2,99  € | 19,93€/kg
3   | birba surtido de galletas selectas con chocolate                                 | 0,21kg       | 8,51  € | 41,51€/kg
4   | hiperdino galletas rellenas de chocolate y crema                                 | 5uds, 0,20kg | 2,03  € | 10,41€/kg
5   | hiperdino galletas rellenas de chocolate y crema                                 | 5uds, 0,20kg | 2,03  € | 10,41€/kg
6   | hiperdino galletas rellenas de chocolate                                         | 0,25kg       | 1,00  € | 4,00€/kg
7   | hiperdino galletas rellenas de chocolate                                         | 3uds, 0,75kg | 3,00  € | 4,00€/kg

👉 Selecciona número (1-7) o [Enter] para omitir: 6
✅ Guardado. (Mejor opción global asignada por rentabilidad: galletas rebuenas hacendado rellenas de chocolate por 1.3€)

> ¿Qué producto buscas?: huevos grandes

--- 🛒 Opciones en MERCADONA ---
Nº   | Producto                                           | Formato      | Precio   | Prec. Est.  
------------------------------------------------------------------------------------------------------------
1   | huevos grandes l                                                                 | 12,00ud      | 3,20  € | 0,27€/ud
2   | huevos grandes l                                                                 | 6,00ud       | 1,80  € | 0,30€/ud
3   | huevos super grandes xl                                                          | 12,00ud      | 4,30  € | 0,36€/ud
4   | huevos                                                                           | 24,00ud      | 5,60  € | 0,23€/ud
5   | huevos cocidos                                                                   | 6,00ud       | 2,20  € | 0,37€/ud
6   | huevos medianos m                                                                | 12,00ud      | 3,00  € | 0,25€/ud
7   | nidos al huevo hacendado                                                         | 0,50kg       | 1,45  € | 2,90€/kg

👉 Selecciona número (1-7) o [Enter] para omitir: 1

--- 🛒 Opciones en HIPERDINO ---
Nº   | Producto                                           | Formato      | Precio   | Prec. Est.  
------------------------------------------------------------------------------------------------------------
1   | rujamar huevos moreno xl                                                         | 10,00ud      | 3,60  € | 0,36€/ud
2   | rujamar huevos camperos                                                          | 10,00ud      | 3,48  € | 0,35€/ud
3   | la islena nidos de pasta al huevo                                                | 0,50kg       | 2,35  € | 4,70€/kg
4   | reina flan de huevo                                                              | 4uds, 0,44kg | 2,75  € | 6,25€/kg
5   | dovo huevo cocido con cascara                                                    | 6,00ud       | 2,18  € | 0,36€/ud
6   | la lechera flan de huevo                                                         | 4uds, 0,40kg | 2,79  € | 6,98€/kg
7   | la lechera flan de huevo                                                         | 4uds, 0,40kg | 2,79  € | 6,98€/kg

👉 Selecciona número (1-7) o [Enter] para omitir: 2
✅ Guardado. (Mejor opción global asignada por rentabilidad: huevos grandes l por 3.2€)

> ¿Qué producto buscas?: aceitunas sin hueso

--- 🛒 Opciones en MERCADONA ---
Nº   | Producto                                           | Formato      | Precio   | Prec. Est.  
------------------------------------------------------------------------------------------------------------
1   | aceitunas negras sin hueso hacendado                                             | 3uds, 1,05kg | 3,00  € | 2,86€/kg
2   | aceitunas negras sin hueso hacendado                                             | 0,35kg       | 1,05  € | 3,00€/kg
3   | aceitunas verdes sin hueso hacendado                                             | 0,81kg       | 2,80  € | 3,46€/kg
4   | aceitunas verdes sin hueso hacendado                                             | 3uds, 0,53kg | 1,85  € | 3,52€/kg
5   | aceitunas verdes sin hueso alinadas hacendado                                    | 0,42kg       | 1,90  € | 4,52€/kg
6   | aceitunas verdes picadedos sin hueso hacendado alinadas                          | 0,71kg       | 3,80  € | 5,35€/kg
7   | aceitunas verdes con hueso hacendado                                             | 0,43kg       | 1,40  € | 3,26€/kg

👉 Selecciona número (1-7) o [Enter] para omitir: 4

--- 🛒 Opciones en HIPERDINO ---
Nº   | Producto                                           | Formato      | Precio   | Prec. Est.  
------------------------------------------------------------------------------------------------------------
1   | fragata aceitunas nega con hueso                                                 | 0,20kg       | 2,19  € | 10,95€/kg
2   | escamilla aceituna gordal sin hueso alinada                                      | 0,40kg       | 5,35  € | 13,37€/kg
3   | escamilla aceituna manzanilla sin hueso                                          | 0,16kg       | 2,09  € | 13,06€/kg
4   | escamilla aceituna manzanilla sin hueso                                          | 0,16kg       | 2,09  € | 13,06€/kg
5   | escamilla aceituna manzanilla sin hueso                                          | 0,45kg       | 4,99  € | 11,09€/kg
6   | ybarra aceituna manzanilla sin hueso                                             | 0,40kg       | 2,95  € | 7,38€/kg
7   | hiperdino aceituna negra sin hueso                                               | 0,15kg       | 0,95  € | 6,33€/kg

👉 Selecciona número (1-7) o [Enter] para omitir: 2
✅ Guardado. (Mejor opción global asignada por rentabilidad: aceitunas verdes sin hueso hacendado por 1.85€)

> ¿Qué producto buscas?: tableta chocolate milka

--- 🛒 Opciones en MERCADONA ---
Nº   | Producto                                           | Formato      | Precio   | Prec. Est.  
------------------------------------------------------------------------------------------------------------
1   | chocolate con leche milka                                                        | 0,15kg       | 1,95  € | 13,00€/kg
2   | chocolate con leche milka galleta                                                | 0,30kg       | 3,95  € | 13,17€/kg
3   | natillas de chocolate con leche hacendado                                        | 4uds, 0,50kg | 1,05  € | 2,10€/kg
4   | chocolate con leche hacendado almendras enteras                                  | 0,15kg       | 2,50  € | 16,67€/kg
5   | chocolate con leche hacendado avellanas troceadas                                | 0,15kg       | 1,65  € | 11,00€/kg
6   | chocolate con leche fundir hacendado                                             | 0,20kg       | 2,25  € | 11,25€/kg
7   | cacahuetes hacendado chocolate con leche                                         | 0,25kg       | 2,05  € | 8,20€/kg

👉 Selecciona número (1-7) o [Enter] para omitir: 1

--- 🛒 Opciones en HIPERDINO ---
Nº   | Producto                                           | Formato      | Precio   | Prec. Est.  
------------------------------------------------------------------------------------------------------------
1   | milka tableta de chocolate daim                                                  | 0,09kg       | 1,99  € | 22,11€/kg
2   | milka tableta de chocolate daim                                                  | 0,09kg       | 1,99  € | 22,11€/kg
3   | milka tableta de chocolate daim                                                  | 0,09kg       | 1,99  € | 22,11€/kg
4   | milka tableta de chocolate daim                                                  | 0,09kg       | 1,99  € | 22,11€/kg
5   | milka tableta milkinis chocolate                                                 | 0,09kg       | 1,99  € | 22,87€/kg
6   | milka tableta de chocolate con oreo                                              | 0,09kg       | 1,99  € | 21,63€/kg
7   | milka tableta de chocolate con oreo                                              | 0,10kg       | 1,36  € | 13,60€/kg

👉 Selecciona número (1-7) o [Enter] para omitir: 1
✅ Guardado. (Mejor opción global asignada por rentabilidad: chocolate con leche milka por 1.95€)

> ¿Qué producto buscas?: SALMÓN AHUMADO ENVASADO

--- 🛒 Opciones en MERCADONA ---
Nº   | Producto                                           | Formato      | Precio   | Prec. Est.  
------------------------------------------------------------------------------------------------------------
1   | salmon ahumado hacendado                                                         | 0,10kg       | 3,70  € | 37,00€/kg
2   | salmon ahumado hacendado                                                         | 2uds, 0,30kg | 10,80 € | 36,00€/kg
3   | taquitos de salmon ahumado hacendado                                             | 0,10kg       | 3,80  € | 38,00€/kg
4   | pate de salmon hacendado                                                         | 0,16kg       | 1,10  € | 6,88€/kg
5   | filete de salmon                                                                 | 0,35kg       | 8,23  € | 23,51€/kg
6   | salmon marinado hacendado                                                        | 0,08kg       | 3,50  € | 43,75€/kg
7   | escalopin de salmon                                                              | 0,35kg       | 8,75  € | 25,00€/kg

👉 Selecciona número (1-7) o [Enter] para omitir: 

--- 🛒 Opciones en HIPERDINO ---
Nº   | Producto                                           | Formato      | Precio   | Prec. Est.  
------------------------------------------------------------------------------------------------------------
1   | skandia dados de salmon ahumado                                                  | 0,10kg       | 6,42  € | 64,20€/kg
2   | royal salmon ahumado                                                             | 0,08kg       | 5,39  € | 71,87€/kg
3   | royal salmon ahumado                                                             | 0,08kg       | 5,39  € | 71,87€/kg
4   | royal salmon ahumado intenso                                                     | 0,08kg       | 6,25  € | 78,13€/kg
5   | royal salmon ahumado marinado                                                    | 0,08kg       | 4,70  € | 58,75€/kg
6   | skandia salmon ahumado basics                                                    | 0,08kg       | 3,99  € | 49,88€/kg
7   | royal salmon ahumado menos 25 de sal                                             | 0,08kg       | 5,69  € | 71,13€/kg

👉 Selecciona número (1-7) o [Enter] para omitir: 
⚠️ Producto descartado (no seleccionaste opciones en ningún supermercado).

> ¿Qué producto buscas?: 
Lista de la compra completada con 8 productos.

🛒 ¡Bienvenido al Optimizador de la Lista de la Compra (Mercadona & Hiperdino)! 🛒
Elige una opción:
1. Crear lista de la compra
2. Ver análisis de listas (Conjunta, Exclusivas y 'Tasa de Pereza')
3. Salir del programa
Responde seleccionando uno de los números del teclado:
2
```

#### Ejemplo Real de Salida del Reporte Analítico Semántico (`buildShopList`)

```text
Cargando tu análisis de compra (Conjunta vs Individual)...
====================================================================
🛒            1. LISTA DE LA COMPRA CONJUNTA OPTIMIZADA (AMBOS)     
====================================================================
- [HIPERDINO ] estrella galicia cerveza especial: 0,76€ | Cantidad: 0,33 (l)
- [HIPERDINO ] hiperdino datiles sin hueso   : 1,63€ | Cantidad: 0,25 (kg)
- [MERCADONA ] leche entera asturiana        : 11,82€ | Cantidad: 9,00 (l)
- [MERCADONA ] salmon ahumado hacendado      : 3,70€ | Cantidad: 0,10 (kg)
- [MERCADONA ] galletas rebuenas hacendado rellenas de chocolate: 1,30€ | Cantidad: 0,50 (kg)
- [MERCADONA ] huevos grandes l              : 3,20€ | Cantidad: 12,00 (ud)
- [MERCADONA ] aceitunas verdes sin hueso hacendado: 1,85€ | Cantidad: 0,53 (kg)
- [MERCADONA ] chocolate con leche milka     : 1,95€ | Cantidad: 0,15 (kg)
--------------------------------------------------------------------
💰 COSTE TOTAL OPTIMIZADO (Yendo a ambos): 26,21€
====================================================================

====================================================================
🛒            2. LISTA DE LA COMPRA EXCLUSIVA EN MERCADONA          
====================================================================
- [MERCADONA ] cerveza especial estrella galicia: 0,90€ | Cantidad: 0,33 (l)
- [MERCADONA ] datiles desecados sin hueso hacendado: 1,65€ | Cantidad: 0,25 (kg)
- [MERCADONA ] leche entera asturiana        : 11,82€ | Cantidad: 9,00 (l)
- [MERCADONA ] salmon ahumado hacendado      : 3,70€ | Cantidad: 0,10 (kg)
- [MERCADONA ] galletas rebuenas hacendado rellenas de chocolate: 1,30€ | Cantidad: 0,50 (kg)
- [MERCADONA ] huevos grandes l              : 3,20€ | Cantidad: 12,00 (ud)
- [MERCADONA ] aceitunas verdes sin hueso hacendado: 1,85€ | Cantidad: 0,53 (kg)
- [MERCADONA ] chocolate con leche milka     : 1,95€ | Cantidad: 0,15 (kg)
--------------------------------------------------------------------
💰 COSTE TOTAL EN MERCADONA: 26,37€
====================================================================

====================================================================
🛒            3. LISTA DE LA COMPRA EXCLUSIVA EN HIPERDINO          
====================================================================
- [HIPERDINO ] estrella galicia cerveza especial: 0,76€ | Cantidad: 0,33 (l)
- [HIPERDINO ] hiperdino datiles sin hueso   : 1,63€ | Cantidad: 0,25 (kg)
- [HIPERDINO ] asturiana leche entera 15 l   : 2,02€ | Cantidad: 1,00 (ud)
- [HIPERDINO ] royal salmon ahumado          : 5,39€ | Cantidad: 0,08 (kg)
- [HIPERDINO ] hiperdino galletas rellenas de chocolate: 1,00€ | Cantidad: 0,25 (kg)
- [HIPERDINO ] rujamar huevos camperos       : 3,48€ | Cantidad: 10,00 (ud)
- [HIPERDINO ] escamilla aceituna gordal sin hueso alinada: 5,35€ | Cantidad: 0,40 (kg)
- [HIPERDINO ] milka tableta de chocolate daim: 1,99€ | Cantidad: 0,09 (kg)
--------------------------------------------------------------------
💰 COSTE TOTAL EN HIPERDINO: 27,62€
====================================================================

====================================================================
📊               4. ANÁLISIS DE PÉRDIDAS Y COMPARATIVA              
====================================================================
▶️ SI COMPRAS TODO EN MERCADONA:
   • Coste Total: 26,37€
   • ❌ Dinero que pierdes por comodidad: 0,16€

▶️ SI COMPRAS TODO EN HIPERDINO:
   • Coste Total: 27,62€
   • ❌ Dinero que pierdes por comodidad: 1,41€
====================================================================
```
