<template>
  <div style="position:fixed;bottom:24px;right:24px;z-index:100">
    <button
      class="btn btn-primary"
      style="width:48px;height:48px;border-radius:50%;padding:0;justify-content:center;font-size:20px"
      @click.stop="visible ? closeDrawer() : openDrawer()"
    >
      <el-icon><Message /></el-icon>
      <span v-if="unreadCount>0" style="position:absolute;top:-4px;right:-4px;background:#ef4444;color:#fff;font-size:10px;min-width:18px;height:18px;border-radius:9px;display:flex;align-items:center;justify-content:center;font-family:var(--mono)">{{ unreadCount>99?'99+':unreadCount }}</span>
    </button>

    <el-drawer
      v-model="visible"
      direction="rtl"
      title="消息信箱"
      size="480px"
      :before-close="closeDrawer"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
      :wrapper-closable="true"
    >
      <div v-if="messages.length===0" style="text-align:center;padding:40px">
        <p class="text-muted">暂无消息</p>
      </div>
      <div v-else class="stack">
        <div v-for="msg in messages" :key="msg.id" class="card" :style="{opacity:msg.isRead?0.7:1}" style="cursor:pointer" @click="handleClick(msg)">
          <div style="display:flex;justify-content:space-between;margin-bottom:6px">
            <strong>{{ msg.title }}</strong>
            <span class="mono">{{ fmt(msg.createdAt) }}</span>
          </div>
          <p class="text-muted" style="font-size:13px;line-height:1.5">{{ msg.content }}</p>
          <div v-if="msg.opinion" style="margin-top:8px;padding:8px;border:1px solid var(--border)" @click.stop="toggleOpinion(msg)">
            <span class="mono">审核意见 ▾</span>
            <p v-if="expanded[msg.id]" class="text-muted mt-8" style="font-size:13px">{{ msg.opinion }}</p>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@element-plus/icons-vue'
import { getMessages, markAsRead, getUnreadCount } from '../api/message'
import type { MessageDTO } from '../api/message'

const router = useRouter()
const visible = ref(false)
const messages = ref<MessageDTO[]>([])
const unreadCount = ref(0)
const expanded = reactive<Record<number,boolean>>({})
let timer: ReturnType<typeof setInterval>|null = null

onMounted(async () => { await fetch(); timer = setInterval(fetch, 30000) })
onUnmounted(() => { if (timer) clearInterval(timer) })

async function fetch() {
  try {
    const r1 = await getMessages(0, 50); if (r1.code === 200) messages.value = r1.data.content
    const r2 = await getUnreadCount(); if (r2.code === 200) unreadCount.value = r2.data
  } catch(e) { console.error(e) }
}

function openDrawer() { visible.value = true; fetch() }
function closeDrawer() { visible.value = false }

async function handleClick(msg: MessageDTO) {
  if (!msg.isRead) {
    try { await markAsRead(msg.id); msg.isRead = true; await fetch() } catch(e) { console.error(e) }
  }
  visible.value = false
  if (msg.pageSlug) router.push(`/page/${msg.pageSlug}`)
}

function toggleOpinion(msg: MessageDTO) { expanded[msg.id] = !expanded[msg.id] }
function fmt(d: string) { return new Date(d).toLocaleDateString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' }) }
</script>
