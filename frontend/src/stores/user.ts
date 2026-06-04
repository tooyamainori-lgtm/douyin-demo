import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/** 用户信息 */
export interface UserInfo {
  id: string
  username: string
  nickname: string
  avatarUrl: string | null
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

    /** 退出登录 */
    function logout() {
      user.value = null
      token.value = null
    }

    return { user, token, isLogin, setLogin, logout }
  },
  {
    // 持久化存储到 localStorage
    persist: {
      key: 'douyin-user',
      storage: localStorage,
    },
  },
)
