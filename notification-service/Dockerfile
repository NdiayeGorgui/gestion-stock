FROM openjdk:17-oracle
VOLUME /tmp
COPY target/*.jar notification-service.jar
ENTRYPOINT ["java", "-jar", "notification-service.jar"]
EXPOSE 8087
