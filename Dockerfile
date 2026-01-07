# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN chmod +x gradlew && ./gradlew bootJar -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install dumb-init for proper signal handling
RUN apk add --no-cache dumb-init

# Create non-root user
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Copy jar from builder
COPY --from=builder /app/build/libs/*.jar app.jar

# Change ownership
RUN chown -R appuser:appgroup /app
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=90s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT:-8080}/me || exit 1

EXPOSE ${PORT:-8080}

ENTRYPOINT ["dumb-init", "--", "sh", "-c", "\
  java \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+UseStringDeduplication \
  -XX:+OptimizeStringConcat \
  -Xmx400m \
  -Xms200m \
  -XX:MaxMetaspaceSize=128m \
  -XX:MetaspaceSize=64m \
  -XX:+TieredCompilation \
  -XX:TieredStopAtLevel=1 \
  -XX:+UseCompressedOops \
  -XX:+UseCompressedClassPointers \
  -XX:ReservedCodeCacheSize=32m \
  -XX:InitialCodeCacheSize=16m \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.profiles.active=production \
  -Dserver.port=${PORT:-8080} \
  -jar app.jar"]