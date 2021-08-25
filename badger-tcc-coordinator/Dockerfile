FROM openjdk:8-jdk-alpine
RUN apk --no-cache add curl
WORKDIR /opt
EXPOSE 8080
ADD target/*.jar /opt/app.jar
ENTRYPOINT exec java  -jar app.jar