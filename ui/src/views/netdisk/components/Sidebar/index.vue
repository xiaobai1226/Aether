<script setup lang="ts">
import {computed, ref, watchEffect} from "vue";
import {useRoute, useRouter} from "vue-router";
import type {MenuChildren} from "@/views/old/types/MenuChildren";
import Utils from "@/utils/Utils";
import {useUserStore} from "@/stores/user";

// 从pinia获取用户数据
const userStore = useUserStore();

const route = useRoute();
const router = useRouter();

const menus: MenuChildren[] = [
  {
    icon: "all-fill",
    name: "全部",
    category: null
  },
  {
    icon: "video_fill_light",
    name: "视频",
    category: 1
  },
  {
    icon: "MusicAcc",
    name: "音频",
    category: 2
  },
  {
    icon: "image-fill",
    name: "图片",
    category: 3
  },
  {
    icon: "format-doc",
    name: "文档",
    category: 4
  },
  {
    icon: "more",
    name: "其他",
    category: 0
  }
];

const currentCategory = ref<number | null>(menus[0].category);
const currentPath = ref<string>(route.path);

const NETDISK_PATH = "/netdisk/main";
const RECYCLE_BIN_PATH = "/netdisk/recyclebin";
const SHARE_PATH = "/netdisk/share";

// 跳转路由
const jump = (routerPath: string, category: number | null) => {
  if (RECYCLE_BIN_PATH === routerPath) {
    if (currentPath.value === RECYCLE_BIN_PATH) {
      return;
    }

    router.push({
      path: routerPath
    });
    return;
  }

  if (SHARE_PATH === routerPath) {
    if (currentPath.value === SHARE_PATH) {
      return;
    }

    router.push({
      path: routerPath
    });
    return;
  }

  if (category == currentCategory.value && !route.query.path && currentPath.value === NETDISK_PATH) {
    return;
  }

  if (category == null) {
    const {category, path, ...query} = route.query;

    router.push({
      path: routerPath,
      query: query
    });
  } else {
    const {path, ...query} = route.query;

    router.push({
      path: routerPath,
      query: {
        ...query,
        category: category
      }
    });
  }

  currentCategory.value = category;
};

// 监听路由中category参数的变化
watchEffect(() => {
  const category = route.query.category;
  const path = route.path;

  if (Array.isArray(category)) {
    // 当遇见多个同名参数时，默认选取第一个
    currentCategory.value = Number(category[0]);
  } else if (category) {
    currentCategory.value = Number(category);
  } else {
    currentCategory.value = null;
  }

  currentPath.value = path;
});

// 使用空间百分比
const usedStoragePercentage = computed(() => {
  if (userStore.useSpaceInfo.totalStorage === 0) {
    return 0;
  }
  return Math.floor(userStore.useSpaceInfo.usedStorage / userStore.useSpaceInfo.totalStorage) * 10000;
});

// 获取用户存储空间使用情况
userStore.handleGetUserSpaceUsage();
</script>

<template>
  <div class="menu-sub-list">
    <div :class="['menu-item-sub', (currentPath == NETDISK_PATH && currentCategory == sub.category) ?  'active' : '']"
         v-for="(sub, index) in menus"
         :key="index"
         @click="jump(NETDISK_PATH, sub.category)">
      <span :class="['iconfont', 'icon-' + sub.icon]" v-if="sub.icon"></span>
      <span class="text">{{ sub.name }}</span>
    </div>

    <el-divider class="divider"/>

    <div :class="['menu-item-sub', currentPath == RECYCLE_BIN_PATH ?  'active' : '']"
         @click="jump(RECYCLE_BIN_PATH, null)">
      <span :class="['iconfont', 'icon-delete']"></span>
      <span class="text">回收站</span>
    </div>

    <el-divider class="divider"/>

<!--    <div :class="['menu-item-sub', currentPath == SHARE_PATH ?  'active' : '']"-->
<!--         @click="jump(SHARE_PATH, null)">-->
<!--      <span :class="['iconfont', 'icon-share']"></span>-->
<!--      <span class="text">分享记录</span>-->
<!--    </div>-->

    <!--    <div class="tips" v-if="currentMenu && currentMenu.tips">{{ currentMenu.tips }}</div>-->

    <div class="space-info">
      <div>空间使用</div>
      <div class="percent">
        <el-progress :percentage="usedStoragePercentage"
                     color="#409eff"></el-progress>
      </div>
      <div class="space-use">
        <div class="use">
          {{
            Utils.sizeToStr(userStore.useSpaceInfo.usedStorage)
          }}/{{ Utils.sizeToStr(userStore.useSpaceInfo.totalStorage) }}
        </div>
        <div class="iconfont icon-refresh" @click="userStore.handleGetUserSpaceUsage"></div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.menu-sub-list {
  width: 200px;
  height: 100%;
  padding: 20px 10px 0;
  position: relative;

  .menu-item-sub {
    text-align: center;
    line-height: 40px;
    border-radius: 5px;
    cursor: pointer;

    &:hover {
      background: #f3f3f3;
    }

    .iconfont {
      font-size: 17px;
      margin-right: 20px;
    }

    .text {
      font-size: 13px;
    }
  }

  .divider {
    width: 80%;
    margin: auto;
  }

  .active {
    background: #eef9fe;

    .iconfont {
      color: #05a1f5;
    }

    .text {
      color: #05a1f5;
    }
  }

  .tips {
    margin-top: 10px;
    color: #888888;
    font-size: 13px;
  }

  .space-info {
    position: absolute;
    bottom: 10px;
    width: calc(100% - 20px);
    padding: 0 5px;

    .percent {
      padding-right: 10px;
    }

    .space-use {
      margin-top: 5px;
      color: #7e7e7e;
      display: flex;
      justify-content: space-around;

      .use {
        flex: 1;
      }

      .iconfont {
        cursor: pointer;
        margin-right: 20px;
        color: #05a1f5;
      }
    }
  }
}
</style>