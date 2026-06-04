<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { videoApi, type VideoInfo } from '@/api/video'

const router = useRouter()
const route = useRoute()
const videos = ref<VideoInfo[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const loading = ref(false)
const keyword = ref((route.query.keyword as string) || '')
const tab = ref<'recommend' | 'hot'>('recommend')

onMounted(() => {
  fetchVideos()
})

// 搜索关键词变化时重新拉取
watch(() => route.query.keyword, (val) => {
  keyword.value = (val as string) || ''
  page.value = 1
  tab.value = 'recommend'
  fetchVideos()
})

// Tab 切换
watch(tab, () => {
  page.value = 1
  fetchVideos()
})

async function fetchVideos() {
  loading.value = true
  try {
    if (tab.value === 'hot') {
      videos.value = await videoApi.getHot(10)
      total.value = videos.value.length
    } else {
      const kw = keyword.value || undefined
      const result = await videoApi.list(page.value, size, kw)
      videos.value = result.records
      total.value = result.total
    }
  } finally {
    loading.value = false
  }
}

function goToDetail(id: string) {
  router.push(`/video/${id}`)
}

/** 格式化播放量 */
function formatCount(count: number): string {
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + '万'
  }
  return String(count)
}

/** 格式化时间 */
function formatTime(time: string): string {
  if (!time) return ''
  const d = new Date(time)
  return d.toLocaleDateString('zh-CN')
}
</script>

<template>
  <div class="home-feed">
    <div class="feed-header">
      <div class="feed-tabs" v-if="!keyword">
        <span
          :class="{ active: tab === 'recommend' }"
          @click="tab = 'recommend'"
        >推荐</span>
        <span
          :class="{ active: tab === 'hot' }"
          @click="tab = 'hot'"
        >🔥 热门</span>
      </div>
      <h2 v-if="keyword">搜索：{{ keyword }}</h2>
      <span class="feed-count">共 {{ total }} 个视频</span>
    </div>

    <div class="video-grid" v-loading="loading">
      <div
        v-for="video in videos"
        :key="video.id"
        class="video-card"
        @click="goToDetail(video.id)"
      >
        <div class="card-cover">
          <img v-if="video.coverUrl" :src="video.coverUrl" alt="" />
          <div v-else class="cover-placeholder">
            <span>暂无封面</span>
          </div>
          <span class="card-duration" v-if="video.duration > 0">
            {{ Math.floor(video.duration / 60) }}:{{ String(Math.floor(video.duration % 60)).padStart(2, '0') }}
          </span>
          <span class="card-views">{{ formatCount(video.viewCount) }} 播放</span>
        </div>
        <div class="card-info">
          <h3 class="card-title">{{ video.title }}</h3>
          <div class="card-meta">
            <span class="card-author">{{ video.author?.nickname || '未知' }}</span>
            <span class="card-date">{{ formatTime(video.createTime) }}</span>
          </div>
          <div class="card-stats">
            <span>{{ formatCount(video.likeCount) }} 赞</span>
          </div>
        </div>
      </div>
    </div>

    <div class="feed-empty" v-if="!loading && videos.length === 0">
      <p>还没有视频，快去上传第一个吧！</p>
    </div>
  </div>
</template>

<style scoped>
.home-feed {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 16px;
}

.feed-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 20px;
}

.feed-header h2 {
  font-size: 20px;
}

.feed-tabs {
  display: flex;
  gap: 20px;
}

.feed-tabs span {
  font-size: 16px;
  color: #909399;
  cursor: pointer;
  padding-bottom: 4px;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}

.feed-tabs span.active {
  color: #303133;
  font-weight: 600;
  border-bottom-color: #fe2c55;
}

.feed-count {
  color: #909399;
  font-size: 14px;
}

.video-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.video-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.video-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.card-cover {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  background: #e4e7ed;
  overflow: hidden;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  font-size: 14px;
}

.card-duration {
  position: absolute;
  bottom: 4px;
  right: 4px;
  background: rgba(0, 0, 0, 0.7);
  color: #fff;
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.card-views {
  position: absolute;
  bottom: 4px;
  left: 4px;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.card-info {
  padding: 12px;
}

.card-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

.card-stats {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.feed-empty {
  text-align: center;
  padding: 80px 0;
  color: #909399;
}
</style>
