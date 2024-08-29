<script setup lang="ts">
  import { computed, ref } from 'vue'
  import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
  import en from 'element-plus/dist/locale/en.mjs'

  const language = ref('zh-cn')
  // TODO 要把语言国际化做上，目前写死了中文
  const locale = computed(() => (language.value === 'zh-cn' ? zhCn : en))

  // 可同时显示的消息最大数量
  const config = ref({
    max: 1
  })
</script>

<template>
  <el-config-provider :locale="locale" :message="config">
      <!-- 一级路由出口组件 -->
      <RouterView />
  </el-config-provider>
</template>

<style scoped>

</style>
