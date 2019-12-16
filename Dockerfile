# docker build -t jvm-operator:1.8.5 .

FROM zenika/alpine-maven:3 as build
COPY src src
COPY pom.xml pom.xml
RUN mvn package

FROM oracle/graalvm-ce:20.0.0
RUN gu install native-image
COPY --from=build /usr/src/app/target/jvm-operator-1.8.5.jar /var/operator.jar
ENTRYPOINT ["java", "-agentlib:native-image-agent=config-output-dir=/graalvm/config", "-jar", "/var/operator.jar"]
