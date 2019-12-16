# docker build -t jvm-operator:2.1 .

ARG VERSION=2.1

FROM maven:3-adoptopenjdk-11 as build
WORKDIR /app
COPY pom.xml .
COPY dependencies /root/.m2/repository/com/squareup/okhttp3/okhttp/3.12.14-graalfix
RUN mvn -B dependency:resolve-plugins dependency:resolve
COPY . .
RUN mvn package

FROM ghcr.io/graalvm/graalvm-ce:21.2.0 as native
ARG VERSION
COPY --from=build /app/target/jvm-operator-$VERSION.jar /var/jvm-operator-$VERSION.jar
COPY config /var/config
WORKDIR /opt/graalvm
RUN gu install native-image \
 && native-image -J-Xmx3072m \
   --allow-incomplete-classpath \
   --no-fallback \
   --no-server \
   -H:EnableURLProtocols=https \
   -H:ConfigurationFileDirectories=/var/config \
   -jar /var/jvm-operator-$VERSION.jar \
 && mv jvm-operator-$VERSION /opt/jvm-operator-$VERSION

FROM frolvlad/alpine-glibc:alpine-3.11_glibc-2.31
ARG VERSION
WORKDIR /home
COPY --from=native /opt/jvm-operator-$VERSION operator
COPY --from=native /opt/graalvm-ce-java11-21.2.0/lib/libsunec.so libsunec.so
RUN apk add --no-cache libstdc++
ENTRYPOINT ["./operator"]
