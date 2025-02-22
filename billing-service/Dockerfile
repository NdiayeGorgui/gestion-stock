FROM openjdk:17-oracle
VOLUME /tmp
COPY target/*.jar billing-service.jar
ENTRYPOINT ["java", "-jar", "billing-service.jar"]
EXPOSE 8083
