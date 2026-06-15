FROM eclipse-temurin:21-jdk-alpine
LABEL maintainer="GodOfGames"
VOLUME /tmp
EXPOSE 8088
ARG JAR_FILE=target/Notificaciones-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]