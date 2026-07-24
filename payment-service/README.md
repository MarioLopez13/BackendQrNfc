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
- URL local simulada: `PLACETOPAY_SIMULATION_PROCESS_URL_BASE`

Para desarrollo puede configurarse `WALLET_SERVICE_TOKEN` en lugar de credenciales client credentials.
El cliente de Keycloak debe usar service accounts y recibir el rol de realm `SERVICE` requerido por Wallet.

## Verificación

```shell
mvn -B clean verify
```

## PlaceToPay local simulado

El perfil `local` activa `payment.placetopay.simulation-enabled`. En este modo, crear una recarga genera
una sesion y una URL locales sin contactar a PlaceToPay ni requerir credenciales externas. Confirmarla
solicita el credito idempotente a Wallet y registra `topup.completed` en el Outbox.

Esta simulacion sirve exclusivamente para demostrar el MVP. No representa una transaccion financiera
real, una certificacion bancaria ni una integracion productiva. Con la simulacion desactivada se conserva
el cliente PlaceToPay Sandbox existente, que requiere `PLACETOPAY_LOGIN` y `PLACETOPAY_SECRET_KEY`.

Flujo local de prueba:

1. Crear la sesion con `POST /api/mobile-payments/top-up/placetopay`.
2. Tomar el `topUpId` de la respuesta.
3. Confirmarla con `POST /api/mobile-payments/top-up/{topUpId}/confirm`.
4. Consultar saldo, historial y notificaciones mediante sus endpoints actuales.
