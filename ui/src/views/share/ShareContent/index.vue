<script setup lang="ts">
import {ref, watch, nextTick} from "vue";
import {useRouter, useRoute} from "vue-router";
import Navigation from "@/components/Navigation.vue";
import Table, {type Column} from "@/components/Table.vue";
import Utils from "@/utils/Utils";
import FolderSelect from "@/components/FolderSelect.vue";
import {getShareLoginInfo, getShareFileListByShareIdPagination} from "@/api/v1/share";
import type {
  ShareInfoResponse,
  GetShareFileListByShareIdPageRequest, ShareFileInfo
} from "@/api/v1/share/types";
import type {GetFileListByPageResponse} from "@/api/v1/file/types";

const router = useRouter();
const route = useRoute();

/**
 * 列表列定义
 */
const columns: Column[] = [
  {
    label: "文件名",
    prop: "fileName",
    scopedSlots: "fileName"
  },
  {
    label: "修改时间",
    prop: "createTime",
    width: 200
  },
  {
    label: "文件大小",
    prop: "fileSize",
    scopedSlots: "fileSize",
    width: 200
  }
];

const shareId = route.params.shareId as string;
const shareInfo = ref<ShareInfoResponse | null>(null);

/**
 * 获取分享信息
 */
const handleGetShareLoginInfo = () => {
  getShareLoginInfo(shareId).then(({data}) => {
    if (data) {
      shareInfo.value = data;

      // 获取分享文件列表
      loadDataList();
    } else {
      router.push("/shareCheck/" + shareId);
      return;
    }
  });
}

/**
 * 初始化列表数据
 */
const tableData = ref<GetFileListByPageResponse>({
  list: [],
  pageNum: 1,
  pageSize: 15,
  total: 0,
  totalPage: 0
});

const tableOptions = ref({
  extHeight: 80,
  selectType: "checkbox"
});

/**
 * 当前目录路径
 */
const currentPath = ref<string | null>(null);

/**
 * 加载文件列表
 */
const loadDataList = () => {
  const getShareFileListByShareIdPaginationRequest: GetShareFileListByShareIdPageRequest = {
    pageNum: tableData.value.pageNum,
    pageSize: tableData.value.pageSize,
    shareId: shareId,
  };

  if (currentPath.value != null) {
    getShareFileListByShareIdPaginationRequest.path = currentPath.value;
  }

  getShareFileListByShareIdPagination(getShareFileListByShareIdPaginationRequest).then(({data}) => {
    if (data == null) {
      tableData.value = {
        list: [],
        pageNum: 1,
        pageSize: 15,
        total: 0,
        totalPage: 0
      }
    } else {
      tableData.value = data;
    }
  });
}

// 获取分享信息
handleGetShareLoginInfo();

/**
 * 导航Ref实例
 */
const navigationRef = ref();

/**
 * 显示操作栏的索引 -1 为不展示，其他为要展示行的索引
 */
const showActionBarIndex = ref<number>(-1);
/**
 * 展示操作栏
 */
const showActionBar = (index: number) => {
  showActionBarIndex.value = index;
};
/**
 * 隐藏操作栏
 */
const hideActionBar = () => {
  showActionBarIndex.value = -1;
}

/**
 * 选中的项ID
 */
const selectedFileIds = ref<number[]>([]);

/**
 * 选中行方法
 * @param rows 选中的数据项
 */
const rowSelected = (rows: ShareFileInfo[]) => {
  selectedFileIds.value = [];
  rows.forEach((item) => {
    if (item.id) {
      selectedFileIds.value.push(item.id);
    }
  });
};

/**
 * 文件夹选择Ref
 */
const folderSelectRef = ref();

/**
 * 监听路由中category，path参数的变化
 */
watch(
    () => route.query, (newQuery, oldQuery) => {
      if (!route.path.startsWith("/share")) {
        return;
      }

      const path = newQuery.path;

      if (Array.isArray(path)) {
        currentPath.value = path[0];
      } else if (path) {
        currentPath.value = path as string;
      } else {
        currentPath.value = null;
      }

      nextTick().then(() => {
        if (navigationRef.value) {
          navigationRef.value.updateFolderList(currentPath.value);
        }
      });

      // 加载数据
      if (shareInfo.value) {
        loadDataList();
      }
    }, {immediate: true}
);

// TODO 实现预览功能
/**
 * 预览
 * @param row
 */
const preview = (row: ShareFileInfo) => {
  // 目录
  if (row.itemType == 0) {
    navigationRef.value.openFolder(row.name);
    return;
  }

  previewRef.value.showPreview(row, 0);
};

const previewRef = ref();

// TODO 实现下载功能

/**
 * 保存到我的网盘
 */
const save2MyPanFileIdArray = [];
const save2MyPan = (row: UserFileInfo) => {

  // TODO 判断是否登录，完了要跳转回来
  router.push("/login?redirectUrl=" + route.path);
}

// TODO 取消分享

