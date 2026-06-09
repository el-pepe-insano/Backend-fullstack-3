# Capa 1: Usamos la imagen oficial de Java 21
FROM eclipse-temurin:21-jdk-alpine

# Capa 2: Información del creador
LABEL maintainer="GodOfGames - SmartLogix"

# Capa 3: Directorio de trabajo
WORKDIR /app

# Capa 4: Copiamos el archivo que acabamos de compilar con Maven
COPY target/Usuarios-Z-0.0.1-SNAPSHOT.jar app.jar

# Capa 5: Exponemos el puerto
EXPOSE 8081

# Capa 6: Comando de encendido
ENTRYPOINT ["java", "-jar", "app.jar"]