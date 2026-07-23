# SmartPayUT Transaction Service

Microservicio de lectura que construye el historial consolidado desde eventos RabbitMQ. No ejecuta pagos,
no modifica saldos y no realiza llamadas REST a otros microservicios.

## Eventos consumidos

Desde `smartpayut.wallet.events`:

- `wallet.created`
- `wallet.credited`
- `wallet.debited`
- `wallet.refunded`

Desde `smartpayut.payment.events`:

- `payment.completed`
- `payment.failed`
- `payment.refunded`
- `topup.completed`
- `topup.failed`

La cola durable predeterminada es `smartpayut.transaction.events`. Cada mensaje admite como máximo tres
intentos y después se envía a `smartpayut.transaction.events.dlq`. `ProcessedEvent` evita volver a aplicar
un evento confirmado.

## API de lectura

- `GET /api/transactions/me?page=0&pageSize=20`
- `GET /api/transactions/me/{id}`
- `GET /api/transactions?page=0&pageSize=20`
- `GET /api/admin/transactions?page=0&pageSize=20`

La consulta administrativa requiere rol `ADMIN` u `OPERATOR`. El JWT debe incluir el UUID de negocio en
`user_id` o `userId`; si no existe, se utiliza `sub` como compatibilidad.

## Verificación

```shell
mvn -B clean verify
```
