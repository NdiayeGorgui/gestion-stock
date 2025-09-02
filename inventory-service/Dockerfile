FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/*.jar inventory-service.jar
ENTRYPOINT ["java", "-jar", "inventory-service.jar"]
EXPOSE 8084
