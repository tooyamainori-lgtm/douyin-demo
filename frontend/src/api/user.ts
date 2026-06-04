import request from '@/utils/request'

/** 注册参数 */
export interface RegisterParams {
  username: string
  password: string
  phone?: string
  email?: string
}

/** 登录参数 */
export interface LoginParams {
  username: string
  password: string
}

/** 用户基本信息 */
export interface UserInfo {
  id: string
  username: string
  nickname: string
  avatarUrl: string | null
  token?: string
}

/** 用户主页信息 */
export interface UserProfile {
  id: string
  username: string
  nickname: string
  avatarUrl: string | null
  bio: string | null
  followCount: number
  fansCount: number
  videoCount: number
  likeCount: number
  createTime: string | null
}

/**
 * 用户 API
 */
export const userApi = {
  /** 注册 */
  register(data: RegisterParams) {
    return request.post<any, UserInfo>('/api/v1/users/register', data)
  },

  /** 登录 */
  login(data: LoginParams) {
    return request.post<any, UserInfo>('/api/v1/users/login', data)
  },

  /** 获取当前用户信息 */
  getMe() {
    return request.get<any, UserInfo>('/api/v1/users/me')
  },

  /** 获取用户主页 */
  getUserProfile(userId: string) {
    return request.get<any, UserProfile>(`/api/v1/users/${userId}`)
  },
}
