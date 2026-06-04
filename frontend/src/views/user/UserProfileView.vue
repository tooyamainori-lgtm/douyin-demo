<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi, type UserProfile } from '@/api/user'
import { videoApi, type VideoInfo } from '@/api/video'

const route = useRoute()
const router = useRouter()
const profile = ref<UserProfile | null>(null)
const videos = ref<VideoInfo[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    const userId = route.params.id as string
    profile.value = await userApi.getUserProfile(userId)
    fetchVideos(userId)
  } catch {
    ElMessage.error('加载用户信息失败')
  } finally {
    loading.value = false
  }
})

async function fetchVideos(userId: string) {
  try {
    // 直接调用 videoApi，复用现有列表接口加用户过滤
    const result = await videoApi.listByUser(userId, 1, 12)
    videos.value = result.records
  } catch { /* ignore */ }
}

function goToDetail(id: string) {
  router.push(`/video/${id}`)
}

function formatCount(n: number) {
  if (n >= 10000) return (n / 10000).toFixed(1) + '万'
  return String(n)
}
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

      <!-- 作品列表 -->
      <div class="user-videos" v-if="videos.length > 0">
        <h3>作品 ({{ videos.length }})</h3>
        <div class="video-mini-grid">
          <div
            v-for="v in videos"
            :key="v.id"
            class="video-mini-card"
            @click="goToDetail(v.id)"
          >
            <div class="mini-cover">
              <div class="cover-placeholder">▶</div>
            </div>
            <div class="mini-info">
              <span class="mini-title">{{ v.title }}</span>
              <span class="mini-views">{{ formatCount(v.viewCount) }} 播放</span>
            </div>
          </div>
        </div>
      </div>
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

.user-videos {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
  text-align: left;
}

.user-videos h3 {
  font-size: 16px;
  margin-bottom: 12px;
}

.video-mini-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 12px;
}

.video-mini-card {
  cursor: pointer;
  border-radius: 6px;
  overflow: hidden;
  background: #f5f7fa;
}

.mini-cover {
  aspect-ratio: 16 / 9;
  background: #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
}

.mini-info {
  padding: 8px;
}

.mini-title {
  display: block;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mini-views {
  font-size: 11px;
  color: #909399;
}
</style>
