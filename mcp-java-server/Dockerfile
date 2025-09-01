FROM openjdk:17-oracle
VOLUME /tmp
COPY target/*.jar mcp-java-server.jar
ENTRYPOINT ["java", "-jar", "mcp-java-server.jar"]
EXPOSE 8899
