<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { chatApi, type ChatContact } from '@/api/chat'
import { useUserStore } from '@/stores/user'
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

const userStore = useUserStore()
const router = useRouter()

const activeTab = ref<'notify' | 'chat'>('chat')
const contacts = ref<ChatContact[]>([])
const notifications = ref<NotificationItem[]>([])
const notifyUnread = ref(0)
const chatUnread = ref(0)
const loading = ref(false)

onMounted(async () => {
  const [chatCount, notifyCount] = await Promise.all([
    request.get<any, number>('/api/v1/messages/unread-count').catch(() => 0),
    request.get<any, number>('/api/v1/notifications/unread-count').catch(() => 0),
  ])
  chatUnread.value = chatCount || 0
  notifyUnread.value = notifyCount || 0
  if (notifyUnread.value > 0 && chatUnread.value === 0) {
    activeTab.value = 'notify'
    loadNotifications()
  } else {
    activeTab.value = 'chat'
    loadChat()
  }
})

function switchTab(tab: 'notify' | 'chat') {
  activeTab.value = tab
  tab === 'chat' ? loadChat() : loadNotifications()
}

async function loadChat() {
  loading.value = true
  try {
    contacts.value = await chatApi.getContacts()
    chatUnread.value = contacts.value.reduce((sum, c) => sum + c.unreadCount, 0)
  } catch { /* ignore */ }
  finally { loading.value = false }
}

async function loadNotifications() {
  loading.value = true
  try {
    const [res, count] = await Promise.all([
      request.get<any, any>('/api/v1/notifications?page=1&size=50'),
      request.get<any, number>('/api/v1/notifications/unread-count'),
    ])
    notifications.value = (res as any)?.records || []
    notifyUnread.value = (count as any) ?? 0
  } catch { /* ignore */ }
  finally { loading.value = false }
}

async function markRead() {
  await request.put('/api/v1/notifications/read')
  notifications.value.forEach(n => n.isRead = true)
  notifyUnread.value = 0
  ElMessage.success('已全部标记为已读')
}

function openChat(userId: string) { router.push(`/chat/${userId}`) }
function goToVideo(id: string | null) { if (id) router.push(`/video/${id}`) }
function goToUser(id: string) { router.push(`/user/${id}`) }
const typeLabel: Record<string, string> = { like: '点赞', comment: '评论', follow: '关注' }
</script>

