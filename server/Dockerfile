FROM openjdk:17

LABEL MAINTAINER="https://github.com/ssolomvi"

LABEL APPLICATION="server"

WORKDIR /usr/app

COPY target/server.jar server.jar

ENTRYPOINT ["java","-jar","server.jar"]

EXPOSE 8200