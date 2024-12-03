FROM openjdk:17-jdk-slim AS build

WORKDIR /build
COPY gradlew gradlew
COPY gradle gradle
COPY src src
COPY build.gradle build.gradle
COPY settings.gradle settings.gradle

RUN chmod +x gradlew
RUN ./gradlew build --no-daemon

FROM openjdk:17-jdk-slim

WORKDIR /app
COPY --from=build /build/build/libs/goodscontrol-0.0.1.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]