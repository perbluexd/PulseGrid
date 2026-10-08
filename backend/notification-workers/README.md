# notification-workers

Microservicio de PulseGrid que consume las alertas disparadas por `alerting-service` desde RabbitMQ y envía una notificación por email por cada una. No expone API de negocio: es un consumidor puro, pensado para escalar horizontalmente con varias réplicas compitiendo por la misma cola (competing consumers).

## Stack

Java 21, Spring Boot 3.5, Spring AMQP (RabbitMQ), Spring Data JPA, PostgreSQL, Flyway, Spring Mail, Resilience4j, Actuator + Micrometer (Prometheus), Docker, Kubernetes, JUnit 5 + Mockito + Testcontainers.

## Flujo

```
alert-notifications (RabbitMQ) → @RabbitListener → ProcessAlertNotificationService
    → INSERT alert_id ... ON CONFLICT DO NOTHING   (idempotencia)
    → email vía JavaMailSender (retry + circuit breaker + rate limiter)
    → COMMIT → ack
```

## Garantías de entrega

- **At-least-once + idempotencia:** RabbitMQ puede reentregar un mensaje (caída antes del ack, reintentos). El `alert_id` se registra en `processed_notifications` dentro de la misma transacción del envío; un duplicado no genera un segundo email.
- **Reintentos del listener:** 3 intentos con backoff exponencial (1s, 2s) para fallos generales; agotados → `alert-notifications.dlq` (mensajes inválidos, JSON roto).
- **Email no disponible:** Resilience4j reintenta el envío (3 × 500ms). Si el SMTP sigue caído, el circuit breaker `email` se abre, el listener se pausa y los mensajes esperan en la cola principal (no van a la DLQ). Al pasar a semiabierto el listener se reanuda y los mensajes pendientes se envían.
- **Rate limiter:** máximo 10 emails/segundo por réplica.
- **Purga:** los registros de `processed_notifications` con más de 7 días se eliminan a diario (03:00).

## Cómo correrlo

Desde la raíz del repo:

```bash
docker compose up -d --build rabbitmq notification-workers-db mailpit notification-workers
```

| Recurso | URL |
|---|---|
| Mailpit (emails enviados) | http://localhost:8025 |
| RabbitMQ management | http://localhost:15672 (`pulsegrid` / `pulsegrid`) |
| Health / probes | http://localhost:8094/actuator/health, `/liveness`, `/readiness` |
| Métricas Prometheus | http://localhost:8094/actuator/prometheus |

## Configuración

| Variable | Default | Uso |
|---|---|---|
| `RABBITMQ_HOST` / `RABBITMQ_PORT` | `localhost` / `5672` | broker |
| `MAIL_HOST` / `MAIL_PORT` | `localhost` / `1025` | servidor SMTP (Mailpit en local) |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | vacío | credenciales de un SMTP real |
| `NOTIFICATIONS_EMAIL_FROM` / `NOTIFICATIONS_EMAIL_TO` | `alerts@pulsegrid.local` / `ops-team@pulsegrid.local` | remitente y destinatario |

## Health checks

- **Liveness** (`/actuator/health/liveness`): solo el estado interno de la JVM/aplicación.
- **Readiness** (`/actuator/health/readiness`): incluye Postgres y RabbitMQ.
- El SMTP y el circuit breaker aparecen en `/actuator/health`, pero **no** en las probes: un SMTP caído no debe reiniciar pods ni sacarlos de servicio; ese caso lo maneja el circuit breaker.

## Kubernetes

Manifiestos en `k8s/`: `notification-workers` (Deployment con requests/limits, startup/liveness/readiness probes, graceful shutdown, Service y HPA por CPU 2–6 réplicas), `postgres` (StatefulSet) y `mailpit`. RabbitMQ se reutiliza de `backend/alerting-service/k8s/rabbitmq`. El HPA requiere `metrics-server` (`minikube addons enable metrics-server`).

Los `secret.yaml` están en `.gitignore` (no se versionan credenciales): hay que crearlos localmente con las claves `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD`, `MAIL_USERNAME`, `MAIL_PASSWORD` (`notification-workers-secret`) y `POSTGRES_USER`, `POSTGRES_PASSWORD` (`notification-workers-db-secret`).

```bash
kubectl apply -f ../alerting-service/k8s/rabbitmq/ -f k8s/postgres/ -f k8s/mailpit/ -f k8s/notification-workers/
```

## Tests

```bash
./mvnw verify
```

Unitarios (service, adapter de email, recoverer) e integración con Testcontainers (Postgres real para la idempotencia y la purga; flujo completo RabbitMQ → Postgres → Mailpit, incluyendo duplicados y mensajes inválidos a la DLQ).
