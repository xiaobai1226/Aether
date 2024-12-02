<template>
  <el-dialog v-model="popupVisible" title="上传" width="280" :close-on-click-modal="false">
    <el-upload :show-file-list="false" :with-credentials="true" :multiple="true"
               :http-request="addUploadFile"
               :accept="fileAccept">
      <!--      <span class="tips">-->
      <!--        点击上传文件或文件夹-->
      <!--      </span>-->

      <div class="buttons">
        <div class="btn_upload_file">
          <el-button type="primary" @click="addWebkitdirectory(false)">
            <span class="iconfont icon-upload" />
            上传文件
          </el-button>
        </div>

        <el-button type="primary" @click="addWebkitdirectory(true)">
          <span class="iconfont icon-upload" />
          上传文件夹
        </el-button>
      </div>
    </el-upload>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import CategoryInfo from '@/js/CategoryInfo'
import { useUploaderStore } from '@/stores/uploader'

const props = defineProps({
  category: {
    type: Number,
    default: null
  },
  path: {
    type: String,
    default: null
  },
  callbackFunction: {
    type: Function,
    default: () => {
    }
  }
})

const popupVisible = ref(false)

const show = () => {
  popupVisible.value = true
}

defineExpose({ show })

const fileAccept = computed(() => {
  const categoryItem = CategoryInfo[props.category]
  return categoryItem ? categoryItem.accept : ''
})

const uploaderStore = useUploaderStore()

/**
 * 增加上传文件夹标识
 * @param isFolder 是否是文件夹
 */
const addWebkitdirectory = (isFolder: Boolean) => {
  const Ele = document.querySelector('.el-upload__input')
  if (Ele) {
    (Ele as any)['webkitdirectory'] = isFolder
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
 * 关闭上传弹窗
 */
const close = () => {
  popupVisible.value = false
}
</script>

<style scoped lang="scss">
.tips {
  text-align: center;
  margin-top: 15px;
  font-size: 16px;
  font-weight: bold;
}

.buttons {
  display: flex;
  justify-content: center;
  //margin-top: 40px;
  //margin-bottom: 15px;
}

.btn_upload_file {
  margin-right: 10px;
}
</style>