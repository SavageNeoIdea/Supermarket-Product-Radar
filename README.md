# Supermarket Product Radar 🛒

### Desarrollado por **SavageNeoIdea**

Bienvenido a **Supermarket Product Radar**, una solución de software empresarial impulsada enteramente por eventos (**EDA - Event-Driven Architecture**) para la monitorización, trazabilidad histórica y optimización del presupuesto de la cesta de la compra en tiempo real.

Este sistema automatiza la recopilación de productos de las cadenas Mercadona e Hiperdino, procesa la información mediante un bróker de mensajería, almacena el histórico en un *Event Store* inmutable y consolida los datos en una unidad de negocio (*Business Unit*) capaz de calcular de forma inteligente la combinación de compra óptima, exponiendo el denominado **"Impuesto de la pereza"**.

---

## 1. Descripción del Proyecto y Propuesta de Valor

### Descripción General

**Supermarket Product Radar** es un ecosistema multimódulo distribuido que elimina la dependencia directa de APIs HTTP externas en caliente, apoyándose en su lugar en una red de publicación/suscripción de eventos. El sistema está compuesto por 4 módulos principales:

1. **Scraper Mercadona:** Módulo con planificador integrado (*Scheduler*) encargado de extraer productos de la plataforma de Mercadona y publicar eventos de actualización de precios.
2. **Scraper Hiperdino:** Módulo homólogo que extrae productos de Hiperdino mediante técnicas de web scraping distribuidas y publica sus respectivos eventos.
3. **EventStoreBuilder:** Componente encargado de escuchar de forma persistentemente los eventos de productos y almacenarlos cronológicamente en un almacén de eventos inmutable (*Event Store*).
4. **Business Unit:** El núcleo de inteligencia de negocio. Al arrancar, reconstruye su estado leyendo el histórico completo del *Event Store* para inicializar su base de datos local de consultas. Posteriormente, procesa eventos en tiempo real para mantener el catálogo actualizado y expone un motor interactivo de optimización de cestas.

### Propuesta de Valor: El "Impuesto de la Pereza" (*Laziness Tax*)

El sistema permite procesar una lista de la compra en formato de texto libre y genera automáticamente tres proyecciones financieras:

* **Cesta Optimizada:** La combinación ideal y más barata del mercado, cruzando dinámicamente los productos de ambos supermercados.
* **Cesta Solo Mercadona:** Coste total si el usuario decide comprar exclusivamente en este establecimiento.
* **Cesta Solo Hiperdino:** Coste total si el usuario decide comprar exclusivamente en este establecimiento.

> 💡 **Métrica Clave:** El sistema calcula y muestra explícitamente el **Análisis de Pérdidas**, que representa la cantidad exacta de dinero que el usuario pierde por la comodidad de no diversificar su compra entre ambos establecimientos (ir a uno solo en lugar de acudir a ambos cuando toca).

---

## 2. Justificación Técnica y Estructura del Datamart

El proyecto implementa de manera estricta los patrones **Event Sourcing** y **CQRS** (*Command Query Responsibility Segregation*):

* **Event Store (Write Model):** Controlado por el módulo `EventStoreBuilder`. Almacena secuencialmente en un registro inmutable (*Append-Only*) cada fluctuación, inserción o cambio de precio de un producto. Esto garantiza auditoría completa y trazabilidad histórica sin pérdida de datos.
* **Read Model / Datamart (Business Unit):** La *Business Unit* utiliza **SQLite** como una base de datos local, ligera y de alto rendimiento para el Datamart de consulta.
* **Proceso de Replay (Warm-up al inicio):** Al arrancar la aplicación, el módulo lee cronológicamente todos los eventos históricos desde el almacenamiento persistente del Event Store y realiza una proyección sobre el fichero SQLite, reconstruyendo el catálogo al último estado conocido.
* **Sincronización en Tiempo Real:** Una vez activa la aplicación, cualquier evento nuevo proveniente del bróker updates directamente el archivo SQLite de forma reactiva, garantizando búsquedas instantáneas sobre datos frescos sin penalizar ni saturar el *Event Store*.

---

## 3. Arquitectura del Sistema

