# PulseGrid

Plataforma de telemetría/IoT con alertas en tiempo real, construida como un conjunto de microservicios en arquitectura hexagonal. Proyecto de portfolio orientado a cubrir tecnologías de mensajería distribuida (Kafka, RabbitMQ), programación reactiva (WebFlux), GraphQL y Kubernetes.

## Microservicios

| Servicio | Estado | Responsabilidad |
|---|---|---|
| [`device-registry`](backend/device-registry/README.md) | Implementado | CRUD de dispositivos/grupos, login del dashboard, emisión de JWT |
| `ingestion-gateway` | Implementado | Ingesta reactiva de telemetría (WebFlux) |
| `aggregation-service` | Implementado | Consumidor Kafka, agregados en TimescaleDB, lectura interna para query-api |
| `alerting-service` | Implementado | Reglas de alerta, consumidor Kafka + productor RabbitMQ |
| [`notification-workers`](backend/notification-workers/README.md) | Implementado | Consumidor RabbitMQ idempotente, envío de emails con Resilience4j |
| [`query-api`](backend/query-api/README.md) | Implementado | Fachada GraphQL de solo lectura para el dashboard (compone los otros servicios) |

Los seis microservicios están implementados, cada uno con su Dockerfile, manifiestos de Kubernetes y pipeline de CI/CD.

## Cómo correr el proyecto

```bash
docker compose up -d --build
```

Levanta los seis microservicios con su infraestructura (Postgres, TimescaleDB, Kafka, RabbitMQ, Redis, Mailpit). El dashboard consulta todo por GraphQL en `http://localhost:8095/graphql` (GraphiQL en `/graphiql`). Ver el [README de `device-registry`](backend/device-registry/README.md) para el detalle de endpoints, autenticación y documentación interactiva de la API.
