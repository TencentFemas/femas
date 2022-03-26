FROM ubuntu:20.04

LABEL maintainer "minghhou <minghhou>"

ENV BUILD_MODE="docker"

RUN sed -i 's/archive.ubuntu.com/mirrors.tencent.com/g' /etc/apt/sources.list && \
    sed -i 's/security.ubuntu.com/mirrors.tencent.com/g' /etc/apt/sources.list

RUN apt update && apt install openjdk-8-jdk maven iproute2 -y

COPY ./ /usr/local/src/femas

RUN echo "start build docker" && cd /usr/local/src/femas && mvn -Dmaven.test.skip=true clean install -U

COPY ./entrypoint.sh /entrypoint.sh

RUN chmod +x /entrypoint.sh

ENTRYPOINT /entrypoint.sh