### Arquitectura de Mensajería (Topología de Eventos)

El flujo de información se distribuye a través de tópicos mediante el bróker de mensajería parametrizado en el archivo `config.json`:

```
[ Scraper Mercadona ] --------(Publica en: "product")--------> [ Bróker Mensajería ]
[ Scraper Hiperdino ] --------(Publica en: "product")--------> [   tcp://61616    ]
                                                                      |
       +--------------------------------------------------------------+
       | (Suscripción en tiempo real)                                 | (Suscripción Histórica)
       v                                                              v
[ Business Unit ]                                             [ EventStoreBuilder ]
 (Topic: "product")                                            (Topic: "product")
       ^
       | [Lectura inicial del histórico / Replay de eventos en disco]
       + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +

```

### Arquitectura de la Aplicación (Módulos Internos)

```
+---------------------------------------------------------------------------------+
|                                  config.json                                    |
|  (Centraliza credenciales, brokerUrl, TOPICS de publicación y suscripción)       |
+---------------------------------------------------------------------------------+
                                       |
       +-------------------------------+-------------------------------+
       |                               |                               |
       v                               v                               v
[ Módulos Scrapers ]         [ EventStoreBuilder ]             [ Business Unit ]
 - Scheduler interno          - Subscriber dedicado             - Replay Engine (Inicio)
 - Algoritmo Extracción       - Persistencia Inmutable          - Real-Time Subscriber
 - Publisher ("product")      - Registro cronológico            - Motor de búsqueda texto
                                                                - Algoritmo de Cestas

```

---

## 4. Principios y Patrones de Diseño Aplicados

Para garantizar un ecosistema mantenible, escalable y robusto, el desarrollo se ha guiado por las mejores prácticas de la ingeniería de software:

* **Patrón Publish-Subscriber (Pub/Sub):** Desacoplamiento total entre los productores de datos (scrapers) y los consumidores finales.
* **Event Sourcing:** Salvaguarda el ciclo de vida del producto como una serie de eventos mutativos, impidiendo la sobrescritura directa de datos de precios.
* **Patrón Strategy (en Scrapers):** Abstrae las diferentes estructuras de extracción (APIs JSON internas en Mercadona frente a la interceptación HTTP requests mediante Playwright en Hiperdino) bajo un comportamiento común de aprovisionamiento de datos.
* **Principio de Responsabilidad Única (SRP):** Cada módulo posee una única razón de cambio bien delimitada (extracción de datos, persistencia del log de eventos o motor analítico de negocio).
* **Inyección de Dependencias y Desacoplamiento:** Uso sistemático de **interfaces** en los puntos de integración y almacenamiento (`Store`, `WebScraper`, etc.), facilitando la sustitución de componentes (por ejemplo, cambiar el bróker o la base de datos) y la implementación de pruebas unitarias.
* **Clean Code & Naming Semántico:** Cuidado minucioso en la nomenclatura de clases, variables y métodos para que el código actúe como documentación viva del dominio.

---

## 5. Configuración del Sistema

### Configuración de la Red de Eventos (`config.json`)

Toda la infraestructura de comunicación se parametriza mediante un archivo centralizado. Asegúrate de ubicarlo en la raíz de los módulos correspondientes:

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
      "password": "admin"
    },
    "mercadona": {
      "brokerUrl": "tcp://localhost:61616",
      "topicName": "product",
      "username": "admin",
      "password": "admin"
    }
  }
}

```

### Configuración del Código Postal (Específico para Hiperdino)

Debido a que Hiperdino regionaliza sus precios, el módulo utiliza un scraper automatizado con Playwright. Puedes configurar tu código postal directamente en la clase principal del módulo `HiperdinoScraper` (`Hiperdino/src/main/java/org/sni/spr/hiperdino/Main.java`):

```java
public class Main {
    // Modify this variable to target your specific location
    private static final String POSTAL_CODE = "35010"; 

