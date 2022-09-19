#syntax=docker/dockerfile:experimental
FROM maven:3.8.6-openjdk-18-slim as build

WORKDIR /build
RUN apt update && apt install -y vim git \
    && git clone https://github.com/gtn3010/KafkaConsumerOffsetsParser.git \
	&& cd KafkaConsumerOffsetsParser \
RUN --mount=type=bind,source=./m2,target=/root/.m2 mvn -T 4 -DskipTests compile package

FROM openjdk:11-jdk-slim
WORKDIR /app
COPY --from=build /build/KafkaConsumerOffsetsParser/target/KafkaConsumerOffsetsMonitoring-0.0.1-SNAPSHOT-jar-with-dependencies.jar .

CMD ["java", "-jar", "KafkaConsumerOffsetsMonitoring-0.0.1-SNAPSHOT-jar-with-dependencies.jar"]
