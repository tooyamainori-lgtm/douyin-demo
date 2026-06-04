# douyin-demo

抖音网页版 Demo，复现抖音核心体验。

## MVP 功能

| 模块 | 功能 | 状态 |
|------|------|------|
| 用户 | 注册 / 登录 / JWT鉴权 / 个人主页 | 待实现 |
| 视频 | 上传 / 信息流 / 详情 | 待实现 |
| 互动 | 点赞 / 评论 | 待实现 |
| 搜索 | 搜索视频 | 待实现 |

### 暂不实现
直播、私信、推荐算法、商城

## 技术栈

| 层 | 技术 |
|------|------|
| 后端 | Spring Boot 3 + MyBatis Plus + MySQL 8 + Redis + JWT + MinIO |
| 前端 | Vue 3 + Vite + Pinia + Axios + Element Plus |

## 快速开始

```bash
# 后端
cd backend
mvn spring-boot:run

# 前端
cd frontend
npm install
npm run dev
```

## 项目文档

- [API 接口文档](docs/api.md)
- [数据库脚本](docs/database.sql)
- [架构设计](docs/architecture.md)
