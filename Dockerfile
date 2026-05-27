FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY cletaeats-backend/gradlew .
COPY cletaeats-backend/gradle gradle
COPY cletaeats-backend/build.gradle.kts .
COPY cletaeats-backend/settings.gradle.kts .

RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon || true

COPY cletaeats-backend/src src

RUN ./gradlew clean bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]