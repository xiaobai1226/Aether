<template>
  <div class="markdown-preview-container">
    <!-- 悬浮操作按钮 -->
    <div class="floating-actions">
      <el-tooltip 
        :content="viewMode === 'preview' ? '查看源码' : '预览渲染'" 
        placement="left" 
        :show-after="300"
      >
        <div class="action-btn toggle-btn" @click="toggleViewMode">
          <el-icon :size="18">
            <View v-if="viewMode === 'preview'" />
            <Document v-else />
          </el-icon>
        </div>
      </el-tooltip>
      <el-tooltip content="复制内容" placement="left" :show-after="300">
        <div class="action-btn copy-btn" @click="copyMarkdown">
          <el-icon :size="18"><DocumentCopy /></el-icon>
        </div>
      </el-tooltip>
      <el-tooltip content="下载文件" placement="left" :show-after="300">
        <div class="action-btn download-btn" @click="downloadMarkdown">
          <el-icon :size="18"><Download /></el-icon>
        </div>
      </el-tooltip>
    </div>

    <!-- Markdown 预览区域 -->
    <div class="markdown-content" v-loading="loading">
      <!-- 预览模式 -->
      <MdPreview 
        v-if="!loading && viewMode === 'preview'"
        :modelValue="markdownContent" 
        :theme="theme"
        previewTheme="github"
        codeTheme="github"
        :showCodeRowNumber="true"
      />
      
      <!-- 源码模式 -->
      <div v-else-if="!loading && viewMode === 'source'" class="source-code-view">
        <highlightjs language="markdown" :code="markdownContent" />
      </div>
      
      <!-- 错误提示 -->
      <div v-if="error" class="error-message">
        <el-icon :size="48" color="#F56C6C"><WarningFilled /></el-icon>
        <p>{{ error }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { DocumentCopy, Download, WarningFilled, View, Document } from '@element-plus/icons-vue'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'
import { getFile } from '@/api/v1/file'

/**
 * 组件接收的属性
 */
const props = defineProps({
  // 文件ID
  fileId: Number,
  // 文件名
  fileName: String
})

// 状态
const markdownContent = ref('')
const loading = ref(true)
const error = ref('')
const blobResult = ref<Blob>()
const viewMode = ref<'preview' | 'source'>('preview') // 视图模式：preview-预览，source-源码

// 主题（可以根据系统主题自动切换）
const theme = computed(() => {
  // 可以从系统配置中读取，这里默认使用 light
  return 'light'
})

/**
 * 切换视图模式
 */
const toggleViewMode = () => {
  viewMode.value = viewMode.value === 'preview' ? 'source' : 'preview'
  ElMessage.success(`已切换到${viewMode.value === 'preview' ? '预览' : '源码'}模式`)
}

/**
 * 读取 Markdown 文件
 */
const readMarkdown = async () => {
  if (!props.fileId) {
    error.value = '文件ID不存在'
    loading.value = false
    return
  }

  try {
    loading.value = true
    error.value = ''
    
    const { data } = await getFile(props.fileId)
    blobResult.value = new Blob([data])
    
    // 读取为文本
    const reader = new FileReader()
    reader.onload = () => {
      markdownContent.value = reader.result as string
      loading.value = false
    }
    reader.onerror = () => {
      error.value = '文件读取失败'
      loading.value = false
    }
    reader.readAsText(blobResult.value, 'utf-8')
  } catch (err) {
    console.error('读取 Markdown 文件失败:', err)
    error.value = '加载文件失败，请稍后重试'
    loading.value = false
  }
}

/**
 * 复制 Markdown 内容
 */
const copyMarkdown = async () => {
  if (!markdownContent.value) {
    ElMessage.warning('暂无内容可复制')
    return
  }

  try {
    await navigator.clipboard.writeText(markdownContent.value)
    ElMessage.success('复制成功')
  } catch (error) {
    ElMessage.error('复制失败')
    console.error('复制失败:', error)
  }
}

/**
 * 下载 Markdown 文件
 */
const downloadMarkdown = () => {
  if (!blobResult.value) {
    ElMessage.warning('文件未加载完成')
    return
  }

  try {
    const url = URL.createObjectURL(blobResult.value)
    const link = document.createElement('a')
    link.href = url
    link.download = props.fileName || 'document.md'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch (error) {
    ElMessage.error('下载失败')
    console.error('下载失败:', error)
  }
}

// 挂载时读取文件
onMounted(() => {
  readMarkdown()
})
</script>

<style scoped lang="scss">
.markdown-preview-container {
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

    .toggle-btn {
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);

      &:hover {
        background: linear-gradient(135deg, #3e9bed 0%, #00e1ed 100%);
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

  .markdown-content {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
    background-color: #f8f9fa;

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

    .error-message {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 60px 20px;
      color: #909399;

      p {
        margin-top: 16px;
        font-size: 14px;
      }
    }

    // 源码视图
    .source-code-view {
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
        }
      }
    }
  }
}

// md-editor-v3 样式优化
:deep(.md-editor-preview-wrapper) {
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

:deep(.md-editor-preview) {
  // 标题样式优化
  h1, h2, h3, h4, h5, h6 {
    margin-top: 24px;
    margin-bottom: 16px;
    font-weight: 600;
    line-height: 1.25;
    
    &:first-child {
      margin-top: 0;
    }
  }

  h1 {
    font-size: 2em;
    border-bottom: 2px solid #eaecef;
    padding-bottom: 0.3em;
  }

  h2 {
    font-size: 1.5em;
    border-bottom: 1px solid #eaecef;
    padding-bottom: 0.3em;
  }

  h3 {
    font-size: 1.25em;
  }

  // 段落间距
  p {
    margin-bottom: 16px;
    line-height: 1.6;
  }

  // 列表样式
  ul, ol {
    margin-bottom: 16px;
    padding-left: 2em;
    
    li {
      margin-bottom: 8px;
      line-height: 1.6;
    }
  }

  // 代码块样式
  pre {
    margin-bottom: 16px;
    border-radius: 6px;
    overflow-x: auto;
  }

  code {
    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  }

  // 行内代码
  p code,
  li code {
    background-color: rgba(175, 184, 193, 0.2);
    padding: 0.2em 0.4em;
    border-radius: 3px;
    font-size: 85%;
  }

  // 引用块样式
  blockquote {
    margin: 16px 0;
    padding: 0 1em;
    color: #6a737d;
    border-left: 4px solid #dfe2e5;
    
    p {
      margin-bottom: 8px;
    }
  }

  // 表格样式
  table {
    width: 100%;
    margin-bottom: 16px;
    border-collapse: collapse;
    border-spacing: 0;

    th, td {
      padding: 8px 13px;
      border: 1px solid #dfe2e5;
    }

    th {
      background-color: #f6f8fa;
      font-weight: 600;
    }

    tr:nth-child(2n) {
      background-color: #f6f8fa;
    }
  }

  // 分割线
  hr {
    margin: 24px 0;
    border: 0;
    border-top: 2px solid #eaecef;
  }

  // 图片样式
  img {
    max-width: 100%;
    height: auto;
    border-radius: 4px;
    margin: 16px 0;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  // 链接样式
  a {
    color: #0366d6;
    text-decoration: none;
    
    &:hover {
      text-decoration: underline;
    }
  }

  // 任务列表样式
  .task-list-item {
    list-style-type: none;
    
    input[type="checkbox"] {
      margin-right: 8px;
    }
  }
}
</style>

