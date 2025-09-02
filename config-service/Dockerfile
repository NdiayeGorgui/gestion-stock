FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/*.jar config-service.jar
ENTRYPOINT ["java", "-jar", "config-service.jar"]
EXPOSE 9191
