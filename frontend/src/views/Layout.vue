<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="logo">
        <span class="logo-text">AI 模型平台</span>
      </div>
      <div class="nav-links">
        <router-link to="/" class="nav-item" :class="{ active: $route.path === '/' }">AI 对话</router-link>
        <router-link v-if="userStore.isAdmin()" to="/admin" class="nav-item" :class="{ active: $route.path === '/admin' }">管理面板</router-link>
      </div>
      <div class="user-info">
        <el-tag :type="userStore.isAdmin() ? 'danger' : 'info'" size="small">
          {{ userStore.isAdmin() ? '管理员' : '普通用户' }}
        </el-tag>
        <span class="username">{{ userStore.username }}</span>
        <el-button text @click="handleLogout">退出</el-button>
      </div>
    </el-header>
    <el-main class="main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  height: 100vh;
  background: #f5f7fa;
}
.header {
  display: flex;
  align-items: center;
  background: #1a1a2e;
  color: #fff;
  padding: 0 24px;
  height: 56px;
}
.logo-text {
  font-size: 18px;
  font-weight: bold;
}
.nav-links {
  display: flex;
  gap: 8px;
  margin-left: 40px;
}
.nav-item {
  color: rgba(255,255,255,0.6);
  text-decoration: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.2s;
}
.nav-item:hover, .nav-item.active {
  color: #fff;
  background: rgba(255,255,255,0.1);
}
.user-info {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
}
.username {
  font-size: 14px;
}
.main {
  padding: 0;
  height: calc(100vh - 56px);
  overflow: hidden;
}
</style>
