# SmartPayUT Wallet Service

Autoridad exclusiva del saldo SmartPayUT. Mantiene una billetera USD por usuario, registra cada cambio como `WalletMovement` y consume idempotentemente `identity.user.created` sin consultar Identity.

Los endpoints del usuario resuelven `keycloakId` desde el claim JWT `sub`; `userId` permanece como identificador de negocio. Las operaciones internas requieren el rol de servicio `SERVICE` y una `idempotencyKey`.

Wallet no crea sesiones PlaceToPay, no confirma recargas con el proveedor y no implementa QR/NFC. Una recarga confirmada se acredita mediante `POST /internal/wallets/credit` con tipo `TOP_UP`.

RabbitMQ publica hechos ya confirmados en `smartpayut.wallet.events`. Si la publicación falla, la excepción provoca rollback de la transacción local; no se implementa Outbox en este MVP.

Verificación: `mvn -B clean verify`. Las pruebas Testcontainers se omiten automáticamente cuando Docker no está operativo.
