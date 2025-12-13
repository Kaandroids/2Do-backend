# --- STAGE 1: Build Stage ---
# Use the official Maven image with Java 21 to build the application.
# This stage compiles the code and packages it into a JAR file.
FROM maven:3.9.9-eclipse-temurin-21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the project definition file first.
# This allows Docker to cache dependencies if the pom.xml hasn't changed.
COPY pom.xml .

# Download all required dependencies offline.
# This step improves build speed by using cached layers for dependencies.
RUN mvn dependency:go-offline

# Copy the actual source code into the container
COPY src ./src

# Build the application and skip tests to speed up the process.
# (Tests are usually run in a separate CI/CD pipeline step).
RUN mvn clean package -DskipTests

# --- STAGE 2: Runtime Stage ---
# Use a lightweight (slim) Java 21 runtime image for the final container.
# This keeps the image size small and secure.
FROM eclipse-temurin:21-jdk-alpine

# Set the working directory for the runtime container
WORKDIR /app

# Copy the compiled JAR file from the 'build' stage.
# We use a wildcard (*.jar) to handle version changes automatically.
COPY --from=build /app/target/*.jar app.jar

# Expose port 8080 to the outside world
EXPOSE 8080

# Define the command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]