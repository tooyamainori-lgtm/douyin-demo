import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import router from '@/router'

/** 用户信息（与 api/user.ts 保持一致，避免循环引用） */
export interface UserInfo {
  id: string
  username: string
  nickname: string
  avatarUrl: string | null
  token?: string
}

/**
 * 用户状态仓库
 */
export const useUserStore = defineStore(
  'user',
  () => {
    const user = ref<UserInfo | null>(null)
    const token = ref<string | null>(null)

    /** 是否已登录 */
    const isLogin = computed(() => !!token.value)

    /** 保存登录信息 */
    function setLogin(userInfo: UserInfo, jwtToken: string) {
      user.value = userInfo
      token.value = jwtToken
    }

    /** 刷新时恢复登录态 — 从后端获取最新用户信息 */
    async function restoreLogin() {
      if (!token.value) return
      try {
        // 动态 import 避免循环依赖
        const { userApi } = await import('@/api/user')
        const info = await userApi.getMe()
        user.value = {
          id: info.id,
          username: info.username,
          nickname: info.nickname,
          avatarUrl: info.avatarUrl,
        }
      } catch {
        // Token 过期或无效，清除登录态
        logout()
      }
    }

    /** 退出登录 */
    function logout() {
      user.value = null
      token.value = null
      router.push('/login')
    }

    return { user, token, isLogin, setLogin, restoreLogin, logout }
  },
  {
    persist: {
      key: 'douyin-user',
      storage: sessionStorage,
    },
  },
)
