# Supermarket Product Radar 🛒

### Desarrollado por **SavageNeoIdea**

Bienvenido a **Supermarket Product Radar**, una solución de software empresarial impulsada enteramente por eventos (**EDA - Event-Driven Architecture**) bajo una **Arquitectura Lambda** para la monitorización, trazabilidad histórica y optimización en tiempo real del presupuesto de la cesta de la compra.

Este sistema automatiza la recopilación de productos de las cadenas Mercadona e Hiperdino, procesa la información mediante un bróker de mensajería, almacena el histórico en un *Event Store* inmutable y consolida los datos en una unidad de negocio (*Business Unit*). Al inicializarse, reconstruye el estado completo del mercado y lo actualiza continuamente de forma reactiva para calcular de forma inteligente la combinación de compra óptima, exponiendo el denominado **"Impuesto de la pereza"**.

---

## 1. Descripción del Proyecto y Propuesta de Valor

### Descripción General

**Supermarket Product Radar** es un ecosistema multimódulo distribuido que elimina la dependencia directa de APIs HTTP externas en caliente, apoyándose en su lugar en una red de publicación/suscripción de eventos. El sistema está compuesto por 4 módulos principales:

1. **Scraper Mercadona:** Módulo con planificador integrado (*Scheduler*) configurado por horas específicas para extraer productos de la plataforma de Mercadona y publicar eventos de actualización de precios.
2. **Scraper Hiperdino:** Módulo homólogo que extrae productos de Hiperdino mediante técnicas de web scraping distribuidas (Playwright), publicando sus eventos según la localización regional y la hora programada.
3. **EventStoreBuilder:** Componente encargado de escuchar de forma persistentemente los eventos de productos y almacenarlos cronológicamente en un almacén de eventos inmutable (*Event Store*).
4. **Business Unit:** El núcleo de inteligencia de negocio. Implementa una **Arquitectura Lambda** para la gestión de datos: al arrancar, reconstruye su estado leyendo el histórico completo para inicializar su base de datos local (Datamart) y, posteriormente, procesa eventos en tiempo real para mantener el catálogo fresco, exponiendo un motor interactivo de optimización de cestas.

### Propuesta de Valor: El "Impuesto de la Pereza" (*Laziness Tax*)

El sistema permite procesar una lista de la compra en formato de texto libre y genera automáticamente tres proyecciones financieras:

* **Cesta Optimizada:** La combinación ideal y más barata del mercado, cruzando dinámicamente los productos de ambos supermercados.
* **Cesta Solo Mercadona:** Coste total si el usuario decide comprar exclusivamente en este establecimiento.
* **Cesta Solo Hiperdino:** Coste total si el usuario decide comprar exclusivamente en este establecimiento.

> 💡 **Métrica Clave:** El sistema calcula y muestra explícitamente el **Análisis de Pérdidas**, que representa la cantidad exacta de dinero que el usuario pierde por la comodidad de no diversificar su compra entre ambos establecimientos (ir a uno solo en lugar de acudir a ambos cuando toca).

---

## 2. Justificación Técnico: Arquitectura Lambda, Event Sourcing y CQRS

El proyecto implementa de manera estricta los patrones de **Event Sourcing**, **CQRS** (*Command Query Responsibility Segregation*) y se consolida sobre una **Arquitectura Lambda** para resolver eficientemente la consistencia y la disponibilidad del modelo de lectura:

### 1. Capa de Lote / Reconstrucción (*Batch Layer*)

Representada por el proceso de **Replay (Warm-up al inicio)** en la `Business Unit`. Debido a que el *Write Model* está controlado por el módulo `EventStoreBuilder` (que almacena secuencialmente en un registro inmutable *Append-Only* cada fluctuación de precio), la *Business Unit* es capaz de sincronizarse desde cero. Al arrancar, lee cronológicamente todos los eventos históricos del *Event Store* y proyecta el estado actual completo sobre el fichero SQLite, garantizando una reconstrucción perfecta y libre de corrupción de datos.

### 2. Capa Rápida (*Speed Layer*)

Una vez que la aplicación ha procesado el histórico y se encuentra activa, la *Speed Layer* entra en juego a través de `businessUnitSubscriber`. Cualquier evento nuevo o delta de precio proveniente del bróker de mensajería es consumido de forma reactiva en tiempo real. Esto permite actualizar directamente las entradas del Datamart para reflejar los cambios más recientes de los mismos productos sin necesidad de volver a computar todo el lote histórico.

### 3. Capa de Servicio (*Serving Layer*)

La base de datos local **SQLite** (parametrizada mediante su cadena de conexión JDBC con soporte WAL) actúa como el **Datamart de consulta**. Al estar completamente desacoplada del almacenamiento de escritura principal, ofrece lecturas de bajísima latencia para alimentar instantáneamente al motor de optimización de listas de la compra.

---

## 3. Arquitectura del Sistema

### Arquitectura de Mensajería y Flujo Lambda

El flujo de información se distribuye a través de tópicos mediante el bróker de mensajería parametrizado en el archivo `config.json`. Aquí se aprecia cómo coexisten el flujo en tiempo real (Speed) y el de reconstrucción (Batch):

