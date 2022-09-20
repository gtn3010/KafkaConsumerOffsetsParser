#syntax=docker/dockerfile:experimental
FROM maven:3.8.6-openjdk-18-slim as build
WORKDIR /build
COPY . .
#RUN --mount=type=bind,source=./m2,target=/root/.m2,readwrite mvn -DskipTests clean install
RUN --mount=type=cache,target=/root/.m2,readwrite mvn -DskipTests clean install

FROM openjdk:11-jdk-slim
WORKDIR /app
COPY --from=build /build/target/KafkaConsumerOffsetsMonitoring-0.0.1-SNAPSHOT-jar-with-dependencies.jar .
CMD ["java", "-jar", "KafkaConsumerOffsetsMonitoring-0.0.1-SNAPSHOT-jar-with-dependencies.jar"]
