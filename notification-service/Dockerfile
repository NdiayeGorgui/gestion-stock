FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/*.jar notification-service.jar
ENTRYPOINT ["java", "-jar", "notification-service.jar"]
EXPOSE 8087
