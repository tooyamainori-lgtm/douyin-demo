<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { videoApi, commentApi, type VideoInfo, type CommentInfo } from '@/api/video'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const video = ref<VideoInfo | null>(null)
const loading = ref(true)
const likeLoading = ref(false)
const hasRecordedView = ref(false)

// 评论
const comments = ref<CommentInfo[]>([])
const commentText = ref('')
const replyTo = ref<{ id: string; nickname: string } | null>(null)
const commentLoading = ref(false)

onMounted(async () => {
  const id = route.params.id as string
  try {
    video.value = await videoApi.getDetail(id)
    fetchComments()
  } catch {
    ElMessage.error('视频不存在')
    router.push('/')
  } finally {
    loading.value = false
  }
})

/** 获取评论列表 */
async function fetchComments() {
  if (!video.value) return
  try {
    comments.value = await commentApi.list(video.value.id)
  } catch { /* ignore */ }
}

/** 发表评论 / 回复 */
async function sendComment() {
  if (!commentText.value.trim() || !video.value) return
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  commentLoading.value = true
  try {
    await commentApi.publish(video.value.id, {
      videoId: video.value.id,
      content: commentText.value.trim(),
      parentId: replyTo.value?.id,
      replyUserId: replyTo.value?.id,
    })
    commentText.value = ''
    replyTo.value = null
    ElMessage.success('评论成功')
    fetchComments()
  } finally {
    commentLoading.value = false
  }
}

/** 回复评论 */
function startReply(comment: CommentInfo) {
  replyTo.value = { id: comment.id, nickname: comment.user.nickname }
  commentText.value = ''
}

/** 取消回复 */
function cancelReply() {
  replyTo.value = null
  commentText.value = ''
}

/** 删除评论 */
async function deleteComment(commentId: string) {
  try {
    await commentApi.delete(commentId)
    ElMessage.success('已删除')
    fetchComments()
  } catch { /* ignore */ }
}

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

        <!-- 评论区 -->
        <div class="comment-section">
          <h3>评论 ({{ comments.length }})</h3>

          <!-- 评论输入框 -->
          <div class="comment-input">
            <p v-if="replyTo" class="reply-hint">
              回复 <strong>{{ replyTo.nickname }}</strong>
              <el-button type="danger" link size="small" @click="cancelReply">取消</el-button>
            </p>
            <el-input
              v-model="commentText"
              :placeholder="replyTo ? `回复 ${replyTo.nickname}...` : '发表评论'"
              maxlength="500"
              show-word-limit
              type="textarea"
              :rows="2"
            />
            <el-button
              type="primary"
              size="small"
              style="margin-top: 8px"
              :loading="commentLoading"
              @click="sendComment"
            >
              {{ replyTo ? '回复' : '发表' }}
            </el-button>
          </div>

          <!-- 评论列表 -->
          <div class="comment-list" v-if="comments.length > 0">
            <div v-for="c in comments" :key="c.id" class="comment-item">
              <div class="comment-avatar">{{ c.user.nickname?.charAt(0) || '?' }}</div>
              <div class="comment-body">
                <div class="comment-header">
                  <span class="comment-nickname">{{ c.user.nickname }}</span>
                  <span class="comment-time">{{ c.createTime?.slice(0, 10) }}</span>
                </div>
                <p class="comment-content">{{ c.content }}</p>
                <div class="comment-actions">
                  <el-button type="primary" link size="small" @click="startReply(c)">回复</el-button>
                  <el-button
                    v-if="userStore.user?.id === c.user.id"
                    type="danger"
                    link
                    size="small"
                    @click="deleteComment(c.id)"
                  >
                    删除
                  </el-button>
                </div>

                <!-- 回复列表 -->
                <div class="replies" v-if="c.replies?.length">
                  <div v-for="r in c.replies" :key="r.id" class="reply-item">
                    <span class="reply-avatar">{{ r.user.nickname?.charAt(0) || '?' }}</span>
                    <div>
                      <span class="reply-nickname">{{ r.user.nickname }}</span>
                      <span v-if="r.replyNickname" class="reply-to"> 回复 @{{ r.replyNickname }}</span>
                      ：{{ r.content }}
                      <span class="reply-time">{{ r.createTime?.slice(0, 10) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <p v-else class="no-comment">暂无评论，来发表第一条评论吧</p>
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

.comment-section {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.comment-section h3 {
  font-size: 16px;
  margin-bottom: 16px;
}

.comment-input {
  margin-bottom: 20px;
}

.reply-hint {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}

.comment-item {
  display: flex;
  gap: 10px;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
}

.comment-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #fe2c55, #25f4ee);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
  flex-shrink: 0;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-header {
  margin-bottom: 4px;
}

.comment-nickname {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  margin-right: 8px;
}

.comment-time {
  font-size: 12px;
  color: #c0c4cc;
}

.comment-content {
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
}

.comment-actions {
  display: flex;
  gap: 8px;
}

.replies {
  margin-top: 10px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 6px;
}

.reply-item {
  display: flex;
  gap: 8px;
  padding: 4px 0;
  font-size: 13px;
  color: #606266;
}

.reply-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #fe2c55, #25f4ee);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  flex-shrink: 0;
}

.reply-nickname {
  color: #409eff;
  font-weight: 500;
}

.reply-to {
  color: #909399;
}

.reply-time {
  font-size: 11px;
  color: #c0c4cc;
  margin-left: 8px;
}

.no-comment {
  text-align: center;
  color: #c0c4cc;
  padding: 40px 0;
}
</style>
