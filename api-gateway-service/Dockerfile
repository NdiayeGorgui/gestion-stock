FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/*.jar api-gateway-service.jar
ENTRYPOINT ["java", "-jar", "api-gateway-service.jar"]
EXPOSE 8761
