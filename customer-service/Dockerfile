FROM openjdk:17-oracle
VOLUME /tmp
COPY target/*.jar customer-service.jar
ENTRYPOINT ["java", "-jar", "customer-service.jar"]
EXPOSE 8082
