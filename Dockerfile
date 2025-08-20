FROM amazoncorretto:21 as build
WORKDIR /opt/project
EXPOSE 8080
COPY gradle             /opt/project/gradle
COPY gradlew            /opt/project/
COPY ./build.gradle     /opt/project/
COPY ./settings.gradle  /opt/project/
COPY ./src              /opt/project/src
RUN ./gradlew clean bootJar

FROM amazoncorretto:21
COPY --from=build /opt/project/build/libs/api-app.jar api-app.jar
EXPOSE 8080
CMD java \
    --add-opens=java.base/java.time=ALL-UNNAMED \
    --add-opens=java.base/java.time.format=ALL-UNNAMED \
    -jar api-app.jar
