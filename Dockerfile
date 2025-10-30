# === Stage 1: Build the JAR file ===
# Use an official Maven image that includes JDK 21
# This image has both 'java' and 'mvn' commands
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the source code and build the application
COPY src ./src
RUN mvn package -DskipTests

# === Stage 2: Create the final, lightweight image ===
# This stage stays the same - it only needs the Java Runtime (JRE)
FROM eclipse-temurin:21-jre-jammy

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the 'build' stage (Stage 1)
# !! IMPORTANT: Make sure this JAR file name is correct !!
COPY --from=build /app/target/your-app-name-0.0.1.jar app.jar

# Expose the port your Spring Boot app runs on (default is 8080)
EXPOSE 8080

# The command to run your application
ENTRYPOINT ["java", "-jar", "app.jar"]