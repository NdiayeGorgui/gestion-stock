FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/*.jar delivered-query-service.jar
ENTRYPOINT ["java", "-jar", "delivered-query-service.jar"]
EXPOSE 8089
