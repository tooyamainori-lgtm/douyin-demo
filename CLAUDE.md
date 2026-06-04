# CLAUDE.md

## 项目概述

**Douyin Web Demo** — 仿抖音短视频网页版，复现抖音核心浏览与互动体验。  
采用前后端分离架构，后端提供 RESTful API，前端 SPA 单页应用。

---

## MVP 功能范围

### 用户模块
- 注册 — `POST /api/v1/users/register`
- 登录 — `POST /api/v1/users/login`
- JWT 鉴权 — 请求头 `Authorization: Bearer <token>`
- 个人主页 — `GET /api/v1/users/{id}`

### 视频模块
- 上传视频 — `POST /api/v1/videos`
- 视频列表（信息流） — `GET /api/v1/videos`
- 视频详情 — `GET /api/v1/videos/{id}`

### 互动模块
- 点赞 / 取消点赞 — `POST|DELETE /api/v1/videos/{videoId}/like`
- 评论列表 — `GET /api/v1/videos/{videoId}/comments`
- 发表评论 — `POST /api/v1/videos/{videoId}/comments`

### 搜索模块
- 搜索视频 — `GET /api/v1/videos?keyword=xxx`

### 暂不实现
直播、私信、推荐算法、商城

---

## 技术栈

### 后端
| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.x | 应用框架 |
| MyBatis Plus | 3.5+ | ORM / 数据库操作 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 7.x | 缓存 / 会话 / 计数 |
| JWT (jjwt) | 0.12+ | 无状态身份认证 |
| MinIO | — | 对象存储（视频、图片、头像） |

### 前端
| 技术 | 用途 |
|------|------|
| Vue 3 | UI 框架（Composition API） |
| Vite | 构建工具 |
| Pinia | 状态管理 |
| Axios | HTTP 请求 |
| Element Plus | UI 组件库 |

---

## 项目结构

```
douyin-demo/
├── backend/                          # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/java/com/douyin/
│       ├── DouyinApplication.java    # 启动类
│       ├── config/                   # 配置类（Security, MyBatisPlus, Redis, MinIO, CORS）
│       ├── controller/               # 控制器层 — 仅处理请求/响应，不写业务
│       ├── service/                  # 业务接口
│       │   └── impl/                 # 业务实现类
│       ├── mapper/                   # MyBatis Plus Mapper 接口
│       ├── entity/                   # 数据库实体（与表一一对应）
│       ├── dto/                      # 数据传输对象 — 接收前端请求
│       ├── vo/                       # 视图对象 — 返回给前端
│       ├── common/                   # 公共类
│       │   ├── Result.java           # 统一响应体 Result<T>
│       │   ├── PageResult.java       # 分页响应体
│       │   └── exception/            # 全局异常处理
│       └── util/                     # 工具类
│
├── frontend/                         # Vue3 前端
│   └── src/
│       ├── api/                      # Axios 接口封装
│       ├── stores/                   # Pinia 状态仓库
│       ├── views/                    # 页面组件
│       ├── components/               # 公共组件
│       ├── router/                   # Vue Router 路由
│       └── utils/                    # 工具函数
│
├── docs/
│   ├── api.md                        # API 接口文档（所有接口变更同步更新）
│   ├── database.sql                  # 数据库初始化脚本（所有表结构变更同步更新）
│   └── architecture.md               # 架构设计文档
│
├── CLAUDE.md
└── README.md
```

---

## 后端开发规范

### 1. 统一响应格式 — `Result<T>`

**所有接口返回值必须包装为 `Result<T>`，不允许直接返回实体或字符串。**

```java
// common/Result.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private Integer code;    // 业务状态码：200 成功，其他为异常
    private String message;  // 提示信息
    private T data;          // 响应数据，可为 null

    /**
     * 请求成功（带数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return Result
     */
    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 请求成功（无数据）
     *
     * @param <T> 数据类型
     * @return Result
     */
    public static <T> Result<T> ok() {
        return ok(null);
    }

    /**
     * 请求失败
     *
     * @param code    业务状态码
     * @param message 错误提示
     * @param <T>     数据类型
     * @return Result
     */
    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}
```

### 2. DTO 与 VO 分离

| 类型 | 职责 | 方向 |
|------|------|------|
| **entity** | 与数据库表一一映射，MyBatis Plus 使用 | — |
| **DTO** | 接收前端请求参数 | 请求方向 |
| **VO** | 组装返回给前端的数据 | 响应方向 |

**规则：**
- Controller 入参使用 **DTO**，出参使用 **VO**
- Service 层可能使用 BO（业务对象），但 DTO/VO 必须独立
- DTO 和 VO 之间转换使用手动映射或 MapStruct，禁止在 Controller 中直接操作 entity
- entity 永不暴露到 Controller 层

```java
// 示例：UserController
@PostMapping("/register")
public Result<UserVO> register(@Valid @RequestBody RegisterDTO dto) {
    UserVO vo = userService.register(dto);
    return Result.ok(vo);
}
```

### 3. 主键规范

- **所有数据库表主键统一使用 `Long` 类型**
- MyBatis Plus 默认雪花算法生成 ID
- 前端接收/发送 ID 一律使用 `number | string`（JS 大数精度问题）

```java
// entity 示例
@Data
@TableName("user")
public class User {
    @TableId(type = IdType.ASSIGN_ID)  // 雪花算法 Long ID
    private Long id;
    // ...
}
```

