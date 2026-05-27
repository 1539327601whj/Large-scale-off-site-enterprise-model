<template>
  <div class="admin-container">
    <el-tabs v-model="activeTab" class="admin-tabs">
      <!-- 知识库管理 -->
      <el-tab-pane label="知识库管理" name="knowledge">
        <div class="section">
          <h3>上传企业资料</h3>
          <el-form :model="uploadForm" label-width="80px">
            <el-form-item label="标题">
              <el-input v-model="uploadForm.title" placeholder="文档标题" />
            </el-form-item>
            <el-form-item label="内容">
              <el-input v-model="uploadForm.content" type="textarea" :rows="6" placeholder="粘贴文档内容..." />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpload" :loading="uploading">上传到知识库</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="section">
          <h3>已上传文档</h3>
          <el-table :data="docList" style="width: 100%" v-loading="loadingDocs">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="createdAt" label="上传时间" width="200" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button type="danger" text size="small" @click="handleDelete(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <!-- 提示词配置 -->
      <el-tab-pane label="提示词配置" name="prompt">
        <div class="section">
          <h3>系统提示词</h3>
          <p class="hint">系统提示词将作为 AI 回复的前置指令，影响 AI 的回答风格和范围。</p>
          <el-input v-model="systemPrompt" type="textarea" :rows="10" placeholder="输入系统提示词..." />
          <div style="margin-top: 16px">
            <el-button type="primary" @click="handleSavePrompt" :loading="savingPrompt">保存提示词</el-button>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'
import { ElMessage } from 'element-plus'

const activeTab = ref('knowledge')
const uploadForm = ref({ title: '', content: '' })
const uploading = ref(false)
const docList = ref([])
const loadingDocs = ref(false)
const systemPrompt = ref('')
const savingPrompt = ref(false)

onMounted(() => {
  loadDocs()
  loadPrompt()
})

async function loadDocs() {
  loadingDocs.value = true
  try {
    const res = await api.get('/admin/knowledge/list')
    docList.value = res.data || []
  } catch (e) {
    ElMessage.error('加载文档列表失败')
  } finally {
    loadingDocs.value = false
  }
}

async function handleUpload() {
  if (!uploadForm.value.title || !uploadForm.value.content) {
    ElMessage.warning('请填写标题和内容')
    return
  }
  uploading.value = true
  try {
    await api.post('/admin/knowledge/upload', uploadForm.value)
    ElMessage.success('上传成功')
    uploadForm.value = { title: '', content: '' }
    loadDocs()
  } catch (e) {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
  }
}

async function handleDelete(id) {
  try {
    await api.delete(`/admin/knowledge/${id}`)
    ElMessage.success('删除成功')
    loadDocs()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

async function loadPrompt() {
  try {
    const res = await api.get('/admin/prompt')
    if (res.data?.systemPrompt) {
      systemPrompt.value = res.data.systemPrompt
    }
  } catch (e) {}
}

async function handleSavePrompt() {
  savingPrompt.value = true
  try {
    await api.post('/admin/prompt', { system_prompt: systemPrompt.value })
    ElMessage.success('提示词已保存')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    savingPrompt.value = false
  }
}
</script>

<style scoped>
.admin-container {
  padding: 24px;
  height: 100%;
  overflow-y: auto;
  background: #fff;
}
.admin-tabs {
  max-width: 900px;
  margin: 0 auto;
}
.section {
  margin-bottom: 32px;
}
.section h3 {
  margin-bottom: 16px;
  color: #333;
}
.hint {
  color: #999;
  font-size: 13px;
  margin-bottom: 12px;
}
</style>
