<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import * as docx from 'docx-preview'
import { getFile } from '@/api/v1/file'

const props = defineProps({
  // 文件ID
  fileId: Number
})

const docRef = ref()
const loading = ref(false)
const initDoc = () => {
  if (props.fileId) {
    loading.value = true
    if (docRef.value) {
      docRef.value.innerHTML = ''
    }
    getFile(props.fileId).then(({ data }) => {
      docx.renderAsync(data, docRef.value).finally(() => {
        loading.value = false
      })
    }).catch(() => {
      loading.value = false
    })
  }
}

onMounted(() => {
  initDoc()
})

watch(() => props.fileId, () => {
  initDoc()
})
</script>

<template>
  <div ref="docRef" class="doc-content" v-loading="loading"></div>
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