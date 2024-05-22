FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17.O.1-jdk-slim
COPY --from=build /target/openAPITalentoB-0.O.1-SNAPSHOT.jar demo.jar
EXPOSE 8080
ENTRYPOINT ["java", "—jar", "demo.jar"]