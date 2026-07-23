# SmartPayUT Payment Service

Microservicio propietario del ciclo de vida de pagos QR/NFC y recargas mediante PlaceToPay Sandbox.
Wallet Service continúa siendo la única autoridad del saldo y recibe las solicitudes de débito, crédito y
reembolso mediante REST.

## Endpoints

- `POST /api/payments/qr`
- `POST /api/payments/nfc`
- `GET /api/payments/{paymentId}`
- `GET /api/payments`
- `POST /api/payments/{paymentId}/refunds`
- `POST /api/payments/placetopay/top-ups`
- `POST /api/payments/placetopay/top-ups/{topUpId}/confirm`
- `POST /api/payments/placetopay/callback`
- Rutas compatibles bajo `/api/mobile-payments` para React y Flutter.

Las operaciones aceptan `Idempotency-Key`. Las rutas legacy generan una clave cuando los clientes actuales
no la envían, preservando su contrato.

## Eventos

Se publican en el exchange directo `smartpayut.payment.events`:

- `payment.completed`
- `payment.failed`
- `payment.refunded`
- `topup.completed`
- `topup.failed`

## Configuración requerida

- PostgreSQL: `PAYMENT_DB_URL`, `PAYMENT_DB_USER`, `PAYMENT_DB_PASSWORD`
- RabbitMQ: `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD`
- JWT: `JWT_JWK_SET_URI`
- Wallet: `WALLET_SERVICE_URL`
- Keycloak M2M: `KEYCLOAK_TOKEN_URI`, `PAYMENT_KEYCLOAK_CLIENT_ID`,
  `PAYMENT_KEYCLOAK_CLIENT_SECRET`
- PlaceToPay Sandbox: `PLACETOPAY_BASE_URL`, `PLACETOPAY_LOGIN`, `PLACETOPAY_SECRET_KEY`

Para desarrollo puede configurarse `WALLET_SERVICE_TOKEN` en lugar de credenciales client credentials.
El cliente de Keycloak debe usar service accounts y recibir el rol de realm `SERVICE` requerido por Wallet.

## Verificación

```shell
mvn -B clean verify
```
