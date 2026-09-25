// 点赞与标签都是多态目标：相册或照片
export type TargetType = 'ALBUM' | 'PHOTO'

// 相册可见范围：默认 PRIVATE，PUBLIC 才会进入关注者的搜索与动态
export type Visibility = 'PUBLIC' | 'PRIVATE'

// 标签视图：relationId 是"某人给某对象打了某标签"这条关联的主键，删除时用它做权限校验
export interface TagView {
  relationId: string
  name: string
  userId: string
}

// 搜索页的标签筛选条：标签本身加上被使用次数
export interface TagCount {
  id: string
  name: string
  useCount: number
}

export interface AlbumComment {
  id: string
  albumId: string
  // 一级回复：parentId 指向所属的一级评论，为空表示本身就是一级评论
  parentId?: string | null
  replyToUserId?: string | null
  replyToUserName?: string | null
  userId: string
  content: string
  createdAt: string
  albumTitle?: string
  userName?: string
  avatarUrl?: string | null
}

export interface LikeSummary {
  likedAlbumIds: string[]
  likedPhotoIds: string[]
}

export interface Album {
  id: string
  userId?: string
  title: string
  description: string | null
  coverUrl: string | null
  // PUBLIC 才会出现在关注者的搜索与动态里
  visibility?: Visibility
  // 跨作者的列表（动态）里由后端关联 user 填上归属人昵称
  uploaderName?: string
  createdAt: string
  updatedAt: string
  likeCount?: number
  commentCount?: number
  tags?: TagView[]
}

export interface Photo {
  id: string
  albumId: string
  fileName: string
  fileUrl: string
  fileSize: number
  mimeType: string
  sortOrder: number
  albumTitle?: string
  createdAt: string
  userId?: string
  likeCount?: number
  tags?: TagView[]
  // 审核相关字段
  status?: 'pending' | 'approved' | 'rejected'
  reviewedBy?: string
  reviewedAt?: string
  rejectReason?: string
  uploaderName?: string
}

export interface ShareLink {
  id: string
  albumId: string
  shareCode: string
  password: string | null
  expireAt: string | null
  allowDownload: boolean
  // 是否允许协作者通过该链接上传照片
  allowUpload: boolean
  visitCount: number
  createdAt: string
}

// 分享页视图：后端按访客身份返回的脱敏信息与权限位（不含访问密码）
export interface ShareView {
  shareCode: string
  // 访问密码未通过时为 null
  albumTitle: string | null
  albumDescription: string | null
  requirePassword: boolean
  canView: boolean
  // 游客也可投稿，上传后需管理员审核
  canContribute: boolean
  // 仅登录成员可获得
  canEdit: boolean
  allowDownload: boolean
  myPermission: 'GUEST' | 'VIEWER' | 'MEMBER' | 'OWNER'
  expireAt: string | null
  // 密码未通过校验时以下四项均为 null
  albumId?: string | null
  albumLikeCount?: number | null
  albumCommentCount?: number | null
  albumTags?: TagView[] | null
}

export interface Result<T> {
  code: number
  message: string
  data: T
}

export interface AlbumDTO {
  title: string
  description?: string
  visibility?: Visibility
}

export interface ShareLinkDTO {
  albumId: string
  password?: string
  expireAt?: string
  allowDownload?: boolean
  // 是否允许协作者上传照片
  allowUpload?: boolean
}

// 用户相关类型
export interface User {
  id: string
  username: string
  nickname: string
  avatarUrl: string | null
  role: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  nickname?: string
}

export interface LoginResponse {
  token: string
  user: User
}

// 用户卡片：关注列表、粉丝列表、@ 候选共用
export interface UserBrief {
  id: string
  username: string
  nickname: string
  avatarUrl: string | null
  // 当前查看者是否已关注 TA，未登录时后端不返回
  followed?: boolean | null
}

export interface UserProfile {
  id: string
  username: string
  nickname: string
  avatarUrl: string | null
  createdAt: string
  albumCount: number
  followerCount: number
  followingCount: number
  // 本人或已关注且对方相册公开时为 true，否则相册列表为空
  canViewContent: boolean
  followedByMe: boolean
  self: boolean
  albums: Album[]
}

// 相册/照片搜索的筛选条件，留空的字段表示不加该条件
export interface SearchQuery {
  keyword?: string
  tagId?: string
  albumId?: string
  ownerId?: string
  // yyyy-MM-dd
  startDate?: string
  endDate?: string
  page?: number
  size?: number
}

export type NotificationType = 'LIKE' | 'COMMENT' | 'REPLY' | 'FOLLOW' | 'MENTION'

export interface NotificationItem {
  id: string
  actorId: string | null
  type: NotificationType
  targetType: 'ALBUM' | 'PHOTO' | 'USER'
  targetId: string | null
  snippet: string | null
  isRead: boolean
  createdAt: string
  actorName?: string | null
  actorAvatarUrl?: string | null
  targetTitle?: string | null
  // 照片类事件也能跳回所属相册，用户类为空
  targetAlbumId?: string | null
}
