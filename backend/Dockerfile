FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENV TZ=Asia/Shanghai
RUN mkdir -p /tmp/exam-import
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
