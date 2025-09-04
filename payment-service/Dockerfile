FROM openjdk:17-oracle
VOLUME /tmp
COPY target/*.jar payment-service.jar
ENTRYPOINT ["java", "-jar", "payment-service.jar"]
EXPOSE 8085
