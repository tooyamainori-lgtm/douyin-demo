<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { favoriteApi, type VideoInfo } from '@/api/video'

const router = useRouter()
const videos = ref<VideoInfo[]>([])
const total = ref(0)
const loading = ref(true)

onMounted(async () => {
  try {
    const result = await favoriteApi.list(1, 50)
    videos.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
})

function goToDetail(id: string) {
  router.push(`/video/${id}`)
}

function formatCount(n: number) {
  if (n >= 10000) return (n / 10000).toFixed(1) + '万'
  return String(n)
}

function formatTime(t: string) {
  if (!t) return ''
  return new Date(t).toLocaleDateString('zh-CN')
}
</script>

<template>
  <div class="favorites-page" v-loading="loading">
    <h2>我的收藏 ({{ total }})</h2>

    <div class="video-grid" v-if="videos.length > 0">
      <div
        v-for="v in videos"
        :key="v.id"
        class="video-card"
        @click="goToDetail(v.id)"
      >
        <div class="card-cover">
          <img v-if="v.coverUrl" :src="v.coverUrl" alt="" />
          <div v-else class="cover-placeholder">▶</div>
        </div>
        <div class="card-info">
          <h3 class="card-title">{{ v.title }}</h3>
          <div class="card-meta">
            <span>{{ v.author?.nickname }}</span>
            <span>{{ formatCount(v.viewCount) }} 播放</span>
            <span>{{ formatTime(v.createTime) }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="empty" v-else>
      <p>还没有收藏视频，去首页看看吧</p>
      <el-button type="primary" @click="router.push('/')">去首页</el-button>
    </div>
  </div>
</template>

<style scoped>
.favorites-page {
  max-width: 880px;
  margin: 0 auto;
  padding: 28px 20px;
}

.favorites-page h2 {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 24px;
  color: var(--color-text);
}

.video-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.video-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  border: 1px solid var(--color-border);
  transition: all var(--transition-normal);
}

.video-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-lg);
}

.card-cover {
  aspect-ratio: 16/9;
  background: linear-gradient(135deg, #f0ebe3, #e8e3db);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 28px;
  overflow: hidden;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-info {
  padding: 12px 14px;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 6px;
  color: var(--color-text);
}

.card-meta {
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: var(--color-text-muted);
}

.empty {
  text-align: center;
  padding: 80px 0;
  color: var(--color-text-muted);
}

.empty p {
  margin-bottom: 16px;
  font-size: 15px;
}

.empty :deep(.el-button--primary) {
  border-radius: var(--radius-lg);
  font-weight: 600;
}
</style>
