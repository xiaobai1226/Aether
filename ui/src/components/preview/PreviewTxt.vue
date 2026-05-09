<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { DocumentCopy, Download, Setting, Select } from '@element-plus/icons-vue'
import { getFile } from '@/api/v1/file'

const props = defineProps({
  // 文件ID
  fileId: Number
})

const txtContent = ref('')
const blobResult = ref()
const encode = ref('utf8')
const loading = ref(true)
const showEncodeSetting = ref(false)

// 可用的编码列表
const encodeOptions = [
  { value: 'utf8', label: 'UTF-8' },
  { value: 'gbk', label: 'GBK' },
  { value: 'gb2312', label: 'GB2312' },
  { value: 'big5', label: 'Big5' },
  { value: 'iso-8859-1', label: 'ISO-8859-1' }
]

// 当前编码显示名称
const currentEncodeName = computed(() => {
  return encodeOptions.find(item => item.value === encode.value)?.label || 'UTF-8'
})

const readTxt = () => {
  if (props.fileId) {
    txtContent.value = ''
    blobResult.value = undefined
    loading.value = true
    getFile(props.fileId).then(({ data }) => {
      blobResult.value = new Blob([data])
      showTxt()
    }).catch(() => {
      loading.value = false
      ElMessage.error('文件加载失败')
    })
  }
}

const changeEncode = (value: string) => {
  encode.value = value
  showEncodeSetting.value = false
  showTxt()
  ElMessage.success(`已切换到 ${currentEncodeName.value} 编码`)
}

const showTxt = () => {
  loading.value = true
  let reader = new FileReader()
  reader.onload = () => {
    txtContent.value = reader.result as string
    loading.value = false
  }
  reader.onerror = () => {
    loading.value = false
    ElMessage.error('文件读取失败')
  }
  reader.readAsText(blobResult.value, encode.value)
}

onMounted(() => {
  readTxt()
})

watch(() => props.fileId, () => {
  readTxt()
})

