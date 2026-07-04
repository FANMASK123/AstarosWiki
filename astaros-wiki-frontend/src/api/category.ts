import { get, post, put, deleteRequest } from './request'
import type { Result } from './request'

// 分类DTO
export interface CategoryDTO {
  id: number
  name: string
  description: string
  parentId?: number
  sortOrder: number
  children?: CategoryDTO[]
}

// 创建分类请求
export interface CategoryCreateRequest {
  name: string
  description?: string
  parentId?: number | null
  sortOrder?: number
}

// 获取所有分类
export function getAllCategories(): Promise<Result<CategoryDTO[]>> {
  return get<CategoryDTO[]>('/categories')
}

// 获取分类详情
export function getCategoryById(id: number): Promise<Result<CategoryDTO>> {
  return get<CategoryDTO>(`/categories/${id}`)
}

// 创建分类
export function createCategory(data: CategoryCreateRequest): Promise<Result<CategoryDTO>> {
  return post<CategoryDTO>('/categories', data)
}

// 更新分类
export function updateCategory(
  id: number,
  data: CategoryCreateRequest,
): Promise<Result<CategoryDTO>> {
  return put<CategoryDTO>(`/categories/${id}`, data)
}

// 删除分类（可选迁移页面到目标分类）
export function deleteCategory(id: number, migrateTo?: number): Promise<Result<void>> {
  const url = migrateTo ? `/categories/${id}?migrateTo=${migrateTo}` : `/categories/${id}`
  return deleteRequest<void>(url)
}
