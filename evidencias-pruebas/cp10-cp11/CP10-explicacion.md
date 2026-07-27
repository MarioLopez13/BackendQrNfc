# CP-10 — RNF-03: Idempotencia

## 1. Nombre de la prueba

CP-10 — Verificación de idempotencia de un pago QR.

## 2. Requerimiento evaluado

RNF-03: evitar el procesamiento duplicado de una operación cuando se repite la solicitud con la misma clave.

## 3. Objetivo

Comprobar que dos solicitudes idénticas, enviadas por el mismo usuario y con la misma clave `Idempotency-Key`, producen un único pago y un único débito.

## 4. Microservicio probado

`payment-service`, con la operación de saldo delegada a `wallet-service`.

## 5. Endpoint y método

`POST http://localhost:8081/api/payments/qr`

## 6. Clave utilizada

`CP10-2026-001`

## 7. Precondiciones

- Gateway, Identity, Wallet, Payment, PostgreSQL y RabbitMQ activos.
- Usuario de prueba real registrado durante la ejecución.
- Billetera creada mediante el evento `identity.user.created`.
- Saldo preparado mediante una recarga PlaceToPay simulada del perfil local.
- La clave elegida no existía previamente en `smartpayut_payment.payments`.

## 8. Procedimiento ejecutado

1. Se registró y autenticó un usuario de prueba.
2. Se esperó la creación automática de su billetera.
3. Se acreditó saldo mediante la integración local de recarga.
4. Se envió dos veces el mismo JSON al endpoint.
5. Ambas solicitudes conservaron el mismo usuario y `Idempotency-Key`.
6. Se consultaron las tablas `payments` y `wallet_movement`.
7. Se revisaron los logs reales de `payment-service`.

## 9. Resultados HTTP

- Primera solicitud: HTTP 200; Payment ID `1ab699e9-0e6e-4d6d-8de5-cfea9472b727`.
- Segunda solicitud: HTTP 200; Payment ID `1ab699e9-0e6e-4d6d-8de5-cfea9472b727`.
- Las respuestas corresponden al mismo pago.

## 10. Consultas SQL

```sql
SELECT COUNT(*)
FROM payments
WHERE idempotency_key = 'CP10-2026-001';
```

```sql
SELECT COUNT(*)
FROM wallet_movement
WHERE reference_id = '1ab699e9-0e6e-4d6d-8de5-cfea9472b727';
```

## 11. Registros encontrados

- Pagos: **1**
- Movimientos de débito: **1**

## 12. Mecanismo implementado

`PaymentExecutionService` consulta `PaymentRepository.findByIdempotencyKey(...)` antes de crear el pago. La entidad `Payment` persiste la clave en `payments.idempotency_key`, columna `NOT NULL` con restricción `UNIQUE`. Al repetir la petición, el servicio devuelve el pago existente.

La llamada REST posterior a Wallet usa una clave interna derivada del pago (`payment:<paymentId>:debit`). `WalletMovementService` consulta esa clave antes de crear el movimiento y `wallet_movement.idempotency_key` también posee una restricción `UNIQUE`.

Adicionalmente, Wallet, Transaction y Notification disponen de `ProcessedEvent` para evitar reprocesar mensajes RabbitMQ ya consumidos. Este mecanismo es distinto de la idempotencia HTTP evaluada en CP-10.

## 13. Logs

No se encontraron mensajes específicos sobre solicitudes duplicadas en los logs reales de `payment-service`. Por ello, la conclusión no se basa en una afirmación inventada sobre logs, sino en la igualdad de las respuestas y en los conteos reales de PostgreSQL.

## 14. Conclusión

**Cumple.** La misma solicitud con la misma clave devolvió el mismo Payment ID y produjo un único pago y un único débito.