const copy = async () => {
  if (!txtContent.value) {
    ElMessage.warning('暂无内容可复制')
    return
  }
  
  try {
    await navigator.clipboard.writeText(txtContent.value)
    ElMessage.success('复制成功')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

const downloadFile = () => {
  if (!blobResult.value) {
    ElMessage.warning('文件未加载完成')
    return
  }

  try {
    const url = URL.createObjectURL(blobResult.value)
    const link = document.createElement('a')
    link.href = url
    link.download = `document_${Date.now()}.txt`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch (error) {
    ElMessage.error('下载失败')
  }
}
</script>

<template>
  <div class="text-preview-container">
    <!-- 悬浮操作按钮 -->
    <div class="floating-actions">
      <el-tooltip content="编码设置" placement="left" :show-after="300">
        <div class="action-btn setting-btn" @click="showEncodeSetting = !showEncodeSetting">
          <el-icon :size="18"><Setting /></el-icon>
        </div>
      </el-tooltip>
      <el-tooltip content="复制内容" placement="left" :show-after="300">
        <div class="action-btn copy-btn" @click="copy">
          <el-icon :size="18"><DocumentCopy /></el-icon>
        </div>
      </el-tooltip>
      <el-tooltip content="下载文件" placement="left" :show-after="300">
        <div class="action-btn download-btn" @click="downloadFile">
          <el-icon :size="18"><Download /></el-icon>
        </div>
      </el-tooltip>
    </div>

    <!-- 编码设置面板 -->
    <transition name="slide-fade">
      <div v-show="showEncodeSetting" class="encode-panel">
        <div class="panel-header">
          <span class="panel-title">编码设置</span>
          <span class="current-encode">当前：{{ currentEncodeName }}</span>
        </div>
        <div class="encode-options">
          <div 
            v-for="option in encodeOptions" 
            :key="option.value"
            class="encode-option"
            :class="{ active: encode === option.value }"
            @click="changeEncode(option.value)"
          >
            <span>{{ option.label }}</span>
            <el-icon v-if="encode === option.value" class="check-icon" color="#67C23A">
              <Select />
            </el-icon>
          </div>
        </div>
        <div class="panel-tip">💡 看到乱码？试试切换编码</div>
      </div>
    </transition>

    <!-- 文本内容区域 -->
    <div class="text-content" v-loading="loading">
      <div class="code-wrapper">
        <highlightjs autodetect :code="txtContent" />
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.text-preview-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #f8f9fa;
  position: relative;

  // 悬浮操作按钮
  .floating-actions {
    position: fixed;
    right: 80px;
    top: 60px;
    z-index: 100;
    display: flex;
    flex-direction: column;
    gap: 12px;

    .action-btn {
      width: 42px;
      height: 42px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all 0.3s ease;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
      backdrop-filter: blur(10px);

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25);
      }

      &:active {
        transform: translateY(0);
      }

      .el-icon {
        color: #fff;
      }
    }

    .setting-btn {
      background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);

      &:hover {
        background: linear-gradient(135deg, #e95f89 0%, #edd02f 100%);
      }
    }

    .copy-btn {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

      &:hover {
        background: linear-gradient(135deg, #5568d3 0%, #653a8b 100%);
      }
    }

    .download-btn {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);

      &:hover {
        background: linear-gradient(135deg, #e082ea 0%, #e4465b 100%);
      }
    }
  }

  // 编码设置面板
  .encode-panel {
    position: fixed;
    right: 140px;
    top: 60px;
    z-index: 99;
    width: 220px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
    overflow: hidden;

    .panel-header {
      padding: 16px;
      background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
      display: flex;
      justify-content: space-between;
      align-items: center;

      .panel-title {
        color: #fff;
        font-weight: 600;
        font-size: 14px;
      }

      .current-encode {
        color: #fff;
        font-size: 12px;
        background: rgba(255, 255, 255, 0.2);
        padding: 2px 8px;
        border-radius: 10px;
      }
    }

    .encode-options {
      padding: 8px;

      .encode-option {
        padding: 10px 12px;
        margin: 4px 0;
        border-radius: 6px;
        cursor: pointer;
        display: flex;
        justify-content: space-between;
        align-items: center;
        transition: all 0.2s ease;
        font-size: 14px;

        &:hover {
          background-color: #f5f7fa;
        }

        &.active {
          background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
          color: #2e7d32;
          font-weight: 500;
        }

        .check-icon {
          font-size: 16px;
        }
      }
    }

    .panel-tip {
      padding: 12px 16px;
      background-color: #f8f9fa;
      color: #606266;
      font-size: 12px;
      border-top: 1px solid #ebeef5;
    }
  }

  // 面板动画
  .slide-fade-enter-active,
  .slide-fade-leave-active {
    transition: all 0.3s ease;
  }

  .slide-fade-enter-from {
    transform: translateX(20px);
    opacity: 0;
  }

  .slide-fade-leave-to {
    transform: translateX(20px);
    opacity: 0;
  }

  // 文本内容区域
  .text-content {
    flex: 1;
    overflow-y: auto;
    padding: 20px;

    // 优化滚动条样式
    &::-webkit-scrollbar {
      width: 8px;
      height: 8px;
    }

    &::-webkit-scrollbar-thumb {
      background-color: rgba(0, 0, 0, 0.2);
      border-radius: 4px;

      &:hover {
        background-color: rgba(0, 0, 0, 0.3);
      }
    }

    &::-webkit-scrollbar-track {
      background-color: rgba(0, 0, 0, 0.05);
    }

    .code-wrapper {
      background-color: #fff;
      border-radius: 8px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
      overflow: hidden;

      :deep(pre) {
        margin: 0;
        border-radius: 8px;
        max-height: none;
        
        code {
          font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
          font-size: 14px;
          line-height: 1.6;
          display: block;
          padding: 20px;
        }
      }

      // 代码行号样式优化
      :deep(.hljs) {
        background: #fff;
        padding: 20px;
      }
    }
  }
}
</style>