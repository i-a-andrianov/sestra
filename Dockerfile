FROM openjdk:17.0.1-slim AS build

WORKDIR /build

COPY gradle gradle
COPY gradlew gradlew
RUN ./gradlew help  # fetch Gradle

COPY build.gradle.kts build.gradle.kts
COPY settings.gradle.kts settings.gradle.kts
RUN ./gradlew classes  # fetch dependencies

COPY src/main src/main
RUN ./gradlew assemble  # compile, uber JAR

FROM openjdk:17.0.1-slim

WORKDIR /app

COPY --from=build /build/build/libs/sestra.jar sestra.jar

CMD ["java", "-jar", "sestra.jar"]
