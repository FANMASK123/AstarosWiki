<template>
  <div class="profile-page">
    <div class="profile-header">
      <p class="hw-eyebrow">账户</p>
      <h1 class="hw-section-title">个人中心</h1>
    </div>

    <div class="hermes-card profile-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="个人信息" name="info">
          <el-form :model="userForm" label-width="100px" class="profile-form">
            <el-form-item label="用户名">
              <el-input v-model="userForm.username" disabled />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="userForm.email" disabled />
            </el-form-item>
            <el-form-item label="角色">
              <el-input :value="userForm.role" disabled />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="密码修改" name="password">
          <el-form :model="passwordForm" label-width="100px" class="profile-form">
            <el-form-item label="旧密码">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="passwordForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认密码">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <button class="hw-btn-primary" @click="handleChangePassword">
                修改密码 <span>→</span>
              </button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getUserInfo, changePassword } from '../api/user'
import { ElMessage } from 'element-plus'

const activeTab = ref('info')

const userForm = reactive({ username: '', email: '', role: '' })
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

onMounted(async () => {
  await loadUserInfo()
})

async function loadUserInfo() {
  const res = await getUserInfo()
  if (res.code === 200 && res.data) {
    userForm.username = res.data.username
    userForm.email = res.data.email
    userForm.role = res.data.role
  }
}

async function handleChangePassword() {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  const res = await changePassword({
    oldPassword: passwordForm.oldPassword,
    newPassword: passwordForm.newPassword,
  })
  if (res.code === 200) {
    ElMessage.success('密码修改成功')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  }
}
</script>

<style scoped>
.profile-page {
  max-width: var(--max-width-container);
  margin: 0 auto;
  padding: 32px 24px;
}
.profile-header {
  margin-bottom: 24px;
}
.profile-card {
  padding: 32px;
}
.profile-form {
  max-width: 480px;
}
.profile-form .el-form-item {
  margin-bottom: 20px;
}
.profile-form .el-tabs__header {
  margin-bottom: 24px;
}
.profile-form .el-tabs__item {
  font-family: var(--font-expanded);
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-muted-foreground);
}
.profile-form .el-tabs__item.is-active {
  color: var(--color-foreground);
}
.profile-form .el-tabs__active-bar {
  background: var(--color-foreground);
}
.profile-form .el-tabs__nav-wrap::after {
  background: var(--color-border);
}
@media (max-width: 640px) {
  .profile-page {
    padding: 16px 12px;
  }
  .profile-card {
    padding: 20px;
  }
}
</style>
