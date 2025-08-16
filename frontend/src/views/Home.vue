<template>
  <div class="home">
    <div class="container">
      <!-- 头部 -->
      <div class="header">
        <h1>Pixel Dock</h1>
        <p>图片上传服务 - 支持拖拽上传和剪切板粘贴</p>
      </div>

      <!-- 上传区域 -->
      <div class="upload-area">
        <el-card class="upload-card">
          <template #header>
            <div class="card-header">
              <span>图片上传</span>
              <el-button type="primary" @click="clearHistory" :disabled="!uploadHistory.length">
                清空历史
              </el-button>
            </div>
          </template>
          
          <!-- 拖拽上传区域 -->
          <el-upload
            ref="uploadRef"
            class="upload-dragger"
            drag
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleFileChange"
            :before-upload="beforeUpload"
            accept="image/*"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              将图片拖拽到此处，或 <em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持 JPG/PNG/GIF 等格式，文件大小不超过 20MB
              </div>
            </template>
          </el-upload>

          <!-- 剪切板提示 -->
          <div class="clipboard-tip">
            <el-alert
              title="剪切板支持"
              type="info"
              :closable="false"
              show-icon
            >
              <template #default>
                <p>支持直接粘贴图片：</p>
                <ul>
                  <li>Windows: Ctrl + V</li>
                  <li>macOS: Cmd + V</li>
                </ul>
              </template>
            </el-alert>
          </div>

          <!-- 加载状态 -->
          <div v-if="isLoading" class="loading">
            <el-progress type="circle" :percentage="uploadProgress" />
            <p>正在上传...</p>
          </div>

          <!-- 错误提示 -->
          <el-alert
            v-if="errorMessage"
            :title="errorMessage"
            type="error"
            show-icon
            closable
            @close="clearError"
          />
        </el-card>
      </div>

      <!-- 上传历史 -->
      <div v-if="uploadHistory.length > 0" class="history-section">
        <el-card>
          <template #header>
            <span>上传历史</span>
          </template>
          
          <div class="history-list">
            <div
              v-for="item in uploadHistory"
              :key="item.id"
              class="history-item"
            >
              <div class="image-preview">
                <img :src="item.fileUrl" :alt="item.originalFileName" />
              </div>
              <div class="image-info">
                <h4>{{ item.originalFileName }}</h4>
                <p>大小: {{ formatFileSize(item.fileSize) }}</p>
                <p>上传时间: {{ formatTime(item.uploadTime) }}</p>
                <div class="actions">
                  <el-button
                    type="primary"
                    size="small"
                    @click="copyUrl(item.fileUrl)"
                  >
                    复制链接
                  </el-button>
                  <el-button
                    type="success"
                    size="small"
                    @click="copyMarkdown(item.fileUrl, item.originalFileName)"
                  >
                    复制Markdown
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useImageStore } from '../stores/image'
import { storeToRefs } from 'pinia'

const imageStore = useImageStore()
const uploadRef = ref(null)
const uploadProgress = ref(0)

// 使用storeToRefs保持响应性
const { uploadHistory, isLoading, errorMessage } = storeToRefs(imageStore)
const { uploadImage, clearError, clearHistory } = imageStore

// 文件上传前的验证
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt20M = file.size / 1024 / 1024 < 20

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt20M) {
    ElMessage.error('图片大小不能超过 20MB!')
    return false
  }
  return true
}




// 文件选择后的处理
const handleFileChange = async (file) => {
  if (file && file.raw) {
    try {
      await uploadImage(file.raw)
      ElMessage.success('图片上传成功!')
    } catch (error) {
      console.error('上传失败:', error)
    }
  }
}

// 复制URL到剪切板
const copyUrl = async (url) => {
  try {
    await navigator.clipboard.writeText(url)
    ElMessage.success('链接已复制到剪切板!')
  } catch (error) {
    // 降级方案
    const textArea = document.createElement('textarea')
    textArea.value = url
    document.body.appendChild(textArea)
    textArea.select()
    document.execCommand('copy')
    document.body.removeChild(textArea)
    ElMessage.success('链接已复制到剪切板!')
  }
}

