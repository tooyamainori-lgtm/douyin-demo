<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { videoApi, type VideoInfo } from '@/api/video'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const video = ref<VideoInfo | null>(null)
const loading = ref(true)
const likeLoading = ref(false)
const hasRecordedView = ref(false)

onMounted(async () => {
  const id = route.params.id as string
  try {
    video.value = await videoApi.getDetail(id)
  } catch {
    ElMessage.error('视频不存在')
    router.push('/')
  } finally {
    loading.value = false
  }
})

/** 视频开始播放时统计播放量（首次） */
function onVideoPlay() {
  if (hasRecordedView.value || !video.value) return
  hasRecordedView.value = true
  videoApi.recordView(video.value.id).catch(() => {})
}

/** 点赞 / 取消点赞 */
async function toggleLike() {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (!video.value) return

  likeLoading.value = true
  try {
    if (video.value.isLiked) {
      await videoApi.unlike(video.value.id)
      video.value.isLiked = false
      video.value.likeCount--
      ElMessage.success('已取消点赞')
    } else {
      await videoApi.like(video.value.id)
      video.value.isLiked = true
      video.value.likeCount++
      ElMessage.success('点赞成功')
    }
  } finally {
    likeLoading.value = false
  }
}

/** 格式化 */
function formatCount(count: number): string {
  if (count >= 10000) return (count / 10000).toFixed(1) + '万'
  return String(count)
}

function formatTime(time: string): string {
  if (!time) return ''
  return new Date(time).toLocaleDateString('zh-CN')
}
</script>

<template>
  <div class="detail-container" v-loading="loading">
    <template v-if="video">
      <!-- 视频播放区 -->
      <div class="player-section">
        <video
          class="video-player"
          :src="video.videoUrl"
          controls
          autoplay
          @play="onVideoPlay"
        >
          您的浏览器不支持视频播放
        </video>
      </div>

      <!-- 视频信息 -->
      <div class="info-section">
        <h1 class="video-title">{{ video.title }}</h1>

        <div class="video-meta">
          <span>{{ formatTime(video.createTime) }} 发布</span>
          <span>{{ formatCount(video.viewCount) }} 播放</span>
          <span>{{ formatCount(video.likeCount) }} 赞</span>
          <span>{{ formatCount(video.commentCount) }} 评论</span>
        </div>

        <!-- 操作按钮 -->
        <div class="action-bar">
          <el-button
            :type="video.isLiked ? 'danger' : 'default'"
            size="large"
            :loading="likeLoading"
            @click="toggleLike"
          >
            {{ video.isLiked ? '❤ 已点赞' : '🤍 点赞' }}
            ({{ formatCount(video.likeCount) }})
          </el-button>
        </div>

        <!-- 作者信息 -->
        <div class="author-section">
          <span class="author-label">UP主：</span>
          <router-link :to="`/user/${video.author.id}`" class="author-name">
            {{ video.author?.nickname || '未知' }}
          </router-link>
        </div>

        <!-- 描述 -->
        <div class="description" v-if="video.description">
          <p>{{ video.description }}</p>
        </div>

        <!-- 标签 -->
        <div class="tags" v-if="video.tags">
          <el-tag
            v-for="tag in video.tags.split(',').filter(Boolean)"
            :key="tag"
            size="small"
            style="margin-right: 8px"
          >
            {{ tag }}
          </el-tag>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.detail-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px 16px;
}

.player-section {
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 20px;
}

.video-player {
  width: 100%;
  max-height: 500px;
  display: block;
}

.info-section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.video-title {
  font-size: 20px;
  margin-bottom: 12px;
}

.video-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #909399;
  margin-bottom: 16px;
}

.action-bar {
  margin-bottom: 16px;
}

.author-section {
  margin-bottom: 12px;
  font-size: 14px;
}

.author-label {
  color: #909399;
}

.author-name {
  color: #409eff;
  text-decoration: none;
}

.description {
  margin-bottom: 12px;
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
  white-space: pre-wrap;
}

.tags {
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}
</style>
