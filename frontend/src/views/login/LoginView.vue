<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi } from '@/api/user'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

/** 表单数据 */
const form = ref({
  username: '',
  password: '',
})

/** 加载状态 */
const loading = ref(false)

/** 登录 */
async function handleLogin() {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    const user = await userApi.login(form.value)
    userStore.setLogin(
      { id: user.id, username: user.username, nickname: user.nickname, avatarUrl: user.avatarUrl, bio: user.bio, gender: user.gender, birthday: user.birthday },
      user.token!,
    )
    ElMessage.success('登录成功')
    router.push('/')
  } catch {
    // 错误已在 request 拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <h2>登录 Douyin Demo</h2>
      <el-form @submit.prevent="handleLogin" label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            native-type="submit"
            style="width: 100%"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      <p class="register-link">
        还没有账号？
        <router-link to="/register">立即注册</router-link>
      </p>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(160deg, #FAF7F2 0%, #F5F0EB 40%, #FFF0F3 100%);
}

.login-card {
  width: 400px;
  padding: 44px 40px;
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--color-border);
}

.login-card h2 {
  text-align: center;
  margin-bottom: 8px;
  color: var(--color-text);
  font-size: 24px;
  font-weight: 700;
}

.login-card :deep(.el-button--primary) {
  border-radius: var(--radius-lg) !important;
  font-weight: 600 !important;
  height: 44px !important;
}

.login-card :deep(.el-input__wrapper) {
  border-radius: var(--radius-sm) !important;
}

.register-link {
  text-align: center;
  color: var(--color-text-muted);
  font-size: 14px;
  margin-top: 20px;
}

.register-link a {
  color: var(--color-primary);
  font-weight: 500;
  text-decoration: none;
}
</style>
