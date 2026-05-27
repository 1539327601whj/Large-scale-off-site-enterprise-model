<template>
  <div class="chat-container">
    <div class="chat-messages" ref="messagesRef">
      <div v-if="messages.length === 0" class="welcome">
        <h2>欢迎使用企业级 AI 助手</h2>
        <p>请输入您的问题，我会尽力为您解答。</p>
      </div>
      <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.role]">
        <div class="avatar">{{ msg.role === 'user' ? '我' : 'AI' }}</div>
        <div class="bubble">
          <div class="content" v-html="formatContent(msg.content)"></div>
          <span v-if="msg.role === 'assistant' && msg.loading" class="cursor">|</span>
        </div>
      </div>
    </div>
    <div class="chat-input">
      <el-input
        v-model="inputText"
        placeholder="输入您的问题..."
        :rows="2"
        type="textarea"
        resize="none"
        @keyup.enter.exact="sendMessage"
        :disabled="isStreaming"
      />
      <el-button type="primary" @click="sendMessage" :loading="isStreaming" :disabled="!inputText.trim()">
        发送
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const inputText = ref('')
const messages = ref([])
const isStreaming = ref(false)
const messagesRef = ref(null)

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

function formatContent(text) {
  if (!text) return ''
  return text.replace(/\n/g, '<br>')
}

function sendMessage() {
  const text = inputText.value.trim()
  if (!text || isStreaming.value) return

  // 添加用户消息
  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  scrollToBottom()

  // 添加 AI 回复占位
  const aiMsgIndex = messages.value.length
  messages.value.push({ role: 'assistant', content: '', loading: true })
  isStreaming.value = true

  // SSE 连接
  const token = userStore.token
  const encodedMsg = encodeURIComponent(text)
  const eventSource = new EventSource(`/api/chat/sse?message=${encodedMsg}&token=${token}`)

  eventSource.addEventListener('message', (e) => {
    if (e.data === '[DONE]') {
      messages.value[aiMsgIndex].loading = false
      isStreaming.value = false
      eventSource.close()
      return
    }
    messages.value[aiMsgIndex].content += e.data
    scrollToBottom()
  })

  eventSource.addEventListener('done', () => {
    messages.value[aiMsgIndex].loading = false
    isStreaming.value = false
    eventSource.close()
  })

  eventSource.addEventListener('error', (e) => {
    messages.value[aiMsgIndex].loading = false
    if (!messages.value[aiMsgIndex].content) {
      messages.value[aiMsgIndex].content = '连接异常：请确认 Ollama 服务已启动（ollama serve），并已下载模型（如 ollama pull deepseek-r1:1.5b）。'
    }
    isStreaming.value = false
    eventSource.close()
  })
}
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;
}
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}
.welcome {
  text-align: center;
  padding: 80px 0;
  color: #999;
}
.welcome h2 {
  color: #333;
  margin-bottom: 8px;
}
.message {
  display: flex;
  margin-bottom: 20px;
  gap: 12px;
}
.message.user {
  flex-direction: row-reverse;
}
.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: bold;
  flex-shrink: 0;
}
.message.user .avatar {
  background: #409eff;
  color: #fff;
}
.message.assistant .avatar {
  background: #1a1a2e;
  color: #fff;
}
.bubble {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.6;
  font-size: 14px;
  word-break: break-word;
}
.message.user .bubble {
  background: #409eff;
  color: #fff;
  border-bottom-right-radius: 4px;
}
.message.assistant .bubble {
  background: #f5f7fa;
  color: #333;
  border-bottom-left-radius: 4px;
}
.cursor {
  animation: blink 0.8s infinite;
  color: #409eff;
  font-weight: bold;
}
@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}
.chat-input {
  display: flex;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #eee;
  background: #fff;
}
.chat-input .el-textarea {
  flex: 1;
}
.chat-input .el-button {
  align-self: flex-end;
  height: 54px;
  padding: 0 24px;
}
</style>
