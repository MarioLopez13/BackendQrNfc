# Instrucciones para capturas de CP-11

No se fabricaron imágenes. Los archivos de construcción contienen la salida real completa de Maven.

## CP11-01-api-gateway.png

Desde PowerShell:

`powershell
$log = Get-Content -LiteralPath "C:\Users\Mario\Desktop\SmartPayUT-Sprint2-3\Backend-Microservices\evidencias-pruebas\cp10-cp11\cp11-api-gateway-build.txt"
$log | Select-Object -First 14
$log | Select-String "Tests run:|BUILD SUCCESS|BUILD FAILURE|Total time:"
`

La captura debe mostrar el nombre, ruta, comando, versiones, pruebas, resultado y tiempo total.
## CP11-02-identity-service.png

Desde PowerShell:

`powershell
$log = Get-Content -LiteralPath "C:\Users\Mario\Desktop\SmartPayUT-Sprint2-3\Backend-Microservices\evidencias-pruebas\cp10-cp11\cp11-identity-service-build.txt"
$log | Select-Object -First 14
$log | Select-String "Tests run:|BUILD SUCCESS|BUILD FAILURE|Total time:"
`

La captura debe mostrar el nombre, ruta, comando, versiones, pruebas, resultado y tiempo total.
## CP11-03-wallet-service.png

Desde PowerShell:

`powershell
$log = Get-Content -LiteralPath "C:\Users\Mario\Desktop\SmartPayUT-Sprint2-3\Backend-Microservices\evidencias-pruebas\cp10-cp11\cp11-wallet-service-build.txt"
$log | Select-Object -First 14
$log | Select-String "Tests run:|BUILD SUCCESS|BUILD FAILURE|Total time:"
`

La captura debe mostrar el nombre, ruta, comando, versiones, pruebas, resultado y tiempo total.
## CP11-04-payment-service.png

Desde PowerShell:

`powershell
$log = Get-Content -LiteralPath "C:\Users\Mario\Desktop\SmartPayUT-Sprint2-3\Backend-Microservices\evidencias-pruebas\cp10-cp11\cp11-payment-service-build.txt"
$log | Select-Object -First 14
$log | Select-String "Tests run:|BUILD SUCCESS|BUILD FAILURE|Total time:"
`

La captura debe mostrar el nombre, ruta, comando, versiones, pruebas, resultado y tiempo total.
## CP11-05-transaction-service.png

Desde PowerShell:

`powershell
$log = Get-Content -LiteralPath "C:\Users\Mario\Desktop\SmartPayUT-Sprint2-3\Backend-Microservices\evidencias-pruebas\cp10-cp11\cp11-transaction-service-build.txt"
$log | Select-Object -First 14
$log | Select-String "Tests run:|BUILD SUCCESS|BUILD FAILURE|Total time:"
`

La captura debe mostrar el nombre, ruta, comando, versiones, pruebas, resultado y tiempo total.
## CP11-06-notification-service.png

Desde PowerShell:

`powershell
$log = Get-Content -LiteralPath "C:\Users\Mario\Desktop\SmartPayUT-Sprint2-3\Backend-Microservices\evidencias-pruebas\cp10-cp11\cp11-notification-service-build.txt"
$log | Select-Object -First 14
$log | Select-String "Tests run:|BUILD SUCCESS|BUILD FAILURE|Total time:"
`

La captura debe mostrar el nombre, ruta, comando, versiones, pruebas, resultado y tiempo total.

## CP11-collage-builds.png

Ejecuta los bloques anteriores en seis terminales de VS Code, organízalas en una cuadrícula de 2 × 3 y captura el conjunto. Verifica que en cada panel puedan leerse el nombre del servicio, BUILD SUCCESS o BUILD FAILURE y Total time.
