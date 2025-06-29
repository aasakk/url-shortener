# Dockerfile
FROM openjdk:17
WORKDIR /app
COPY target/shortener.jar app.jar
EXPOSE 9096
ENTRYPOINT ["java", "-jar", "app.jar"]
