FROM maven:3.8.6-amazoncorretto-17 as build

WORKDIR /usr/src/app

COPY . .

RUN mvn clean package


FROM amazoncorretto:17-alpine-jdk

COPY --from=build /usr/src/app/target/*.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
