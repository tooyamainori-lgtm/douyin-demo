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
}
