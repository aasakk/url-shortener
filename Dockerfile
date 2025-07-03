FROM openjdk:17
WORKDIR /app
RUN mkdir /dump
COPY target/shortener.jar app.jar
COPY src/main/resources/application.properties application.properties

EXPOSE 9096

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=file:/app/application.properties"]
