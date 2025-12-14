# ===== Build =====
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Fișierele reale sunt sub ./demo, deci folosim POM_DIR
ARG POM_DIR=demo

COPY ${POM_DIR}/pom.xml ./pom.xml
RUN mvn dependency:go-offline -B

COPY ${POM_DIR}/src ./src
RUN mvn clean package -DskipTests

# ===== Runtime =====
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
