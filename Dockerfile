# Etap 1: Budowanie (Maven + Java 21)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
# Kopiujemy CAŁY projekt (zakładając, że Dockerfile jest w głównym folderze)
COPY . .
RUN mvn clean package -DskipTests

# Etap 2: Uruchamianie (JRE 21)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Upewnij się, że ta nazwa pliku jar jest identyczna z tą w Twoim pom.xml
COPY --from=build /app/target/betacom-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]