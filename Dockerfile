# docker build -t jvm-operator:1.10 .

ARG VERSION=1.10

FROM zenika/alpine-maven:3 as build
COPY src src
COPY pom.xml pom.xml
RUN mvn package

FROM oracle/graalvm-ce:20.0.0 as native
ARG VERSION
COPY --from=build /usr/src/app/target/jvm-operator-$VERSION.jar /var/jvm-operator-$VERSION.jar
COPY config /var/config
WORKDIR /opt/graalvm
RUN gu install native-image \
 && native-image -J-Xmx3072m \
   --allow-incomplete-classpath \
   --no-fallback \
   --no-server \
   -H:EnableURLProtocols=https \
   -H:ConfigurationFileDirectories=/var/config \
   -H:+AddAllCharsets \
   -jar /var/jvm-operator-$VERSION.jar \
 && mv jvm-operator-$VERSION /opt/jvm-operator-$VERSION

FROM frolvlad/alpine-glibc:alpine-3.11_glibc-2.30
ARG VERSION
WORKDIR /home
COPY --from=native /opt/jvm-operator-$VERSION operator
COPY --from=native /opt/graalvm-ce-java8-20.0.0/jre/lib/amd64/libsunec.so libsunec.so
RUN apk add --no-cache libstdc++
ENTRYPOINT ["./operator"]