<script setup lang="ts">
import {ref} from "vue";
import {useRoute, useRouter} from "vue-router";

const router = useRouter();
const route = useRoute();

const props = defineProps({
  // 是否监听路由
  watchPath: {
    type: Boolean,
    default: true
  }
});

/**
 * 目录集合
 */
const folderList = ref<Array<string>>([]);

// 打开文件夹
const openFolder = (name: string) => {
  folderList.value.push(name);
  setPath();
};

/**
 * 更新目录集合
 * @param path 当前路径
 */
const updateFolderList = (path: string | null) => {
  if (!path) {
    folderList.value = [];
  } else {
    folderList.value = path.split("/").filter(Boolean);
  }
};

defineExpose({openFolder, updateFolderList});

// 返回上一级
const backParent = () => {
  setCurrentFolder(folderList.value.length - 2);
};

// 点击导航，设置当前目录
const setCurrentFolder = (index: number) => {
  if (index == -1) {
    // 返回全部
    folderList.value = [];
  } else {
    folderList.value.splice(index + 1);
  }
  setPath();
};

const emit = defineEmits(["navChange"]);
const doCallback = () => {
  let path = null;
  if (folderList.value.length > 0) {
    path = "/" + folderList.value.join("/");
  }
  emit("navChange", path);
}

/**
 * 设置路径
 */
const setPath = () => {
  if (!props.watchPath) {
    doCallback();
    return;
  }

  if (folderList.value.length == 0) {
    router.push({path: route.path});
  } else {
    router.push({
      path: route.path,
      query: {path: "/" + folderList.value.join("/")}
    });
  }
};

// 监听路由中path参数的变化
// TODO 要确定加不加 {immediate: true}
// watch(
//     () => route.query.path, (path, prevPath) => {
//       if (!path) {
//         folderList.value = [];
//
//       } else if (typeof path === 'string') {
//         folderList.value = path.split("/").filter(Boolean);
//
//         // doCallback();
//         // router.push({
//         //   path: route.path,
//         //   query: pathArray.length == 0 ? "" : {path: "/" + pathArray.join("/")}
//         // });
//       }
//     },
//     {immediate: true}
// );
</script>

<template>
  <div class="top-navigation">
    <template v-if="folderList.length > 0">
      <span class="back link" @click="backParent">返回上一层</span>
      <el-divider direction="vertical"></el-divider>
    </template>
    <span v-if="folderList.length == 0" class="all-file">全部文件</span>
    <span v-if="folderList.length > 0" class="link" @click="setCurrentFolder(-1)">全部文件</span>
    <template v-for="(name, index) in folderList" :key="index">
      <span class="iconfont icon-right-arrow"></span>
      <span class="link" v-if="index < folderList.length - 1" @click="setCurrentFolder(index)">{{
          name
        }}</span>
      <span v-if="index == folderList.length - 1" class="text">{{ name }}</span>
    </template>
  </div>
</template>

<style scoped lang="scss">
.top-navigation {
  font-size: 13px;
  display: flex;
  align-items: center;
  line-height: 40px;

  .all-file {
    font-weight: bold;
  }

  .link {
    color: #06a7ff;
    cursor: pointer;
  }

  .icon-right-arrow {
    color: #06a7ff;
    padding: 0 5px;
    font-size: 13px;
  }
}
</style>