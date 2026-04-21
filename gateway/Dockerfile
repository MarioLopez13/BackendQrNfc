# ============================================
# Dockerfile Optimizado para Gateway Service
# ============================================
# Optimizaciones implementadas:
# - Multi-stage build con caché de dependencias
# - Spring Boot Layered JARs para mejor cacheo
# - JVM optimizado para contenedores
# - Imagen distroless para seguridad y tamaño reducido
# ============================================

# Global build arguments for cross-platform support
ARG BUILDPLATFORM=linux/amd64
ARG TARGETPLATFORM=linux/amd64

# ============================================
# Etapa 1: Descarga de dependencias (cacheada)
# ============================================
# Spring Boot: 3.3.13 | Spring Cloud: 2023.0.6 | Java: 21
FROM --platform=$BUILDPLATFORM maven:3.9.9-eclipse-temurin-21-alpine AS dependencies

WORKDIR /build

# Copiar el pom.xml padre (si existe)
COPY pom.xml* .

# Copiar el módulo share completo
COPY share ./share

# Copiar solo pom.xml del gateway primero para aprovechar cache de Docker
COPY gateway/pom.xml ./gateway/

# Primero instalar share (si existe)
RUN if [ -d share ]; then \
    cd share && mvn clean install -DskipTests -B; \
    fi

# Descargar dependencias del gateway (esta capa se cachea si pom.xml no cambia)
RUN cd gateway && mvn dependency:go-offline -B || mvn dependency:resolve -B

# ============================================
# Etapa 2: Construcción de la aplicación
# ============================================
FROM --platform=$BUILDPLATFORM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copiar dependencias ya resueltas desde stage anterior (incluye share instalado)
COPY --from=dependencies /root/.m2 /root/.m2
COPY --from=dependencies /build/pom.xml* .
COPY --from=dependencies /build/share ./share
COPY --from=dependencies /build/gateway/pom.xml ./gateway/

# Copiar código fuente del gateway
COPY gateway/src ./gateway/src

# Build de la aplicación (rápido porque dependencias ya están cacheadas)
RUN cd gateway && mvn clean package -DskipTests -B

# Verificar que el JAR fue creado
RUN ls -lh gateway/target/gateway-*.jar

# ============================================
# Etapa 3: Extraer capas del JAR
# ============================================
FROM --platform=$BUILDPLATFORM eclipse-temurin:21-jre-alpine AS extractor

WORKDIR /extract

# Copiar JAR desde build stage
COPY --from=builder /build/gateway/target/gateway-*.jar app.jar

# Extraer layers del Spring Boot JAR
RUN java -Djarmode=layertools -jar app.jar extract

# ============================================
# Etapa 4: Imagen final optimizada
# ============================================
FROM gcr.io/distroless/java21-debian12:nonroot

# Metadata de la imagen
LABEL maintainer="smartpayut"
LABEL service="gateway"
LABEL version="1.0.0"

# Configura zona horaria
ENV TZ="America/Guayaquil"

# Configura el directorio de trabajo
WORKDIR /app

# Copia las capas extraídas del JAR en orden de cambio (menos frecuente -> más frecuente)
# Esto optimiza el cacheo de Docker, ya que las dependencias cambian menos que el código
COPY --from=extractor --chown=nonroot:nonroot /extract/dependencies/ ./
COPY --from=extractor --chown=nonroot:nonroot /extract/spring-boot-loader/ ./
COPY --from=extractor --chown=nonroot:nonroot /extract/snapshot-dependencies/ ./
COPY --from=extractor --chown=nonroot:nonroot /extract/application/ ./

# Expone el puerto del gateway
EXPOSE 8080

# Usuario non-root (ya configurado en distroless:nonroot)
USER nonroot

# Comando de inicio con JVM optimizado para Java 21 y contenedores
# Optimizaciones de JVM:
# - UseContainerSupport: Detecta límites de memoria del contenedor
# - MaxRAMPercentage: Usa hasta 75% de la RAM disponible
# - UseZGC + ZGenerational: Garbage collector de baja latencia (nuevo en Java 21)
# - TieredStopAtLevel: Optimiza tiempo de startup
# - UseStringDeduplication: Reduce uso de memoria para strings
# - ExitOnOutOfMemoryError: Mata el proceso si hay OOM (Kubernetes reiniciará)
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:InitialRAMPercentage=50.0", \
    "-XX:+UseZGC", \
    "-XX:+ZGenerational", \
    "-XX:+TieredCompilation", \
    "-XX:TieredStopAtLevel=1", \
    "-XX:+UseStringDeduplication", \
    "-XX:+ExitOnOutOfMemoryError", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.backgroundpreinitializer.ignore=true", \
    "org.springframework.boot.loader.launch.JarLauncher"]
