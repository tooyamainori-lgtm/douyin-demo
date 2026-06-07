# API 接口文档

> 所有接口变更必须同步更新此文件。
>
> 基础路径：`/api/v1`
>
> 统一响应格式：
> ```json
> {
>   "code": 200,
>   "message": "操作成功",
>   "data": {}
> }
> ```
>
> 认证方式：请求头 `Authorization: Bearer <token>`

---

## 目录

- [用户模块](#用户模块)
- [视频模块](#视频模块)
- [互动模块](#互动模块)
- [关注模块](#关注模块)
- [消息通知](#消息通知)
- [搜索模块](#搜索模块)
- [错误码说明](#错误码说明)

---

## 用户模块

### 注册

```
POST /api/v1/users/register
```

**请求体**
```json
{
  "username": "string, 必填, 3-30位字母数字下划线",
  "password": "string, 必填, 6-32位",
  "phone": "string, 选填, 11位手机号",
  "email": "string, 选填, 邮箱"
}
```

**响应**
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "id": "1234567890",
    "username": "testuser",
    "nickname": "用户_1234567890",
    "avatarUrl": null,
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

---

### 登录

```
POST /api/v1/users/login
```

**请求体**
```json
{
  "username": "string, 必填",
  "password": "string, 必填"
}
```

**响应**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "id": "1234567890",
    "username": "testuser",
    "nickname": "测试用户",
    "avatarUrl": "https://minio.example.com/avatars/xxx.jpg",
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

---

### 获取当前用户信息

```
GET /api/v1/users/me
Authorization: Bearer <token>
```

**响应**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "1234567890",
    "username": "testuser",
    "nickname": "测试用户",
    "avatarUrl": "https://minio.example.com/avatars/xxx.jpg",
    "bio": "这是我的简介",
    "followCount": 100,
    "fansCount": 50,
    "videoCount": 12,
    "likeCount": 999
  }
}
```

---

### 更新个人资料

```
PUT /api/v1/users/me
Authorization: Bearer <token>
```

**请求体**
```json
{
  "nickname": "string, 选填",
  "bio": "string, 选填",
  "gender": "integer, 选填, 0-未知 1-男 2-女",
  "birthday": "string, 选填, 格式 YYYY-MM-DD"
}
```

---

### 上传头像

```
POST /api/v1/users/me/avatar
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

**请求参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | 图片文件 |

**响应**
```json
{
  "code": 200,
  "data": "http://localhost:9000/douyin/avatars/xxx.png"
}
```

---

### 获取用户主页

```
GET /api/v1/users/{id}
```

**响应**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "1234567890",
    "username": "testuser",
    "nickname": "测试用户",
    "avatarUrl": "https://minio.example.com/avatars/xxx.jpg",
    "bio": "这是我的简介",
    "followCount": 100,
    "fansCount": 50,
    "videoCount": 12,
    "likeCount": 999,
    "videos": [
      {
        "id": "9876543210",
        "title": "我的第一条视频",
        "coverUrl": "https://minio.example.com/covers/xxx.jpg",
        "likeCount": 52
      }
    ]
  }
}
```

---

## 视频模块

### 上传视频

```
POST /api/v1/videos
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

**请求参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| video | File | 是 | 视频文件 |
| cover | File | 否 | 封面图片 |
| title | String | 是 | 视频标题，1-100字 |
| description | String | 否 | 视频描述 |

**响应**
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "id": "9876543210",
    "title": "我的第一条视频",
    "description": "视频描述",
    "videoUrl": "https://minio.example.com/videos/xxx.mp4",
    "coverUrl": "https://minio.example.com/covers/xxx.jpg",
    "duration": 15.5,
    "width": 1080,
    "height": 1920
  }
}
```

---

### 视频列表（信息流）

```
GET /api/v1/videos?page=1&size=10
```

**请求参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认 1 |
| size | int | 否 | 每页条数，默认 10，最大 30 |

**响应**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 100,
    "page": 1,
    "size": 10,
    "records": [
      {
        "id": "9876543210",
        "title": "视频标题",
        "coverUrl": "https://minio.example.com/covers/xxx.jpg",
        "videoUrl": "https://minio.example.com/videos/xxx.mp4",
        "duration": 15.5,
        "viewCount": 1000,
        "likeCount": 52,
        "commentCount": 8,
        "author": {
          "id": "1234567890",
          "nickname": "作者昵称",
          "avatarUrl": "https://minio.example.com/avatars/xxx.jpg"
        },
        "isLiked": false
      }
    ]
  }
}
```

### 关注动态 Feed

```
GET /api/v1/videos/following?page=1&size=10
Authorization: Bearer <token>
```

**说明**：返回当前用户关注的人发布的视频，按时间倒序。未登录返回空。

**请求参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认 1 |
| size | int | 否 | 每页条数，默认 10 |

**响应**：同视频列表，分页格式。

---

### 热门视频排行榜

```
GET /api/v1/videos/hot?top=10
```

**请求参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| top | int | 否 | 前 N 名，默认 10 |

**响应**
```json
{
  "code": 200,
  "data": [
    { "id": "...", "title": "...", "viewCount": 100, "likeCount": 20, "author": {...} }
  ]
}
```

> 排行依据：Redis ZSet，播放 +1 分，点赞 +3 分。

---

### 视频详情

```
GET /api/v1/videos/{id}
```

**响应**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "9876543210",
    "title": "视频标题",
    "description": "视频描述",
    "videoUrl": "https://minio.example.com/videos/xxx.mp4",
    "coverUrl": "https://minio.example.com/covers/xxx.jpg",
    "duration": 15.5,
    "width": 1080,
    "height": 1920,
    "tags": ["搞笑", "日常"],
    "viewCount": 1000,
    "likeCount": 52,
    "commentCount": 8,
    "shareCount": 3,
    "createTime": "2026-06-04T12:00:00",
    "author": {
      "id": "1234567890",
      "nickname": "作者昵称",
      "avatarUrl": "https://minio.example.com/avatars/xxx.jpg",
      "followCount": 100,
      "fansCount": 50
    },
    "isLiked": true
  }
}
```

---

### 删除视频

```
DELETE /api/v1/videos/{id}
Authorization: Bearer <token>
```

> 仅作者可删，同时清除 MinIO 文件。

### 视频封面

上传视频时附带可选 `cover` 参数（图片文件），不传封面时 FFmpeg 自动截取第 0.5 秒帧。

### 批量补封面

```
POST /api/v1/videos/admin/generate-covers
Authorization: Bearer <token>
```

> 为所有无封面的 MinIO 视频自动生成封面（FFmpeg 截帧）。

### 我点赞的视频

```
GET /api/v1/users/me/likes?page=1&size=10
Authorization: Bearer <token>
```

---

## 互动模块

### 点赞视频

```
POST /api/v1/videos/{videoId}/like
Authorization: Bearer <token>
```

**响应**
```json
{
  "code": 200,
  "message": "点赞成功",
  "data": null
}
```

---

### 取消点赞

```
DELETE /api/v1/videos/{videoId}/like
Authorization: Bearer <token>
```

**响应**
```json
{
  "code": 200,
  "message": "取消点赞成功",
  "data": null
}
```

---

### 评论列表

```
GET /api/v1/videos/{videoId}/comments?page=1&size=20
```

**响应**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 99,
    "page": 1,
    "size": 20,
    "records": [
      {
        "id": "1111111111",
        "content": "评论内容",
        "likeCount": 12,
        "createTime": "2026-06-04T12:30:00",
        "user": {
          "id": "1234567890",
          "nickname": "评论人昵称",
          "avatarUrl": "https://minio.example.com/avatars/xxx.jpg"
        }
      }
    ]
  }
}
```

