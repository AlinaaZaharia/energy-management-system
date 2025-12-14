FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

ARG POM_DIR=.
COPY . .

RUN --mount=type=cache,target=/root/.m2 mvn -q -f ${POM_DIR}/pom.xml -DskipTests dependency:go-offline
RUN --mount=type=cache,target=/root/.m2 mvn -q -f ${POM_DIR}/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
ARG POM_DIR=.

COPY --from=build /app/${POM_DIR}/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]