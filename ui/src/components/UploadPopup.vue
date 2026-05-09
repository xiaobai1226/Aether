<template>
  <el-dialog v-model="popupVisible" title="上传" width="500" :close-on-click-modal="false">
    <div class="upload-container">
      <!-- 拖拽区域 -->
      <div 
        class="drag-area"
        :class="{ 'is-dragover': isDragover }"
        @drop.prevent="handleDrop"
        @dragover.prevent="handleDragover"
        @dragleave.prevent="handleDragleave"
        @dragenter.prevent="handleDragover"
      >
        <div class="drag-content">
          <el-icon :size="60" class="upload-icon">
            <Upload />
          </el-icon>
          <p class="drag-text">拖拽文件或文件夹到此处</p>
          <p class="drag-subtext">或点击下方按钮选择</p>
        </div>
      </div>

      <!-- 上传按钮区域 -->
      <div class="buttons">
        <el-upload 
          ref="uploadFileRef"
          :show-file-list="false" 
          :with-credentials="true" 
          :multiple="true"
          :http-request="addUploadFile"
          :accept="fileAccept"
          style="display: inline-block; margin-right: 12px;"
        >
          <el-button type="primary" @click="setWebkitDirectory(false)">
            <el-icon><Document /></el-icon>
            <span style="margin-left: 6px;">上传文件</span>
          </el-button>
        </el-upload>

        <el-upload 
          ref="uploadFolderRef"
          :show-file-list="false" 
          :with-credentials="true" 
          :multiple="true"
          :http-request="addUploadFile"
          :accept="fileAccept"
          style="display: inline-block;"
        >
          <el-button type="primary" @click="setWebkitDirectory(true)">
            <el-icon><Folder /></el-icon>
            <span style="margin-left: 6px;">上传文件夹</span>
          </el-button>
        </el-upload>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, type PropType, ref } from 'vue'
import { Upload, Document, Folder } from '@element-plus/icons-vue'
import CategoryInfo from '@/js/CategoryInfo'
import { useUploaderStore } from '@/stores/uploader'

const props = defineProps({
  category: {
    type: [Number, null] as PropType<number | null>,
    default: null
  },
  path: {
    type: [String, null] as PropType<string | null>,
    default: null
  },
  callbackFunction: {
    type: Function,
    default: () => {
    }
  }
})

const popupVisible = ref(false)
const isDragover = ref(false)

const show = () => {
  popupVisible.value = true
}

defineExpose({ show })

const fileAccept = computed(() => {
  const categoryItem = CategoryInfo[props.category]
  return categoryItem ? categoryItem.accept : ''
})

const uploaderStore = useUploaderStore()

const uploadFileRef = ref()
const uploadFolderRef = ref()

/**
 * 设置文件或文件夹上传模式
 * @param isFolder 是否是文件夹模式
 */
const setWebkitDirectory = (isFolder: boolean) => {
  // 获取对应的 upload 组件的 input 元素
  const targetRef = isFolder ? uploadFolderRef.value : uploadFileRef.value
  if (targetRef && targetRef.$el) {
    const input = targetRef.$el.querySelector('input[type="file"]')
    if (input) {
      if (isFolder) {
        input.setAttribute('webkitdirectory', 'true')
        input.setAttribute('directory', 'true')
      } else {
        input.removeAttribute('webkitdirectory')
        input.removeAttribute('directory')
      }
    }
  }
}

/**
 * 增加上传文件
 * @param fileData
 */
const addUploadFile = (fileData: any) => {
  uploaderStore.addUploadFile(fileData.file, fileData.file.uid, props.path, props.callbackFunction as () => void)

  // 如果弹窗开启则关闭
  if (popupVisible.value) {
    close()
  }
}

/**
 * 处理拖拽进入
 */
const handleDragover = (e: DragEvent) => {
  isDragover.value = true
}

/**
 * 处理拖拽离开
 */
const handleDragleave = (e: DragEvent) => {
  isDragover.value = false
}

/**
 * 处理文件/文件夹拖放
 */
