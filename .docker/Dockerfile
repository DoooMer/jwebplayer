FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dtracks.rootDirectory=/app/data", "-Dvite.manifest-path=static/player/", "app.jar"]