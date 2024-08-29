<script setup lang="ts">
import { ref, onMounted } from 'vue'
import VuePdfEmbed from 'vue-pdf-embed'
import { getFile } from '@/api/file'

const props = defineProps({
  // 文件ID
  fileId: Number
})

const pdfRef = ref()
const state = ref({
  pageNum: 0,
  numPages: 0
})

const initPdf = () => {
  if (props.fileId) {
    getFile(props.fileId).then(({ data }) => {
      pdfRef.value = data
    })
  }
}

onMounted(() => {
  initPdf()
})
</script>

<template>
  <div class="pdf">
    <vue-pdf-embed ref="pdfRef" :source="pdfRef" class="vue-pdf-embed" width="850"
                   :page="state.pageNum"></vue-pdf-embed>
  </div>
</template>

<style scoped lang="scss">
.pdf {
  width: 100%;
}
</style>