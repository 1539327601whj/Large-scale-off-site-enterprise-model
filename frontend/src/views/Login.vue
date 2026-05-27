<template>
  <div class="login-container">
    <div class="login-card">
      <h2>企业级大模型平台</h2>
      <p class="subtitle">Enterprise LLM Platform</p>
      <el-form :model="form" @keyup.enter="handleLogin">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" placeholder="密码" type="password" prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" size="large" style="width:100%">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="tips">
        <p>管理员：admin / admin123</p>
        <p>普通用户：user / user123</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import api from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

async function handleLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const res = await api.post('/auth/login', form)
    if (res.code === 200) {
      userStore.setLogin(res.data)
      ElMessage.success('登录成功')
      router.push('/')
    } else {
      ElMessage.error(res.msg || '登录失败')
    }
  } catch (e) {
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
}
.login-card {
  width: 400px;
  padding: 40px;
  background: rgba(255,255,255,0.95);
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
  text-align: center;
}
.login-card h2 {
  margin: 0 0 8px;
  color: #1a1a2e;
  font-size: 24px;
}
.subtitle {
  color: #999;
  margin-bottom: 30px;
  font-size: 14px;
}
.tips {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #eee;
  color: #999;
  font-size: 12px;
}
.tips p {
  margin: 4px 0;
}
</style>
