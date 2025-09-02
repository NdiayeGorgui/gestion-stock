FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/*.jar customer-service.jar
ENTRYPOINT ["java", "-jar", "customer-service.jar"]
EXPOSE 8082
