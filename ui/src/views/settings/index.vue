<template>
  <div class="settings-container">
    <div class="settings-sidebar">
      <div class="sidebar-title">设置</div>
      <el-menu :default-active="currentRoute" router>
        <el-menu-item index="/settings/storage">
          <el-icon><FolderOpened /></el-icon>
          <span>存储源管理</span>
        </el-menu-item>
      </el-menu>
    </div>
    <div class="settings-content">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { FolderOpened } from '@element-plus/icons-vue'

const route = useRoute()
const currentRoute = computed(() => route.path)
</script>

<style scoped lang="scss">
.settings-container {
  display: flex;
  height: calc(100vh - 56px);
  background-color: #f5f5f5;

  .settings-sidebar {
    width: 200px;
    background-color: #fff;
    border-right: 1px solid #e0e0e0;
    
    .sidebar-title {
      padding: 20px;
      font-size: 18px;
      font-weight: bold;
      color: #333;
      border-bottom: 1px solid #e0e0e0;
    }

    .el-menu {
      border-right: none;
    }
  }

  .settings-content {
    flex: 1;
    padding: 20px;
    overflow-y: auto;
  }
}
</style>

