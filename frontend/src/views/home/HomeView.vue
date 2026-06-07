<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { videoApi, type VideoInfo } from '@/api/video'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const videos = ref<VideoInfo[]>([])
const total = ref(0)
const page = ref(1)
const size = 10
const loading = ref(false)
const noMore = ref(false)
const keyword = ref((route.query.keyword as string) || '')
const tab = ref<'recommend' | 'following' | 'hot'>('recommend')
const tabs = [
  { key: 'recommend', label: '推荐', icon: '🏠' },
  { key: 'following', label: '关注', icon: '👥' },
  { key: 'hot', label: '热门', icon: '🔥' },
]

let sentinel: HTMLElement | null = null

onMounted(() => {
  fetchVideos()
  nextTick(setupScroll)
})

onUnmounted(() => {
  if (sentinel) sentinel.remove()
})

// 搜索关键词变化
watch(() => route.query.keyword, (val) => {
  keyword.value = (val as string) || ''
  page.value = 1
  tab.value = 'recommend'
  fetchVideos()
})

// Tab 切换
watch(tab, () => {
  page.value = 1
  videos.value = []
  noMore.value = false
  fetchVideos()
})

// 登录态变化刷新关注 Tab
watch(() => userStore.token, () => {
  if (tab.value === 'following') {
    page.value = 1
    videos.value = []
    noMore.value = false
    fetchVideos()
  }
})

async function fetchVideos() {
  loading.value = true
  try {
    if (tab.value === 'hot') {
      const data = await videoApi.getHot(20)
      videos.value = data
      total.value = data.length
      noMore.value = true
    } else if (tab.value === 'following') {
      if (!userStore.token) {
        videos.value = []
        total.value = 0
        noMore.value = true
        return
      }
      const result = await videoApi.getFollowingFeed(page.value, size)
      videos.value = page.value === 1 ? result.records : [...videos.value, ...result.records]
      total.value = result.total
      noMore.value = videos.value.length >= result.total
    } else {
      const kw = keyword.value || undefined
      const result = await videoApi.list(page.value, size, kw)
      videos.value = page.value === 1 ? result.records : [...videos.value, ...result.records]
      total.value = result.total
      noMore.value = videos.value.length >= result.total
    }
  } finally {
    loading.value = false
  }
}

/** 无限滚动 — IntersectionObserver 监听底部哨兵元素 */
function setupScroll() {
  sentinel = document.createElement('div')
  sentinel.className = 'scroll-sentinel'
  document.querySelector('.home-feed')?.appendChild(sentinel)

  const observer = new IntersectionObserver((entries) => {
    if (entries[0].isIntersecting && !loading.value && !noMore.value) {
      page.value++
      fetchVideos()
    }
  }, { rootMargin: '200px' })
  observer.observe(sentinel)
}

function switchTab(t: 'recommend' | 'following' | 'hot') {
  if (t === 'following' && !userStore.token) {
    router.push('/login')
    return
  }
  tab.value = t
}

function goToDetail(id: string) {
  router.push(`/video/${id}`)
}

function formatCount(count: number): string {
  if (count >= 10000) return (count / 10000).toFixed(1) + '万'
  return String(count)
}

</script>

