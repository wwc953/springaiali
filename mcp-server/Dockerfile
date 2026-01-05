FROM openjdk:17.0.1-jdk-slim
LABEL authors="wangwc"
ENV TZ="Asia/Shanghai"
COPY ./target/*.jar /app.jar
EXPOSE 27070
ENTRYPOINT ["java","-jar","/app.jar"]