const handleDrop = async (e: DragEvent) => {
  isDragover.value = false
  
  const items = e.dataTransfer?.items
  if (!items) return

  // 收集所有的 entry
  const entries: any[] = []
  for (let i = 0; i < items.length; i++) {
    const item = items[i]
    if (item.kind === 'file') {
      const entry = item.webkitGetAsEntry()
      if (entry) {
        entries.push(entry)
      }
    }
  }

  // 顺序处理所有拖入的项目
  for (let i = 0; i < entries.length; i++) {
    const entry = entries[i]
    try {
      await processEntry(entry, '')
    } catch (error) {
      console.error(`处理项目失败: ${entry.name}`, error)
    }
  }

  // 关闭弹窗
  if (popupVisible.value) {
    close()
  }
}

/**
 * 递归处理文件系统条目（文件或文件夹）
 * @param entry 文件系统条目
 * @param parentPath 父路径（用于构建 webkitRelativePath）
 */
const processEntry = async (entry: any, parentPath: string): Promise<void> => {
  if (entry.isFile) {
    // 处理文件 - 包装成 Promise 以便正确等待
    return new Promise<void>((resolve, reject) => {
      entry.file((file: File) => {
        try {
          let fileToUpload: File
          
          // 只有当有父路径时（即文件在文件夹中），才添加 webkitRelativePath
          if (parentPath) {
            // 构建完整的相对路径
            const relativePath = `${parentPath}/${file.name}`
            
            // 创建一个新的 File 对象，添加 webkitRelativePath 属性
            fileToUpload = new File([file], file.name, {
              type: file.type,
              lastModified: file.lastModified
            })
            
            // 手动添加 webkitRelativePath 属性（模拟文件夹上传）
            Object.defineProperty(fileToUpload, 'webkitRelativePath', {
              value: relativePath,
              writable: false,
              configurable: true
            })
          } else {
            // 单个文件拖拽，直接使用原始文件，不设置 webkitRelativePath
            fileToUpload = file
          }
          
          // 生成唯一ID（转换为字符串）
          const uid = `${Date.now()}-${Math.random()}`
          uploaderStore.addUploadFile(fileToUpload, uid, props.path, props.callbackFunction as () => void)
          
          // 完成处理
          resolve()
        } catch (error) {
          reject(error)
        }
      }, (error: any) => {
        reject(error)
      })
    })
  } else if (entry.isDirectory) {
    // 处理文件夹
    const dirReader = entry.createReader()
    
    // 构建当前文件夹的路径
    const currentPath = parentPath ? `${parentPath}/${entry.name}` : entry.name
    
    // 读取目录内容（可能需要多次读取，因为 readEntries 每次最多返回 100 个条目）
    const readAllEntries = async (): Promise<any[]> => {
      const allEntries: any[] = []
      let entries: any[] = []
      
      do {
        entries = await new Promise<any[]>((resolve, reject) => {
          dirReader.readEntries(
            (entries: any[]) => resolve(entries),
            (error: any) => reject(error)
          )
        })
        allEntries.push(...entries)
      } while (entries.length > 0)
      
      return allEntries
    }

    const entries = await readAllEntries()
    
    // 递归处理文件夹中的所有文件
    for (const childEntry of entries) {
      await processEntry(childEntry, currentPath)
    }
  }
}

/**
 * 关闭上传弹窗
 */
const close = () => {
  popupVisible.value = false
}
</script>

<style scoped lang="scss">
.upload-container {
  padding: 10px 0;
}

.drag-area {
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  background-color: #fafafa;
  padding: 40px 20px;
  text-align: center;
  transition: all 0.3s ease;
  cursor: pointer;
  margin-bottom: 20px;

  &:hover {
    border-color: #409eff;
    background-color: #f0f8ff;
  }

  &.is-dragover {
    border-color: #409eff;
    background-color: #e6f4ff;
    border-style: solid;
    transform: scale(1.02);
  }
}

.drag-content {
  pointer-events: none;
}

.upload-icon {
  color: #409eff;
  margin-bottom: 16px;
}

.drag-text {
  font-size: 16px;
  color: #303133;
  margin: 0 0 8px 0;
  font-weight: 500;
}

.drag-subtext {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.buttons {
  display: flex;
  justify-content: center;
  align-items: center;

  :deep(.el-button) {
    display: flex;
    align-items: center;
    padding: 12px 24px;
    font-size: 14px;
  }
}
</style>