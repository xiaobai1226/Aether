<script setup lang="ts">
/**
 * 父类回调方法
 */
const emit = defineEmits(['generate-thumbnails'])
</script>

<template>
  <div class="top-op">
    <el-button type="primary" @click="emit('generate-thumbnails')">生成缩略图</el-button>
  </div>
</template>

<style scoped lang="scss">
.top-op {
  display: flex;
  align-items: center;
  gap: 5px;
}
</style>