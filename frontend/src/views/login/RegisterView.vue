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
  confirmPassword: '',
})

/** 加载状态 */
const loading = ref(false)

/** 注册 */
async function handleRegister() {
  // 简单校验
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (form.value.username.length < 3 || form.value.username.length > 30) {
    ElMessage.warning('用户名长度为 3-30 位')
    return
  }
  if (form.value.password.length < 6 || form.value.password.length > 32) {
    ElMessage.warning('密码长度为 6-32 位')
    return
  }
  if (form.value.password !== form.value.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }

  loading.value = true
  try {
    const user = await userApi.register({
      username: form.value.username,
      password: form.value.password,
    })
    userStore.setLogin(
      { id: user.id, username: user.username, nickname: user.nickname, avatarUrl: user.avatarUrl, bio: user.bio, gender: user.gender, birthday: user.birthday },
      user.token!,
    )
    ElMessage.success('注册成功')
    router.push('/')
  } catch {
    // 错误已在 request 拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-container">
    <div class="register-card">
      <h2>注册 Douyin Demo</h2>
      <el-form @submit.prevent="handleRegister" label-position="top">
        <el-form-item label="用户名">
          <el-input
            v-model="form.username"
            placeholder="3-30位字母、数字或下划线"
            size="large"
            maxlength="30"
          />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="6-32位密码"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="再次输入密码"
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
            注册
          </el-button>
        </el-form-item>
      </el-form>
      <p class="login-link">
        已有账号？
        <router-link to="/login">立即登录</router-link>
      </p>
    </div>
  </div>
</template>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f5f7fa;
}

.register-card {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.register-card h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
}

.login-link {
  text-align: center;
  color: #909399;
  font-size: 14px;
}

.login-link a {
  color: #409eff;
}
</style>
