# docker build -t jvm-operator:2.0 .
# kubectl cp custom-operator:/graalvm/config /tmp

FROM maven:3-adoptopenjdk-11 as build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:resolve-plugins dependency:resolve
COPY . .
RUN mvn package

FROM ghcr.io/graalvm/graalvm-ce:21.2.0
RUN gu install native-image
COPY --from=build /app/target/jvm-operator-2.0.jar /var/operator.jar
RUN mkdir -p /graalvm/config
ENTRYPOINT ["java", "-agentlib:native-image-agent=config-output-dir=/graalvm/config", "-jar", "/var/operator.jar"]
