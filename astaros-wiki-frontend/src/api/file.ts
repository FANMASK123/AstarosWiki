import service from './request'
import type { Result } from './request'

// 上传图片文件
export function uploadFile(file: File): Promise<Result<string>> {
  const form = new FormData()
  form.append('file', file)
  return service.post('/files/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }) as unknown as Promise<Result<string>>
}
