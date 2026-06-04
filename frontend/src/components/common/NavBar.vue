<script setup lang="ts">
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

function handleLogout() {
  userStore.logout()
}
</script>

<template>
  <header class="navbar">
    <router-link to="/" class="navbar-brand">Douyin Demo</router-link>

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
}

.navbar-brand {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  text-decoration: none;
}

.navbar-nav a {
  color: #606266;
  text-decoration: none;
  margin-right: 16px;
}

.navbar-user {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-info {
  text-decoration: none;
  color: #303133;
}

.nickname {
  font-size: 14px;
}
</style>