---

### 发表评论

```
POST /api/v1/videos/{videoId}/comments
Authorization: Bearer <token>
```

**请求体**
```json
{
  "content": "string, 必填, 1-500字",
  "parentId": "string, 选填, 回复某条评论时传入父评论ID"
}
```

**响应**
```json
{
  "code": 200,
  "message": "评论成功",
  "data": {
    "id": "1111111111",
    "content": "评论内容",
    "likeCount": 0,
    "createTime": "2026-06-04T12:30:00",
    "user": {
      "id": "1234567890",
      "nickname": "用户昵称",
      "avatarUrl": "https://minio.example.com/avatars/xxx.jpg"
    }
  }
}
```

---

## 关注模块

### 关注用户

```
POST /api/v1/follows/{userId}
Authorization: Bearer <token>
```

### 取消关注

```
DELETE /api/v1/follows/{userId}
Authorization: Bearer <token>
```

### 是否已关注

```
GET /api/v1/follows/{userId}/status
```

### 关注列表

```
GET /api/v1/users/{userId}/following
```

### 粉丝列表

```
GET /api/v1/users/{userId}/followers
```

---

## 消息通知

### 通知列表

```
GET /api/v1/notifications?page=1&size=20
Authorization: Bearer <token>
```

### 未读数量

```
GET /api/v1/notifications/unread-count
Authorization: Bearer <token>
```

### 全部已读

```
PUT /api/v1/notifications/read
Authorization: Bearer <token>
```

---

## 搜索模块

### 搜索视频

```
GET /api/v1/videos?keyword=xxx&page=1&size=10
```

**请求参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | string | 是 | 搜索关键词 |
| page | int | 否 | 页码，默认 1 |
| size | int | 否 | 每页条数，默认 10 |

**响应**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 20,
    "page": 1,
    "size": 10,
    "records": [
      {
        "id": "9876543210",
        "title": "匹配关键词的视频",
        "coverUrl": "https://minio.example.com/covers/xxx.jpg",
        "duration": 15.5,
        "viewCount": 1000,
        "likeCount": 52,
        "author": {
          "id": "1234567890",
          "nickname": "作者昵称",
          "avatarUrl": "https://minio.example.com/avatars/xxx.jpg"
        }
      }
    ]
  }
}
```

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录 / Token 过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 409 | 资源冲突（如用户名已存在） |
| 413 | 上传文件过大 |
| 500 | 服务器内部错误 |

| 业务错误码 | 说明 |
|------------|------|
| 1001 | 用户名已存在 |
| 1002 | 用户名或密码错误 |
| 1003 | 账号已被禁用 |
| 2001 | 视频不存在 |
| 2002 | 上传失败 |
| 3001 | 评论不存在 |
| 3002 | 已经点过赞了 |
| 3003 | 已经关注过了 |
| 3004 | 未关注该用户 |
