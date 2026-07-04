import { get, put } from './request'
import type { Result } from './request'

export interface UserInfo {
  id: number
  username: string
  email: string
  role: string
}

export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
}

export function getUserInfo(): Promise<Result<UserInfo>> {
  return get<UserInfo>('/users/me')
}

export function changePassword(data: ChangePasswordRequest): Promise<Result<void>> {
  return put<void>('/users/me/password', data)
}
