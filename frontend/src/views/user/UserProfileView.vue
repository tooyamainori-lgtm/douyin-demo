<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi, type UserProfile } from '@/api/user'

const route = useRoute()
const profile = ref<UserProfile | null>(null)
const loading = ref(true)

onMounted(async () => {
  try {
    const userId = route.params.id as string
    profile.value = await userApi.getUserProfile(userId)
  } catch {
    ElMessage.error('加载用户信息失败')
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="profile-container">
    <div class="profile-card" v-loading="loading">
      <div class="avatar-section">
        <div class="avatar">
          {{ profile?.nickname?.charAt(0) || '?' }}
        </div>
      </div>
      <h2 class="nickname">{{ profile?.nickname || '未知用户' }}</h2>
      <p class="username">@{{ profile?.username }}</p>
      <div class="stats">
        <div class="stat-item">
          <span class="stat-value">{{ profile?.followCount || 0 }}</span>
          <span class="stat-label">关注</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ profile?.fansCount || 0 }}</span>
          <span class="stat-label">粉丝</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ profile?.videoCount || 0 }}</span>
          <span class="stat-label">视频</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ profile?.likeCount || 0 }}</span>
          <span class="stat-label">获赞</span>
        </div>
      </div>
      <p class="bio" v-if="profile?.bio">{{ profile.bio }}</p>
      <p class="create-time" v-if="profile?.createTime">
        {{ new Date(profile.createTime).toLocaleDateString('zh-CN') }} 加入
      </p>
    </div>
  </div>
</template>

<style scoped>
.profile-container {
  max-width: 600px;
  margin: 40px auto;
  padding: 0 16px;
}

.profile-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.avatar-section {
  margin-bottom: 16px;
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #fe2c55, #25f4ee);
  color: #fff;
  font-size: 32px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
}

.nickname {
  font-size: 20px;
  margin-bottom: 4px;
}

.username {
  color: #909399;
  font-size: 14px;
  margin-bottom: 24px;
}

.stats {
  display: flex;
  justify-content: center;
  gap: 40px;
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 18px;
  font-weight: bold;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.bio {
  color: #606266;
  margin-bottom: 12px;
}

.create-time {
  color: #c0c4cc;
  font-size: 12px;
}
</style>
