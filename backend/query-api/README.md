# query-api

Microservicio de PulseGrid que expone una **API GraphQL de solo lectura** para el dashboard. No tiene base de datos propia: es una fachada que compone datos de los otros servicios (patrón *API Composition*). Los comandos (crear, modificar) siguen yendo por REST a cada servicio dueño de su entidad (CQRS).

## Stack

Java 21, Spring Boot 3.5, Spring for GraphQL, Spring Security (JWT), RestClient, Actuator + Micrometer (Prometheus), Docker, Kubernetes, JUnit 5 + Mockito + GraphQlTester.

## De dónde sale cada dato

| Dato GraphQL | Servicio | Endpoint | Autenticación |
|---|---|---|---|
| `Device`, `DeviceGroup` | device-registry | `GET /api/v1/devices`, `/{id}`, `/api/v1/device-groups` | JWT del usuario (token relay) |
| `Alert`, `AlertRule` | alerting-service | `GET /api/v1/alerts`, `/api/v1/alert-rules` | JWT del usuario (token relay) |
| `Reading`, `TelemetryBucket` | aggregation-service | `GET /api/v1/internal/telemetry/latest`, `/history` | `X-Internal-Service` |
| `Notification` | notification-workers | `GET /api/v1/internal/notifications`, `/recent` | `X-Internal-Service` |

## Ejemplo

```graphql
query Dashboard($id: ID!, $from: DateTime!, $to: DateTime!) {
  device(id: $id) {
    name status
    latestReadings { metricType value recordedAt }
    telemetryHistory(metricType: "TEMPERATURE", from: $from, to: $to, bucketSize: FIVE_MINUTES) {
      bucketStart avg min max sampleCount
    }
  }
  alerts(filter: { deviceId: $id }, first: 10) {
    totalCount
    pageInfo { hasNextPage endCursor }
    edges { node { severity status triggeredValue rule { condition threshold } notification { notifiedAt } } }
  }
}
```

Una sola petición devuelve datos de los cinco servicios, y el cliente recibe solo los campos que pidió.

## Decisiones de diseño

- **N+1 resuelto con DataLoader:** los campos que dependen de otro servicio se cargan en lote. `Device.latestReadings` y `Alert.notification` usan `@BatchMapping` contra endpoints que aceptan muchos ids en una llamada. `Alert.device` y `AlertRule.device` comparten un DataLoader registrado en `BatchLoaderRegistry`, así que una consulta con 50 alertas hace **una** llamada a device-registry, no 50. Como device-registry y alerting no tienen endpoint de búsqueda por varios ids, ese lote se resuelve con el listado completo (una llamada por consulta GraphQL).
- **Datos parciales:** los campos que vienen de otro servicio son *nullable* en el schema. Si un servicio cae, ese campo vuelve `null` y la respuesta trae un error en `errors` con `extensions.code = DOWNSTREAM_UNAVAILABLE` y el nombre del servicio. El resto de la respuesta llega igual.
- **Errores:** GraphQL responde HTTP 200 con `data` y `errors`. Códigos propios en `extensions.code`: `DOWNSTREAM_UNAVAILABLE`, `ACCESS_DENIED`, `INVALID_ARGUMENT`.
- **Paginación por cursor** en `alerts` (`first` / `after`, `pageInfo.endCursor`). El cursor es opaco (Base64).
- **Scalar `DateTime`** propio (ISO-8601 ↔ `Instant`).
- **Protección contra consultas abusivas:** profundidad máxima 8 y complejidad máxima 300 (`app.graphql.*`).
- **Seguridad:** `/graphql` exige JWT con rol `ADMIN` o `VIEWER`. El token del usuario se reenvía a device-registry y alerting, que aplican sus propios permisos.

## Cómo correrlo

Desde la raíz del repo:

```bash
docker compose up -d --build query-api
```

| Recurso | URL |
|---|---|
| Endpoint GraphQL | `POST http://localhost:8095/graphql` (header `Authorization: Bearer <jwt>`) |
| GraphiQL (explorador) | http://localhost:8095/graphiql — cargar el JWT en la pestaña *Headers* |
| Schema | `GET http://localhost:8095/graphql/schema` |
| Health / probes | http://localhost:8095/actuator/health, `/liveness`, `/readiness` |

El JWT se obtiene con `POST /api/v1/auth/login` en device-registry.

## Configuración

| Variable | Default | Uso |
|---|---|---|
| `DEVICE_REGISTRY_BASE_URL` | `http://localhost:8090` | device-registry |
| `ALERTING_SERVICE_BASE_URL` | `http://localhost:8093` | alerting-service |
| `AGGREGATION_SERVICE_BASE_URL` | `http://localhost:8092` | aggregation-service |
| `NOTIFICATION_WORKERS_BASE_URL` | `http://localhost:8094` | notification-workers |
| `JWT_SECRET` | placeholder de desarrollo | validar el JWT (el mismo secreto que device-registry) |
| `INTERNAL_SERVICE_TOKEN` | placeholder de desarrollo | header `X-Internal-Service` |

## Kubernetes

Manifiestos en `k8s/query-api/`: Deployment (requests/limits, startup/liveness/readiness probes, graceful shutdown), Service, ConfigMap y HPA por CPU (2–6 réplicas). El `secret.yaml` está en `.gitignore`: hay que crearlo localmente (`query-api-secret`) con las claves `JWT_SECRET` e `INTERNAL_SERVICE_TOKEN`. aggregation-service y notification-workers también necesitan `INTERNAL_SERVICE_TOKEN` en su secret.

```bash
kubectl apply -f k8s/query-api/
```

## Tests

```bash
./mvnw verify
```

`@GraphQlTest` + `GraphQlTester` (selección de campos, batching N+1, DataLoader compartido, datos parciales, paginación, scalar, límite de profundidad), seguridad del endpoint con JWT real, adapter REST con `MockRestServiceServer` (token relay y traducción de errores) y servicios con Mockito.
