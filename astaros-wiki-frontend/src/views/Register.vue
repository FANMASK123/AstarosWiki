<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-header">
        <p class="hw-eyebrow">账户</p>
        <h1 class="register-title">注册创作者</h1>
        <p class="register-desc">创建账户以开始在阿斯塔洛斯 Wiki 中编写页面内容。</p>
      </div>

      <div class="hermes-card register-card">
        <el-form
          :model="registerForm"
          :rules="rules"
          ref="formRef"
          @submit.prevent="handleRegister"
        >
          <el-form-item prop="username">
            <el-input v-model="registerForm.username" placeholder="用户名" :prefix-icon="User" />
          </el-form-item>

          <el-form-item prop="email">
            <el-input v-model="registerForm.email" placeholder="邮箱" :prefix-icon="Message" />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="密码"
              show-password
              :prefix-icon="Lock"
            />
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="确认密码"
              show-password
              :prefix-icon="Lock"
            />
          </el-form-item>

          <el-form-item>
            <button
              type="submit"
              class="hw-btn-primary"
              style="width: 100%; justify-content: center"
              :disabled="loading"
            >
              {{ loading ? '注册中...' : '注册' }}
              <span v-if="!loading">→</span>
            </button>
          </el-form-item>
        </el-form>

        <div class="register-notice">
          <p><strong>注意：</strong>注册后需要等待管理员审核才能成为创作者。</p>
        </div>

        <div class="register-footer">
          <router-link to="/login" class="register-link">
            已有账户？立即登录 <span>↗</span>
          </router-link>
        </div>
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

const registerForm = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
})

const validatePassword = (_rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else {
    if (registerForm.value.confirmPassword !== '') {
      formRef.value?.validateField('confirmPassword')
    }
    callback()
  }
}

const validateConfirmPassword = (_rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.value.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在 3 到 50 个字符', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' },
  ],
  password: [
    { required: true, validator: validatePassword, trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度在 6 到 100 个字符', trigger: 'blur' },
  ],
  confirmPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }],
}

async function handleRegister() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      const success = await userStore.register({
        username: registerForm.value.username,
        email: registerForm.value.email,
        password: registerForm.value.password,
      })
      loading.value = false
      if (success) {
        router.push('/login')
      }
    }
  })
}
</script>

<style scoped>
.register-page {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  min-height: 80vh;
  padding: 80px 24px 0;
}
.register-container {
  width: 100%;
  max-width: 440px;
}
.register-header {
  margin-bottom: 32px;
}
.register-title {
  font-family: var(--font-expanded);
  font-size: clamp(2rem, 4vw, 3rem);
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  margin-top: 8px;
  color: var(--color-foreground);
}
.register-desc {
  margin-top: 12px;
  font-size: 15px;
  color: var(--color-muted-foreground);
}
.register-card {
  padding: 32px;
}
.register-card .el-form-item {
  margin-bottom: 20px;
}
.register-notice {
  padding: 14px;
  border: 1px solid var(--color-border);
  margin-top: 8px;
}
.register-notice p {
  font-size: 13px;
  color: var(--color-muted-foreground);
  line-height: 1.5;
}
.register-notice strong {
  color: var(--color-foreground);
}
.register-footer {
  text-align: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--color-border);
}
.register-link {
  font-size: 14px;
  color: var(--color-muted-foreground);
  transition: color 0.15s;
}
.register-link:hover {
  color: var(--color-foreground);
}
@media (max-width: 640px) {
  .register-page {
    padding: 40px 12px 0;
  }
  .register-card {
    padding: 20px;
  }
}
</style>