### 4. RESTful API 风格

```
GET     /api/v1/users          # 查询用户列表
GET     /api/v1/users/{id}     # 查询单个用户
POST    /api/v1/users          # 新增用户
PUT     /api/v1/users/{id}     # 更新用户
DELETE  /api/v1/users/{id}     # 删除用户

# 子资源
GET     /api/v1/videos/{videoId}/comments      # 视频下的评论
POST    /api/v1/videos/{videoId}/comments      # 发表评论
POST    /api/v1/videos/{videoId}/like          # 点赞视频
DELETE  /api/v1/videos/{videoId}/like          # 取消点赞
```

**规则：**
- URL 使用小写 + 短横线（kebab-case）
- 版本号 `/api/v1/` 前缀
- 资源名使用复数名词
- 复杂动作使用子资源表示（如 `like`、`share`、`follow`）

### 5. 禁止使用 JPA

- **严禁**引入 `spring-boot-starter-data-jpa` 依赖
- **严禁**使用 `@Entity`、`@Table`（javax.persistence）注解
- 数据访问统一使用 **MyBatis Plus** 的 `BaseMapper<T>`
- 复杂查询使用 MyBatis Plus 的 `QueryWrapper` / `LambdaQueryWrapper`，或自定义 SQL

### 6. MyBatis Plus 使用规范

```java
// Mapper 接口继承 BaseMapper
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 简单 CRUD 使用 BaseMapper 提供的方法
    // 复杂查询在此定义，在 XML 或注解中写 SQL
}
```

- 实体类使用 `@TableName` 指定表名
- 逻辑删除使用 `@TableLogic` 注解
- 自动填充使用 `@TableField(fill = FieldFill.INSERT)` 配合 MetaObjectHandler
- 分页查询使用 MyBatis Plus 的 `Page<T>` 对象

### 7. 异常处理

```java
// 全局异常处理器
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail(500, "服务器内部错误");
    }
}
```

---

## 前端开发规范

### 组件结构

```vue
<script setup lang="ts">
// 1. 导入
// 2. Props / Emits
// 3. 响应式状态（ref / reactive）
// 4. 计算属性（computed）
// 5. 生命周期（onMounted 等）
// 6. 方法
</script>

<template>
  <!-- 模板 -->
</template>

<style scoped>
/* 样式 */
</style>
```

### API 封装

```typescript
// api/user.ts
import request from '@/utils/request'

export const userApi = {
  /** 用户注册 */
  register(data: RegisterDTO) {
    return request.post<Result<UserVO>>('/api/v1/users', data)
  },
  /** 获取用户信息 */
  getById(id: number) {
    return request.get<Result<UserVO>>(`/api/v1/users/${id}`)
  }
}
```

### 状态管理（Pinia）

```typescript
// stores/user.ts
export const useUserStore = defineStore('user', () => {
  const user = ref<UserVO | null>(null)
  const isLogin = computed(() => !!user.value)

  async function fetchUser(id: number) {
    const res = await userApi.getById(id)
    user.value = res.data.data
  }

  return { user, isLogin, fetchUser }
})
```

---

## 数据库规范

### 通用表结构模板

```sql
-- 所有表必须包含以下字段
CREATE TABLE `xxx` (
    `id`          BIGINT       NOT NULL COMMENT '主键ID（雪花算法）',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='表说明';
```

### 命名规则

| 项 | 规则 | 示例 |
|------|------|------|
| 表名 | 小写 + 下划线 | `user_profile` |
| 字段名 | 小写 + 下划线 | `video_count` |
| 索引名 | `idx_表名_字段` | `idx_user_phone` |
| 唯一索引 | `uk_表名_字段` | `uk_user_email` |

---

## 同步机制（重要）

### api.md
- 每次新增/修改接口，**必须同步更新** `docs/api.md`
- 包含：请求方式、URL、请求参数（Header/Body/Query）、响应示例、错误码说明

### database.sql
- 每次新增/修改表结构，**必须同步更新** `docs/database.sql`
- 保持为可一键执行的完整建表脚本
- 按模块分组，注释分隔

---

## 编码约定

- **中文注释**：所有类、方法、字段、业务逻辑注释一律使用中文
- **禁止使用 JPA**：数据访问层只用 MyBatis Plus
- **主键统一 Long**：雪花算法生成，前端注意大数精度
- **JSON 序列化**：Long 类型 ID 在前端序列化为字符串，避免精度丢失

### 命名约定

| 类型 | 命名规则 | 示例 |
|------|----------|------|
| Java 类 | PascalCase | `VideoController` |
| Java 方法/变量 | camelCase | `getVideoList()` |
| Java 包名 | 全小写 | `com.douyin.controller` |
| SQL 表/字段 | snake_case | `user_id` |
| Vue 组件文件 | PascalCase | `VideoPlayer.vue` |
| TypeScript 函数/变量 | camelCase | `getVideoList` |
| REST URL | kebab-case | `/api/v1/video-comments` |

---

## 环境要求

| 工具 | 最低版本 |
|------|----------|
| JDK | 17 |
| Maven | 3.8+ |
| Node.js | 18 |
| MySQL | 8.0 |
| Redis | 7.x |
| MinIO | 最新稳定版 |
