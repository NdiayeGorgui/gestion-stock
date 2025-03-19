FROM openjdk:17-oracle
VOLUME /tmp
COPY target/*.jar delivered-query-service.jar
ENTRYPOINT ["java", "-jar", "delivered-query-service.jar"]
EXPOSE 8089
