<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi, followApi, type UserProfile } from '@/api/user'
import { videoApi, type VideoInfo } from '@/api/video'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const route = useRoute()
const router = useRouter()
const profile = ref<UserProfile | null>(null)
const videos = ref<VideoInfo[]>([])
const loading = ref(true)

async function loadProfile(userId: string) {
  loading.value = true
  try {
    profile.value = await userApi.getUserProfile(userId)
    await fetchVideos(userId)
  } catch {
    ElMessage.error('加载用户信息失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => loadProfile(route.params.id as string))

// 路由参数变化时重新加载（解决同组件切换不刷新的问题）
watch(() => route.params.id, (newId) => {
  if (newId) loadProfile(newId as string)
})

async function fetchVideos(userId: string) {
  try {
    // 直接调用 videoApi，复用现有列表接口加用户过滤
    const result = await videoApi.listByUser(userId, 1, 12)
    videos.value = result.records
  } catch (e) { console.error('加载用户作品失败', e) }
}

function goToDetail(id: string) {
  router.push(`/video/${id}`)
}

const followLoading = ref(false)

async function toggleFollow() {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (!profile.value) return
  followLoading.value = true
  try {
    if (profile.value.isFollowing) {
      await followApi.unfollow(profile.value.id)
      profile.value.isFollowing = false
      profile.value.fansCount--
      ElMessage.success('已取消关注')
    } else {
      await followApi.follow(profile.value.id)
      profile.value.isFollowing = true
      profile.value.fansCount++
      ElMessage.success('关注成功')
    }
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '操作失败')
  } finally {
    followLoading.value = false
  }
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
          <img v-if="profile?.avatarUrl" :src="profile.avatarUrl" class="avatar-img" />
          <span v-else class="avatar-text">{{ profile?.nickname?.charAt(0) || '?' }}</span>
        </div>
      </div>
      <h2 class="nickname">{{ profile?.nickname || '未知用户' }}</h2>
      <p class="username">@{{ profile?.username }}</p>
      <el-button
        v-if="userStore.isLogin && userStore.user?.id !== profile?.id"
        :type="profile?.isFollowing ? 'default' : 'primary'"
        :loading="followLoading"
        size="small"
        @click="toggleFollow"
        class="follow-btn"
      >
        {{ profile?.isFollowing ? '已关注' : '+ 关注' }}
      </el-button>
      <div class="stats">
        <router-link :to="`/user/${profile?.id}/follows?tab=following`" class="stat-item">
          <span class="stat-value">{{ profile?.followCount || 0 }}</span>
          <span class="stat-label">关注</span>
        </router-link>
        <router-link :to="`/user/${profile?.id}/follows?tab=followers`" class="stat-item">
          <span class="stat-value">{{ profile?.fansCount || 0 }}</span>
          <span class="stat-label">粉丝</span>
        </router-link>
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
      <router-link
        v-if="userStore.isLogin && userStore.user?.id === profile?.id"
        to="/profile/edit"
        class="edit-link"
      >编辑资料</router-link>
      <p class="create-time" v-if="profile?.createTime">
        {{ new Date(profile.createTime).toLocaleDateString('zh-CN') }} 加入
      </p>

      <!-- 作品列表 -->
      <div class="user-videos">
        <h3>作品 ({{ videos.length }})</h3>
        <div class="video-mini-grid" v-if="videos.length > 0">
          <div
            v-for="v in videos"
            :key="v.id"
            class="video-mini-card"
            @click="goToDetail(v.id)"
          >
            <div class="mini-cover">
              <img v-if="v.coverUrl" :src="v.coverUrl" class="mini-cover-img" alt="" />
              <div v-else class="cover-placeholder">▶</div>
            </div>
            <div class="mini-info">
              <span class="mini-title">{{ v.title }}</span>
              <span class="mini-views">{{ formatCount(v.viewCount) }} 播放</span>
            </div>
          </div>
        </div>
        <p class="no-videos" v-else>暂无作品</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-container {
  max-width: 640px;
  margin: 24px auto;
  padding: 0 20px;
}

.profile-card {
  background: transparent;
  border-radius: 0;
  padding: 20px 0;
  text-align: center;
  box-shadow: none;
}

/* 头部信息区 */
.avatar-section { margin-bottom: 12px; }

.avatar {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-warm));
  color: #fff;
  font-size: 34px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(254,44,85,0.2);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-text {
  color: #fff;
  font-size: 34px;
  font-weight: bold;
}

.nickname {
  font-size: 22px;
  font-weight: 700;
  margin: 12px 0 2px;
  color: var(--color-text);
}

.username {
  color: var(--color-text-muted);
  font-size: 14px;
  margin-bottom: 16px;
}

.follow-btn {
  margin-bottom: 20px;
  border-radius: var(--radius-lg) !important;
  font-weight: 600 !important;
}

/* 数据统计 — 半透明胶囊背景 */
.stats {
  display: flex;
  justify-content: center;
  gap: 32px;
  margin-bottom: 20px;
  padding: 16px 24px;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
}

.stat-item {
  display: flex;
  flex-direction: column;
  text-decoration: none;
  color: inherit;
  cursor: pointer;
  transition: transform var(--transition-fast);
}

.stat-item:hover { transform: translateY(-1px); }

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text);
}

.stat-label {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-top: 2px;
}

.bio {
  color: var(--color-text-secondary);
  font-size: 14px;
  margin: 8px 0;
  padding: 0 20px;
}

.edit-link {
  display: inline-block;
  margin: 8px 0;
  font-size: 13px;
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
}

.create-time {
  color: var(--color-text-muted);
  font-size: 12px;
  margin-top: 4px;
}

/* 作品区 — 白色卡片 */
.user-videos {
  margin-top: 28px;
  padding: 20px 24px;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  text-align: left;
}

.user-videos h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 14px;
  color: var(--color-text);
}

.video-mini-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(170px, 1fr));
  gap: 14px;
}

.video-mini-card {
  cursor: pointer;
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--color-bg-alt);
  transition: all var(--transition-normal);
}

.video-mini-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.mini-cover {
  aspect-ratio: 16 / 9;
  background: linear-gradient(135deg, #f0ebe3, #e8e3db);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-muted);
  font-size: 20px;
  overflow: hidden;
}

.mini-cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.mini-info {
  padding: 10px;
}

.mini-title {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mini-views {
  font-size: 11px;
  color: var(--color-text-muted);
  margin-top: 2px;
}

.no-videos {
  color: var(--color-text-muted);
  font-size: 14px;
  text-align: center;
  padding: 20px 0;
}
</style>
