<script setup lang="ts">
import {ref} from "vue";
import Table from "@/components/Table.vue";
import Utils from "@/utils/Utils";
import type {
  DeleteOrRestoreRequest,
  GetRecycleBinListByPageRequest,
  GetRecycleBinListByPageResponse,
  RecycleBinFileInfo
} from "@/api/recycleBin/types";
import {getRecycleBinListByPage, del, restore} from "@/api/recycleBin";
import Icon from "@/components/Icon.vue";
import Confirm from "@/utils/Confirm";
import {ElMessage} from "element-plus";
import {ResultErrorMsgEnum} from "@/enums/ResultErrorMsgEnum";

/**
 * 列定义
 */
const columns = [
  {
    label: "文件名",
    prop: "fileName",
    scopedSlots: "fileName"
  },
  {
    label: "删除时间",
    prop: "deleteTime",
    width: 200
  },
  {
    label: "文件大小",
    prop: "fileSize",
    scopedSlots: "fileSize",
    width: 200
  },
  {
    label: "有效时间",
    prop: "validityPeriod",
    scopedSlots: "validityPeriod",
    width: 200
  }
];

/**
 * 初始化列表数据
 */
const tableData = ref<GetRecycleBinListByPageResponse>({
  list: [],
  pageNum: 1,
  pageSize: 15,
  total: 0,
  totalPage: 0
});
const tableOptions = {
  extHeight: 20,
  selectType: "checkbox"
}

/**
 * 加载文件列表
 */
