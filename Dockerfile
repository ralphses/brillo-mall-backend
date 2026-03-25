# syntax=docker/dockerfile:1.4

# ======================
# Stage 1: Build
# ======================
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Copy everything (including mvnw, pom.xml, and source code)
COPY . .

# Ensure mvnw has correct permissions (important for Linux builds)
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Build your application (skip tests)
RUN ./mvnw clean package -DskipTests

# ======================
# Stage 2: Runtime
# ======================
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/*.jar brillo_mall_api.jar

# Expose the application port
EXPOSE 9000

# Run the JAR
ENTRYPOINT ["java", "-jar", "brillo_mall_api.jar"]
