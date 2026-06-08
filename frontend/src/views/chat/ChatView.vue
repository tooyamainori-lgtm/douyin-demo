<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { chatApi, type ChatMessage } from '@/api/chat'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const userId = computed(() => route.params.userId as string)
const messages = ref<ChatMessage[]>([])
const inputText = ref('')
const sending = ref(false)
const contactInfo = ref<{ id: string; nickname: string; avatarUrl: string | null }>({ id: '', nickname: '...', avatarUrl: null })
let pollTimer: ReturnType<typeof setInterval> | null = null
const chatBody = ref<HTMLElement | null>(null)
const isNearBottom = ref(true)
const hasNewMessage = ref(false)

const myAvatar = computed(() => userStore.user?.avatarUrl || null)
const myNickname = computed(() => userStore.user?.nickname || '?')

onMounted(async () => {
  await fetchUserInfo()
  await fetchMessages()
  pollTimer = setInterval(fetchMessages, 3000)
})

onUnmounted(() => { if (pollTimer) clearInterval(pollTimer) })

async function fetchUserInfo() {
  try {
    const res: any = await request.get(`/api/v1/users/${userId.value}`)
    if (res) contactInfo.value = res
  } catch { /* */ }
}

async function fetchMessages() {
  try {
    const prevCount = messages.value.length
    const msgs = await chatApi.getHistory(userId.value)
    const newCount = msgs.length
    messages.value = msgs
    if (isNearBottom.value) {
      await nextTick(scrollToBottom)
      hasNewMessage.value = false
    } else if (newCount > prevCount) {
      hasNewMessage.value = true
    }
  } catch { /* */ }
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text) return
  sending.value = true
  try {
    await chatApi.send(userId.value, text)
    inputText.value = ''
    await fetchMessages()
    await nextTick(scrollToBottom)
  } catch { ElMessage.error('发送失败') }
  finally { sending.value = false }
}

function onEnter(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); sendMessage() }
}

function onScroll() {
  if (!chatBody.value) return
  const el = chatBody.value
  isNearBottom.value = el.scrollHeight - el.scrollTop - el.clientHeight < 80
}

function scrollToBottom() {
  if (chatBody.value) chatBody.value.scrollTop = chatBody.value.scrollHeight
  isNearBottom.value = true
  hasNewMessage.value = false
}

function goToProfile() { router.push(`/user/${userId.value}`) }

function isVideoShare(content: string): boolean { return content.startsWith('[视频分享]') }

function parseShareUrl(content: string): { text: string; url: string } | null {
  const m = content.match(/^\[视频分享\] (.+) \n(.+)$/s)
  if (m) return { text: m[1], url: m[2] }
  return null
}

function openVideo(url: string) {
  const id = url.match(/\/video\/(\d+)/)?.[1]
  if (id) router.push(`/video/${id}`)
}
</script>

