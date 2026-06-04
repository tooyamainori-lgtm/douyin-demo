# CLAUDE.md

Douyin (抖音) 网页版 Demo 项目，模拟抖音的短视频播放、推荐、互动等核心功能。

## 技术栈

- **Frontend**: React + TypeScript + Vite
- **Backend**: Node.js / Express + TypeScript
- **Database**: PostgreSQL / Redis

## 项目结构

```
douyin-demo/
├── backend/          # 后端服务 - REST API, 视频处理, 用户系统
├── frontend/         # 前端 - 视频播放, 交互界面
├── docs/             # 项目文档 - API 文档, 架构设计
├── CLAUDE.md         # AI 辅助开发配置
└── README.md         # 项目说明
```

## 开发约定

- 使用 TypeScript 全栈
- 前端组件使用函数式组件 + Hooks
- 后端使用分层架构 (Controller → Service → Repository)
- 代码风格遵循 ESLint + Prettier
