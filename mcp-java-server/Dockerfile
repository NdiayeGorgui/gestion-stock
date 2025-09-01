FROM openjdk:17-oracle
VOLUME /tmp
COPY target/*.jar mpc-java-server.jar
ENTRYPOINT ["java", "-jar", "mpc-java-server.jar"]
EXPOSE 8899
