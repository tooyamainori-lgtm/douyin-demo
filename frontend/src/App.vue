<script setup lang="ts">
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import NavBar from '@/components/common/NavBar.vue'

const userStore = useUserStore()

// 应用启动时恢复登录态
onMounted(() => {
  userStore.restoreLogin()
})
</script>

<template>
  <NavBar />
  <main class="main-content">
    <router-view />
  </main>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background: #f5f7fa;
  color: #303133;
}

.main-content {
  min-height: calc(100vh - 56px);
}
</style>
