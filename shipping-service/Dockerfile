FROM openjdk:17-oracle
VOLUME /tmp
COPY target/*.jar shipping-service.jar
ENTRYPOINT ["java", "-jar", "shipping-service.jar"]
EXPOSE 8086
