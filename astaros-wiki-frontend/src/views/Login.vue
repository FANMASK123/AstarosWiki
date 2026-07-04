<template>
  <div class="auth-page">
    <div class="auth-box">
      <div class="auth-header">
        <p class="eyebrow">账户</p>
        <h1 class="auth-title">登录</h1>
        <p class="text-muted" style="font-size:14px">登录您的阿斯塔洛斯 Wiki 账户</p>
      </div>
      <div class="card">
        <el-form :model="form" :rules="rules" ref="formRef" @submit.prevent="handleLogin">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码" show-password :prefix-icon="Lock" />
          </el-form-item>
          <el-form-item>
            <button type="submit" class="btn btn-primary" style="width:100%;justify-content:center" :disabled="loading">
              {{ loading ? '登录中...' : '登录' }} <span>→</span>
            </button>
          </el-form-item>
        </el-form>
        <div class="auth-footer">
          <router-link to="/register">没有账户？立即注册 <span>↗</span></router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { User, Lock } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const form = ref({ username: '', password: '' })
const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}
async function handleLogin() {
  if (!formRef.value) return
  await formRef.value.validate(async (v) => {
    if (v) { loading.value = true; const ok = await userStore.login(form.value); loading.value = false; if (ok) router.push('/') }
  })
}
</script>
