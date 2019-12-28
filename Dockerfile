# docker build -t jvm-operator:1.13 .

ARG VERSION=1.13

FROM zenika/alpine-maven:3 as build
COPY src src
COPY pom.xml pom.xml
RUN mvn package

FROM oracle/graalvm-ce:20.0.0 as native
ARG VERSION
COPY --from=build /usr/src/app/target/jvm-operator-$VERSION.jar /var/jvm-operator-$VERSION.jar
WORKDIR /opt/graalvm
RUN gu install native-image \
 && native-image -jar /var/jvm-operator-$VERSION.jar \
 && mv jvm-operator-$VERSION /opt/jvm-operator-$VERSION

FROM scratch
ARG VERSION
WORKDIR /home
COPY --from=native /opt/jvm-operator-$VERSION operator
ENTRYPOINT ["./operator"]