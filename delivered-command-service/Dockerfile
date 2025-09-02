FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/*.jar delivered-command-service.jar
ENTRYPOINT ["java", "-jar", "delivered-command-service.jar"]
EXPOSE 8088
