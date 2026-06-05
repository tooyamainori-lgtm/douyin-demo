import request from '@/utils/request'

/** 视频作者 */
export interface VideoAuthor {
  id: string
  nickname: string
  avatarUrl: string | null
}

/** 视频信息 */
export interface VideoInfo {
  id: string
  title: string
  description: string
  videoUrl: string
  coverUrl: string
  duration: number
  width: number
  height: number
  tags: string
  viewCount: number
  likeCount: number
  commentCount: number
  shareCount: number
  createTime: string
  author: VideoAuthor
  isLiked: boolean
  isFavorited: boolean
}

/** 分页结果 */
export interface PageResult<T> {
  total: number
  page: number
  size: number
  records: T[]
}

/**
 * 视频 API
 */
export const videoApi = {
  /** 视频列表 */
  list(page: number, size: number, keyword?: string) {
    let url = `/api/v1/videos?page=${page}&size=${size}`
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`
    return request.get<any, PageResult<VideoInfo>>(url)
  },

  /** 视频详情 */
  getDetail(id: string) {
    return request.get<any, VideoInfo>(`/api/v1/videos/${id}`)
  },

  /** 用户作品列表 */
  listByUser(userId: string, page: number, size: number) {
    return request.get<any, PageResult<VideoInfo>>(`/api/v1/users/${userId}/videos?page=${page}&size=${size}`)
  },

  /** 播放量统计 */
  recordView(id: string) {
    return request.post(`/api/v1/videos/${id}/view`)
  },

  /** 点赞 */
  like(id: string) {
    return request.post(`/api/v1/videos/${id}/like`)
  },

  /** 取消点赞 */
  unlike(id: string) {
    return request.delete(`/api/v1/videos/${id}/like`)
  },

  /** 上传视频 */
  upload(data: FormData) {
    return request.post<any, VideoInfo>('/api/v1/videos', data, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },

  /** 删除视频（仅作者） */
  delete(id: string) {
    return request.delete(`/api/v1/videos/${id}`)
  },

  /** 热门排行榜 */
  getHot(top: number = 10) {
    return request.get<any, VideoInfo[]>(`/api/v1/videos/hot?top=${top}`)
  },
}

/** 评论信息 */
export interface CommentInfo {
  id: string
  content: string
  likeCount: number
  createTime: string
  parentId: string | null
  replyNickname: string | null
  user: { id: string; nickname: string; avatarUrl: string | null }
  replies: CommentInfo[]
}

/** 发送评论参数 */
export interface CommentParams {
  videoId: string
  content: string
  parentId?: string
  replyUserId?: string
}

/**
 * 评论 API
 */
export const favoriteApi = {
  add(videoId: string) {
    return request.post(`/api/v1/videos/${videoId}/favorite`)
  },
  remove(videoId: string) {
    return request.delete(`/api/v1/videos/${videoId}/favorite`)
  },
  list(page: number, size: number) {
    return request.get<any, PageResult<VideoInfo>>(`/api/v1/users/me/favorites?page=${page}&size=${size}`)
  },
}

export const commentApi = {
  /** 获取评论列表 */
  list(videoId: string) {
    return request.get<any, CommentInfo[]>(`/api/v1/videos/${videoId}/comments`)
  },

  /** 发表评论 */
  publish(videoId: string, data: CommentParams) {
    return request.post<any, CommentInfo>(`/api/v1/videos/${videoId}/comments`, data)
  },

  /** 删除评论 */
  delete(commentId: string) {
    return request.delete(`/api/v1/comments/${commentId}`)
  },
}
