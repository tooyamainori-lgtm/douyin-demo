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
  bio: string | null
  gender: number | null
  birthday: string | null
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
  isFollowing: boolean
  createTime: string | null
}

/**
 * 用户 API
 */
/** 更新资料参数 */
export interface UpdateProfileParams {
  nickname?: string
  bio?: string
  gender?: number
  birthday?: string
}

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

  /** 更新个人资料 */
  updateProfile(data: UpdateProfileParams) {
    return request.put<any, UserInfo>('/api/v1/users/me', data)
  },

  /** 上传头像 */
  uploadAvatar(data: FormData) {
    return request.post<any, string>('/api/v1/users/me/avatar', data, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },

  /** 获取关注列表 */
  getFollowing(userId: string) {
    return request.get<any, UserInfo[]>(`/api/v1/users/${userId}/following`)
  },

  /** 获取粉丝列表 */
  getFollowers(userId: string) {
    return request.get<any, UserInfo[]>(`/api/v1/users/${userId}/followers`)
  },
}

/** 关注 API */
export const followApi = {
  follow(userId: string) {
    return request.post(`/api/v1/follows/${userId}`)
  },
  unfollow(userId: string) {
    return request.delete(`/api/v1/follows/${userId}`)
  },
  isFollowing(userId: string) {
    return request.get<any, boolean>(`/api/v1/follows/${userId}/status`)
  },
}
