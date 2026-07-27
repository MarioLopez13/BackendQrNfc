# Instrucciones para capturas de CP-10

No se generaron capturas automáticamente porque la herramienta de terminal disponible devuelve texto, no una ventana gráfica capturable. No se fabricaron imágenes.

## CP10-01-dos-peticiones.png

1. En VS Code, abre en dos grupos:
   - cp10-peticion-1.txt y cp10-respuesta-1.json
   - cp10-peticion-2.txt y cp10-respuesta-2.json
2. Ajusta el zoom para mostrar:
   - POST http://localhost:8081/api/payments/qr
   - Idempotency-Key: CP10-2026-001
   - los códigos HTTP;
   - el mismo identificador de pago en ambas respuestas.
3. Captura ambos grupos en una sola imagen y guárdala como CP10-01-dos-peticiones.png.

## CP10-02-un-solo-registro-bd.png

Desde Backend-Microservices, ejecuta:

`powershell
docker compose exec -T postgres psql -U postgres -d smartpayut_payment -c "SELECT COUNT(*) FROM payments WHERE idempotency_key = 'CP10-2026-001';"
`

La terminal debe mostrar count = 1. Incluye comando, tabla, filtro y resultado. Guarda la captura como CP10-02-un-solo-registro-bd.png.

## CP10-03-logs-idempotencia.png

Es opcional. La implementación no emitió mensajes específicos para la repetición. Abre cp10-logs.txt para documentar esta ausencia sin inventar mensajes.
