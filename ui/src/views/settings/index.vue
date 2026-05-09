<script setup lang="ts">
import Sidebar from '@/views/settings/components/Sidebar/index.vue'
</script>

<template>
  <div class="body">
    <div class="left-sider">
      <Sidebar />
    </div>

    <div class="body-content">
      <RouterView v-slot="{ Component }">
        <component :is="Component"></component>
      </RouterView>
    </div>
  </div>
</template>

<style scoped lang="scss">
.body {
  display: flex;
  flex: 1;

  .left-sider {
    border-right: 1px solid #f1f2f4;
  }

  .body-content {
    flex: 1;
    padding: 20px;
    overflow-y: auto;
  }
}
</style>