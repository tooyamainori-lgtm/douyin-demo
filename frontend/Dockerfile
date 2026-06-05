# ============================================================
# Douyin Web Demo — Frontend Dockerfile
# 多阶段构建：Node 编译 → Nginx 静态服务
# ============================================================

# ---------- 阶段1：构建 ----------
FROM node:18-alpine AS build

WORKDIR /build
COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build

# ---------- 阶段2：运行 ----------
FROM nginx:alpine

# 复制构建产物
COPY --from=build /build/dist /usr/share/nginx/html
# 复制 Nginx 配置（SPA 路由回退）
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
