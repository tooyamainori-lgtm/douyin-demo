import request from '@/utils/request'

/** 标签信息（含视频数） */
export interface TagInfo {
  name: string
  videoCount: number
  icon: string
}

/**
 * 标签 API
 */
export const tagApi = {
  /** 获取所有预设标签 */
  list() {
    return request.get<any, TagInfo[]>('/api/v1/tags')
  },

  /** 按标签获取视频列表 */
  getVideos(tagName: string, page: number, size: number) {
    return request.get<any, { total: number; page: number; size: number; records: any[] }>(
      `/api/v1/tags/${encodeURIComponent(tagName)}/videos?page=${page}&size=${size}`,
    )
  },
}
