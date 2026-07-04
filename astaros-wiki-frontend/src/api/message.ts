import { get, put, deleteRequest } from './request'
import type { Result, PageResult } from './request'

export interface MessageDTO {
  id: number
  title: string
  content: string
  opinion?: string
  isRead: boolean
  pageId?: number
  pageSlug?: string
  createdAt: string
}

export function getMessages(
  page: number = 0,
  size: number = 50,
): Promise<Result<PageResult<MessageDTO>>> {
  return get<PageResult<MessageDTO>>('/messages', { page, size })
}

export function getUnreadCount(): Promise<Result<number>> {
  return get<number>('/messages/unread-count')
}

export function getUnreadMessages(): Promise<Result<MessageDTO[]>> {
  return get<MessageDTO[]>('/messages/unread')
}

export function markAsRead(messageId: number): Promise<Result<void>> {
  return put<void>(`/messages/${messageId}/read`)
}

export function markAllAsRead(): Promise<Result<void>> {
  return put<void>('/messages/all/read')
}

export function deleteMessage(messageId: number): Promise<Result<void>> {
  return deleteRequest<void>(`/messages/${messageId}`)
}
