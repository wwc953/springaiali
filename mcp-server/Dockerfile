#FROM openjdk:17.0.1-jdk-slim
FROM eclipse-temurin:17-alpine-3.23
#FROM docker.m.daocloud.io/eclipse-temurin:17-alpine-3.23
#FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/openjdk:17
LABEL authors="wangwc"
ENV TZ="Asia/Shanghai"
COPY ./target/mcp-server-0.0.1-SNAPSHOT.jar /app.jar
EXPOSE 17070
ENTRYPOINT ["java","-jar","/app.jar"]