import { post } from './request'
import type { Result } from './request'

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
}

export interface AuthResponse {
  token: string
  username: string
  role: string
  userId: number
}

export function login(data: LoginRequest): Promise<Result<AuthResponse>> {
  return post<AuthResponse>('/auth/login', data)
}

export function register(data: RegisterRequest): Promise<Result<void>> {
  return post<void>('/auth/register', data)
}
