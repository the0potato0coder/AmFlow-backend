# === Stage 1: Build the JAR file ===
# Use the official Eclipse Temurin image, which includes Maven
FROM eclipse-temurin:21-jdk-jammy AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the source code and build the application
COPY src ./src
RUN mvn package -DskipTests

# === Stage 2: Create the final, lightweight image ===
# Use the slim Temurin JRE (Java Runtime Environment) image
FROM eclipse-temurin:21-jre-jammy

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the 'build' stage (Stage 1)
# Make sure to update 'your-app-name-0.0.1.jar' to your JAR's actual name!
COPY --from=build /app/target/your-app-name-0.0.1.jar app.jar

# Expose the port your Spring Boot app runs on (default is 8080)
EXPOSE 8080

# The command to run your application
ENTRYPOINT ["java", "-jar", "app.jar"]