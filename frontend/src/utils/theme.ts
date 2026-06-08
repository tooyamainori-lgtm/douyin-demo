import { ref, watchEffect } from 'vue'

const THEME_KEY = 'douyin-theme'

type Theme = 'light' | 'dark'

/** 当前主题（响应式） */
export const currentTheme = ref<Theme>(getSavedTheme())

/** 获取 localStorage 中保存的主题，默认 light */
function getSavedTheme(): Theme {
  const saved = localStorage.getItem(THEME_KEY)
  if (saved === 'dark' || saved === 'light') return saved
  // 跟随系统偏好
  if (window.matchMedia('(prefers-color-scheme: dark)').matches) return 'dark'
  return 'light'
}

/** 应用主题到 DOM */
function applyTheme(theme: Theme) {
  document.documentElement.setAttribute('data-theme', theme)
}

/** 切换主题 */
export function toggleTheme() {
  currentTheme.value = currentTheme.value === 'light' ? 'dark' : 'light'
}

// 响应式：主题变化时自动应用到 DOM 并持久化
watchEffect(() => {
  applyTheme(currentTheme.value)
  localStorage.setItem(THEME_KEY, currentTheme.value)
})

// 页面加载时立刻应用（避免闪烁）
applyTheme(currentTheme.value)
