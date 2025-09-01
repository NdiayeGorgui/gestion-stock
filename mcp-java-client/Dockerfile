FROM eclipse-temurin:17-alpine
VOLUME /tmp
COPY target/*.jar mcp-java-client.jar
ENTRYPOINT ["java", "-jar", "mcp-java-client.jar"]
EXPOSE 8866
