FROM openjdk:8-jdk-alpine
WORKDIR /opt
EXPOSE 8080
ADD target/*.jar /opt/app.jar
ENTRYPOINT exec java  -jar app.jar