# docker build -t jvm-operator:2.0 .

FROM zenika/alpine-maven:3 as build
COPY pom.xml .
RUN mvn -B dependency:resolve-plugins dependency:resolve
COPY . .
RUN mvn package

FROM oracle/graalvm-ce:20.0.0
RUN gu install native-image
COPY --from=build /usr/src/app/target/jvm-operator-2.0.jar /var/operator.jar
ENTRYPOINT ["java", "-agentlib:native-image-agent=config-output-dir=/graalvm/config", "-jar", "/var/operator.jar"]
