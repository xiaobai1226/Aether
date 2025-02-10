# 使用 OpenJDK 21 作为基础镜像
FROM openjdk:21-slim

# 更新 apt 源并安装 FFmpeg
RUN apt-get update && apt-get install -y ffmpeg && rm -rf /var/lib/apt/lists/*

ENV JAVA_OPTS="-server -Xmx512m -Xms512m  -Djava.awt.headless=true -Dreactor.netty.pool.leasingStrategy=lifo"

RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone

RUN mkdir -p /root/aether/config

# 设置工作目录
WORKDIR /root/aether

# 将项目构建生成的 jar 文件复制到容器中
COPY ./build/libs/aether.jar /root/aether/aether.jar

ENTRYPOINT exec java $JAVA_OPTS -jar aether.jar --solon.config.add=./config/app.yml --env=pro

#MAINTAINER www.microwu.com <microwu@microwu.com>