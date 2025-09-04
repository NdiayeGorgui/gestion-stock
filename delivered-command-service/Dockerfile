FROM openjdk:17-oracle
VOLUME /tmp
COPY target/*.jar delivered-command-service.jar
ENTRYPOINT ["java", "-jar", "delivered-command-service.jar"]
EXPOSE 8088