```
[ Scraper Mercadona ] --------(Publica en: "product")--------> [ Bróker Mensajería ]
[ Scraper Hiperdino ] --------(Publica en: "product")--------> [   tcp://61616    ]
                                                                      |
       +--------------------------------------------------------------+
       | (SPEED LAYER: Suscripción en tiempo real)                    | (BATCH LAYER: Almacén Histórico)
       v                                                              v
[ Business Unit ]                                             [ EventStoreBuilder ]
 (Topic: "product")                                            (Topic: "product")
       ^
       | [BATCH LAYER: Replay inicial del histórico desde el Event Store]
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
 - Planificación Horaria      - Subscriber dedicado             - Replay Engine (Batch)
 - Algoritmo Extracción       - Persistencia Inmutable          - Real-Time Sub (Speed)
 - Publisher ("product")      - Registro cronológico            - Datamart SQLite (Serving)

```

---

## 4. Principios y Patrones de Diseño Aplicados

Para garantizar un ecosistema mantenible, escalable y robusto, el desarrollo se ha guiado por las mejores prácticas de la ingeniería de software:

* **Patrón Publish-Subscriber (Pub/Sub):** Desacoplamiento total entre los productores de datos (scrapers) y los consumidores finales.
* **Event Sourcing:** Salvaguarda el ciclo de vida del producto como una serie de eventos mutativos, impidiendo la sobrescritura directa de datos de precios.
* **Scrapers:** Extracción con APIs JSON internas en Mercadona frente a la interceptación de peticiones HTTP mediante Playwright en Hiperdino bajo un comportamiento común de aprovisionamiento de datos.
* **Principio de Responsabilidad Única (SRP):** Hemos tratado de prestar bastante atención a SRP.
* **Inyección de Dependencias y Desacoplamiento:** Uso sistemático de **interfaces** y almacenamiento (`Store`, `WebScraper`, etc.), facilitando la sustitución de componentes (por ejemplo, cambiar el bróker o la base de datos).

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

> 📌 **Notas de Configuración Clave:**
> * **Datamart con SQLite:** Definido en `"datamartUrl"`. Actúa como nuestra *Serving Layer* e incluye optimizaciones críticas de concurrencia y velocidad como el modo WAL (`journal_mode=WAL`) y tiempos de espera (`busy_timeout=5000`).
> * **Planificación del Scraping (Scheduler):** Los campos `"ScheduleTimeHour"` y `"ScheduleTimeMinutes"` determinan de forma exacta a qué hora del día se disparará el proceso automático de extracción de datos para cada supermercado, evitando ejecuciones redundantes y permitiendo una sincronización controlada.
> * **Localización Regional:** Hiperdino regionaliza sus precios. El código postal de consulta se configura dinámicamente mediante la propiedad `"postalCode"`.
---

## 6. Instrucciones de Compilación y Ejecución

Para garantizar la integridad de la arquitectura Lambda y evitar pérdidas de mensajes durante la sincronización, **se debe seguir estrictamente el siguiente orden de encendido**:

### Requisitos Previos

* Tener instalado y en ejecución un Bróker de Mensajería (ej. Apache ActiveMQ / Artemis) escuchando en el puerto configurado (por defecto `tcp://localhost:61616`).
* Asegurar que el archivo `config.json` esté presente en la raíz de la carpeta principal que contiene todos los módulos del proyecto.

### Pasos para el Arranque del Sistema

1. **Levantar el Bróker:** Asegúrate de que ActiveMQ esté operativo.
2. **Levantar el Event Store (`EventStoreBuilder`):** Ejecuta `Main.java` en este módulo. Comenzará a escuchar y persistir de manera inmediata cualquier evento entrante para salvaguardar el histórico.
3. **Levantar la Unidad de Negocio (`Business Unit`):** Ejecuta `Main.java` en el módulo `BusinessUnit`. Se conectará a la *Serving Layer* SQLite (creándola si no existe), ejecutará de inmediato el **Replay de la Capa Batch** para reconstruir el estado actual, y finalmente dejará abierta la **Capa Speed** para asimilar variaciones en tiempo real.
4. **Iniciar Scrapers (La fuente de datos):** Ejecuta los archivos `Main.java` de los módulos `Hiperdino` y `Mercadona`. Estos iniciarán sus respectivos *Schedulers* internos basándose en las propiedades `ScheduleTimeHour` y `ScheduleTimeMinutes` provistas en el `config.json`.

---

## 7. Ejemplos de Uso e Interacción

Al iniciar el módulo **Business Unit**, se desplegará la interfaz interactiva por consola basada en comandos de teclado.

> 📝 **Nota de Uso:** Para obtener mejores resultados en las modificaciones de texto, introduce sustantivos claros línea por línea (ej: "aceite oliva", "salmon", "leche").

### Flujo en la Consola de Comandos

Al arrancar la aplicación, verás el menú principal interactivo generado por la interfaz CLI:

```text
Bienvenido a la lista de compra automatica de Hiperdino!: elige una opción:
1. Crear lista de la compra
2. Observar la lista creada
3. Salir del programa:
Responde seleccionando uno de los números del teclado: 

```

1. Introduce `1` para iniciar el asistente de entrada de datos (`Creando lista...`). El sistema te pedirá los artículos línea por línea hasta que escribas `fin` o dejes una línea vacía.
2. Introduce `2` (`Cargando tu lista actual...`) para invocar de inmediato al motor analítico. Este cruzará los datos del Datamart optimizado y generará el reporte de ahorro comparativo.

#### Ejemplo Real de Salida del Reporte Analítico (`buildShopList`)

El sistema evaluará los datos consolidados en la base de datos y arrojará el siguiente formato de salida estructurado:

```text
====================================================================
🛒             LISTA DE LA COMPRA CONJUNTA OPTIMIZADA               
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
