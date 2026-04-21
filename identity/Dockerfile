# Etapa de construcción: usa Maven con OpenJDK 21 para compilar la aplicación
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

# Establece el directorio de trabajo
WORKDIR /build

# Copia los archivos del proyecto, incluyendo el pom.xml
COPY . .

# Compila las dependencias y construye el paquete sin ejecutar pruebas
RUN mvn dependency:go-offline && \
    mvn clean package -DskipTests

# Etapa final: usa Distroless para una imagen ligera y segura en producción
FROM gcr.io/distroless/java21-debian12

# Configura la zona horaria
ENV TZ="America/Guayaquil"

# Copia el archivo JAR generado en la etapa de construcción
COPY --from=builder /build/target/identity-1.0.0.jar /app/identity-1.0.0.jar

# Expone el puerto utilizado por la aplicación
EXPOSE 9909

# Comando de inicio para ejecutar la aplicación
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-XX:MaxMetaspaceSize=192m", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=200", "-XX:+UseStringDeduplication", "-XX:+ExitOnOutOfMemoryError", "-jar", "/app/identity-1.0.0.jar"]
