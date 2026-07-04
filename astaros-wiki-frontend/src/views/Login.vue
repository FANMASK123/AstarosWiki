<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <p class="hw-eyebrow">账户</p>
        <h1 class="login-title">登录</h1>
        <p class="login-desc">登录您的阿斯塔洛斯 Wiki 账户以创建和管理页面内容。</p>
      </div>

      <div class="hw-card login-card">
        <el-form :model="loginForm" :rules="rules" ref="formRef" @submit.prevent="handleLogin">
          <el-form-item prop="username">
            <el-input v-model="loginForm.username" placeholder="用户名" :prefix-icon="User" />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="密码"
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
              {{ loading ? '登录中...' : '登录' }}
              <span v-if="!loading">→</span>
            </button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <router-link to="/register" class="login-link">
            没有账户？立即注册 <span>↗</span>
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
import { User, Lock } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = ref({
  username: '',
  password: '',
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      const success = await userStore.login(loginForm.value)
      loading.value = false
      if (success) {
        router.push('/')
      }
    }
  })
}
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  min-height: 80vh;
  padding: 80px 24px 0;
}

.login-container {
  width: 100%;
  max-width: 440px;
}

.login-header {
  margin-bottom: 32px;
}

.login-title {
  font-family: var(--font-expanded);
  font-size: clamp(2rem, 4vw, 3rem);
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  margin-top: 8px;
  color: var(--color-foreground);
}

.login-desc {
  margin-top: 12px;
  font-size: 15px;
  color: var(--color-muted-foreground);
  line-height: 1.6;
}

.login-card {
  padding: 32px;
}

.login-card .el-form-item {
  margin-bottom: 20px;
}

.login-footer {
  text-align: center;
  margin-top: 8px;
  padding-top: 16px;
  border-top: 1px solid var(--color-border);
}

.login-link {
  font-family: var(--font-display);
  font-size: 14px;
  color: var(--color-muted-foreground);
  transition: color 0.15s;
}
.login-link:hover {
  color: var(--color-foreground);
}

@media (max-width: 640px) {
  .login-page {
    padding: 40px 12px 0;
  }
  .login-card {
    padding: 20px;
  }
}
</style>
