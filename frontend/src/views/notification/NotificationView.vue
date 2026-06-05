<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

interface NotificationItem {
  id: string
  type: string
  content: string
  isRead: boolean
  videoId: string | null
  createTime: string
  fromUser: { id: string; nickname: string; avatarUrl: string | null }
}

const list = ref<NotificationItem[]>([])
const loading = ref(true)
const unread = ref(0)

onMounted(() => fetchAll())

async function fetchAll() {
  loading.value = true
  try {
    const [res, count] = await Promise.all([
      request.get<any, any>('/api/v1/notifications?page=1&size=50'),
      request.get<any, number>('/api/v1/notifications/unread-count'),
    ])
    // request 拦截器已剥掉外层 data，直接就是 Result.data
    list.value = (res as any)?.records || []
    unread.value = (count as any) ?? 0
  } catch (e) { console.error('通知加载失败', e) } finally {
    loading.value = false
  }
}

async function markRead() {
  try {
    await request.put('/api/v1/notifications/read')
    list.value.forEach(n => n.isRead = true)
    unread.value = 0
    ElMessage.success('已全部标记为已读')
  } catch { /* ignore */ }
}

function goToVideo(id: string | null) {
  if (id) window.open(`/video/${id}`, '_self')
}

function goToUser(id: string) {
  window.open(`/user/${id}`, '_self')
}

const typeLabel: Record<string, string> = { like: '点赞', comment: '评论', follow: '关注' }
</script>

<template>
  <div class="notify-page">
    <div class="notify-card" v-loading="loading">
      <div class="notify-header">
        <h2>消息中心</h2>
        <el-button v-if="unread > 0" size="small" text @click="markRead">全部已读 ({{ unread }})</el-button>
      </div>
      <div v-if="list.length === 0" class="empty">暂无消息</div>
      <div
        v-for="n in list" :key="n.id"
        :class="['notify-item', { unread: !n.isRead }]"
        :style="{ cursor: n.videoId ? 'pointer' : 'default' }"
      >
        <span class="notify-dot" v-if="!n.isRead"></span>
        <span
          v-if="n.fromUser"
          class="notify-user"
          @click="goToUser(n.fromUser.id)"
        >
          <img v-if="n.fromUser.avatarUrl" :src="n.fromUser.avatarUrl" class="notify-avatar" />
          <span v-else class="notify-avatar-placeholder">{{ n.fromUser.nickname?.charAt(0) }}</span>
          {{ n.fromUser.nickname }}
        </span>
        <span class="notify-content" @click="goToVideo(n.videoId)">
          {{ typeLabel[n.type] || n.type }}了你的{{ n.videoId ? '视频' : '' }}
        </span>
        <span class="notify-time">{{ n.createTime?.slice(0, 10) }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.notify-page { max-width: 520px; margin: 40px auto; padding: 0 16px; }
.notify-card { background: #fff; border-radius: 8px; padding: 24px; }
.notify-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.notify-header h2 { margin: 0; font-size: 18px; }
.empty { color: #c0c4cc; text-align: center; padding: 32px 0; }
.notify-item { display: flex; align-items: center; gap: 10px; padding: 10px 0; border-bottom: 1px solid #f5f5f5; }
.notify-item.unread { background: #f0f7ff; margin: 0 -24px; padding: 10px 24px; }
.notify-dot { width: 6px; height: 6px; border-radius: 50%; background: #fe2c55; flex-shrink: 0; }
.notify-user { display: flex; align-items: center; gap: 6px; font-size: 13px; color: #303133; cursor: pointer; flex-shrink: 0; min-width: 80px; }
.notify-avatar { width: 24px; height: 24px; border-radius: 50%; object-fit: cover; }
.notify-avatar-placeholder { width: 24px; height: 24px; border-radius: 50%; background: linear-gradient(135deg, #fe2c55, #25f4ee); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 11px; }
.notify-content { flex: 1; font-size: 14px; }
.notify-time { color: #c0c4cc; font-size: 12px; flex-shrink: 0; }
</style>