// 复制Markdown格式到剪切板
const copyMarkdown = async (url, fileName) => {
  const markdown = `![${fileName}](${url})`
  try {
    await navigator.clipboard.writeText(markdown)
    ElMessage.success('Markdown已复制到剪切板!')
  } catch (error) {
    // 降级方案
    const textArea = document.createElement('textarea')
    textArea.value = markdown
    document.body.appendChild(textArea)
    textArea.select()
    document.execCommand('copy')
    document.body.removeChild(textArea)
    ElMessage.success('Markdown已复制到剪切板!')
  }
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 格式化时间
const formatTime = (time) => {
  return new Date(time).toLocaleString('zh-CN')
}

// 剪切板粘贴事件处理
const handlePaste = async (event) => {
  const items = event.clipboardData?.items || []
  
  for (let item of items) {
    if (item.type.startsWith('image/')) {
      event.preventDefault()
      
      const file = item.getAsFile()
      if (file) {
        try {
          await uploadImage(file)
          ElMessage.success('剪切板图片上传成功!')
        } catch (error) {
          console.error('剪切板上传失败:', error)
        }
      }
      break
    }
  }
}

// 键盘快捷键处理
const handleKeydown = (event) => {
  if ((event.ctrlKey || event.metaKey) && event.key === 'v') {
    // 延迟处理，确保剪切板数据已经准备好
    setTimeout(() => {
      // 这里不直接调用handlePaste，因为需要用户主动粘贴
    }, 100)
  }
}

onMounted(() => {
  document.addEventListener('paste', handlePaste)
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('paste', handlePaste)
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
.home {
  min-height: 100vh;
  padding: 20px;
  background: linear-gradient(135deg, #f8e5e5 0%, #e8d5c4 50%, #d4e4d7 100%);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  text-align: center;
  color: white;
  margin-bottom: 30px;
}

.header h1 {
  font-size: 3rem;
  margin: 0 0 10px 0;
  font-weight: 300;
  color: #8b7355;
  text-shadow: 2px 2px 4px rgba(139, 115, 85, 0.1);
}

.header p {
  font-size: 1.2rem;
  margin: 0;
  color: #a67c52;
  opacity: 0.8;
}

.upload-area {
  margin-bottom: 30px;
}

.upload-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 12px 40px rgba(139, 115, 85, 0.15);
  border: 1px solid rgba(232, 213, 196, 0.3);
  backdrop-filter: blur(10px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #8b7355;
  font-weight: 500;
}

.upload-dragger {
  margin-bottom: 20px;
}

.upload-dragger :deep(.el-upload-dragger) {
  border: 2px dashed #d4b08c;
  border-radius: 16px;
  background: rgba(248, 229, 229, 0.3);
  transition: all 0.3s ease;
}

.upload-dragger :deep(.el-upload-dragger:hover) {
  border-color: #c19a7b;
  background: rgba(248, 229, 229, 0.5);
  transform: translateY(-2px);
}

.upload-dragger :deep(.el-icon--upload) {
  color: #d4b08c;
  font-size: 48px;
  margin-bottom: 16px;
}

.upload-dragger :deep(.el-upload__text) {
  color: #8b7355;
  font-size: 16px;
}

.upload-dragger :deep(.el-upload__text em) {
  color: #c19a7b;
  font-style: normal;
  font-weight: 600;
}

.upload-dragger :deep(.el-upload__tip) {
  color: #a67c52;
  font-size: 14px;
  margin-top: 12px;
}

.clipboard-tip {
  margin: 20px 0;
}

.clipboard-tip :deep(.el-alert) {
  border-radius: 12px;
  border: 1px solid rgba(212, 228, 215, 0.5);
  background: rgba(212, 228, 215, 0.2);
}

.clipboard-tip :deep(.el-alert__title) {
  color: #6b8e6b;
  font-weight: 600;
}

.clipboard-tip :deep(.el-alert__content) {
  color: #7a9a7a;
}

.clipboard-tip ul {
  margin: 10px 0;
  padding-left: 20px;
  color: #7a9a7a;
}

.clipboard-tip li {
  margin: 4px 0;
}

.clipboard-tip ul {
  margin: 10px 0;
  padding-left: 20px;
}

.loading {
  text-align: center;
  margin: 20px 0;
}

.loading p {
  margin-top: 10px;
  color: #8b7355;
  font-weight: 500;
}

.loading :deep(.el-progress) {
  --el-progress-text-color: #8b7355;
}

.history-section {
  margin-top: 30px;
}

.history-section :deep(.el-card) {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 12px 40px rgba(139, 115, 85, 0.15);
  border: 1px solid rgba(232, 213, 196, 0.3);
  backdrop-filter: blur(10px);
}

.history-section :deep(.el-card__header) {
  color: #8b7355;
  font-weight: 600;
  border-bottom: 1px solid rgba(232, 213, 196, 0.3);
}

.history-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.history-item {
  display: flex;
  border: 1px solid rgba(212, 228, 215, 0.4);
  border-radius: 16px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
  box-shadow: 0 4px 20px rgba(139, 115, 85, 0.08);
}

.history-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(139, 115, 85, 0.15);
  border-color: rgba(212, 228, 215, 0.6);
}

.image-preview {
  width: 100px;
  height: 100px;
  flex-shrink: 0;
  border-radius: 12px;
  overflow: hidden;
  border: 2px solid rgba(212, 228, 215, 0.3);
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.image-preview:hover img {
  transform: scale(1.05);
}

.image-info {
  flex: 1;
  padding: 15px;
}

.image-info h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #8b7355;
  word-break: break-all;
  font-weight: 600;
}

.image-info p {
  margin: 5px 0;
  font-size: 12px;
  color: #a67c52;
  opacity: 0.8;
}

.actions {
  margin-top: 10px;
  display: flex;
  gap: 8px;
}

.actions :deep(.el-button) {
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.actions :deep(.el-button--primary) {
  background: linear-gradient(135deg, #d4b08c 0%, #c19a7b 100%);
  border-color: #d4b08c;
}

.actions :deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #c19a7b 0%, #b08a6a 100%);
  border-color: #c19a7b;
  transform: translateY(-1px);
}

.actions :deep(.el-button--success) {
  background: linear-gradient(135deg, #a8c4a8 0%, #8fb08f 100%);
  border-color: #a8c4a8;
}

.actions :deep(.el-button--success:hover) {
  background: linear-gradient(135deg, #8fb08f 0%, #7a9a7a 100%);
  border-color: #8fb08f;
  transform: translateY(-1px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .home {
    padding: 10px;
  }
  
  .header h1 {
    font-size: 2rem;
  }
  
  .header p {
    font-size: 1rem;
  }
  
  .history-list {
    grid-template-columns: 1fr;
  }
  
  .history-item {
    flex-direction: column;
  }
  
  .image-preview {
    width: 100%;
    height: 200px;
  }
}
</style>
