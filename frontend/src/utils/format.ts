import { format, formatDistanceToNow } from 'date-fns'
import { zhCN } from 'date-fns/locale'

export function formatDate(dateStr: string) {
  return format(new Date(dateStr), 'yyyy-MM-dd HH:mm')
}

// 分组键：同一天的照片归为同一组
export function formatDateKey(dateStr: string) {
  return format(new Date(dateStr), 'yyyy-MM-dd')
}

// 分组标题：由分组键（yyyy-MM-dd）格式化为「2026-09-20 星期日」
export function formatDateLabel(dateKey: string) {
  return format(new Date(`${dateKey}T00:00:00`), 'yyyy-MM-dd EEEE', { locale: zhCN })
}

// 照片上传时间（仅时分，日期由分组标题给出）
export function formatTime(dateStr: string) {
  return format(new Date(dateStr), 'HH:mm')
}

export function formatRelativeTime(dateStr: string) {
  return formatDistanceToNow(new Date(dateStr), { addSuffix: true, locale: zhCN })
}

export function formatFileSize(bytes: number) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}
