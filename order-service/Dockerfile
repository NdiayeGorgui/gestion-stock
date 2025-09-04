FROM openjdk:17-oracle
VOLUME /tmp
COPY target/*.jar order-service.jar
ENTRYPOINT ["java", "-jar", "order-service.jar"]
EXPOSE 8081
