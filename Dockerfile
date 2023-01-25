FROM openjdk:17.0.2-jdk
VOLUME /main-app
COPY service/build/libs/service-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "--enable-preview", "/app.jar"]
