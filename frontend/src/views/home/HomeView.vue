<script setup lang="ts">
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
</script>

<template>
  <div class="home">
    <div class="welcome">
      <h1 v-if="userStore.isLogin">
        你好，{{ userStore.user?.nickname }}
      </h1>
      <h1 v-else>Douyin Web Demo</h1>
      <p>视频信息流（待实现）</p>
    </div>
  </div>
</template>

<style scoped>
.home {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 56px);
}

.welcome {
  text-align: center;
}

.welcome h1 {
  font-size: 28px;
  margin-bottom: 8px;
}

.welcome p {
  color: #909399;
  font-size: 16px;
}
</style>
