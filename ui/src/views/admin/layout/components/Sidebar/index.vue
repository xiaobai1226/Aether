<script setup lang="ts">

</script>

<template>
  <el-menu class="menu" :router="true">
    <el-menu-item index="/admin/dashboard">
      <template #title>
        <span>仪表板</span>
      </template>
    </el-menu-item>
    <el-menu-item index="/admin/userlist">
      <template #title>
        <span>用户管理</span>
      </template>
    </el-menu-item>
    <el-menu-item index="/admin/filelist">
      <template #title>
        <span>文件管理</span>
      </template>
    </el-menu-item>
  </el-menu>
</template>

<style scoped lang="scss">
.menu {
  width: 200px;
  height: calc(100vh - 56px);
  // 防止侧边栏被压缩
  flex-shrink: 0;

  .el-menu-item {
    // 让菜单项内的内容水平居中
    display: flex;
    justify-content: center;
    align-items: center;
  }
}
</style>