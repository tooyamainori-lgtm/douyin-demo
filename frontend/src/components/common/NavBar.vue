<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { currentTheme, toggleTheme } from '@/utils/theme'
import request from '@/utils/request'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const keyword = ref((route.query.keyword as string) || '')
const unreadTotal = ref(0)
let unreadTimer: ReturnType<typeof setInterval> | null = null

async function fetchUnread() {
  if (!userStore.isLogin) { unreadTotal.value = 0; return }
  try {
    const [chat, notify] = await Promise.all([
      request.get<any, number>('/api/v1/messages/unread-count').catch(() => 0),
      request.get<any, number>('/api/v1/notifications/unread-count').catch(() => 0),
    ])
    unreadTotal.value = (chat || 0) + (notify || 0)
  } catch { /* */ }
}

onMounted(() => { fetchUnread(); unreadTimer = setInterval(fetchUnread, 5000) })
onUnmounted(() => { if (unreadTimer) clearInterval(unreadTimer) })
watch(() => userStore.isLogin, () => { if (userStore.isLogin) fetchUnread(); else unreadTotal.value = 0 })
const suggestions = ref<{ keyword: string; type: string }[]>([])
const showSuggestions = ref(false)
const selectedIdx = ref(-1)
let debounceTimer: ReturnType<typeof setTimeout> | null = null

/** 输入变化时获取联想建议 */
watch(keyword, (val) => {
  if (debounceTimer) clearTimeout(debounceTimer)
  if (!val || !val.trim()) {
    suggestions.value = []
    return
  }
  debounceTimer = setTimeout(async () => {
    try {
      const res: any = await request.get(`/api/v1/search/suggestions?keyword=${encodeURIComponent(val)}&limit=8`)
      suggestions.value = res || []
      showSuggestions.value = true
      selectedIdx.value = -1
    } catch { suggestions.value = [] }
  }, 200)
})

/** 选择联想词 */
function selectSuggestion(item: { keyword: string; type: string }) {
  keyword.value = item.keyword
  showSuggestions.value = false
  doSearch(item.keyword)
}

/** 执行搜索 */
function doSearch(kw: string) {
  showSuggestions.value = false
  if (kw.trim()) {
    router.push({ path: '/', query: { keyword: kw.trim() } })
  } else {
    router.push({ path: '/' })
  }
}

function handleSearch() {
  doSearch(keyword.value)
}

/** 键盘导航 */
function onKeydown(e: KeyboardEvent) {
  if (!showSuggestions.value || suggestions.value.length === 0) return
  if (e.key === 'ArrowDown') {
    e.preventDefault()
    selectedIdx.value = Math.min(selectedIdx.value + 1, suggestions.value.length - 1)
  } else if (e.key === 'ArrowUp') {
    e.preventDefault()
    selectedIdx.value = Math.max(selectedIdx.value - 1, -1)
  } else if (e.key === 'Enter' && selectedIdx.value >= 0) {
    e.preventDefault()
    selectSuggestion(suggestions.value[selectedIdx.value])
  } else if (e.key === 'Escape') {
    showSuggestions.value = false
  }
}

function onFocus() {
  if (suggestions.value.length > 0) showSuggestions.value = true
}

function onBlur() {
  // 延迟关闭，让点击事件触发
  setTimeout(() => { showSuggestions.value = false }, 150)
}

function handleLogout() {
  userStore.logout()
}
</script>

<template>
  <header class="navbar">
    <router-link to="/" class="navbar-brand">
      <span class="brand-icon">▶</span>
      <span class="brand-text">Douyin<span class="brand-accent">Demo</span></span>
    </router-link>

    <div class="navbar-search">
      <div class="search-wrapper">
        <el-input
          v-model="keyword"
          placeholder="探索更多视频..."
          size="large"
          clearable
          @keyup.enter="handleSearch"
          @keydown="onKeydown"
          @focus="onFocus"
          @blur="onBlur"
          class="search-input"
        >
          <template #prefix>
            <span class="search-icon-inline">🔍</span>
          </template>
        </el-input>
        <!-- 联想下拉 -->
        <ul class="suggestions-dropdown" v-if="showSuggestions && suggestions.length > 0">
          <li
            v-for="(item, i) in suggestions"
            :key="item.keyword"
            :class="['suggestion-item', { selected: i === selectedIdx }]"
            @mousedown.prevent="selectSuggestion(item)"
          >
            <span class="suggestion-type">{{ item.type === 'title' ? '📹' : item.type === 'tag' ? '🏷️' : '🔥' }}</span>
            <span class="suggestion-text">{{ item.keyword }}</span>
          </li>
        </ul>
      </div>
    </div>

    <nav class="navbar-nav">
      <router-link to="/" class="nav-item">发现</router-link>
      <router-link to="/tag/搞笑" class="nav-item">标签</router-link>
    </nav>

    <div class="navbar-user">
      <button class="theme-toggle" :title="currentTheme === 'light' ? '切换深色模式' : '切换浅色模式'" @click="toggleTheme()">
        {{ currentTheme === 'light' ? '🌙' : '☀️' }}
      </button>
      <template v-if="userStore.isLogin && userStore.user">
        <router-link to="/upload" class="nav-item upload-btn">
          <span class="upload-dot"></span>发布
        </router-link>
        <router-link to="/notifications" class="nav-item msg-nav">
          消息
          <span class="msg-badge" v-if="unreadTotal > 0">{{ unreadTotal > 99 ? '99+' : unreadTotal }}</span>
        </router-link>
        <router-link to="/history" class="nav-item">历史</router-link>
        <router-link to="/favorites" class="nav-item">收藏</router-link>
        <router-link :to="`/user/${userStore.user.id}`" class="user-chip">
          <span class="user-avatar-placeholder">{{ userStore.user.nickname?.charAt(0) }}</span>
          <span class="nickname">{{ userStore.user.nickname }}</span>
        </router-link>
        <button class="logout-btn" @click="handleLogout">退出</button>
      </template>
      <template v-else>
        <button class="btn-ghost" @click="$router.push('/login')">登录</button>
        <button class="btn-primary" @click="$router.push('/register')">注册</button>
      </template>
    </div>
  </header>
