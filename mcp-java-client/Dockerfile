FROM openjdk:17-oracle
VOLUME /tmp
COPY target/*.jar mcp-java-client.jar
ENTRYPOINT ["java", "-jar", "mcp-java-client.jar"]
EXPOSE 8866
