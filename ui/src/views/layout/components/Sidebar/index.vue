<script setup lang="ts">
import {ref, onMounted, watch} from "vue";
import {useRouter, useRoute} from "vue-router";
import type {Menu} from "@/views/old/types/Menu";

const router = useRouter();
const route = useRoute();

const menus: Menu[] = [
  {
    icon: "main",
    name: "首页",
    category: "main",
    path: "/",
    allShow: true,
    menuCode: ""
  },
  // {
  //   icon: "share",
  //   name: "分享",
  //   menuCode: "share",
  //   path: "/myshare",
  //   allShow: true
  // },
  // {
  //   icon: "delete",
  //   name: "回收站",
  //   menuCode: "recycle",
  //   path: "/recycle",
  //   tips: "回收站中的文件会在10天后自动删除",
  //   allShow: true
  // },
  {
    icon: "settings",
    name: "设置",
    category: "settings",
    menuCode: "settings",
    path: "/settings",
    allShow: true
  },
];

const currentMenu = ref<Menu>(menus[0]);

const currentPath = ref<string>(menus[0].path);

/**
 * 根据路径更新菜单高亮状态
 * @param path - 当前路径
 */
const updateMenuByPath = (path: string) => {
  // 找到匹配的菜单项（支持路径前缀匹配）
  const matchedMenu = menus.find(item => {
    if (item.path === path) return true;
    // 支持子路径匹配（如 /settings/storage 匹配 /settings）
    if (item.path !== '/' && path.startsWith(item.path)) return true;
    return false;
  }) || menus.find(item => item.path === '/'); // 默认首页

  if (matchedMenu) {
    currentMenu.value = matchedMenu;
    currentPath.value = path;
  }
};

const setMenu = (menuCode: string, path: string) => {
  if (menuCode) {
    currentMenu.value = menus.find(item => item.menuCode == menuCode) as Menu;
  }
  currentPath.value = path;
};

/**
 * 跳转路由
 * @param menuCode
 * @param path
 */
const jump = (menuCode: string, path: string) => {
  if (!path || (menuCode == currentMenu.value.menuCode && path == currentPath.value)) {
    return;
  }
  router.push(path);
  setMenu(menuCode, path);
};

// 监听路由变化，自动更新菜单高亮
watch(() => route.path, (newPath) => {
  updateMenuByPath(newPath);
}, { immediate: true });

// 组件挂载时初始化菜单状态
onMounted(() => {
  updateMenuByPath(route.path);
});
</script>

<template>
  <div class="menu-list">
    <div :class="['menu-item', item.menuCode == currentMenu.menuCode ? 'active' : '']" v-for="(item, index) in menus"
         :key="index" @click="jump(item.menuCode, item.path)">
      <div :class="['iconfont', 'icon-' + item.icon]"></div>
      <div class="text">{{ item.name }}</div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.menu-list {
  height: calc(100vh - 56px);
  width: 80px;
  box-shadow: 0 3px 10px 0 rgb(0 0 0 / 6%);
  border-right: 1px solid #f1f2f4;

  .menu-item {
    text-align: center;
    font-size: 14px;
    font-weight: bold;
    padding: 20px 0;
    cursor: pointer;

    &:hover {
      background: #f3f3f3;
    }

    .iconfont {
      font-weight: normal;
      font-size: 28px;
    }
  }

  .active {
    .iconfont {
      color: #06a7ff;
    }

    .text {
      color: #06a7ff;
    }
  }
}
</style>