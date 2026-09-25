import type { NotificationItem } from '../types'

// 通知只存类型与目标，动词文案在前端统一维护
export function describeNotification(item: NotificationItem): string {
  switch (item.type) {
    case 'LIKE':
      return item.targetType === 'PHOTO' ? '赞了你的照片' : '赞了你的相册'
    case 'COMMENT':
      return '评论了你的相册'
    case 'REPLY':
      return '回复了你的评论'
    case 'FOLLOW':
      return '关注了你'
    case 'MENTION':
      return '在评论里提到了你'
    default:
      return '有新动态'
  }
}

// 关注类通知跳到关注者主页，其余回到被操作的相册
export function notificationLink(item: NotificationItem): string | null {
  if (item.type === 'FOLLOW') {
    return item.actorId ? `/user/${item.actorId}` : null
  }
  const albumId = item.targetAlbumId ?? (item.targetType === 'ALBUM' ? item.targetId : null)
  return albumId ? `/album/${albumId}` : null
}
