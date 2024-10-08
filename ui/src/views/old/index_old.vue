<script setup lang="ts">
import {ref, watch} from "vue";
import {useRoute, useRouter} from "vue-router";
import type {Menu} from "@/views/old/types/Menu";
import type {MenuChildren} from "@/views/old/types/MenuChildren";

const route = useRoute();
const router = useRouter();

const userInfo = ref({nickName: "张三"});

const menus: Menu[] = [
  {
    icon: "main",
    name: "首页",
    category: "main",
    path: "/main/all",
    allShow: true,
    menuCode: "",
    children: [
      {
        icon: "all",
        name: "全部",
        category: "all",
        path: "/main/all"
      },
      {
        icon: "video",
        name: "视频",
        category: "video",
        path: "/main/video"
      },
      {
        icon: "music",
        name: "音频",
        category: "music",
        path: "/main/music"
      },
      {
        icon: "image",
        name: "图片",
        category: "image",
        path: "/main/image"
      },
      {
        icon: "doc",
        name: "文档",
        category: "doc",
        path: "/main/doc"
      },
      {
        icon: "more",
        name: "其他",
        category: "others",
        path: "/main/others"
      },
    ]
  },
  {
    icon: "share",
    name: "分享",
    menuCode: "share",
    path: "/myshare",
    allShow: true,
    children: [
      {
        name: "分享记录",
        path: "/myshare"
      }
    ]
  },
  {
    icon: "delete",
    name: "回收站",
    menuCode: "recycle",
    path: "/recycle",
    tips: "回收站中的文件会在10天后自动删除",
    allShow: true,
    children: [
      {
        name: "删除的文件",
        path: "/recycle"
      }
    ]
  },
  {
    icon: "settings",
    name: "设置",
    menuCode: "settings",
    path: "/settings/fileList",
    allShow: false,
    children: [
      {
        name: "用户文件",
        path: "/settings/fileList"
      },
      {
        name: "用户管理",
        path: "/settings/userList"
      },
      {
        name: "系统设置",
        path: "/settings/sysSetting"
      }
    ]
  },
];

const currentMenu = ref<Menu>({
  children: [{
    icon: "",
    name: "",
    path: "",
    category: ""
  }],
  percent: 0,
  tips: "",
  menuCode: "",
  icon: "",
  name: "",
  category: "",
  path: "",
  allShow: true,
});

const currentPath = ref();

const jump = (data: Menu | MenuChildren) => {
  if (!data.path || data.menuCode == currentMenu.value.menuCode) {
    return;
  }
  router.push(data.path);
};

const setMenu = (menuCode: string, path: string) => {
  const menu: Menu = menus.find(item => item.menuCode == menuCode) as Menu;
  currentMenu.value = menu;
  currentPath.value = path;
};

watch(
    () => route,
    (newVal, oldVal) => {
      if (newVal.meta.menuCode) {
        setMenu(newVal.meta.menuCode.toString(), newVal.path);
      }
    },
    {immediate: true, deep: true}
);
</script>

<template>
  <div class="framework">
    <div class="header">
      <div class="logo">
        <span class="iconfont icon-NAS"></span>
        <div class="name">Netdisk</div>
      </div>
      <div class="right-panel">
        <el-popover :width="800" trigger="click" :v-model:visible="true" :offset="20" transition="none" :hide-after="0"
                    :popper-style="{ padding: '0px' }">
          <template #reference>
            <span class="iconfont icon-transfer"></span>
          </template>
          <template #default>这里是上传区域</template>
        </el-popover>

        <el-dropdown>
          <div class="user-info">
            <div class="avatar"></div>
            <span class="nick-name">{{ userInfo.nickName }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item>修改头像</el-dropdown-item>
              <el-dropdown-item>修改密码</el-dropdown-item>
              <el-dropdown-item>退出</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    <div class="body">
      <div class="left-sider">
        <div class="menu-list">
          <div :class="['menu-item', item.menuCode == currentMenu.menuCode ? 'active' : '']" v-for="item in menus"
               @click="jump(item)">
            <div :class="['iconfont', 'icon-' + item.icon]"></div>
            <div class="text">{{ item.name }}</div>
          </div>
        </div>
        <div class="menu-sub-list">
          <div :class="['menu-item-sub', currentPath == sub.path ?  'active' : '']" v-for="sub in currentMenu.children"
               @click="jump(sub)">
            <span :class="['iconfont', 'icon-' + sub.icon]" v-if="sub.icon"></span>
            <span class="text">{{ sub.name }}</span>
          </div>
          <div class="tips" v-if="currentMenu && currentMenu.tips">{{ currentMenu.tips }}</div>
          <div class="space-info">
            <div>空间使用</div>
            <div class="percent">已使用{{ currentMenu.percent }}%</div>
            <!--            <el-progress :percentage="currentMenu.percent" text-inside></el-progress>-->
            <!--            <div class="space-use">-->
            <!--              <div class="use">已使用{{currentMenu.use}}</div>-->
            <!--              <div class="total">共{{currentMenu.total}}</div>-->
            <!--              <div class="iconfont icon-refresh" @click="refreshSpace"></div>-->
            <!--            </div>-->
            <div/>
          </div>
        </div>
        <body class="body-content">
        <RouterView v-slot="{Component}">
          <component :is="Component"></component>
        </RouterView>
        </body>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.header {
  box-shadow: 0 3px 10px 0 rgb(0 0 0 / 6%);
  height: 56px;
  padding-left: 24px;
  padding-right: 24px;
  position: relative;
  z-index: 200;
  display: flex;
  align-items: center;
  justify-content: space-between;

  .logo {
    display: flex;
    align-items: center;

    .icon-pan {
      font-size: 40px;
      color: #1296db;
    }

    .name {
      font-weight: bold;
      margin-left: 5px;
      font-size: 25px;
      color: #05a1f5;
    }
  }

  .right-panel {
    display: flex;
    align-items: center;

    .icon-transfer {
      cursor: pointer;
    }

    .user-info {
      margin-left: 10px;
      display: flex;
      align-items: center;
      cursor: pointer;

      .avatar {
        margin: 0px 5px 0px 15px;
      }

      .nick-name {
        color: #05a1f5;
      }
    }
  }
}

.body {
  display: flex;

  .left-sider {
    border-right: 1px solid #f1f2f4;
    display: flex;

    .menu-list {
      height: calc(100vh - 56px);
      width: 80px;
      box-shadow: 0 3px 10px 0 rgb(0 0 0 / 6%);
      border-right: 1px solid #f1f2f4;

      .menu-item {
        text-align: center;
        font-size: 14px;
        font-weight: bold;
        padding: 20px 0px;
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

    .menu-sub-list {
      width: 200px;
      padding: 20px 10px 0px;
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
          font-size: 14px;
          margin-right: 20px;
        }

        .text {
          font-size: 13px;
        }
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
        width: 100%;
        padding: 0px 5px;

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
  }

  .body-content {
    flex: 1;
    width: 0;
    padding-left: 20px;
  }
}
</style>