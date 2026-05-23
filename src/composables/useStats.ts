import { ref } from 'vue'

export interface CleanupRecord {
  date: string
  deletedCount: number
  movedCount: number
  keptCount: number
  freedBytes: number
  durationMs: number
}

export interface StatsData {
  totalDeleted: number
  totalMoved: number
  totalKept: number
  totalFreedBytes: number
  totalDurationMs: number
  records: CleanupRecord[]
}

const STATS_KEY = 'photo-zhengli-stats'

function loadStats(): StatsData {
  try {
    const raw = localStorage.getItem(STATS_KEY)
    if (raw) return JSON.parse(raw)
  } catch {}
  return { totalDeleted: 0, totalMoved: 0, totalKept: 0, totalFreedBytes: 0, totalDurationMs: 0, records: [] }
}

function saveStats(data: StatsData) {
  localStorage.setItem(STATS_KEY, JSON.stringify(data))
}

const stats = ref<StatsData>(loadStats())

export function useStats() {
  function addRecord(record: CleanupRecord) {
    stats.value.totalDeleted += record.deletedCount
    stats.value.totalMoved += record.movedCount
    stats.value.totalKept += (record.keptCount || 0)
    stats.value.totalFreedBytes += record.freedBytes
    stats.value.totalDurationMs += record.durationMs
    stats.value.records.unshift(record)
    // Keep only last 100 records
    if (stats.value.records.length > 100) {
      stats.value.records = stats.value.records.slice(0, 100)
    }
    saveStats(stats.value)
  }

  function formatDuration(ms: number): string {
    const totalSeconds = Math.floor(ms / 1000)
    if (totalSeconds < 60) return `${totalSeconds} 秒`
    const minutes = Math.floor(totalSeconds / 60)
    if (minutes < 60) return `${minutes} 分钟`
    const hours = Math.floor(minutes / 60)
    const remainMin = minutes % 60
    return `${hours} 小时 ${remainMin} 分`
  }

  function formatBytes(bytes: number): string {
    if (bytes === 0) return '0 B'
    const units = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(1024))
    return (bytes / Math.pow(1024, i)).toFixed(1) + ' ' + units[i]
  }

  return {
    stats,
    addRecord,
    formatDuration,
    formatBytes,
  }
}
