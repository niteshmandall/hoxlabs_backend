FROM amazoncorretto:17-alpine-jdk

WORKDIR /app

COPY target/calorieai-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
