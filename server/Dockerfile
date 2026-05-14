FROM openjdk:21-jdk-slim
WORKDIR /app

# Copy the locally-built fat jar (shadowJar produces server-fat.jar)
COPY build/libs/server-fat.jar /app/server-fat.jar

ENV JAVA_TOOL_OPTIONS="-Djava.security.egd=file:/dev/./urandom"
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/server-fat.jar"]

