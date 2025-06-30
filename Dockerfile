FROM openjdk:17
WORKDIR /app

# Optional debug dump (for verifying .jar)
RUN mkdir /dump
COPY target/shortener.jar /dump/

COPY target/shortener.jar app.jar

EXPOSE 9096

ENTRYPOINT ["java", "-jar", "app.jar"]