const loadDataList = () => {
  const getRecycleBinListByPageRequest: GetRecycleBinListByPageRequest = {
    pageNum: tableData.value.pageNum,
    pageSize: tableData.value.pageSize
  };

  getRecycleBinListByPage(getRecycleBinListByPageRequest).then(({data}) => {
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

/**
 * 选中的项ID
 */
const selectedRecycleIds = ref<string[]>([]);

/**
 * 选中行方法
 * @param rows 选中的数据项
 */
const rowSelected = (rows: RecycleBinFileInfo[]) => {
  selectedRecycleIds.value = [];
  rows.forEach((item) => {
    if (item.id) {
      selectedRecycleIds.value.push(item.recycleId);
    }
  });
};

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
 * 还原选中的文件
 */
const restoreFile = (recycleBinFileInfo: RecycleBinFileInfo) => {
  if (!recycleBinFileInfo || !recycleBinFileInfo.recycleId) {
    ElMessage.warning("请选择要还原的文件");
    return
  }

  const currentRecycleIds: Array<string> = [];
  currentRecycleIds.push(recycleBinFileInfo.recycleId);

  const message = `你确定要还原 ${recycleBinFileInfo.name} 吗？`;

  handleRestore(currentRecycleIds, message);
}

/**
 * 批量还原选中的文件
 */
const restoreFileBatch = () => {
  if (selectedRecycleIds.value.length == 0) {
    ElMessage.warning("请选择要还原的文件");
    return
  }
  let currentRecycleIds: Array<string> = [];
  currentRecycleIds = currentRecycleIds.concat(selectedRecycleIds.value);

  const message = "你确定要还原所选的文件吗？";

  handleRestore(currentRecycleIds, message);
}

/**
 * 还原文件
 *
 * @param currentRecycleIds 要还原的回收ID集合
 * @param message 提示信息
 */
const handleRestore = (currentRecycleIds: Array<string>, message: string) => {
  Confirm(message, () => {
    const data: DeleteOrRestoreRequest = {
      recycleIds: currentRecycleIds.join(",")
    };

    restore(data).then(() => {
      loadDataList();
    });
  });
}

/**
 * 删除选中的文件
 */
const delFile = (recycleBinFileInfo: RecycleBinFileInfo) => {
  if (!recycleBinFileInfo || !recycleBinFileInfo.recycleId) {
    ElMessage.warning(ResultErrorMsgEnum.ERROR_DEL_CONTENT_EMPTY);
    return
  }

  const currentRecycleIds: Array<string> = [];
  currentRecycleIds.push(recycleBinFileInfo.recycleId);

  const message = `文件删除后将无法恢复，您确认要彻底删除 ${recycleBinFileInfo.name} 吗？`;

  handleDelete(currentRecycleIds, message);
}

/**
 * 批量删除选中的文件
 */
const delFileBatch = () => {
  if (selectedRecycleIds.value.length == 0) {
    ElMessage.warning(ResultErrorMsgEnum.ERROR_DEL_CONTENT_EMPTY);
    return
  }
  let currentRecycleIds: Array<string> = [];
  currentRecycleIds = currentRecycleIds.concat(selectedRecycleIds.value);

  const message = "文件删除后将无法恢复，您确定要彻底删除所选的文件吗？";

  handleDelete(currentRecycleIds, message);
}

/**
 * 清空回收站
 */
const clearRecycleBin = () => {
  let currentRecycleIds: Array<string> = [];
  currentRecycleIds[0] = "-1";

  const message = "文件删除后将无法恢复，您确定要清空回收站吗？";

  handleDelete(currentRecycleIds, message);
}

/**
 * 删除文件
 *
 * @param currentRecycleIds 要删除的回收ID集合
 * @param message 提示信息
 */
const handleDelete = (currentRecycleIds: Array<string>, message: string) => {
  Confirm(message, () => {
    const data: DeleteOrRestoreRequest = {
      recycleIds: currentRecycleIds.join(",")
    };

    del(data).then(() => {
      loadDataList();
    });
  });
}

/**
 * 计算有效期
 */
const calculateValidityPeriod = (deleteTime: string): string => {

// 将删除时间字符串转换为Date对象
  const deleteTimeDate = new Date(deleteTime);

// 计算有效期结束时间（增加10天）
  deleteTimeDate.setDate(deleteTimeDate.getDate() + 10);

// 获取当前时间
  const currentTimeDate = new Date();

  const remainingTime = deleteTimeDate.getTime() - currentTimeDate.getTime();
  const remainingDays = Math.ceil(remainingTime / (1000 * 60 * 60 * 24));

  return remainingDays + "天";
}

// 拉取数据
loadDataList();
</script>

<template>
  <div>
    <div class="top">
      <el-button type="primary" @click="clearRecycleBin">
        <span class="iconfont icon-delete"></span>
        清空回收站
      </el-button>
      <el-button type="success" v-show="selectedRecycleIds.length > 0" @click="restoreFileBatch">
        <span class="iconfont icon-restore"></span>
        还原
      </el-button>
      <el-button type="danger" v-show="selectedRecycleIds.length > 0" @click="delFileBatch">
        <span class="iconfont icon-delete"></span>
        删除
      </el-button>
    </div>
    <div class="file-list" v-if="tableData.list && tableData.list.length > 0">
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

            <span class="file-name" :title="row.name">{{ row.name }}</span>
            <span class="op">
              <template v-if="showActionBarIndex == index && row.id">
                <span class="iconfont icon-restore" @click="restoreFile(row)">还原</span>
                <span class="iconfont icon-delete" @click="delFile(row)">删除</span>
              </template>
            </span>
          </div>
        </template>
        <template #fileSize="{ row }">
          <span v-if="row.size">{{ Utils.sizeToStr(row.size) }}</span>
          <span v-else>-</span>
        </template>
        <template #validityPeriod="{ row }">
          <span v-if="row.deleteTime">{{ calculateValidityPeriod(row.deleteTime) }}</span>
        </template>
      </Table>
    </div>
    <div class="no-data" v-else>
      <div class="no-data-inner">
        <Icon iconName="no_data" :width="120" fit="fill"></Icon>
        <div class="tips">您的回收站为空哦</div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
@import "@/styles/file.list.scss";

.file-list {
  margin-top: 10px;

  .file-item {
    .op {
      width: 120px;
    }
  }
}
</style>