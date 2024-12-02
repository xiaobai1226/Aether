<template>
  <div class="pdf-viewer">
    <div class="app-header">
      <div v-if="isLoading">
        加载页数中...
      </div>
      <div v-else>
        <span v-if="showAllPages">
          共 {{ pageCount }} 页
        </span>

        <span v-else>
          <button class="btn-pre-next-page" :disabled="page <= 1" @click="page--">❮</button>
          {{ page }} / {{ pageCount }}
          <button class="btn-pre-next-page" :disabled="page >= pageCount" @click="page++">❯</button>
        </span>
      </div>

      <span>
        <button class="btn-plus-sub" @click="pdfViewCanvasWidthNum-=5">-</button>
        <span>
          <span>缩放比例</span>
          <span style="margin-left: 5px">{{ pdfViewCanvasWidthNum }}%</span>
        </span>
        <button class="btn-plus-sub" @click="pdfViewCanvasWidthNum+=5">+</button>
      </span>

      <label :class="isLoading ? 'invisible' : 'visible'">
        <input v-model="showAllPages" type="checkbox">
        显示所有页
      </label>
    </div>

    <div class="app-content">
      <vue-pdf-embed
        ref="pdfRef"
        :source="pdfSource"
        :page="page"
        @rendered="handleDocumentRender"
      />

      <!--      <vue-pdf-embed ref="pdfRef" :source="pdfRef" class="vue-pdf-embed"-->
      <!--                     :page="state.pageNum"></vue-pdf-embed>-->
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import VuePdfEmbed from 'vue-pdf-embed'
import { getFileUrl } from '@/api/file'
import { useMagicKeys } from '@vueuse/core'

/**
 * 组件接收的属性：
 * @param fileId    文件ID
 */
const props = defineProps({
  // 文件ID
  fileId: Number
})

const pdfRef = ref()

const pdfViewCanvasWidthNum = ref(100)

// 图片外部容器宽度, 100 / 图片列数
let pdfViewCanvasWidth = computed(() => {
  return `${pdfViewCanvasWidthNum.value}%`
})

let isLoading = ref(true)
let page = ref<number>(0)
let pageCount = ref(1)
let pdfSource = ref(props.fileId ? getFileUrl(props.fileId) : null)
let showAllPages = ref(true)

watch(() => showAllPages.value, () => {
  page.value = showAllPages.value ? 0 : 1
})

const handleDocumentRender = () => {
  isLoading.value = false
  pageCount.value = pdfRef.value.doc._pdfInfo.numPages
}

const { ArrowLeft, ArrowRight, NumpadAdd, NumpadSubtract } = useMagicKeys()

// 支持按键翻页
watch(() => [ArrowLeft.value, ArrowRight.value], (value) => {
  if (isLoading.value) return
  if (showAllPages.value) return
  if (value[0] && page.value && page.value > 1) {
    page.value--
  }
  if (value[1] && page.value && page.value < pageCount.value) {
    page.value++
  }
})

// 支持按键缩放
watch(() => [NumpadSubtract.value, NumpadAdd.value], (value) => {
  if (value[0]) {
    pdfViewCanvasWidthNum.value -= 5
  }
  if (value[1]) {
    pdfViewCanvasWidthNum.value += 5
  }
})
</script>

<style scoped lang="scss">
.pdf-viewer {
  width: 100%;
}

.vue-pdf-embed {
  :deep(> div) {
    box-shadow: 0 2px 8px 4px rgba(0, 0, 0, 0.1);
    width: 100%;
    height: 100%;
    margin-top: 0.5rem;
  }

  :deep(canvas) {
    width: v-bind('pdfViewCanvasWidth') !important;
    height: 100% !important;
    margin: 0 auto;
  }
}

.app-header {
  box-shadow: 0 2px 8px 4px rgba(0, 0, 0, 0.1);
  background-color: #555;
  color: #ddd;
  display: flex;
  align-content: space-between;
  justify-content: space-between;
  padding: 0.5rem;
}

.app-content {
  padding: 24px 0;
}

.btn-plus-sub {
  padding-left: 10px;
  padding-right: 10px;
  cursor: pointer;
  color: #ddd;
  background-color: transparent;
}

.btn-pre-next-page {
  color: #ddd;
  cursor: pointer;
  background-color: transparent;
}
</style>