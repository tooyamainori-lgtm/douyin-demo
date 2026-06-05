<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi, type UserInfo } from '@/api/user'

const route = useRoute()
const userId = route.params.id as string
const tab = ref<'following' | 'followers'>((route.query.tab as any) || 'following')
const users = ref<UserInfo[]>([])
const loading = ref(true)

const title = computed(() => tab.value === 'following' ? '关注列表' : '粉丝列表')

onMounted(() => fetchUsers())

async function fetchUsers() {
  loading.value = true
  try {
    users.value = tab.value === 'following'
      ? await userApi.getFollowing(userId)
      : await userApi.getFollowers(userId)
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function switchTab(t: 'following' | 'followers') {
  tab.value = t
  fetchUsers()
}
</script>

<template>
  <div class="follow-page">
    <div class="follow-card" v-loading="loading">
      <h2>{{ title }}</h2>
      <div class="tab-bar">
        <span :class="{ active: tab === 'following' }" @click="switchTab('following')">关注</span>
        <span :class="{ active: tab === 'followers' }" @click="switchTab('followers')">粉丝</span>
      </div>
      <div v-if="users.length === 0" class="empty">暂无数据</div>
      <div v-for="u in users" :key="u.id" class="user-row">
        <router-link :to="`/user/${u.id}`" class="user-link">
          <span class="user-avatar">
            <img v-if="u.avatarUrl" :src="u.avatarUrl" />
            <span v-else class="avatar-placeholder">{{ u.nickname?.charAt(0) }}</span>
          </span>
          <span class="user-name">{{ u.nickname }}</span>
        </router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.follow-page { max-width: 480px; margin: 40px auto; padding: 0 16px; }
.follow-card { background: #fff; border-radius: 8px; padding: 24px; }
h2 { margin: 0 0 16px; font-size: 18px; }
.tab-bar { display: flex; gap: 24px; margin-bottom: 16px; border-bottom: 1px solid #f0f0f0; }
.tab-bar span { cursor: pointer; padding-bottom: 8px; color: #909399; }
.tab-bar span.active { color: #303133; border-bottom: 2px solid #fe2c55; font-weight: bold; }
.empty { color: #c0c4cc; text-align: center; padding: 32px 0; }
.user-row { padding: 10px 0; border-bottom: 1px solid #f5f5f5; }
.user-link { display: flex; align-items: center; gap: 12px; text-decoration: none; color: #303133; }
.user-avatar { width: 40px; height: 40px; border-radius: 50%; overflow: hidden; background: linear-gradient(135deg, #fe2c55, #25f4ee); display: flex; align-items: center; justify-content: center; }
.user-avatar img { width: 100%; height: 100%; object-fit: cover; }
.avatar-placeholder { color: #fff; font-weight: bold; font-size: 16px; }
</style>