// TODO jump跳转首页
</script>

<template>
  <div class="share">
    <div class="header">
      <div class="header-content">
        <div class="logo" @click="jump">
          <span class="iconfont icon-NAS"></span>
          <span class="name">Netdisk</span>
        </div>
      </div>
    </div>
  </div>
  <div class="share-body">
    <!-- TODO 加载动画 -->
    <!--    <template v-if="Object.keys(shareInfo).length == 0">-->
    <!--      <div class="loading" v-loading="Object.keys(shareInfo).length == 0"></div>-->
    <!--    </template>-->
    <!--    <template v-else>-->
    <template v-if="shareInfo">
      <div class="share-panel">
        <div class="share-user-info">
          <div class="avatar">
            <Avatar :avatar="shareInfo.avatar" :width="50"/>
          </div>
          <div class="share-info">
            <div class="user-info">
              <span class="nick-name">{{ shareInfo.nickname }}</span>
              <span class="share-time">分享于 {{ shareInfo.shareTime }}</span>
            </div>
            <div class="file-name">分享文件：{{ shareInfo.name }}</div>
          </div>
        </div>
        <div class="share-op-btn">
          <el-button type="primary" @click="download" :disabled="selectedFileIds.length == 0">
            <span class="iconfont icon-download"></span>
            下载
          </el-button>
          <el-button type="primary" @click="cancelShare" v-if="shareInfo.currentUser">
            <span class="iconfont icon-cancel"></span>
            取消分享
          </el-button>
          <el-button v-else type="primary" @click="save2MyPan" :disabled="selectedFileIds.length == 0">
            <span class="iconfont icon-import"></span>
            保存到我的网盘
          </el-button>
        </div>
      </div>
      <Navigation ref="navigationRef" :shareId="shareId"/>
      <div class="file-list">
        <Table ref="dataTableRef" :columns="columns" :dataSource="tableData" :fetch="loadDataList" :initFetch="false"
               :options="tableOptions" @rowSelected="rowSelected">
          <template #fileName="{index, row}">
            <div class="file-item" @mouseenter="showActionBar(index)" @mouseleave="hideActionBar">
              <!-- 只有图片或视频，并且已经是转码成功状态才展示图片-->
              <template v-if="row.category == 1 || row.category == 3">
                <Icon :thumbnail="row.thumbnail" :width="32"></Icon>
              </template>
              <template v-else>
                <!-- 如果是文件-->
                <Icon v-if="row.itemType == 1" :fileType=row.fileType></Icon>
                <!-- 如果是文件夹-->
                <Icon v-if="row.itemType == 0" :fileType="-1"></Icon>
              </template>
              <span class="file-name" :title="row.name">
              <span @click="preview(row)">{{ row.name }}</span>
            </span>
              <!-- 操作栏 -->
              <span class="op" v-if="showActionBarIndex == index && row.id">
                <span class="iconfont icon-download" @click="download(row)">下载</span>
                <span class="iconfont icon-import" v-if="!shareInfo.currentUser"
                      @click="save2MyPan(row)">保存到我的网盘</span>
            </span>
            </div>
          </template>
          <template #fileSize="{ row }">
            <span v-if="row.size">{{ Utils.sizeToStr(row.size) }}</span>
            <span v-else>-</span>
          </template>
        </Table>
      </div>
    </template>
    <FolderSelect ref="folderSelectRef" @folderSelect="save2MyPanDone"></FolderSelect>
    <!-- 预览 -->
    <!--    <Preview ref="previewRef"></Preview>-->
  </div>
</template>

<style scoped lang="scss">
@import "@/styles/file.list";

.header {
  width: 100%;
  position: fixed;
  background: #0c95f7;
  height: 50px;

  .header-content {
    width: 70%;
    margin: 0 auto;
    color: #fff;
    line-height: 50px;

    .logo {
      display: flex;
      align-items: center;
      cursor: pointer;

      .icon-NAS {
        font-size: 40px;
      }

      .name {
        font-weight: bold;
        margin-left: 5px;
        font-size: 25px;
      }
    }
  }
}

.share-body {
  width: 70%;
  margin: 0 auto;
  padding-top: 50px;

  .loading {
    height: calc(100vh / 2);
    width: 100%;
  }

  .share-panel {
    margin-top: 20px;
    display: flex;
    justify-content: space-around;
    border-bottom: 1px solid #ddd;
    padding-bottom: 10px;

    .share-user-info {
      flex: 1;
      display: flex;
      align-items: center;

      .avatar {
        margin-right: 5px;
      }

      .share-info {
        .user-info {
          display: flex;
          align-items: center;

          .nick-name {
            font-size: 15px;
          }

          .share-time {
            margin-left: 20px;
            font-size: 12px;
          }
        }

        .file-name {
          margin-top: 10px;
          font-size: 12px;
        }
      }
    }
  }
}

.file-list {
  margin-top: 10px;

  .file-item {
    .op {
      width: 170px;
    }
  }
}
</style>