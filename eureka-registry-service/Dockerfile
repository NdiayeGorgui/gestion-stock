FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/eureka-registry-service-0.0.1-SNAPSHOT.jar eureka-registry-service.jar
ENTRYPOINT ["java", "-jar", "eureka-registry-service.jar"]
EXPOSE 8761