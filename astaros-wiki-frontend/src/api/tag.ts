import { get, post, deleteRequest } from './request'
import type { Result } from './request'

export interface Tag {
  id: number
  name: string
}

export interface TagCreateRequest {
  name: string
}

export function getAllTags(): Promise<Result<Tag[]>> {
  return get<Tag[]>('/tags')
}

export function createTag(name: string): Promise<Result<Tag>> {
  return post<Tag>('/tags', { name })
}

export function deleteTag(id: number): Promise<Result<void>> {
  return deleteRequest<void>(`/tags/${id}`)
}

export function mergeTags(sourceId: number, targetId: number): Promise<Result<void>> {
  return post<void>(`/tags/merge?sourceId=${sourceId}&targetId=${targetId}`)
}