</template>

<style scoped>
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 28px;
  background: rgba(255,255,255,0.82);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-bottom: 1px solid var(--color-border);
  position: sticky;
  top: 0;
  z-index: 100;
  gap: 20px;
}

/* Brand */
.navbar-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  flex-shrink: 0;
}

.brand-icon {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm);
  background: linear-gradient(135deg, var(--color-primary), var(--color-warm));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.brand-text {
  font-size: 20px;
  font-weight: 800;
  color: var(--color-text);
  letter-spacing: -0.5px;
}

.brand-accent {
  color: var(--color-primary);
  font-weight: 600;
}

/* Search */
.navbar-search {
  flex: 1;
  max-width: 400px;
}

.search-input :deep(.el-input__wrapper) {
  background: var(--color-bg-alt) !important;
  border: 2px solid transparent !important;
  border-radius: var(--radius-lg) !important;
  transition: all var(--transition-normal) !important;
  box-shadow: none !important;
}

.search-input :deep(.el-input__wrapper:hover) {
  background: var(--color-surface) !important;
  border-color: var(--color-border) !important;
}

.search-input :deep(.el-input__wrapper.is-focus) {
  background: var(--color-surface) !important;
  border-color: var(--color-primary) !important;
  box-shadow: 0 0 0 3px var(--color-primary-soft) !important;
}

.search-icon-inline { opacity: 0.4; font-size: 14px; }

/* 联想下拉 */
.search-wrapper {
  position: relative;
}

.suggestions-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 4px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  list-style: none;
  padding: 6px 0;
  z-index: 200;
  max-height: 360px;
  overflow-y: auto;
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  cursor: pointer;
  transition: background var(--transition-fast);
  color: var(--color-text);
  font-size: 14px;
}

.suggestion-item:hover,
.suggestion-item.selected {
  background: var(--color-bg-alt);
}

.suggestion-type { font-size: 13px; flex-shrink: 0; }

.suggestion-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Nav links */
.navbar-nav { display: flex; gap: 4px; flex-shrink: 0; }

.nav-item {
  padding: 6px 14px;
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all var(--transition-fast);
}

.nav-item:hover { color: var(--color-text); background: var(--color-bg-alt); }
.nav-item.router-link-exact-active { color: var(--color-primary); background: var(--color-primary-soft); }

/* 消息红点 */
.msg-nav { position: relative; }
.msg-badge {
  position: absolute; top: -2px; right: -4px;
  min-width: 18px; height: 18px; border-radius: 9px;
  background: var(--color-primary); color: #fff;
  font-size: 11px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  padding: 0 5px; line-height: 1;
}

/* Upload button */
.upload-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: var(--color-primary) !important;
  color: #fff !important;
  border-radius: var(--radius-lg) !important;
  padding: 6px 16px !important;
}

.upload-btn:hover { background: var(--color-primary-light) !important; }
.upload-dot { width: 6px; height: 6px; border-radius: 50%; background: #fff; }

/* User */
.navbar-user {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px 4px 4px;
  border-radius: var(--radius-lg);
  background: var(--color-bg-alt);
  text-decoration: none;
  transition: all var(--transition-fast);
}

.user-chip:hover { background: var(--color-primary-soft); }

.user-avatar-placeholder {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-warm));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}

.nickname { font-size: 13px; color: var(--color-text); font-weight: 500; }

/* Buttons */
.logout-btn {
  padding: 6px 14px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.logout-btn:hover { border-color: var(--color-primary); color: var(--color-primary); }

.btn-ghost {
  padding: 7px 18px;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: transparent;
  color: var(--color-text);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn-ghost:hover { border-color: var(--color-primary); color: var(--color-primary); }

.btn-primary {
  padding: 7px 20px;
  border: none;
  border-radius: var(--radius-lg);
  background: var(--color-primary);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn-primary:hover { background: var(--color-primary-light); transform: translateY(-1px); box-shadow: var(--shadow-glow); }

/* Theme toggle */
.theme-toggle {
  width: 36px;
  height: 36px;
  border: 1.5px solid var(--color-border);
  border-radius: 50%;
  background: var(--color-surface);
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-normal);
  margin-right: 8px;
  flex-shrink: 0;
}

.theme-toggle:hover {
  border-color: var(--color-warm);
  transform: rotate(15deg);
  box-shadow: var(--shadow-md);
}
</style>
