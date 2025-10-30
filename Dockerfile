# === Stage 1: Build the JAR file ===
# Use an official OpenJDK image that includes Maven to build our project
# We use '17-jdk' as an example. Change this to your project's Java version.
FROM maven:3.8.5-openjdk-21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the source code and build the application
COPY src ./src
RUN mvn package -DskipTests

# === Stage 2: Create the final, lightweight image ===
# Use a slim JRE (Java Runtime Environment) image, which is smaller
# and more secure because it doesn't include the compiler (JDK).
FROM openjdk:21-jre-slim

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the 'build' stage (Stage 1)
# Make sure to update 'your-app-name-0.0.1.jar' to your JAR's actual name!
COPY --from=build /app/target/your-app-name-0.0.1.jar app.jar

# Expose the port your Spring Boot app runs on (default is 8080)
EXPOSE 8080

# The command to run your application
ENTRYPOINT ["java", "-jar", "app.jar"]