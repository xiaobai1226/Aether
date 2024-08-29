<script setup lang="ts">

import Navigation from "@/components/Navigation.vue";
import Utils from "@/utils/Utils";
import Table from "@/components/Table.vue";
import Icon from "@/components/Icon.vue";
import Preview from "@/components/preview/Preview.vue";
import {ref} from "vue";
import type {GetFileListByPaginationResponse} from "@/api/file/types";

const api = {
  loadDataList: "admin/loadFileList",
  delFile: "admin/delFile",
  createDownloadUrl: "/admin/createDownloadUrl",
  download: "/api/admin/download",
}

// 列定义
const columns: Column[] = [
  {
    label: "文件名",
    prop: "fileName",
    scopedSlots: "fileName"
  },
  {
    label: "发布人",
    prop: "nickName",
    width: 250
  },
  {
    label: "修改时间",
    prop: "updateTime",
    width: 200
  },
  {
    label: "文件大小",
    prop: "fileSize",
    scopedSlots: "fileSize",
    width: 200
  }
];

// 搜素
const search = () => {
  showLoading.value = true;
  loadDataList();
};

const tableData = ref<GetFileListByPaginationResponse>({
  list: [],
  pageNum: 1,
  pageSize: 10,
  total: 0,
  totalPage: 0
});

const tableOptions = ref({
  extHeight: 50,
  selectType: "checkbox"
});

/**
 * 选中的项ID
 */
const selectedFileIds = ref<number[]>([]);

/**
 * 选中行方法
 * @param rows 选中的数据项
 */
const rowSelected = (rows: UserFileInfo[]) => {
  selectedFileIds.value = [];
  rows.forEach((item) => {
    if (item.id) {
      selectedFileIds.value.push(item.userId + "_" + item.id);
    }
  });
};

const fileNameFuzzy = ref();
const showLoading = ref(true);

/**
 * 加载文件列表
 */
const loadDataList = () => {
  let getFileListByPaginationRequest: GetFileListByPaginationRequest = {
    pageNum: tableData.value.pageNum,
    pageSize: tableData.value.pageSize,
    // fileNameFuzzy: fileNameFuzzy.value,
    parentId: 0,
  };

  getFileListByPagination(getFileListByPaginationRequest).then(({data}) => {
    tableData.value = data;
  });
}

// 当前目录
const currentFolder = ref({fileId: 0});
const navChange = (data) => {
  const {categoryId, curFolder} = data;
  currentFolder.value = curFolder;
  showLoading.value = true;
  loadDataList();
}
</script>

<template>
  <div>
    <div class="top">
      <div class="top-op">
        <div class="search-panel">
          <el-input clearable placeholder="输入文件名搜索" v-model="fileNameFuzzy" @keyup.enter="search">
            <template #suffix>
              <i class="iconfont icon-search" @click="search"></i>
            </template>
          </el-input>
        </div>
        <div class="iconfont icon-refresh" @click="loadDataList"></div>
      </div>
      <!-- 导航 -->
      <Navigation ref="navigationRef" @navChange="navChange"/>
    </div>
    <div class="file-list" v-if="tableData.list && tableData.list.length > 0">
      <Table ref="dataTableRef" :columns="columns" :dataSource="tableData" :fetch="loadDataList" :initFetch="true"
             :options="tableOptions" @rowSelected="rowSelected">
        <template #fileName="{index, row}">
          <div class="file-item" @mouseenter="showActionBar(index)" @mouseleave="hideActionBar">
            <!-- 只有是图片或视频，并且已经是转码成功状态才展示图片-->
            <template v-if="(row.fileType == 3 || row.fileType == 1) && row.status == 2">
              <Icon :cover="row.fileCover" :width="32"></Icon>
            </template>
            <template v-else>
              <!-- TODO fileType参数上传文件时需确定类型 -->
              <!-- 如果是文件-->
              <Icon v-if="row.itemType == 1" :fileType="10"></Icon>
              <!-- 如果是文件夹-->
              <Icon v-if="row.itemType == 0" :fileType="0"></Icon>
            </template>
            <span class="file-name" :title="row.name" v-if="showEditPanelIndex != index">
              <span @click="preview(row)">{{ row.name }}</span>
              <span v-if="row.status == 0" class="transfer-status">转码中</span>
              <span v-if="row.status == 1" class="transfer-status transfer-fail">转码失败</span>
            </span>
            <!-- 新建文件夹或重命名输入栏 -->
            <div class="edit-panel" v-if="showEditPanelIndex == index">
              <el-input v-model.trim="editPanelFileName" ref="editPanelRef" :maxlength="190"
                        @keyup.enter="submitEditPanel(index)">
                <template #suffix>{{ editPanelFileNameSuffix }}</template>
              </el-input>
              <span :class="['iconfont icon-right', editPanelFileName ? '' : 'not-allow']"
                    @click="submitEditPanel(index)"></span>
              <span class="iconfont icon-error" @click="hideEditPanel(index)"></span>
            </div>
            <!-- 操作栏 -->
            <span class="op">
              <template v-if="showActionBarIndex == index && row.id && row.fileStatus == 1">
                <span class="iconfont icon-share" @click="share(row)">分享</span>
                <span class="iconfont icon-download" v-if="row.folderType == 0" @click="download(row)">下载</span>
                <span class="iconfont icon-delete" @click="delFile(row)">删除</span>
                <span class="iconfont icon-edit" @click="showEditPanel(index)">重命名</span>
                <span class="iconfont icon-move" @click="moveFolder">移动</span>
              </template>
            </span>
          </div>
        </template>
        <template #fileSize="{ index, row }">
          <span v-if="row.size">{{ Utils.sizeToStr(row.size) }}</span>
          <span v-else>-</span>
        </template>
      </Table>
    </div>
    <!-- 预览 -->
    <Preview ref="previewRef"></Preview>
  </div>
</template>

<style scoped lang="scss">
@import "@/styles/file.list.scss";

.search-panel {
  margin-left: 0 !important;
}

.file-list {
  margin-top: 10px;

  .file-item {
    .op {
      width: 120px;
    }
  }
}
</style>