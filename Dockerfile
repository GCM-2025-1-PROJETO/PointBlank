FROM eclipse-temurin:21-jdk-jammy as builder

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./

RUN ./mvnw dependency:go-offline

COPY src ./src

RUN ./mvnw clean install -DskipTests


FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

ARG JAR_FILE=target/*.jar

COPY --from=builder /app/${JAR_FILE} application.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "application.jar"]
