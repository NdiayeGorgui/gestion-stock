FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/*.jar order-service.jar
ENTRYPOINT ["java", "-jar", "order-service.jar"]
EXPOSE 8081
