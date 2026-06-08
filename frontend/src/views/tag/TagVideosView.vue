<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { tagApi, type TagInfo } from '@/api/tag'
import type { VideoInfo } from '@/api/video'

const route = useRoute()
const router = useRouter()

const tagName = computed(() => route.params.tagName as string)
const allTags = ref<TagInfo[]>([])
const videos = ref<VideoInfo[]>([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const noMore = ref(false)
const size = 12

/** 当前标签的 emoji */
const currentTag = computed(() => allTags.value.find(t => t.name === tagName.value))

onMounted(async () => {
  await Promise.all([fetchTags(), fetchVideos()])
})

async function fetchTags() {
  try {
    allTags.value = await tagApi.list()
  } catch { /* 忽略 */ }
}

async function fetchVideos() {
  loading.value = true
  try {
    const result = await tagApi.getVideos(tagName.value, page.value, size)
    videos.value = page.value === 1 ? result.records : [...videos.value, ...result.records]
    total.value = result.total
    noMore.value = videos.value.length >= result.total
  } finally {
    loading.value = false
  }
}

function loadMore() {
  page.value++
  fetchVideos()
}

function goToVideo(id: string) {
  router.push(`/video/${id}`)
}

function goToTag(name: string) {
  router.push(`/tag/${encodeURIComponent(name)}`)
}

function formatCount(count: number): string {
  if (count >= 10000) return (count / 10000).toFixed(1) + '万'
  return String(count)
}
</script>

<template>
  <div class="tag-page">
    <!-- 标签列表 -->
    <aside class="tag-sidebar">
      <h3 class="sidebar-title">🏷️ 标签分类</h3>
      <div class="tag-list">
        <button
          v-for="tag in allTags"
          :key="tag.name"
          :class="['tag-item', { active: tag.name === tagName }]"
          @click="goToTag(tag.name)"
        >
          <span class="tag-item-icon">{{ tag.icon }}</span>
          <span class="tag-item-name">{{ tag.name }}</span>
          <span class="tag-item-count">{{ tag.videoCount }}</span>
        </button>
      </div>
    </aside>

    <!-- 主内容区 -->
    <section class="tag-main">
      <div class="tag-header">
        <span class="tag-header-icon">{{ currentTag?.icon || '🏷️' }}</span>
        <h2>#{{ tagName }}</h2>
        <span class="tag-header-count">共 {{ total }} 个视频</span>
      </div>

      <div class="video-grid" v-loading="loading && videos.length === 0">
        <article
          v-for="(v, i) in videos"
          :key="v.id"
          class="video-card"
          :style="{ animationDelay: `${i * 40}ms` }"
          @click="goToVideo(v.id)"
        >
          <div class="card-cover">
            <img v-if="v.coverUrl" :src="v.coverUrl" alt="" loading="lazy" />
            <div v-else class="cover-placeholder">🎬</div>
            <span class="card-duration" v-if="v.duration > 0">
              {{ Math.floor(v.duration / 60) }}:{{ String(Math.floor(v.duration % 60)).padStart(2, '0') }}
            </span>
          </div>
          <div class="card-body">
            <h3 class="card-title">{{ v.title }}</h3>
            <div class="card-footer">
              <span class="author-name">{{ v.author?.nickname || '未知' }}</span>
              <span class="card-like">❤ {{ formatCount(v.likeCount) }}</span>
            </div>
          </div>
        </article>
      </div>

      <div class="feed-loading" v-if="loading && videos.length > 0">加载中...</div>

      <div class="load-more" v-if="!loading && !noMore && videos.length > 0">
        <button class="btn-load" @click="loadMore">加载更多</button>
      </div>

      <div class="feed-empty" v-if="!loading && videos.length === 0">
        <span class="empty-icon">📭</span>
        <p>「{{ tagName }}」标签下暂无视频</p>
      </div>
    </section>
  </div>
</template>

<style scoped>
.tag-page {
  max-width: 1280px;
  margin: 0 auto;
  padding: 28px 20px;
  display: flex;
  gap: 28px;
}

/* Sidebar */
.tag-sidebar {
  width: 200px;
  flex-shrink: 0;
}

.sidebar-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 14px;
}

.tag-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.tag-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 14px;
  border: none;
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--color-text-secondary);
  font-size: 14px;
  cursor: pointer;
  transition: all var(--transition-fast);
  text-align: left;
}

.tag-item:hover {
  background: var(--color-bg-alt);
  color: var(--color-text);
}

.tag-item.active {
  background: var(--color-primary-soft);
  color: var(--color-primary);
  font-weight: 600;
}

.tag-item-icon { font-size: 16px; }
.tag-item-name { flex: 1; }
.tag-item-count {
  font-size: 12px;
  color: var(--color-text-muted);
}

/* Main */
.tag-main {
  flex: 1;
  min-width: 0;
}

.tag-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.tag-header-icon { font-size: 28px; }

.tag-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text);
}

.tag-header-count {
  font-size: 14px;
  color: var(--color-text-muted);
}

/* Video grid */
.video-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.video-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  border: 1px solid var(--color-border);
  transition: all var(--transition-slow);
  animation: cardFadeIn 0.5s ease both;
}

@keyframes cardFadeIn {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.video-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.card-cover {
  position: relative;
  aspect-ratio: 16 / 10;
  background: var(--color-bg-alt);
  overflow: hidden;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  opacity: 0.3;
}

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
}

.card-body { padding: 12px 14px 14px; }

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.author-name { font-size: 13px; color: var(--color-text-secondary); }
.card-like { font-size: 12px; color: var(--color-text-muted); }

/* Empty & Loading */
.feed-empty {
  text-align: center;
  padding: 80px 20px;
  color: var(--color-text-muted);
}

.feed-empty .empty-icon { font-size: 56px; display: block; margin-bottom: 12px; }
.feed-empty p { font-size: 15px; }

.feed-loading {
  text-align: center;
  padding: 32px 0;
  color: var(--color-text-muted);
  font-size: 13px;
}

.load-more { text-align: center; margin-top: 28px; }

.btn-load {
  padding: 10px 32px;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  color: var(--color-text-secondary);
  font-size: 14px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn-load:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}
</style>
