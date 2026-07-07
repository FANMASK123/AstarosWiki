import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { AuthResponse } from '../api/auth'
import { login as loginApi, register as registerApi } from '../api/auth'
import type { LoginRequest, RegisterRequest } from '../api/auth'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const userInfo = ref<AuthResponse | null>(
    localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')!) : null,
  )

  const login = async (data: LoginRequest) => {
    try {
      const res = await loginApi(data)
      if (res.code === 200) {
        token.value = res.data.token
        userInfo.value = res.data
        localStorage.setItem('token', res.data.token)
        localStorage.setItem('user', JSON.stringify(res.data))
        ElMessage.success('登录成功')
        return true
      }
      return false
    } catch (error) {
      console.error(error)
      return false
    }
  }

  const register = async (data: RegisterRequest) => {
    try {
      const res = await registerApi(data)
      if (res.code === 200) {
        ElMessage.success('注册成功，请等待管理员审核')
        return true
      }
      return false
    } catch (error) {
      console.error(error)
      return false
    }
  }

  const logout = () => {
    token.value = null
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    ElMessage.success('已退出登录')
  }

  const isLoggedIn = () => {
    return token.value && userInfo.value
  }

  const isAdmin = () => {
    return userInfo.value?.role === 'ADMIN'
  }

  const isCreator = () => {
    return userInfo.value?.role === 'CREATOR'
  }

  return {
    token,
    userInfo,
    login,
    register,
    logout,
    isLoggedIn,
    isAdmin,
    isCreator,
  }
})
