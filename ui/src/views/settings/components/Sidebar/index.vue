<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

interface MenuItem {
  icon: string
  name: string
  path: string
  menuCode: string
}

const menus: MenuItem[] = [
  {
    icon: 'FolderOpened',
    name: '存储源管理',
    path: '/settings/storage',
    menuCode: 'storage'
  }
]

const currentMenu = computed(() => {
  const menu = menus.find(item => item.path === route.path)
  return menu || menus[0]
})

/**
 * 跳转路由
 */
const jump = (item: MenuItem) => {
  if (route.path === item.path) {
    return
  }
  router.push(item.path)
}
</script>

<template>
  <div class="settings-sidebar">
    <div class="sidebar-title">设置</div>
    <div class="menu-list">
      <div
        v-for="(item, index) in menus"
        :key="index"
        :class="['menu-item', item.menuCode === currentMenu.menuCode ? 'active' : '']"
        @click="jump(item)"
      >
        <div class="menu-content">
          <span class="menu-name">{{ item.name }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.settings-sidebar {
  width: 200px;
  background-color: #fff;
  border-right: 1px solid #f1f2f4;
  
  .sidebar-title {
    padding: 20px;
    font-size: 16px;
    font-weight: bold;
    color: #333;
    border-bottom: 1px solid #f1f2f4;
  }

  .menu-list {
    .menu-item {
      padding: 15px 20px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        background: #f3f3f3;
      }

      .menu-content {
        display: flex;
        align-items: center;

        .menu-name {
          font-size: 14px;
          color: #333;
        }
      }

      &.active {
        background: #ecf5ff;

        .menu-name {
          color: #409eff;
          font-weight: bold;
        }
      }
    }
  }
}
</style>