<template>
  <div class="home-feed">
    <!-- Tab 栏 -->
    <div class="feed-header" v-if="!keyword">
      <div class="feed-tabs">
        <button
          v-for="t in tabs" :key="t.key"
          :class="['tab-btn', { active: tab === t.key }]"
          @click="switchTab(t.key as any)"
        >
          <span class="tab-icon">{{ t.icon }}</span>
          <span class="tab-label">{{ t.label }}</span>
        </button>
      </div>
      <span class="feed-count">{{ total }} 个作品</span>
    </div>

    <h2 class="search-title" v-if="keyword">🔍 「{{ keyword }}」的搜索结果</h2>

    <!-- 视频网格 -->
    <div class="video-grid" v-loading="loading && videos.length === 0">
      <article
        v-for="(video, i) in videos"
        :key="video.id"
        class="video-card"
        :style="{ animationDelay: `${i * 40}ms` }"
        @click="goToDetail(video.id)"
      >
        <div class="card-cover">
          <img v-if="video.coverUrl" :src="video.coverUrl" alt="" loading="lazy" />
          <div v-else class="cover-placeholder">
            <span class="cover-icon">🎬</span>
          </div>
          <span class="card-duration" v-if="video.duration > 0">
            {{ Math.floor(video.duration / 60) }}:{{ String(Math.floor(video.duration % 60)).padStart(2, '0') }}
          </span>
          <div class="card-overlay">
            <span class="play-icon">▶</span>
          </div>
        </div>
        <div class="card-body">
          <h3 class="card-title">{{ video.title }}</h3>
          <div class="card-footer">
            <div class="card-author">
              <span class="author-avatar">{{ video.author?.nickname?.charAt(0) || '?' }}</span>
              <span class="author-name">{{ video.author?.nickname || '未知' }}</span>
            </div>
            <div class="card-stats">
              <span class="stat">❤ {{ formatCount(video.likeCount) }}</span>
            </div>
          </div>
        </div>
      </article>
    </div>

    <div class="feed-loading" v-if="loading && videos.length > 0">
      <span class="loading-dot"></span> 正在加载更多...
    </div>

    <div class="feed-empty" v-if="!loading && videos.length === 0">
      <div class="empty-illustration">📭</div>
      <p v-if="tab === 'following'">还没有关注任何人</p>
      <p v-else-if="keyword">没有找到相关视频</p>
      <p v-else>还没有作品，去发布第一个吧</p>
      <button v-if="tab === 'following'" class="btn-primary" @click="switchTab('recommend')">去看看推荐</button>
    </div>
  </div>
</template>

<style scoped>
.home-feed {
  max-width: 1280px;
  margin: 0 auto;
  padding: 28px 20px;
}

/* Header / Tabs */
.feed-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
}

.search-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 24px;
  color: var(--color-text);
}

.feed-tabs {
  display: flex;
  gap: 6px;
  background: var(--color-bg-alt);
  padding: 4px;
  border-radius: var(--radius-lg);
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  border: none;
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--color-text-secondary);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-normal);
}

.tab-btn:hover { color: var(--color-text); }
.tab-btn.active {
  background: var(--color-surface);
  color: var(--color-primary);
  font-weight: 600;
  box-shadow: var(--shadow-sm);
}

.tab-icon { font-size: 15px; }

.feed-count {
  font-size: 13px;
  color: var(--color-text-muted);
}

/* Grid */
.video-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 22px;
}

/* Card */
.video-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-slow);
  animation: cardFadeIn 0.5s ease both;
  border: 1px solid var(--color-border);
}

@keyframes cardFadeIn {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.video-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: transparent;
}

.video-card:hover .card-overlay { opacity: 1; }
.video-card:hover .card-cover img { transform: scale(1.05); }

/* Cover */
.card-cover {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 10;
  background: linear-gradient(135deg, #f0ebe3, #e8e3db);
  overflow: hidden;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-slow);
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-icon { font-size: 40px; opacity: 0.3; }

.card-duration {
  position: absolute;
  bottom: 8px;
  right: 8px;
  background: rgba(0,0,0,0.65);
  backdrop-filter: blur(4px);
  color: #fff;
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.card-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.play-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: rgba(255,255,255,0.92);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  backdrop-filter: blur(8px);
}

/* Card Body */
.card-body { padding: 14px 16px 16px; }

.card-title {
  font-size: 15px;
  font-weight: 600;
  line-height: 1.4;
  margin-bottom: 12px;
  color: var(--color-text);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-author {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-avatar {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-warm));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 600;
}

.author-name { font-size: 13px; color: var(--color-text-secondary); }

.card-stats { display: flex; gap: 8px; }

.stat {
  font-size: 12px;
  color: var(--color-text-muted);
}

/* Empty & Loading */
.feed-empty {
  text-align: center;
  padding: 80px 20px;
  color: var(--color-text-muted);
}

.feed-empty p { margin: 12px 0 20px; font-size: 15px; }
.empty-illustration { font-size: 56px; margin-bottom: 12px; }

.feed-loading {
  text-align: center;
  padding: 32px 0;
  color: var(--color-text-muted);
  font-size: 13px;
}

.loading-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-primary);
  animation: pulse 1s ease infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 1; }
}

.btn-primary {
  padding: 10px 24px;
  border: none;
  border-radius: var(--radius-lg);
  background: var(--color-primary);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn-primary:hover { background: var(--color-primary-light); }

.scroll-sentinel { height: 1px; }
</style>
