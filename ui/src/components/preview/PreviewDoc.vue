<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as docx from 'docx-preview'
import { getFile } from '@/api/v1/file'

const props = defineProps({
  // 文件ID
  fileId: Number
})

const docRef = ref()
const initDoc = () => {
  if (props.fileId) {
    getFile(props.fileId).then(({ data }) => {
      docx.renderAsync(data, docRef.value)
    })
  }
}

onMounted(() => {
  initDoc()
})
</script>

<template>
  <div ref="docRef" class="doc-content"></div>
</template>

<style scoped lang="scss">
.doc-content {
  margin: 0 auto;

  :deep .docx-wrapper {
    background: #fff;
    padding: 10px 0;
  }

  :deep .docx-wrapper > section.docx {
    margin-bottom: 0;
  }
}
</style>