FROM openjdk:17-oracle
VOLUME /tmp
COPY target/*.jar mpc-java-client.jar
ENTRYPOINT ["java", "-jar", "mpc-java-client.jar"]
EXPOSE 8866
