<script setup lang="ts">
import LayoutHeader from "@/views/layout/components/LayoutHeader/index.vue";
import Sidebar from "@/views/layout/components/Sidebar/index.vue";
</script>

<template>
  <LayoutHeader/>

  <div class="body">
    <!-- 侧边栏 -->
    <Sidebar/>

    <!-- 二级路由出口 -->
    <RouterView/>
  </div>
</template>

<style lang="scss" scoped>
.body {
  display: flex;
}
</style>