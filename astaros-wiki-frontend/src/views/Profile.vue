<template>
  <div>
    <p class="eyebrow">账户</p>
    <h1 class="section-title">个人中心</h1>
    <div class="card mt-24">
      <el-tabs v-model="tab">
        <el-tab-pane label="个人信息" name="info">
          <el-form :model="userForm" label-width="100px" style="max-width:480px">
            <el-form-item label="用户名"><el-input v-model="userForm.username" disabled /></el-form-item>
            <el-form-item label="邮箱"><el-input v-model="userForm.email" disabled /></el-form-item>
            <el-form-item label="角色"><el-input :value="userForm.role" disabled /></el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="密码修改" name="password">
          <el-form :model="pw" label-width="100px" style="max-width:480px">
            <el-form-item label="旧密码"><el-input v-model="pw.old" type="password" show-password /></el-form-item>
            <el-form-item label="新密码"><el-input v-model="pw.new" type="password" show-password /></el-form-item>
            <el-form-item label="确认"><el-input v-model="pw.confirm" type="password" show-password /></el-form-item>
            <el-form-item><button class="btn btn-primary" @click="change">修改密码 <span>→</span></button></el-form-item>
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

const tab = ref('info')
const userForm = reactive({ username:'', email:'', role:'' })
const pw = reactive({ old:'', new:'', confirm:'' })

onMounted(async () => { const r = await getUserInfo(); if (r.code===200 && r.data) { userForm.username=r.data.username; userForm.email=r.data.email; userForm.role=r.data.role } })
async function change() {
  if (pw.new !== pw.confirm) { ElMessage.error('两次密码不一致'); return }
  const r = await changePassword({ oldPassword: pw.old, newPassword: pw.new })
  if (r.code===200) { ElMessage.success('已修改'); pw.old=''; pw.new=''; pw.confirm='' }
}
</script>
