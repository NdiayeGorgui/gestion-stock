FROM openjdk:17-oracle
VOLUME /tmp
COPY target/*.jar config-service.jar
ENTRYPOINT ["java", "-jar", "config-service.jar"]
EXPOSE 9191