<template>
  <div class="chat-view">
    <!-- Header -->
    <div class="chat-header">
      <button class="back-btn" @click="router.back()">← 返回</button>
      <div class="chat-header-user" @click="goToProfile">
        <div class="chat-header-avatar">
          <img v-if="contactInfo.avatarUrl" :src="contactInfo.avatarUrl" />
          <span v-else>{{ contactInfo.nickname?.charAt(0) || '?' }}</span>
        </div>
        <span class="chat-header-name">{{ contactInfo.nickname }}</span>
      </div>
    </div>

    <!-- Messages -->
    <div class="chat-body" ref="chatBody" @scroll="onScroll">
      <div v-if="messages.length === 0" class="chat-empty">开始聊天吧~</div>

      <div v-for="m in messages" :key="m.id" :class="['msg-row', { mine: m.isMine }]">

        <!-- 对方消息 -->
        <template v-if="!m.isMine">
          <div class="msg-avatar">
            <img v-if="contactInfo.avatarUrl" :src="contactInfo.avatarUrl" />
            <span v-else>{{ contactInfo.nickname?.charAt(0) || '?' }}</span>
          </div>
          <div class="msg-bubble">
            <template v-if="isVideoShare(m.content)">
              <div class="share-card" @click="openVideo(parseShareUrl(m.content)?.url || '')">
                <span class="share-icon">🎬</span>
                <div class="share-info">
                  <span class="share-label">视频分享</span>
                  <span class="share-title">{{ parseShareUrl(m.content)?.text || '点击查看' }}</span>
                </div>
              </div>
            </template>
            <template v-else>
              <span class="msg-text">{{ m.content }}</span>
            </template>
            <span class="msg-time">{{ m.time }}</span>
          </div>
          <div class="msg-spacer"></div>
        </template>

        <!-- 自己的消息 -->
        <template v-else>
          <div class="msg-fail-dot">
            <span class="fail-icon" v-if="m.failed" title="发送失败">ⓘ</span>
          </div>
          <div class="msg-spacer"></div>
          <div class="mine-col">
            <div class="msg-bubble mine-bubble">
              <template v-if="isVideoShare(m.content)">
                <div class="share-card mine-share" @click="openVideo(parseShareUrl(m.content)?.url || '')">
                  <span class="share-icon">🎬</span>
                  <div class="share-info">
                    <span class="share-label">视频分享</span>
                    <span class="share-title">{{ parseShareUrl(m.content)?.text || '点击查看' }}</span>
                  </div>
                </div>
              </template>
              <template v-else>
                <span class="msg-text">{{ m.content }}</span>
              </template>
              <span class="msg-time right-time">{{ m.time }}</span>
            </div>
            <div class="msg-read-hint" v-if="m.isRead">已读</div>
          </div>
          <div class="msg-avatar">
            <img v-if="myAvatar" :src="myAvatar" />
            <span v-else>{{ myNickname?.charAt(0) || '?' }}</span>
          </div>
        </template>

      </div>
    </div>

    <!-- 新消息提示 -->
    <div class="new-msg-tip" v-if="hasNewMessage" @click="scrollToBottom">
      <span>↓ 有新消息</span>
    </div>

    <!-- Input -->
    <div class="chat-input-bar">
      <textarea v-model="inputText" class="chat-input" placeholder="输入消息..." rows="1"
        @keydown="onEnter" :disabled="sending"></textarea>
      <button class="send-btn" :disabled="!inputText.trim() || sending" @click="sendMessage">
        {{ sending ? '...' : '发送' }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.chat-view {
  max-width: 700px; margin: 0 auto;
  display: flex; flex-direction: column;
  height: calc(100vh - 80px);
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  overflow: hidden;
}

/* Header */
.chat-header {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 16px; background: var(--color-bg-alt);
  border-bottom: 1px solid var(--color-border);
}
.back-btn { border: none; background: transparent; color: var(--color-primary); font-size: 14px; cursor: pointer; }
.chat-header-user {
  display: flex; align-items: center; gap: 10px; cursor: pointer;
  padding: 2px 8px 2px 2px; border-radius: var(--radius-lg);
  transition: background var(--transition-fast);
}
.chat-header-user:hover { background: var(--color-primary-soft); }
.chat-header-avatar {
  width: 32px; height: 32px; border-radius: 50%; overflow: hidden;
  background: linear-gradient(135deg, var(--color-primary), var(--color-cool));
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.chat-header-avatar img { width: 100%; height: 100%; object-fit: cover; }
.chat-header-avatar span { color: #fff; font-size: 13px; font-weight: bold; }
.chat-header-name { font-size: 16px; font-weight: 600; color: var(--color-text); }

/* Body */
.chat-body { flex: 1; overflow-y: auto; padding: 16px; display: flex; flex-direction: column; gap: 16px; }
.chat-empty { text-align: center; color: var(--color-text-muted); padding: 60px 0; }

/* Message row */
.msg-row { display: flex; align-items: flex-end; gap: 8px; }
.msg-row.mine { justify-content: flex-end; }

/* Avatar */
.msg-avatar {
  width: 36px; height: 36px; border-radius: 50%; overflow: hidden; flex-shrink: 0;
  background: linear-gradient(135deg, var(--color-primary), var(--color-cool));
  display: flex; align-items: center; justify-content: center;
}
.msg-avatar img { width: 100%; height: 100%; object-fit: cover; }
.msg-avatar span { color: #fff; font-size: 14px; font-weight: bold; }
.msg-spacer { width: 36px; flex-shrink: 0; }

/* Right-side column: bubble + read-hint */
.mine-col {
  display: flex; flex-direction: column; align-items: flex-end;
  max-width: 60%;
}

/* Bubble (shared) */
.msg-bubble {
  max-width: 60%;
  padding: 10px 14px;
  border-radius: 16px 16px 16px 4px;
  background: var(--color-bg-alt);
}

.mine-col .msg-bubble {
  max-width: 100%;
}

.msg-bubble.mine-bubble {
  background: var(--color-primary-soft);
  border-radius: 16px 16px 4px 16px;
}

.msg-text { font-size: 15px; color: var(--color-text); line-height: 1.5; word-break: break-word; }
.msg-time { font-size: 11px; color: var(--color-text-muted); margin-top: 4px; display: block; text-align: right; }
.right-time { text-align: right; }

/* Read hint */
.msg-read-hint {
  font-size: 11px; color: var(--color-text-muted); opacity: 0.6;
  margin-top: 2px; text-align: right;
}

/* Failed mark */
.msg-fail-dot {
  width: 20px; flex-shrink: 0; display: flex; align-items: flex-end; justify-content: center;
  padding-bottom: 4px;
}
.fail-icon { color: #EF4444; font-size: 14px; cursor: pointer; font-weight: bold; }

/* Share card */
.share-card {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 12px; background: rgba(0,0,0,0.05);
  border-radius: var(--radius-sm); cursor: pointer;
  min-width: 200px; max-width: 280px;
  transition: background var(--transition-fast);
}
.share-card:hover { background: rgba(0,0,0,0.1); }
.mine-share { background: rgba(254,44,85,0.08); }
.mine-share:hover { background: rgba(254,44,85,0.15); }
.share-icon { font-size: 28px; flex-shrink: 0; }
.share-info { display: flex; flex-direction: column; min-width: 0; overflow: hidden; }
.share-label { font-size: 11px; color: var(--color-text-muted); }
.share-title { font-size: 13px; color: var(--color-text); font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

/* New message tip */
.new-msg-tip {
  display: flex; justify-content: center; padding: 6px 0;
  background: var(--color-surface); cursor: pointer;
}
.new-msg-tip span {
  background: var(--color-primary-soft); color: var(--color-primary);
  padding: 4px 16px; border-radius: 12px;
  font-size: 13px; font-weight: 500;
  transition: background var(--transition-fast);
}
.new-msg-tip span:hover { background: var(--color-primary); color: #fff; }

/* Input */
.chat-input-bar {
  display: flex; gap: 10px; padding: 12px 16px;
  border-top: 1px solid var(--color-border); background: var(--color-bg-alt);
}
.chat-input {
  flex: 1; padding: 10px 14px; border: 1.5px solid var(--color-border);
  border-radius: var(--radius-lg); resize: none;
  background: var(--color-surface); color: var(--color-text);
  font-size: 14px; font-family: inherit; outline: none;
  transition: border-color var(--transition-fast);
}
.chat-input:focus { border-color: var(--color-primary); }
.send-btn {
  padding: 10px 20px; border: none; border-radius: var(--radius-lg);
  background: var(--color-primary); color: #fff;
  font-size: 14px; font-weight: 600; cursor: pointer;
  transition: all var(--transition-fast); flex-shrink: 0;
}
.send-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.send-btn:not(:disabled):hover { background: var(--color-primary-light); }
</style>
