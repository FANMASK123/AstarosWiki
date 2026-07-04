import { get, post, deleteRequest } from './request'
import type { Result, PageResult } from './request'
import type { PageDTO } from './page'

export interface User {
  id: number
  username: string
  email: string
  role: string
  status: string
  createdAt: string
}

export interface UserRequest {
  id: number
  userId: number
  requestType: string
  status: string
  reviewerId?: number
  reviewComment?: string
  createdAt: string
  updatedAt: string
}

export interface OperationLog {
  id: number
  userId: number
  action: string
  targetType: string
  targetId: number
  detail: string
  ipAddress: string
  createdAt: string
}

export function getUsers(page: number, size: number): Promise<Result<PageResult<User>>> {
  return get<PageResult<User>>(`/admin/users?page=${page}&size=${size}`)
}

export function enableUser(userId: number): Promise<Result<void>> {
  return post<void>(`/admin/users/${userId}/enable`)
}

export function disableUser(userId: number): Promise<Result<void>> {
  return post<void>(`/admin/users/${userId}/disable`)
}

export function deleteUser(userId: number): Promise<Result<void>> {
  return deleteRequest<void>(`/admin/users/${userId}`)
}

export function getPendingRequests(
  page: number,
  size: number,
): Promise<Result<PageResult<UserRequest>>> {
  return get<PageResult<UserRequest>>(`/admin/requests?page=${page}&size=${size}`)
}

export function approveRequest(requestId: number): Promise<Result<void>> {
  return post<void>(`/admin/requests/${requestId}/approve`)
}

export function rejectRequest(requestId: number): Promise<Result<void>> {
  return post<void>(`/admin/requests/${requestId}/reject`)
}

export function getOperationLogs(
  page: number,
  size: number,
): Promise<Result<PageResult<OperationLog>>> {
  return get<PageResult<OperationLog>>(`/admin/logs?page=${page}&size=${size}`)
}

export function getDeletedPages(page: number, size: number): Promise<Result<PageResult<PageDTO>>> {
  return get<PageResult<PageDTO>>(`/admin/pages/deleted?page=${page}&size=${size}`)
}

export function restorePage(pageId: number): Promise<Result<void>> {
  return post<void>(`/admin/pages/${pageId}/restore`)
}

export function permanentlyDeletePage(pageId: number): Promise<Result<void>> {
  return deleteRequest<void>(`/admin/pages/${pageId}/permanent`)
}

export function lockPage(pageId: number): Promise<Result<void>> {
  return post<void>(`/admin/pages/${pageId}/lock`)
}

export function unlockPage(pageId: number): Promise<Result<void>> {
  return post<void>(`/admin/pages/${pageId}/unlock`)
}
