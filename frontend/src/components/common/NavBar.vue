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
    <router-link to="/" class="navbar-brand">Douyin Demo</router-link>

    <div class="navbar-search">
      <el-input
        v-model="keyword"
        placeholder="搜索视频..."
        size="small"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #suffix>
          <el-icon class="search-icon" @click="handleSearch" style="cursor:pointer">🔍</el-icon>
        </template>
      </el-input>
    </div>

    <nav class="navbar-nav">
      <router-link to="/">首页</router-link>
    </nav>

    <div class="navbar-user">
      <template v-if="userStore.isLogin && userStore.user">
        <router-link :to="`/user/${userStore.user.id}`" class="user-info">
          <span class="nickname">{{ userStore.user.nickname }}</span>
        </router-link>
        <el-button type="danger" size="small" plain @click="handleLogout">退出</el-button>
      </template>
      <template v-else>
        <el-button type="primary" size="small" @click="$router.push('/login')">登录</el-button>
        <el-button size="small" @click="$router.push('/register')">注册</el-button>
      </template>
    </div>
  </header>
</template>

<style scoped>
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  position: sticky;
  top: 0;
  z-index: 100;
  gap: 16px;
}

.navbar-brand {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  text-decoration: none;
  flex-shrink: 0;
}

.navbar-search {
  flex: 1;
  max-width: 360px;
}

.navbar-nav a {
  color: #606266;
  text-decoration: none;
  flex-shrink: 0;
}

.navbar-user {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.user-info {
  text-decoration: none;
  color: #303133;
}

.nickname {
  font-size: 14px;
}
</style>
