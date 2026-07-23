# SmartPayUT Identity Service

Servicio propietario de las cuentas y perfiles SmartPayUT. Keycloak administra credenciales; PostgreSQL conserva `UserAccount` y `UserProfile`.

## Contratos

- Públicos: `POST /api/auth/authenticate`, `POST /api/auth/register`.
- Autenticados: `POST /api/auth/delete-account`, `GET /api/users/me`.
- `ADMIN` u `OPERATOR`: búsqueda, consulta por ID y actualización.

Login conserva los tokens en raíz. Las demás respuestas usan `{success,message,data}`, salvo la búsqueda paginada, que mantiene `items` en raíz por compatibilidad.

## Evento

Publica `identity.user.created` versión 1 en `smartpayut.identity.events`. Identity no declara colas privadas. Si RabbitMQ falla durante el registro, el MVP revierte la persistencia e intenta eliminar el usuario creado en Keycloak.

## Verificación

Ejecutar `mvn -B clean verify`. Las pruebas Testcontainers se omiten cuando Docker no está operativo y se ejecutan con el mismo comando cuando Docker Desktop está iniciado.
