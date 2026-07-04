<template>
  <div class="mailbox-wrapper">
    <div class="mailbox-ball" @click="toggleMailbox">
      <el-icon class="mailbox-icon"><Message /></el-icon>
      <span v-if="unreadCount > 0" class="mailbox-badge">{{
        unreadCount > 99 ? '99+' : unreadCount
      }}</span>
    </div>
    <el-drawer v-model="visible" direction="rtl" title="消息信箱" size="480px">
      <div v-if="messages.length === 0" style="text-align: center; padding: 40px">
        <el-icon size="48" color="#999"><Message /></el-icon>
        <p style="margin-top: 16px">暂无消息</p>
      </div>
      <div v-else class="mailbox-list">
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="mailbox-item"
          :class="{ 'mailbox-unread': !msg.isRead }"
          @click="handleMessageClick(msg)"
        >
          <div class="mailbox-item-header">
            <span class="mailbox-item-title">{{ msg.title }}</span>
            <span class="mailbox-item-time">{{ formatDate(msg.createdAt) }}</span>
          </div>
          <div class="mailbox-item-content">{{ msg.content }}</div>

          <div v-if="msg.opinion" class="mailbox-item-opinion">
            <div class="mailbox-opinion-header" @click.stop="toggleOpinion(msg)">
              <el-icon :class="{ rotated: expandedOpinions[msg.id] }"><ArrowDown /></el-icon>
              <span>{{ getOpinionLabel(msg) }}</span>
            </div>
            <div v-show="expandedOpinions[msg.id]" class="mailbox-opinion-content">
              {{ msg.opinion }}
            </div>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Message, ArrowDown } from '@element-plus/icons-vue'
import { getMessages, markAsRead as markMessageAsRead, getUnreadCount } from '../api/message'
import type { MessageDTO } from '../api/message'

const router = useRouter()
const visible = ref(false)
const unreadCount = ref(0)
const messages = ref<MessageDTO[]>([])
const expandedOpinions = reactive<Record<number, boolean>>({})
let refreshTimer: number | null = null

const toggleMailbox = async () => {
  visible.value = !visible.value
  if (visible.value) {
    await loadMessages()
  }
}

const loadMessages = async () => {
  try {
    const res = await getMessages(0, 50)
    if (res.code === 200 && res.data) {
      messages.value = res.data.content || []
      Object.keys(expandedOpinions).forEach((key) => {
        delete expandedOpinions[parseInt(key)]
      })
    }
    await loadUnreadCount()
  } catch (error) {
    console.error('加载消息失败:', error)
  }
}

const loadUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    if (res.code === 200) {
      unreadCount.value = res.data
    }
  } catch (error) {
    console.error('加载未读消息数失败:', error)
  }
}

const toggleOpinion = (msg: MessageDTO) => {
  expandedOpinions[msg.id] = !expandedOpinions[msg.id]
}

const getOpinionLabel = (msg: MessageDTO) => {
  if (msg.title.includes('通过')) return '审核意见'
  if (msg.title.includes('驳回')) return '驳回原因'
  if (msg.title.includes('下架')) return '下架原因'
  if (msg.title.includes('建议')) return '修改建议'
  return '详情'
}

const handleMessageClick = async (msg: MessageDTO) => {
  if (!msg.isRead) {
    try {
      await markMessageAsRead(msg.id)
      msg.isRead = true
      unreadCount.value = messages.value.filter((m) => !m.isRead).length
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }

  if (msg.pageSlug) {
    if (msg.title.includes('通过')) {
      router.push(`/page/${msg.pageSlug}`)
    } else {
      router.push(`/pages/edit/${msg.pageSlug}`)
    }
  }
}

const formatDate = (date: string) => {
  const d = new Date(date)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return d.toLocaleDateString('zh-CN')
}

onMounted(() => {
  loadUnreadCount()
  refreshTimer = window.setInterval(loadUnreadCount, 30000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.mailbox-wrapper {
  position: fixed;
  left: 24px;
  bottom: 24px;
  z-index: 9999;
}

.mailbox-ball {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(
    135deg,
    var(--classic-accent-primary),
    var(--classic-accent-secondary)
  );
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  transition: transform 0.2s;
  position: relative;
}

.mailbox-ball:hover {
  transform: scale(1.1);
}

.mailbox-icon {
  font-size: 24px;
  color: white;
}

.mailbox-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background-color: #f56c6c;
  color: white;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 10px;
}

.mailbox-list {
  padding: 8px;
  max-height: 600px;
  overflow-y: auto;
}

.mailbox-item {
  padding: 12px;
  margin-bottom: 8px;
  background-color: var(--classic-bg-card);
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.mailbox-item:hover {
  background-color: var(--classic-bg-hover);
}

.mailbox-unread {
  border-left: 4px solid var(--classic-accent-primary);
}

.mailbox-item-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.mailbox-item-title {
  font-weight: bold;
  color: var(--classic-text-primary);
}

.mailbox-item-time {
  font-size: 12px;
  color: var(--classic-text-secondary);
}

.mailbox-item-content {
  font-size: 14px;
  color: var(--classic-text-secondary);
  line-height: 1.4;
}

.mailbox-item-opinion {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid var(--classic-border);
}

.mailbox-opinion-header {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: var(--classic-accent-primary);
  cursor: pointer;
}

.mailbox-opinion-header span {
  margin-left: 4px;
}

.mailbox-opinion-content {
  margin-top: 8px;
  padding: 8px;
  background-color: var(--classic-bg-secondary);
  border-radius: 4px;
  font-size: 13px;
  color: var(--classic-text-secondary);
  line-height: 1.5;
}
</style>
