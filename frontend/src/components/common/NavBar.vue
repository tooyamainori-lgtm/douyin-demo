<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const keyword = ref((route.query.keyword as string) || '')

/** 搜索 */
function handleSearch() {
  if (keyword.value.trim()) {
    router.push({ path: '/', query: { keyword: keyword.value.trim() } })
  } else {
    router.push({ path: '/' })
  }
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
      <el-input
        v-model="keyword"
        placeholder="探索更多视频..."
        size="large"
        clearable
        @keyup.enter="handleSearch"
        class="search-input"
      >
        <template #prefix>
          <span class="search-icon-inline">🔍</span>
        </template>
      </el-input>
    </div>

    <nav class="navbar-nav">
      <router-link to="/" class="nav-item">发现</router-link>
    </nav>

    <div class="navbar-user">
      <template v-if="userStore.isLogin && userStore.user">
        <router-link to="/upload" class="nav-item upload-btn">
          <span class="upload-dot"></span>发布
        </router-link>
        <router-link to="/notifications" class="nav-item">消息</router-link>
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
</style>
