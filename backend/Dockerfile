# ============================================================
# Douyin Web Demo — Backend Dockerfile
# 多阶段构建：Maven 编译 → JRE 运行
# ============================================================

# ---------- 阶段1：构建 ----------
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build
COPY pom.xml .
# 预先下载依赖（利用 Docker 缓存层，失败也不影响后续构建）
RUN mvn dependency:resolve -B -q || true

COPY src ./src
RUN mvn package -DskipTests -B -q

# ---------- 阶段2：运行 ----------
FROM eclipse-temurin:17-jre-alpine

# 安装 FFmpeg（视频自动截取封面）
RUN apk add --no-cache ffmpeg

WORKDIR /app
COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
