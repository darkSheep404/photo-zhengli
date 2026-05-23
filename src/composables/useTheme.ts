import { ref } from 'vue'

export type ThemeType = 'ios' | 'pixel'

const STORAGE_KEY = 'photo-zhengli-theme'

const currentTheme = ref<ThemeType>(
  (localStorage.getItem(STORAGE_KEY) as ThemeType) || 'ios'
)

export function useTheme() {
  function setTheme(theme: ThemeType) {
    currentTheme.value = theme
    document.documentElement.setAttribute('data-theme', theme)
    localStorage.setItem(STORAGE_KEY, theme)
  }

  function initTheme() {
    document.documentElement.setAttribute('data-theme', currentTheme.value)
  }

  return {
    currentTheme,
    setTheme,
    initTheme,
  }
}
