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
          <div class="cover-placeholder">▶</div>
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
  max-width: 800px;
  margin: 0 auto;
  padding: 20px 16px;
}

.favorites-page h2 {
  margin-bottom: 20px;
}

.video-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 12px;
}

.video-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s;
}

.video-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.card-cover {
  aspect-ratio: 16/9;
  background: #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 28px;
}

.card-info {
  padding: 10px;
}

.card-title {
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 4px;
}

.card-meta {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: #909399;
}

.empty {
  text-align: center;
  padding: 80px 0;
  color: #909399;
}

.empty p {
  margin-bottom: 16px;
}
</style>
