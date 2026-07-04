<template>
  <div class="auth-page">
    <div class="auth-box">
      <div class="auth-header">
        <p class="eyebrow">账户</p>
        <h1 class="auth-title">注册创作者</h1>
        <p class="text-muted" style="font-size:14px">创建账户以开始编写 Wiki 页面</p>
      </div>
      <div class="card">
        <el-form :model="form" :rules="rules" ref="formRef" @submit.prevent="handleRegister">
          <el-form-item prop="username"><el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" /></el-form-item>
          <el-form-item prop="email"><el-input v-model="form.email" placeholder="邮箱" :prefix-icon="Message" /></el-form-item>
          <el-form-item prop="password"><el-input v-model="form.password" type="password" placeholder="密码" show-password :prefix-icon="Lock" /></el-form-item>
          <el-form-item prop="confirm"><el-input v-model="form.confirm" type="password" placeholder="确认密码" show-password :prefix-icon="Lock" /></el-form-item>
          <el-form-item>
            <button type="submit" class="btn btn-primary" style="width:100%;justify-content:center" :disabled="loading">
              {{ loading ? '注册中...' : '注册' }} <span>→</span>
            </button>
          </el-form-item>
        </el-form>
        <div style="padding:12px;border:1px solid var(--border);margin-bottom:14px">
          <p style="font-size:13px;color:var(--muted);line-height:1.5"><strong style="color:var(--fg)">注意：</strong>注册后需等待管理员审核</p>
        </div>
        <div class="auth-footer"><router-link to="/login">已有账户？立即登录 <span>↗</span></router-link></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { User, Lock, Message } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const form = ref({ username: '', email: '', password: '', confirm: '' })

const validatePass = (_r: any, v: any, cb: any) => { if (v && form.value.confirm) formRef.value?.validateField('confirm'); cb() }
const validateConfirm = (_r: any, v: any, cb: any) => cb(v !== form.value.password ? new Error('两次密码不一致') : undefined)

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 3, max: 50, message: '3-50 字符', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }, { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  password: [{ required: true, validator: validatePass, trigger: 'blur' }, { min: 6, message: '至少6位', trigger: 'blur' }],
  confirm: [{ required: true, validator: validateConfirm, trigger: 'blur' }],
}

async function handleRegister() {
  if (!formRef.value) return
  await formRef.value.validate(async (v) => {
    if (v) { loading.value = true; const ok = await userStore.register({ username: form.value.username, email: form.value.email, password: form.value.password }); loading.value = false; if (ok) router.push('/login') }
  })
}
</script>