    public static void main(String[] args) {
        try {
            HiperdinoProductParser productParser = new HiperdinoProductParser();
            WebScraper webScraper = new HiperdinoPlaywrightManager(POSTAL_CODE);
            Store storer = new ActivemqStore();
            
            Controller controller = new Controller(
                    new HiperdinoFeeder(productParser, webScraper),
                    storer
            );
            controller.startScheduler();
        } catch (Exception e) {
            System.err.println("Ocurrió un error durante el proceso: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

```

---

## 6. Instrucciones de Compilación y Ejecución

Para garantizar la integridad del flujo de datos en tiempo real y evitar la pérdida de mensajes durante el proceso de sincronización inicial, **se debe seguir estrictamente el siguiente orden de encendido**:

### Requisitos Previos

* Tener instalado y en ejecución un Bróker de Mensajería (ej. Apache ActiveMQ / Artemis) escuchando en el puerto configurado (por defecto `tcp://localhost:61616`).
* Asegurar que el archivo `config.json` esté presente en el directorio de ejecución de cada módulo.

### Pasos para el Arranque del Sistema

1. **Levantar el Bróker:** Asegúrate de que ActiveMQ esté operativo.
2. **Levantar el Event Store (`EventStoreBuilder`):** Ejecuta `Main.java` en el módulo `EventStoreBuilder`. Comenzará a escuchar y persistir de manera inmediata cualquier evento entrante.
3. **Levantar el Read Model (`Business Unit`):** Ejecuta `Main.java` en el módulo `BusinessUnit`. Creará el archivo de base de datos SQLite si no existe, ejecutará el Replay de los eventos históricos desde el almacén y se mantendrá a la escucha de actualizaciones en tiempo real.
4. **Iniciar Scrapers (La fuente de datos):** Ejecuta los archivos `Main.java` de los módulos `Hiperdino` y `Mercadona` para comenzar el volcado periódico de productos.

---

## 7. Ejemplos de Uso e Interacción

Al iniciar el módulo **Business Unit**, se desplegará la interfaz interactiva por consola basada en comandos de teclado. Si es la primera vez que se inicia, el sistema generará de forma automática el Datamart en SQLite, procesará el histórico del *Event Store* y se suscribirá dinámicamente al bróker para recibir datos en tiempo real.

> 📝 **Nota de Uso:** Para obtener mejores resultados en las coincidencias de texto, introduce sustantivos claros línea por línea (ej: "aceite oliva", "salmon", "leche").

### Flujo en la Consola de Comandos

Al arrancar la aplicación, verás el menú principal interactivo:

```text
Bienvenido a la lista de compra automatica de Hiperdino!: elige una opción:
1. Crear lista de la compra
2. Consultar una lista
3. Observar la lista creada actual
4. Salir del programa: 
Responde seleccionando uno de los números del teclado: 

```

1. Introduce `1` para iniciar el asistente de entrada de datos (`Creando lista...`).
2. Una vez rellenada tu lista, introduce `3` (`Cargando tu lista actual...`) para invocar al motor analítico y procesar el reporte comparativo.

#### Ejemplo Real de Salida del Reporte Analítico (`buildShopList`)

El sistema evaluará los datos cruzados de ambos supermercados y arrojará el siguiente formato de salida estructurado:

```text
====================================================================
🛒            LISTA DE LA COMPRA CONJUNTA OPTIMIZADA               
====================================================================
- [MERCADONA] Aceite Oliva Suave 1L: 7.20€ | Cantidad: 1 (unidad)
- [HIPERDINO] Salmon Fresco Rodajas: 4.50€ | Cantidad: 1 (pack)
- [HIPERDINO] Leche Entera Clásica: 5.10€ | Cantidad: 6 (litros)
--------------------------------------------------------------------
💰 COSTE TOTAL OPTIMIZADO (Yendo a ambos): 16.80€
====================================================================

====================================================================
📊               ANÁLISIS DE PÉRDIDAS POR COMPRAR EN UN SOLO SITIO   
====================================================================
▶️ SI COMPRAS TODO EN MERCADONA:
   • Coste Total: 19.45€
   • ❌ Dinero que pierdes por no ir a Hiperdino cuando toca: 2.65€

▶️ SI COMPRAS TODO EN HIPERDINO:
   • Coste Total: 18.20€
   • ❌ Dinero que pierdes por no ir a Mercadona cuando toca: 1.40€
====================================================================

```
