# device-registry

Microservicio de PulseGrid encargado del CRUD de dispositivos y grupos de dispositivos, la rotación de sus ApiKeys, y el login del dashboard (emisión de JWT). Arquitectura hexagonal (`domain` → `application` → `infrastructure` → `api`).

## Stack

Java 21, Spring Boot 3.5, Spring Security (JWT + ApiKey), Spring Data JPA, PostgreSQL, Flyway, Docker, JUnit 5 + Mockito + Testcontainers, springdoc-openapi (Swagger UI).

## Cómo correrlo

Desde la raíz del repo (`PulseGrid/`):

```bash
docker compose up -d --build device-registry
```

Levanta el servicio (puerto `8090`) y su base de datos Postgres (puerto `5440`). Flyway corre las migraciones automáticamente al arrancar.

## Documentación de la API (Swagger / OpenAPI)

Con el servicio corriendo:

- **Swagger UI (interactiva):** http://localhost:8090/swagger-ui/index.html
- **Documento OpenAPI (JSON):** http://localhost:8090/v3/api-docs

La mayoría de los endpoints requieren un JWT (`Authorization: Bearer <token>`), obtenido vía `POST /api/v1/auth/login`. Los endpoints bajo `/api/v1/internal/**` son tráfico servicio-a-servicio, autenticado con el header `X-Internal-Service`, no con JWT.

## Tests

```bash
./mvnw test
```

Unitarios (`application`, Mockito), integración (`infrastructure/persistence`, Testcontainers + Postgres real) y de capa `api` (`@WebMvcTest` + `MockMvc`).
