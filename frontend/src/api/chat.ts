import request from '@/utils/request'

/** 联系人 */
export interface ChatContact {
  userId: string
  nickname: string
  avatarUrl: string | null
  lastMessage: string
  lastTime: string | null
  unreadCount: number
}

/** 消息 */
export interface ChatMessage {
  id: string
  senderId: string
  receiverId: string
  content: string
  isMine: boolean
  isRead: boolean
  failed?: boolean  // 发送失败标记
  time: string
}

/**
 * 聊天 API
 */
export const chatApi = {
  /** 联系人列表 */
  getContacts() {
    return request.get<any, ChatContact[]>('/api/v1/messages/contacts')
  },

  /** 聊天记录 */
  getHistory(userId: string) {
    return request.get<any, ChatMessage[]>(`/api/v1/messages/${userId}?limit=50`)
  },

  /** 发送消息 */
  send(userId: string, content: string) {
    return request.post<any, ChatMessage>(`/api/v1/messages/${userId}`, { content })
  },

  /** 未读总数 */
  getUnreadCount() {
    return request.get<any, number>('/api/v1/messages/unread-count')
  },
}
