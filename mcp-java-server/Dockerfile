FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/*.jar mcp-java-server.jar
ENTRYPOINT ["java", "-jar", "mcp-java-server.jar"]
EXPOSE 8899
