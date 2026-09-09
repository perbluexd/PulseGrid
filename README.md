# PulseGrid

Plataforma de telemetría/IoT con alertas en tiempo real, construida como un conjunto de microservicios en arquitectura hexagonal. Proyecto de portfolio orientado a cubrir tecnologías de mensajería distribuida (Kafka, RabbitMQ), programación reactiva (WebFlux), GraphQL y Kubernetes.

## Microservicios

| Servicio | Estado | Responsabilidad |
|---|---|---|
| [`device-registry`](backend/device-registry/README.md) | En construcción | CRUD de dispositivos/grupos, login del dashboard, emisión de JWT |
| `ingestion-gateway` | Pendiente | Ingesta reactiva de telemetría (WebFlux) |
| `aggregation-service` | Pendiente | Consumidor Kafka, agregados en TimescaleDB |
| `alerting-service` | Pendiente | Reglas de alerta, consumidor Kafka + productor RabbitMQ |
| `notification-workers` | Pendiente | Consumidor RabbitMQ, notificaciones |
| `query-api` | Pendiente | API de consulta (GraphQL) para el dashboard |

Solo `device-registry` tiene código por ahora; el resto está diseñado (ver documentación de fases) pero no arrancado.

## Cómo correr el proyecto

```bash
docker compose up -d --build
```

Levanta `device-registry` junto a su base de datos Postgres. Ver el [README de `device-registry`](backend/device-registry/README.md) para el detalle de endpoints, autenticación y documentación interactiva de la API.
