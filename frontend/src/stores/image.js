import { defineStore } from 'pinia'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

export const useImageStore = defineStore('image', () => {
  const uploadHistory = ref([])
  const isLoading = ref(false)
  const errorMessage = ref('')

  // 上传图片
  const uploadImage = async (file) => {
    isLoading.value = true
    errorMessage.value = ''
    
    try {
      const formData = new FormData()
      formData.append('file', file)
      
      const response = await axios.post('/api/images/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
      
      if (response.data.success) {
        const imageInfo = response.data.data
        uploadHistory.value.unshift({
          ...imageInfo,
          uploadTime: new Date(imageInfo.uploadTime)
        })
        return imageInfo
      } else {
        throw new Error(response.data.message)
      }
    } catch (error) {
      errorMessage.value = error.response?.data?.message || error.message || '上传失败'
      throw error
    } finally {
      isLoading.value = false
    }
  }

  // 清空错误信息
  const clearError = () => {
    errorMessage.value = ''
  }

  // 清空上传历史
  const clearHistory = () => {
    uploadHistory.value = []
    ElMessage.success('历史记录已清空')
  }

  return {
    uploadHistory,
    isLoading,
    errorMessage,
    uploadImage,
    clearError,
    clearHistory
  }
})
