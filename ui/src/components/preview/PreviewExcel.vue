<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as XLSX from 'xlsx'
import { getFile } from '@/api/v1/file'

const props = defineProps({
  // 文件ID
  fileId: Number
})

const excelContent = ref()
const initExcel = () => {
  if (props.fileId) {
    getFile(props.fileId).then(({ data }) => {
      let workbook = XLSX.read(new Uint8Array(data), { type: 'array' })
      let worksheet = workbook.Sheets[workbook.SheetNames[0]]
      excelContent.value = XLSX.utils.sheet_to_html(worksheet)
    })
  }
}

onMounted(() => {
  initExcel()
})
</script>

<template>
  <div v-html="excelContent" class="table-info"></div>
</template>

<style scoped lang="scss">
.table-info {
  width: 100%;
  padding: 10px;

  :deep table {
    width: 100%;
    border-collapse: collapse;

    td {
      border: 1px solid #ddd;
      border-collapse: collapse;
      padding: 5px;
      height: 30px;
      min-width: 50px;
    }
  }
}
</style>