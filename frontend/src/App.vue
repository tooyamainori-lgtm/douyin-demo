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
/* ============================================================
   Global Design System — 轻奢编辑风
   ============================================================ */
:root {
  /* 主色调 */
  --color-primary: #FE2C55;
  --color-primary-light: #FF6B81;
  --color-primary-soft: #FFF0F3;

  /* 中性色 */
  --color-bg: #FAF7F2;
  --color-bg-alt: #F5F0EB;
  --color-surface: #FFFFFF;
  --color-border: #EDE8E3;

  /* 文字色 */
  --color-text: #2D2A26;
  --color-text-secondary: #8B8680;
  --color-text-muted: #B8B3AE;

  /* 氛围色 */
  --color-warm: #F59E0B;
  --color-cool: #06B6D4;
  --color-success: #10B981;

  /* 阴影 */
  --shadow-sm: 0 1px 2px rgba(0,0,0,0.04);
  --shadow-md: 0 4px 16px rgba(0,0,0,0.06);
  --shadow-lg: 0 12px 32px rgba(0,0,0,0.08);
  --shadow-glow: 0 4px 20px rgba(254,44,85,0.12);

  /* 圆角 */
  --radius-sm: 8px;
  --radius-md: 14px;
  --radius-lg: 20px;
  --radius-xl: 28px;

  /* 过渡 */
  --transition-fast: 0.15s ease;
  --transition-normal: 0.25s ease;
  --transition-slow: 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', -apple-system, BlinkMacSystemFont, sans-serif;
  background: var(--color-bg);
  color: var(--color-text);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* ============================================================
   Dark Mode
   ============================================================ */
[data-theme="dark"] {
  --color-bg: #1A1A2E;
  --color-bg-alt: #222240;
  --color-surface: #2A2A45;
  --color-border: #3A3A5C;
  --color-text: #EAE8E3;
  --color-text-secondary: #9D9AA0;
  --color-text-muted: #6B6870;
  --shadow-sm: 0 1px 2px rgba(0,0,0,0.2);
  --shadow-md: 0 4px 16px rgba(0,0,0,0.3);
  --shadow-lg: 0 12px 32px rgba(0,0,0,0.4);
  --shadow-glow: 0 4px 20px rgba(254,44,85,0.25);
}

[data-theme="dark"] body {
  color-scheme: dark;
}

[data-theme="dark"] .navbar {
  background: rgba(26, 26, 46, 0.85);
}

[data-theme="dark"] .el-button--default {
  --el-button-bg-color: var(--color-bg-alt);
  --el-button-border-color: var(--color-border);
  --el-button-text-color: var(--color-text);
  --el-button-hover-bg-color: var(--color-border);
  --el-button-hover-text-color: var(--color-text);
}

[data-theme="dark"] .el-input__wrapper {
  background: var(--color-bg-alt) !important;
  box-shadow: none !important;
}

[data-theme="dark"] .el-input__inner {
  color: var(--color-text) !important;
}

[data-theme="dark"] input::placeholder {
  color: var(--color-text-muted);
}

[data-theme="dark"] textarea::placeholder {
  color: var(--color-text-muted);
}

/* Element Plus 全局覆盖 */
.el-button--primary {
  --el-button-bg-color: var(--color-primary);
  --el-button-border-color: var(--color-primary);
  --el-button-hover-bg-color: var(--color-primary-light);
  --el-button-hover-border-color: var(--color-primary-light);
}

.el-button--danger {
  --el-button-bg-color: var(--color-primary);
  --el-button-border-color: var(--color-primary);
}

.el-input__wrapper {
  border-radius: var(--radius-sm) !important;
  box-shadow: var(--shadow-sm) !important;
}

.main-content {
  min-height: calc(100vh - 60px);
}
</style>
