FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/*.jar shipping-service.jar
ENTRYPOINT ["java", "-jar", "shipping-service.jar"]
EXPOSE 8086
