# CP-11 — RNF-04: Construcción independiente

## Entorno

- Java: OpenJDK Temurin 21.0.10 LTS
- Maven: Apache Maven 3.9.12
- Fecha de ejecución: 2026-07-26
- Comando ejecutado por proyecto: `mvn -B clean verify`

## Resultados

| Microservicio | Pruebas | Fallidas | Errores | Omitidas | Resultado | Tiempo Maven |
|---|---:|---:|---:|---:|---|---:|
| api-gateway | 2 | 0 | 0 | 0 | BUILD SUCCESS | 7.918 s |
| identity-service | 33 | 0 | 0 | 2 | BUILD SUCCESS | 14.009 s |
| wallet-service | 24 | 0 | 0 | 2 | BUILD SUCCESS | 13.808 s |
| payment-service | 20 | 0 | 0 | 0 | BUILD SUCCESS | 13.398 s |
| transaction-service | 30 | 0 | 0 | 0 | BUILD SUCCESS | 12.705 s |
| notification-service | 37 | 0 | 0 | 0 | BUILD SUCCESS | 11.919 s |

## Resumen

- Microservicios Spring Boot independientes identificados: **6**.
- Construcciones correctas: **6**.
- Construcciones fallidas: **0**.
- Pruebas ejecutadas: **146**.
- Pruebas fallidas: **0**.
- Errores: **0**.
- Pruebas omitidas: **4**.

Cada construcción se ejecutó desde la raíz del proyecto que contiene su propio `pom.xml`, clase `@SpringBootApplication`, recursos y pruebas. No se utilizó una compilación agregada desde la carpeta padre.

## Elementos excluidos

- `BackendQrNfc`: backend legado conservado para rollback; no pertenece al backend objetivo de microservicios.
- `Frontend-admin`: cliente React/TypeScript.
- `Frontend-Mobile`: cliente Flutter.
- `infrastructure/postgres`: scripts de inicialización, no una aplicación Spring Boot.
- Keycloak, PostgreSQL y RabbitMQ: infraestructura de Docker Compose, no proyectos Java compilables.
- Raíz `Backend-Microservices`: no contiene un `pom.xml` agregador.

## Interpretación

La ejecución separada de `clean verify` demuestra independencia de construcción: cada servicio resuelve sus dependencias, compila su código y ejecuta sus pruebas sin requerir un módulo Maven compartido ni una compilación padre.

Los dos tests omitidos en Identity y los dos omitidos en Wallet son pruebas de integración condicionadas por entorno; Maven los reportó explícitamente como `Skipped`, no como fallos.