<template>
  <div class="messages-page">
    <div class="msg-header">
      <h2>消息</h2>
      <div class="msg-tabs">
        <button :class="['msg-tab', { active: activeTab === 'notify' }]" @click="switchTab('notify')">
          🔔 互动消息
          <span class="tab-badge" v-if="notifyUnread > 0">{{ notifyUnread > 99 ? '99+' : notifyUnread }}</span>
        </button>
        <button :class="['msg-tab', { active: activeTab === 'chat' }]" @click="switchTab('chat')">
          💬 好友聊天
          <span class="tab-badge" v-if="chatUnread > 0">{{ chatUnread > 99 ? '99+' : chatUnread }}</span>
        </button>
      </div>
    </div>

    <!-- 互动消息 -->
    <div class="msg-list" v-if="activeTab === 'notify'" v-loading="loading">
      <div v-if="notifyUnread > 0" class="mark-read-bar">
        <span>{{ notifyUnread }} 条未读</span>
        <button @click="markRead">全部已读</button>
      </div>
      <div v-if="notifications.length === 0" class="msg-empty">暂无互动消息</div>
      <div v-for="n in notifications" :key="n.id" :class="['notify-item', { unread: !n.isRead }]" @click="goToVideo(n.videoId)">
        <span class="notify-dot" v-if="!n.isRead"></span>
        <span class="notify-user" @click.stop="goToUser(n.fromUser.id)">
          <img v-if="n.fromUser?.avatarUrl" :src="n.fromUser.avatarUrl" class="notify-avatar" />
          <span v-else class="notify-avatar-placeholder">{{ n.fromUser?.nickname?.charAt(0) }}</span>
          {{ n.fromUser?.nickname }}
        </span>
        <span class="notify-text">{{ typeLabel[n.type] || n.type }}了你的{{ n.videoId ? '视频' : '' }}</span>
        <span class="notify-time">{{ n.createTime?.slice(0, 10) }}</span>
      </div>
    </div>

    <!-- 好友聊天 -->
    <div class="msg-list" v-if="activeTab === 'chat'" v-loading="loading">
      <div v-if="contacts.length === 0" class="msg-empty">
        <p>暂无好友</p>
        <p class="hint">互相关注即可成为好友，开始聊天</p>
      </div>
      <div v-for="c in contacts" :key="c.userId" class="contact-item" @click="openChat(c.userId)">
        <div class="contact-avatar">
          <img v-if="c.avatarUrl" :src="c.avatarUrl" />
          <span v-else>{{ c.nickname?.charAt(0) || '?' }}</span>
        </div>
        <div class="contact-body">
          <div class="contact-top">
            <span class="contact-name">{{ c.nickname }}</span>
            <span class="contact-time">{{ c.lastTime }}</span>
          </div>
          <p class="contact-msg">{{ c.lastMessage }}</p>
        </div>
        <span class="contact-badge" v-if="c.unreadCount > 0">{{ c.unreadCount > 99 ? '99+' : c.unreadCount }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.messages-page { max-width: 680px; margin: 0 auto; padding: 24px 20px; }
.msg-header { margin-bottom: 20px; }
.msg-header h2 { font-size: 22px; font-weight: 700; color: var(--color-text); margin-bottom: 16px; }

.msg-tabs { display: flex; gap: 6px; background: var(--color-bg-alt); padding: 4px; border-radius: var(--radius-lg); width: fit-content; }

.msg-tab {
  padding: 8px 20px; border: none; border-radius: var(--radius-md);
  background: transparent; color: var(--color-text-secondary);
  font-size: 14px; font-weight: 500; cursor: pointer; transition: all var(--transition-fast);
}

.msg-tab.active { background: var(--color-surface); color: var(--color-primary); font-weight: 600; box-shadow: var(--shadow-sm); }

.msg-tab { position: relative; }
.tab-badge {
  position: absolute; top: -4px; right: -6px;
  min-width: 18px; height: 18px; border-radius: 9px;
  background: var(--color-primary); color: #fff;
  font-size: 11px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  padding: 0 5px; line-height: 1;
}

.msg-list { background: var(--color-surface); border-radius: var(--radius-lg); border: 1px solid var(--color-border); overflow: hidden; }
.msg-empty { text-align: center; padding: 60px 20px; color: var(--color-text-muted); font-size: 14px; }
.msg-empty .hint { font-size: 13px; margin-top: 6px; }

.mark-read-bar { display: flex; justify-content: space-between; align-items: center; padding: 10px 18px; font-size: 13px; color: var(--color-text-secondary); border-bottom: 1px solid var(--color-border); background: var(--color-bg-alt); }
.mark-read-bar button { border: none; background: transparent; color: var(--color-primary); font-size: 13px; cursor: pointer; font-weight: 500; }

/* Notify */
.notify-item { display: flex; align-items: center; gap: 10px; padding: 12px 18px; cursor: pointer; border-bottom: 1px solid var(--color-border); transition: background var(--transition-fast); }
.notify-item.unread { background: var(--color-primary-soft); }
.notify-item:hover { background: var(--color-bg-alt); }
.notify-dot { width: 6px; height: 6px; border-radius: 50%; background: var(--color-primary); flex-shrink: 0; }
.notify-user { display: flex; align-items: center; gap: 6px; font-size: 13px; color: var(--color-text); font-weight: 500; cursor: pointer; flex-shrink: 0; min-width: 80px; }
.notify-avatar { width: 24px; height: 24px; border-radius: 50%; object-fit: cover; }
.notify-avatar-placeholder { width: 24px; height: 24px; border-radius: 50%; background: linear-gradient(135deg, var(--color-primary), var(--color-cool)); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 11px; }
.notify-text { flex: 1; font-size: 14px; color: var(--color-text-secondary); }
.notify-time { color: var(--color-text-muted); font-size: 12px; flex-shrink: 0; }

/* Contact */
.contact-item { display: flex; align-items: center; gap: 12px; padding: 14px 18px; cursor: pointer; border-bottom: 1px solid var(--color-border); transition: background var(--transition-fast); position: relative; }
.contact-item:hover { background: var(--color-bg-alt); }
.contact-avatar { width: 48px; height: 48px; border-radius: 50%; overflow: hidden; flex-shrink: 0; background: linear-gradient(135deg, var(--color-primary), var(--color-cool)); display: flex; align-items: center; justify-content: center; }
.contact-avatar img { width: 100%; height: 100%; object-fit: cover; }
.contact-avatar span { color: #fff; font-size: 18px; font-weight: bold; }
.contact-body { flex: 1; min-width: 0; }
.contact-top { display: flex; justify-content: space-between; margin-bottom: 4px; }
.contact-name { font-size: 15px; font-weight: 600; color: var(--color-text); }
.contact-time { font-size: 12px; color: var(--color-text-muted); }
.contact-msg { font-size: 13px; color: var(--color-text-secondary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.contact-badge { min-width: 20px; height: 20px; border-radius: 10px; background: var(--color-primary); color: #fff; font-size: 11px; font-weight: 600; display: flex; align-items: center; justify-content: center; padding: 0 6px; flex-shrink: 0; }
</style>
