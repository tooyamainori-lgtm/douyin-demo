<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { historyApi } from '@/api/user'
import type { VideoInfo } from '@/api/video'

const router = useRouter()
const videos = ref<VideoInfo[]>([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const noMore = ref(false)
const size = 12

onMounted(() => fetchHistory())

async function fetchHistory() {
  loading.value = true
  try {
    const result = await historyApi.list(page.value, size)
    videos.value = page.value === 1 ? result.records : [...videos.value, ...result.records]
    total.value = result.total
    noMore.value = videos.value.length >= result.total
  } finally {
    loading.value = false
  }
}

function loadMore() {
  page.value++
  fetchHistory()
}

async function clearAll() {
  try {
    await ElMessageBox.confirm('确定清空所有观看历史？', '确认', { type: 'warning' })
  } catch { return }
  await historyApi.clear()
  videos.value = []
  total.value = 0
  ElMessage.success('已清空')
}

async function removeOne(videoId: string) {
  await historyApi.remove(videoId)
  videos.value = videos.value.filter(v => v.id !== videoId)
  total.value--
  ElMessage.success('已移除')
}

function goToVideo(id: string) {
  router.push(`/video/${id}`)
}

function formatCount(count: number): string {
  if (count >= 10000) return (count / 10000).toFixed(1) + '万'
  return String(count)
}
</script>

<template>
  <div class="history-page">
    <div class="page-header">
      <h2>🕐 观看历史</h2>
      <div class="header-info">
        <span class="history-count">共 {{ total }} 条记录</span>
        <button v-if="videos.length > 0" class="btn-clear" @click="clearAll">清空全部</button>
      </div>
    </div>

    <div class="video-grid" v-loading="loading && videos.length === 0">
      <article v-for="(v, i) in videos" :key="v.id" class="video-card"
               :style="{ animationDelay: `${i * 35}ms` }">
        <div class="card-cover" @click="goToVideo(v.id)">
          <img v-if="v.coverUrl" :src="v.coverUrl" alt="" loading="lazy" />
          <div v-else class="cover-placeholder">🎬</div>
          <span class="card-duration" v-if="v.duration > 0">
            {{ Math.floor(v.duration / 60) }}:{{ String(Math.floor(v.duration % 60)).padStart(2, '0') }}
          </span>
        </div>
        <div class="card-body" @click="goToVideo(v.id)">
          <h3 class="card-title">{{ v.title }}</h3>
          <div class="card-footer">
            <span class="author-name">{{ v.author?.nickname || '未知' }}</span>
            <span class="card-stats">
              <span>👁 {{ formatCount(v.viewCount) }}</span>
              <span>❤ {{ formatCount(v.likeCount) }}</span>
            </span>
          </div>
        </div>
        <button class="btn-remove" @click.stop="removeOne(v.id)" title="移除">✕</button>
      </article>
    </div>

    <div class="load-more" v-if="!loading && !noMore && videos.length > 0">
      <button class="btn-load" @click="loadMore">加载更多</button>
    </div>

    <div class="empty" v-if="!loading && videos.length === 0">
      <span class="empty-icon">📺</span>
      <p>暂无观看记录</p>
      <p class="sub">去发现更多精彩视频吧</p>
      <button class="btn-go" @click="router.push('/')">去发现</button>
    </div>
  </div>
</template>

<style scoped>
.history-page {
  max-width: 1280px;
  margin: 0 auto;
  padding: 28px 20px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-text);
}

.header-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.history-count { font-size: 14px; color: var(--color-text-muted); }

.btn-clear {
  padding: 6px 16px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn-clear:hover { border-color: var(--color-primary); color: var(--color-primary); }

/* Grid */
.video-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 18px;
}

.video-card {
  position: relative;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--color-border);
  animation: cardIn 0.5s ease both;
}

@keyframes cardIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.card-cover {
  position: relative;
  aspect-ratio: 16 / 10;
  background: var(--color-bg-alt);
  cursor: pointer;
  overflow: hidden;
}

.card-cover img { width: 100%; height: 100%; object-fit: cover; }
.cover-placeholder { display: flex; align-items: center; justify-content: center; height: 100%; font-size: 36px; opacity: 0.3; }

.card-duration {
  position: absolute; bottom: 6px; right: 6px;
  background: rgba(0,0,0,0.65); backdrop-filter: blur(4px);
  color: #fff; padding: 2px 6px; border-radius: 5px; font-size: 11px; font-weight: 600;
}

.card-body { padding: 10px 12px 12px; cursor: pointer; }
.card-title { font-size: 14px; font-weight: 600; color: var(--color-text); margin-bottom: 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.card-footer { display: flex; justify-content: space-between; align-items: center; }
.author-name { font-size: 13px; color: var(--color-text-secondary); }
.card-stats { display: flex; gap: 10px; font-size: 12px; color: var(--color-text-muted); }

.btn-remove {
  position: absolute; top: 6px; right: 6px;
  width: 26px; height: 26px; border-radius: 50%;
  border: none; background: rgba(0,0,0,0.5); color: #fff;
  font-size: 12px; cursor: pointer; opacity: 0; transition: opacity var(--transition-fast);
}

.video-card:hover .btn-remove { opacity: 1; }
.btn-remove:hover { background: var(--color-primary); }

/* Load more / empty */
.load-more { text-align: center; margin-top: 28px; }
.btn-load {
  padding: 10px 32px; border: 1.5px solid var(--color-border); border-radius: var(--radius-lg);
  background: var(--color-surface); color: var(--color-text-secondary); font-size: 14px; cursor: pointer; transition: all var(--transition-fast);
}
.btn-load:hover { border-color: var(--color-primary); color: var(--color-primary); }

.empty { text-align: center; padding: 80px 20px; color: var(--color-text-muted); }
.empty-icon { font-size: 56px; display: block; margin-bottom: 12px; }
.empty p { font-size: 15px; margin-bottom: 4px; }
.empty .sub { font-size: 13px; }

.btn-go {
  margin-top: 20px; padding: 10px 28px; border: none; border-radius: var(--radius-lg);
  background: var(--color-primary); color: #fff; font-size: 14px; font-weight: 600; cursor: pointer;
}
</style>
