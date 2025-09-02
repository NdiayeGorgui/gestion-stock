FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/*.jar billing-service.jar
ENTRYPOINT ["java", "-jar", "billing-service.jar"]
EXPOSE 8083
