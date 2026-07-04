import { get, post, put, deleteRequest } from './request'
import type { Result, PageResult } from './request'

export interface PageDTO {
  id: number
  title: string
  content: string
  draftContent?: string
  slug: string
  categoryId: number | null
  categoryName: string
  authorId: number
  authorName: string
  status: string
  draftStatus?: string
  version: number
  viewCount: number
  createdAt: string
  updatedAt: string
  draftUpdatedAt?: string
  tags: string[]
  rejectReason?: string
}

export interface PageVersion {
  id: number
  pageId: number
  content: string
  editorId: number
  versionNumber: number
  createdAt: string
}

export interface PageCreateRequest {
  title: string
  content: string
  slug?: string
  categoryId?: number
  tags?: string[]
  publish?: boolean
}

export interface PageUpdateRequest {
  title?: string
  content?: string
  categoryId?: number
  tags?: string[]
  publish?: boolean
}

export function getRecentPages(): Promise<Result<PageDTO[]>> {
  return get<PageDTO[]>('/pages/recent')
}

export function getPublishedPages(
  page: number = 0,
  size: number = 10,
): Promise<Result<PageResult<PageDTO>>> {
  return get<PageResult<PageDTO>>('/pages', { page, size })
}

export function getDraftPages(
  page: number = 0,
  size: number = 10,
): Promise<Result<PageResult<PageDTO>>> {
  return get<PageResult<PageDTO>>('/pages/drafts', { page, size })
}

export function getMyDraftPages(
  page: number = 0,
  size: number = 10,
): Promise<Result<PageResult<PageDTO>>> {
  return get<PageResult<PageDTO>>('/pages/my/drafts', { page, size })
}

export function getMyPublishedPages(
  page: number = 0,
  size: number = 10,
): Promise<Result<PageResult<PageDTO>>> {
  return get<PageResult<PageDTO>>('/pages/my/published', { page, size })
}

export function getPagesByAuthor(
  authorId: number,
  page: number = 0,
  size: number = 10,
): Promise<Result<PageResult<PageDTO>>> {
  return get<PageResult<PageDTO>>(`/pages/author/${authorId}`, { page, size })
}

export function getPagesByAuthorAndStatus(
  authorId: number,
  status: string,
  page: number = 0,
  size: number = 10,
): Promise<Result<PageResult<PageDTO>>> {
  return get<PageResult<PageDTO>>(`/pages/author/${authorId}/status/${status}`, { page, size })
}

export function getMyPages(
  page: number = 0,
  size: number = 10,
): Promise<Result<PageResult<PageDTO>>> {
  return get<PageResult<PageDTO>>('/pages/my', { page, size })
}

export function getMySavedPages(
  page: number = 0,
  size: number = 10,
): Promise<Result<PageResult<PageDTO>>> {
  return get<PageResult<PageDTO>>('/pages/my/saved', { page, size })
}

export function getSavedPages(
  page: number = 0,
  size: number = 10,
): Promise<Result<PageResult<PageDTO>>> {
  return get<PageResult<PageDTO>>('/pages/saved', { page, size })
}

export function getMyPagesByStatus(
  status: string,
  page: number = 0,
  size: number = 10,
): Promise<Result<PageResult<PageDTO>>> {
  if (status === 'DRAFT') {
    return getMyDraftPages(page, size)
  } else if (status === 'SAVED') {
    return getMySavedPages(page, size)
  } else {
    return getMyPublishedPages(page, size)
  }
}

export function getPagesByCategory(
  categoryId: number,
  page: number = 0,
  size: number = 10,
): Promise<Result<PageResult<PageDTO>>> {
  return get<PageResult<PageDTO>>(`/pages/category/${categoryId}`, { page, size })
}

export function getPagesByTag(
  tagName: string,
  page: number = 0,
  size: number = 10,
): Promise<Result<PageResult<PageDTO>>> {
  return get<PageResult<PageDTO>>(`/pages/tag/${tagName}`, { page, size })
}

export function searchPages(
  keyword: string,
  page: number = 0,
  size: number = 10,
): Promise<Result<PageResult<PageDTO>>> {
  return get<PageResult<PageDTO>>('/pages/search', { keyword, page, size })
}

export function getPage(slug: string): Promise<Result<PageDTO>> {
  return get<PageDTO>(`/pages/${slug}`)
}

export function getPageBySlug(slug: string): Promise<Result<PageDTO>> {
  return get<PageDTO>(`/pages/${slug}`)
}

export function createPage(data: PageCreateRequest): Promise<Result<PageDTO>> {
  return post<PageDTO>('/pages', data)
}

export function updatePage(slug: string, data: PageUpdateRequest): Promise<Result<PageDTO>> {
  return put<PageDTO>(`/pages/${slug}`, data)
}

export function approvePage(slug: string, opinion?: string): Promise<Result<PageDTO>> {
  return post<PageDTO>(`/pages/${slug}/approve`, { opinion })
}

export function rejectPage(slug: string, opinion?: string): Promise<Result<PageDTO>> {
  return post<PageDTO>(`/pages/${slug}/reject`, { rejectReason: opinion })
}

export function unpublishPage(slug: string, reason?: string): Promise<Result<PageDTO>> {
  return post<PageDTO>(`/pages/${slug}/unpublish`, { reason })
}

export function deletePage(slug: string): Promise<Result<void>> {
  return deleteRequest<void>(`/pages/${slug}`)
}

export function getPageHistory(slug: string): Promise<Result<any[]>> {
  return get<any[]>(`/pages/${slug}/history`)
}

export function sendAdvice(slug: string, opinion: string): Promise<Result<PageDTO>> {
  return post<PageDTO>(`/pages/${slug}/advice`, { opinion })
}

export function rollbackPage(slug: string, versionNumber: number): Promise<Result<PageDTO>> {
  return post<PageDTO>(`/pages/${slug}/rollback/${versionNumber}`)
}
