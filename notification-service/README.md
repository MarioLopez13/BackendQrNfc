# SmartPayUT Notification Service

Microservicio de notificaciones internas que consume eventos RabbitMQ, construye mensajes seguros y los
persiste en su base PostgreSQL exclusiva. No modifica información financiera ni consulta bases o APIs de
otros microservicios.

## Arquitectura y eventos

Consume una copia independiente de los eventos mediante la cola durable `smartpayut.notification.events`.

- Exchange topic `smartpayut.wallet.events`: `wallet.created`, `wallet.credited`, `wallet.debited`,
  `wallet.refunded`.
- Exchange directo `smartpayut.payment.events`: `payment.completed`, `payment.failed`,
  `payment.refunded`, `topup.completed`, `topup.failed`.

Se permiten tres intentos con backoff de 500 ms, multiplicador 2 y máximo 2 segundos. Los mensajes agotados
se rechazan hacia la DLQ durable `smartpayut.notification.events.dlq` mediante
`smartpayut.notification.dlx`.

RabbitMQ ofrece entrega al menos una vez. `ProcessedEvent` y la restricción única sobre `event_id` garantizan
idempotencia; Notification y ProcessedEvent se escriben dentro de la misma transacción PostgreSQL.

## Endpoints

- `GET /api/notifications/me`
- `GET /api/notifications/me/{id}`
- `GET /api/notifications/me/unread-count`
- `PATCH /api/notifications/me/{id}/read`
- `PATCH /api/notifications/me/read-all`
- `GET /api/admin/notifications`

La consulta personal admite filtros `status`, `source` y `type`. La administrativa añade `userId`,
`dateFrom` y `dateTo`. Todas permiten paginación entre 1 y 100 elementos.

## Seguridad

Spring Security valida JWT. El identificador se resuelve desde `user_id`, luego `userId` y finalmente `sub`.
La API administrativa requiere `ADMIN` u `OPERATOR`. Nunca se confía en cabeceras de identidad enviadas por
los clientes.

## Variables

- `NOTIFICATION_DB_URL`, `NOTIFICATION_DB_USER`, `NOTIFICATION_DB_PASSWORD`
- `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD`
- `JWT_JWK_SET_URI`, `NOTIFICATION_SERVICE_PORT`
- Opcionales: `WALLET_EVENTS_EXCHANGE`, `PAYMENT_EVENTS_EXCHANGE`, `NOTIFICATION_EVENTS_QUEUE`,
  `NOTIFICATION_DLX`, `NOTIFICATION_DLQ`, `RABBITMQ_LISTENER_AUTO_STARTUP`

Base predeterminada: `smartpayut_notification`.

## Ejecución y pruebas

```shell
mvn spring-boot:run
mvn -B clean verify
```

## Limitaciones del MVP

Las notificaciones son únicamente in-app y se registran en logs. No existen correo, SMS, WhatsApp, push,
WebSocket ni proveedores externos. Tampoco se implementan preferencias avanzadas, Outbox o limpieza
automática.
