<script setup lang="ts">
import { ref, computed } from 'vue'
import type { PropType } from 'vue'

export interface Config {
  width: number,
  height: number,
  iconWidth: number
}

const props = defineProps({
  /**
   * 展示数据
   */
  dataSource: {
    type: Object,
    default: () => {
      return {
        list: [],
        pageNum: 1,
        pageSize: 15,
        total: 0
      }
    }
  },
  config: {
    type: Object as PropType<Config>,
    default: () => ({
      width: 128,
      height: 166,
      iconWidth: 60
    })
  },
  options: {
    type: Object,
    default: function() {
      return {
        extHeight: 0,
        showIndex: false
      }
    }
  },
  fetch: Function,
  initFetch: {
    type: Boolean,
    default: true
  },
  loading: {
    type: Boolean,
    default: false
  },
  sortChange: Function,

  click: Function
})

// 顶部 60，内容区域距离顶部20，内容上下间距15*2 分页区域高度46
const topHeight = 60 + 20 + 30 + 46

const tableHeight = ref(
  props.options.tableHeight ? props.options.tableHeight : window.innerHeight - topHeight - props.options.extHeight
)

/**
 * 加载下一页
 */
const loadNextPage = () => {
  props.dataSource.pageNum = props.dataSource.pageNum + 1
  props.fetch()
}

/**
 * 是否没有更多
 */
const noMore = computed(() => props.dataSource.list.length >= props.dataSource.total)

/**
 * 是否禁用
 */
const disabled = computed(() => props.loading || noMore.value)

/**
 * loading状态
 */
const loadingStatus = computed(() => props.loading && props.dataSource.pageNum !== 1)
</script>

<template>
  <div class="container" v-infinite-scroll="loadNextPage" :infinite-scroll-disabled="disabled"
       :style="{height: tableHeight + 'px'}">
    <div class="file-items-container">
      <template v-for="(item, index) in dataSource.list">
        <slot name="content" :item="item" :index="index" @click="click"></slot>
      </template>
    </div>
    <div v-if="loadingStatus" class="loading" v-loading="loadingStatus" element-loading-text="加载中..." />
    <div v-if="noMore" class="no_more">没有更多了</div>
  </div>
</template>

<style scoped lang="scss">
.container {
  display: flex;
  flex-direction: column;
  overflow: auto;
}

.file-items-container {
  display: flex;
  flex-wrap: wrap;
}

.loading {
  height: 70px;
  margin-bottom: 20px;
}

.no_more {
  font-size: 14.5px;
  height: 50px;
  text-align: center;
  line-height: 50px;
  color: #25252B5C;
  margin-bottom: 20px;
}
</style>