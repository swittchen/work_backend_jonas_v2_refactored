# BackendJonasv2 — Документация проекта

> Сервис синхронизации биологических источников (BioSource) между внешним API Signals и локальной базой данных.

---

## Содержание

1. [Обзор проекта](#1-обзор-проекта)
2. [Технический стек](#2-технический-стек)
3. [Архитектура](#3-архитектура)
4. [Структура проекта](#4-структура-проекта)
5. [Слои приложения](#5-слои-приложения)
   - [API Layer](#51-api-layer)
   - [Application Layer](#52-application-layer)
   - [Domain Layer](#53-domain-layer)
   - [Infrastructure Layer](#54-infrastructure-layer)
   - [Config Layer](#55-config-layer)
6. [Pipeline — конвейер синхронизации](#6-pipeline--конвейер-синхронизации)
7. [Доменная модель](#7-доменная-модель)
8. [REST API](#8-rest-api)
9. [Конфигурация и переменные окружения](#9-конфигурация-и-переменные-окружения)
10. [Схема потока данных](#10-схема-потока-данных)
11. [Обработка ошибок](#11-обработка-ошибок)
12. [Зависимости между компонентами](#12-зависимости-между-компонентами)
13. [Запуск проекта](#13-запуск-проекта)

---

## 1. Обзор проекта

`BackendJonasv2` — это Spring Boot сервис, который синхронизирует данные типа **BioSource** из внешнего REST API (Signals) в локальную базу данных PostgreSQL.

**Ключевая задача:**  
Найти разницу между тем, что есть в Signals, и тем, что уже сохранено локально — и подтянуть только новые записи.

**Принципы, на которых построен проект:**
- **Гексагональная архитектура** (Ports & Adapters) — бизнес-логика не знает ничего о HTTP или базе данных
- **Pipeline Pattern** — синхронизация разбита на 6 независимых шагов
- **Single Responsibility** — каждый класс делает ровно одно дело

---

## 2. Технический стек

| Компонент | Технология |
|-----------|-----------|
| Язык | Java 17 |
| Фреймворк | Spring Boot 4.0.6 |
| Реактивный HTTP | Spring WebFlux (Project Reactor) |
| HTTP-клиент | `WebClient` (реактивный) |
| Сериализация JSON | Jackson 3.x (`tools.jackson`) |
| База данных | PostgreSQL (драйвер подключён) |
| Утилиты | Lombok (`@Value`, `@Builder`, `@Slf4j`, ...) |
| Сборка | Maven |

---

## 3. Архитектура

Проект реализует **гексагональную архитектуру** (также известную как «Ports and Adapters»):

```
┌──────────────────────────────────────────────────────────┐
│                        API Layer                         │
│         BioSourceController  ·  GlobalExceptionHandler   │
└───────────────────────────┬──────────────────────────────┘
                            │ вызывает Use Case
┌───────────────────────────▼──────────────────────────────┐
│                   Application Layer                      │
│     SyncBioSourcesUseCase  ·  Pipeline  ·  6 Stages      │
└──────┬─────────────────────────────────────┬─────────────┘
       │ через Port (интерфейс)              │ через Port (интерфейс)
┌──────▼──────────┐               ┌──────────▼─────────────┐
│  Domain Layer   │               │  Domain Layer           │
│ BioSourceRemote │               │ BioSourceRepository     │
│  ClientPort     │               │  Port                   │
└──────┬──────────┘               └──────────┬──────────────┘
       │ реализует                            │ реализует
┌──────▼──────────┐               ┌──────────▼─────────────┐
│ Infrastructure  │               │ Infrastructure          │
│ SignalsWebClient│               │ BioSourceRepository     │
│ Adapter         │               │ Adapter                 │
└─────────────────┘               └────────────────────────┘
```

**Правило зависимостей:**
- `api` → `application` → `domain` ← `infrastructure`
- `domain` не зависит ни от чего
- `infrastructure` зависит только от `domain` (через порты)

---

## 4. Структура проекта

```
src/main/java/org/sergei/backendJonasv2/
│
├── api/                                  # Входная точка для HTTP-запросов
│   ├── controller/
│   │   └── BioSourceController.java      # REST-контроллер /api/v1/biosources
│   ├── dto/
│   │   └── SyncResponse.java             # DTO ответа синхронизации
│   └── exception/
│       └── GlobalExceptionHandler.java   # Централизованная обработка ошибок
│
├── application/                          # Логика приложения (Use Cases)
│   ├── usecase/
│   │   └── SyncBioSourcesUseCase.java    # Оркестратор синхронизации
│   └── pipeline/
│       ├── Pipeline.java                 # Исполнитель конвейера
│       ├── PipelineContext.java          # Общее состояние между шагами
│       ├── PipelineResult.java           # Результат выполнения конвейера
│       ├── PipelineStage.java            # Контракт для шага конвейера
│       └── stages/                       # Реализации шагов
│           ├── FetchRemoteBioSourcesStage.java
│           ├── LoadBioSourcesStage.java
│           ├── FilterNewBioSourceStage.java
│           ├── FetchBioSourceDetailsStage.java
│           ├── EnrichAncestorRankStage.java
│           └── PersistBioSourceStage.java
│
├── domain/                               # Чистая бизнес-логика — без Spring, без JPA
│   ├── model/
│   │   └── BioSource.java                # Доменная модель (иммутабельная)
│   ├── port/
│   │   ├── BioSourceRemoteClientPort.java  # Порт для внешнего API
│   │   └── BioSourceRepositoryPort.java    # Порт для базы данных
│   └── exception/
│       ├── BioSourceNotFoundException.java
│       └── PipelineException.java
│
├── infrastructure/                       # Адаптеры для внешних систем
│   ├── http/
│   │   ├── PagedResponse.java            # Record для страничного ответа
│   │   ├── client/
│   │   │   └── SignalsWebClientAdapter.java  # HTTP-адаптер для Signals API
│   │   └── mapper/
│   │       └── SignalsResponseMapper.java   # Маппинг JSON → BioSource
│   └── persistence/
│       └── BioSourceRepositoryAdapter.java  # Адаптер репозитория (TODO: DB)
│
├── config/
│   ├── PipelineConfig.java               # Порядок шагов конвейера
│   └── WebClientConfig.java              # Конфигурация WebClient
│
└── BackendJonasv2Application.java        # Точка входа Spring Boot
```

---

## 5. Слои приложения

### 5.1 API Layer

**Пакет:** `org.sergei.backendJonasv2.api`

Отвечает за приём HTTP-запросов и возврат HTTP-ответов. Не содержит бизнес-логики.

#### `BioSourceController`

```
POST /api/v1/biosources/sync
```

- Вызывает `SyncBioSourcesUseCase.execute()`
- При успехе → `200 OK` + `SyncResponse`
- При ошибке → `500 Internal Server Error` + `SyncResponse` с описанием ошибки

#### `SyncResponse` (DTO)

| Поле | Тип | Описание |
|------|-----|----------|
| `success` | `boolean` | Успешно ли выполнена синхронизация |
| `syncedCount` | `int` | Количество сохранённых записей |
| `warnings` | `List<String>` | Предупреждения в ходе выполнения |
| `failedStage` | `String` | Имя шага, на котором произошла ошибка |
| `errorMessage` | `String` | Сообщение об ошибке |
| `timestamp` | `Instant` | Время выполнения |

#### `GlobalExceptionHandler`

Ловит исключения на уровне Spring MVC и возвращает структурированный JSON:

| Исключение | HTTP-статус |
|-----------|------------|
| `BioSourceNotFoundException` | `404 Not Found` |
| `PipelineException` | `500 Internal Server Error` |
| `Exception` (любой) | `500 Internal Server Error` |

---

### 5.2 Application Layer

**Пакет:** `org.sergei.backendJonasv2.application`

Содержит Use Cases и Pipeline. Зависит от `domain`, не знает о HTTP и базах данных.

#### `SyncBioSourcesUseCase`

Единственный Use Case приложения. При вызове `execute()`:
1. Создаёт новый `PipelineContext`
2. Создаёт `Pipeline` с упорядоченными шагами из конфига
3. Запускает `pipeline.run(context)`
4. Возвращает `PipelineResult`

#### `Pipeline`

Исполняет шаги последовательно по стратегии **Fail-Fast**:
- Если шаг вернул исключение → конвейер останавливается
- Результат упаковывается в `PipelineResult.failure(...)`
- Каждый шаг логирует время выполнения

#### `PipelineContext`

Общий изменяемый контейнер состояния, который передаётся через все шаги:

| Поле | Тип | Заполняется на шаге |
|------|-----|---------------------|
| `remoteBioSources` | `List<BioSource>` | 1 — FetchRemote |
| `localBioSources` | `List<BioSource>` | 2 — LoadLocal |
| `newEids` | `List<String>` | 3 — FilterNew |
| `fetchedDetails` | `List<BioSource>` | 4 — FetchDetails |
| `readyToPersist` | `List<BioSource>` | 5 — Enrich |
| `warnings` | `List<String>` | любой шаг |
| `metadata` | `Map<String, Object>` | любой шаг |

#### `PipelineResult` (record)

```java
record PipelineResult(boolean success, PipelineContext context,
                      String failedStageName, String errorMessage)
```

Статические фабрики: `PipelineResult.success(context)`, `PipelineResult.failure(context, stage, error)`

---

### 5.3 Domain Layer

**Пакет:** `org.sergei.backendJonasv2.domain`

Чистое ядро — без Spring-аннотаций, без JPA, без HTTP. Может быть протестировано в изоляции.

#### `BioSource` (доменная модель)

Иммутабельный объект (`@Value` Lombok):

| Поле | Тип | Описание |
|------|-----|----------|
| `eid` | `String` | Уникальный внешний идентификатор |
| `name` | `String` | Название |
| `taxId` | `String` | Таксономический ID |
| `rank` | `String` | Таксономический ранг |
| `synonyms` | `List<String>` | Синонимы |
| `ancestorEid` | `String` | EID предка |
| `ancestorName` | `String` | Имя предка |
| `ancestorRank` | `String` | Ранг предка |
| `location` | `String` | Местоположение |

Поддерживает `@Builder` и `@With` (создание копии с изменённым полем).

#### Порты (интерфейсы)

**`BioSourceRemoteClientPort`** — что нужно от внешнего API:

```java
Flux<BioSource> fetchAllChildren();                    // все дочерние элементы
Flux<BioSource> fetchDetailsByEids(List<String> eids); // детали по EID
Mono<String>    createBioSource(BioSource bioSource);  // создание новой записи
```

**`BioSourceRepositoryPort`** — что нужно от базы данных:

```java
List<BioSource>          findAll();
Optional<BioSource>      findByEid(String eid);
List<BioSource>          saveAll(List<BioSource> bioSources);
```

#### Исключения домена

| Класс | Родитель | Назначение |
|-------|---------|-----------|
| `BioSourceNotFoundException` | `RuntimeException` | Запись не найдена по EID |
| `PipelineException` | `RuntimeException` | Ошибка конкретного шага конвейера |

`PipelineException` хранит `stageName` — имя шага, где произошла ошибка.

---

### 5.4 Infrastructure Layer

**Пакет:** `org.sergei.backendJonasv2.infrastructure`

Реализует порты домена. Знает о HTTP и базах данных.

#### `SignalsWebClientAdapter`

Реализует `BioSourceRemoteClientPort` через реактивный `WebClient`.

**Пагинация** (`fetchAllChildren`):
- Запрашивает страницы по 100 записей
- Использует `expand()` оператор Reactor для рекурсивной загрузки страниц
- Останавливается, когда `hasMore = false`

**Параллельная загрузка** (`fetchDetailsByEids`):
- Загружает детали по EID с параллелизмом = 5

Константы:
```
BIO_SOURCE_LIBRARY_EID = "assetType:69171dcc6da72b77e913daa5"
PAGE_SIZE = 100
```

#### `SignalsResponseMapper`

Маппер между JSON (формат JSON:API) и доменной моделью `BioSource`.

| Метод | Описание |
|-------|----------|
| `toPagedResponse(json, offset, pageSize)` | JSON → `PagedResponse` (список + признак наличия следующей страницы) |
| `toBioSource(node)` | JSON-узел → `BioSource` |
| `toCreateRequestPayload(bioSource)` | `BioSource` → JSON-строка для создания |

#### `PagedResponse` (record)

```java
record PagedResponse(List<BioSource> items, int offset, boolean hasMore)
```

Вспомогательный тип для пагинации HTTP-ответов. Живёт в `infrastructure.http`.

#### `BioSourceRepositoryAdapter`

Реализует `BioSourceRepositoryPort`. В текущей версии содержит заглушки — готов к подключению реальной БД (Spring Data JPA / JDBC).

---

### 5.5 Config Layer

**Пакет:** `org.sergei.backendJonasv2.config`

#### `PipelineConfig`

Явно задаёт порядок шагов конвейера. Spring без этого конфига взял бы произвольный порядок бинов — что было бы опасно.

```java
List.of(fetchRemote, loadLocal, filterNew, fetchDetails, enrichAncestor, persist)
```

#### `WebClientConfig`

Создаёт бин `WebClient` для Signals API. База URL конфигурируется через `application.properties`.

---

## 6. Pipeline — конвейер синхронизации

Синхронизация выполняется через 6 последовательных шагов:

```
┌─────────────────────────────────────────────────────────────┐
│                      SyncBioSourcesUseCase                  │
│                                                             │
│  ┌──────────────┐   ┌──────────────┐   ┌────────────────┐  │
│  │   Step 1     │   │   Step 2     │   │    Step 3      │  │
│  │ FetchRemote  │──▶│  LoadLocal   │──▶│  FilterNew     │  │
│  │ BioSources   │   │  BioSources  │   │  BioSources    │  │
│  │              │   │              │   │                │  │
│  │ → remote     │   │ → local      │   │ → newEids      │  │
│  └──────────────┘   └──────────────┘   └────────┬───────┘  │
│                                                 │          │
│  ┌──────────────┐   ┌──────────────┐   ┌────────▼───────┐  │
│  │   Step 6     │   │   Step 5     │   │    Step 4      │  │
│  │   Persist    │◀──│    Enrich    │◀──│ FetchDetails   │  │
│  │  BioSources  │   │  Ancestor    │   │  by EIDs       │  │
│  │              │   │    Rank      │   │                │  │
│  │  saveAll()   │   │ → ready      │   │ → fetched      │  │
│  └──────────────┘   └──────────────┘   └────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Шаги конвейера

| # | Класс | Действие | Читает из Context | Пишет в Context |
|---|-------|----------|-------------------|-----------------|
| 1 | `FetchRemoteBioSourcesStage` | Загружает все BioSource из Signals | — | `remoteBioSources` |
| 2 | `LoadBioSourcesStage` | Загружает все BioSource из локальной БД | — | `localBioSources` |
| 3 | `FilterNewBioSourceStage` | Находит EID, которых нет в БД | `remote`, `local` | `newEids` |
| 4 | `FetchBioSourceDetailsStage` | Загружает полные данные по новым EID | `newEids` | `fetchedDetails` |
| 5 | `EnrichAncestorRankStage` | Фильтрует записи без предка, добавляет предупреждения | `fetchedDetails` | `readyToPersist` |
| 6 | `PersistBioSourceStage` | Сохраняет все обогащённые записи в БД | `readyToPersist` | — |

### Оптимизация — `shouldRun()`

Шаги 4 и 6 пропускаются автоматически если нечего обрабатывать:
- Шаг 4: пропускается, если `newEids` пуст
- Шаг 6: пропускается, если `readyToPersist` пуст

---

## 7. Доменная модель

```
BioSource
├── eid          ← уникальный ключ (внешний ID в Signals)
├── name         ← название
├── taxId        ← таксономический ID (NCBI Taxonomy)
├── rank         ← таксономический ранг (species, genus, ...)
├── synonyms     ← список синонимов
├── ancestorEid  ← EID родительского таксона
├── ancestorName ← имя родителя
├── ancestorRank ← ранг родителя
└── location     ← географическое происхождение
```

Объект **иммутабельный** — нет сеттеров. Изменение через `@With`:
```java
BioSource updated = original.withRank("species");
```

---

## 8. REST API

### `POST /api/v1/biosources/sync`

Запускает полный цикл синхронизации.

**Запрос:** тело не требуется.

**Успешный ответ** `200 OK`:
```json
{
  "success": true,
  "syncedCount": 42,
  "warnings": [],
  "failedStage": null,
  "errorMessage": null,
  "timestamp": "2026-05-23T10:15:30Z"
}
```

**Ответ при ошибке** `500 Internal Server Error`:
```json
{
  "success": false,
  "syncedCount": 0,
  "warnings": [],
  "failedStage": "FetchRemoteBioSources",
  "errorMessage": "Connection refused: signals.api",
  "timestamp": "2026-05-23T10:15:31Z"
}
```

**Ответ при исключении** (через `GlobalExceptionHandler`):
```json
{
  "timestamp": "2026-05-23T10:15:31Z",
  "status": 500,
  "message": "Interner Fehler"
}
```

---

## 9. Конфигурация и переменные окружения

Настраивается через `src/main/resources/application.properties`:

| Свойство | По умолчанию | Описание |
|----------|-------------|----------|
| `signals.api.base-url` | `http://localhost` | Базовый URL внешнего Signals API |
| `spring.datasource.url` | — | URL базы данных PostgreSQL |
| `spring.datasource.username` | — | Пользователь БД |
| `spring.datasource.password` | — | Пароль БД |

Пример `application.properties`:
```properties
signals.api.base-url=https://signals.example.com/api

spring.datasource.url=jdbc:postgresql://localhost:5432/biosource_db
spring.datasource.username=postgres
spring.datasource.password=secret
```

---

## 10. Схема потока данных

```
Внешний клиент
     │
     │  POST /api/v1/biosources/sync
     ▼
BioSourceController
     │
     │  execute()
     ▼
SyncBioSourcesUseCase
     │
     │  pipeline.run(context)
     ▼
Pipeline (Fail-Fast)
     │
     ├──▶ FetchRemoteBioSourcesStage
     │         │  WebClient.get("/entities/.../children") ──▶ Signals API
     │         │◀─ Flux<BioSource>
     │
     ├──▶ LoadBioSourcesStage
     │         │  repository.findAll() ──▶ PostgreSQL
     │         │◀─ List<BioSource>
     │
     ├──▶ FilterNewBioSourceStage
     │         │  сравнивает EID: remote \ local
     │         │◀─ List<String> newEids
     │
     ├──▶ FetchBioSourceDetailsStage  [пропускается если newEids пуст]
     │         │  WebClient.get("/materials/{eid}") ──▶ Signals API (×N, параллельно)
     │         │◀─ Flux<BioSource>
     │
     ├──▶ EnrichAncestorRankStage
     │         │  фильтрует по наличию ancestorEid
     │         │◀─ List<BioSource> readyToPersist
     │
     └──▶ PersistBioSourceStage  [пропускается если readyToPersist пуст]
               │  repository.saveAll() ──▶ PostgreSQL
               │◀─ List<BioSource>
     │
     ▼
PipelineResult (success/failure)
     │
     ▼
BioSourceController → ResponseEntity<SyncResponse>
     │
     ▼
Внешний клиент
```

---

## 11. Обработка ошибок

### Стратегия в Pipeline

- **Fail-Fast**: первая же ошибка в любом шаге останавливает конвейер
- Исключение перехватывается в `Pipeline.run()`, оборачивается в `PipelineResult.failure()`
- Имя упавшего шага (`stageName`) и сообщение (`message`) передаются в ответ

### Стратегия в HTTP

Все непойманные исключения перехватываются `GlobalExceptionHandler`:

```
BioSourceNotFoundException  →  404
PipelineException           →  500 (с логированием)
Exception (любое)           →  500 "Interner Fehler" (с логированием)
```

### Предупреждения (Warnings)

Некритичные ситуации не прерывают конвейер — они добавляются в `context.warnings`:
- Пример: BioSource без `ancestorEid` пропускается на шаге 5, в warnings добавляется запись

---

## 12. Зависимости между компонентами

```
BioSourceController
    └── SyncBioSourcesUseCase
            └── Pipeline
                    └── PipelineStage (интерфейс)
                            ├── FetchRemoteBioSourcesStage
                            │       └── BioSourceRemoteClientPort (интерфейс)
                            │               └── SignalsWebClientAdapter
                            │                       ├── WebClient
                            │                       └── SignalsResponseMapper
                            │
                            ├── LoadBioSourcesStage
                            │       └── BioSourceRepositoryPort (интерфейс)
                            │               └── BioSourceRepositoryAdapter
                            │
                            ├── FilterNewBioSourceStage
                            │
                            ├── FetchBioSourceDetailsStage
                            │       └── BioSourceRemoteClientPort
                            │
                            ├── EnrichAncestorRankStage
                            │
                            └── PersistBioSourceStage
                                    └── BioSourceRepositoryPort
```

---

## 13. Запуск проекта

### Требования

- Java 17+
- Maven 3.8+
- PostgreSQL (для полноценной работы)
- Доступ к Signals API

### Сборка

```bash
mvn clean package
```

### Запуск

```bash
java -jar target/backendJonasv2-0.0.1-SNAPSHOT.jar \
  --signals.api.base-url=https://signals.example.com/api \
  --spring.datasource.url=jdbc:postgresql://localhost:5432/biosource_db \
  --spring.datasource.username=postgres \
  --spring.datasource.password=secret
```

### Или через Maven (dev)

```bash
mvn spring-boot:run \
  -Dspring-boot.run.arguments="--signals.api.base-url=https://signals.example.com/api"
```

### Вызов синхронизации

```bash
curl -X POST http://localhost:8080/api/v1/biosources/sync
```

---

*Документация сгенерирована для версии `0.0.1-SNAPSHOT`*
