<script setup lang="ts">
import {ref} from "vue";
import Table from "@/components/Table.vue";
import Icon from "@/components/Icon.vue";
import {ElMessage} from "element-plus";
import type {
  CancelShareFileRequest,
  GetShareListByPageRequest,
  GetShareListByPageResponse,
  ShareFileInfo
} from "@/api/share/types";
import {cancel, getShareListByPage} from "@/api/share";
import Confirm from "@/utils/Confirm";

/**
 * 列定义
 */
const columns = [
  {
    label: "分享文件",
    prop: "fileName",
    scopedSlots: "fileName"
  },
  {
    label: "分享时间",
    prop: "createTime",
    width: 200
  },
  {
    label: "状态",
    prop: "shareStatus",
    scopedSlots: "shareStatus",
    width: 200
  },
  {
    label: "浏览次数",
    prop: "viewNum",
    scopedSlots: "viewNum",
    width: 200
  }
];

/**
 * 初始化列表数据
 */
const tableData = ref<GetShareListByPageResponse>({
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
 * 加载分享文件列表
 */
const loadDataList = () => {
  const getShareListByPaginationRequest: GetShareListByPageRequest = {
    pageNum: tableData.value.pageNum,
    pageSize: tableData.value.pageSize
  };

  getShareListByPage(getShareListByPaginationRequest).then(({data}) => {
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
const selectedShareIds = ref<string[]>([]);

/**
 * 选中行方法
 * @param rows 选中的数据项
 */
const rowSelected = (rows: ShareFileInfo[]) => {
  selectedShareIds.value = [];
  rows.forEach((item) => {
    if (item.id) {
      selectedShareIds.value.push(item.id);
    }
  });
};

const shareUrl = ref(document.location.origin + '/share/');

/**
 * 复制链接
 */
const copyBatch = () => {
  const shareFileInfo = tableData.value.list.find((item) => {
    return item.id === selectedShareIds.value[0];
  });

  if (shareFileInfo) {
    copy(shareFileInfo);
  }
}

/**
 * 复制链接
 */
const copy = (shareFileInfo: ShareFileInfo) => {
  navigator.clipboard.writeText(`链接：${shareUrl.value}${shareFileInfo.id} 提取码：${shareFileInfo.extractionCode}`)
      .then(() => {
        ElMessage.success("复制成功");
      })
      .catch(error => {
        // 如果用户拒绝访问或某些原因无法复制,捕获异常
        ElMessage.success("复制失败");
      });
}

/**
 * 取消分享
 */
const cancelShare = (shareFileInfo: ShareFileInfo) => {
  if (!shareFileInfo || !shareFileInfo.id) {
    ElMessage.warning("请选择要取消分享的文件");
    return;
  }

  const currentShareIds: Array<string> = [];
  currentShareIds.push(shareFileInfo.id);

  handleCancelShareFile(currentShareIds);
}

/**
 * 批量取消选中的分享文件
 */
const cancelShareBatch = () => {
  if (selectedShareIds.value.length == 0) {
    ElMessage.warning("请选择要取消分享的文件");
    return
  }
  let currentShareIds: Array<string> = [];
  currentShareIds = currentShareIds.concat(selectedShareIds.value);

  handleCancelShareFile(currentShareIds);
}

/**
 * 批量取消选中的分享文件
 */
const handleCancelShareFile = (currentShareIds: Array<string>) => {
  Confirm("你确定要取消分享吗？", () => {
    const data: CancelShareFileRequest = {
      ids: currentShareIds.join(",")
    };

    cancel(data).then(() => {
      loadDataList();
    });
  });
}

/**
 * 计算有效期
 */
const calculateValidityPeriod = (shareTime: string, validityPeriod: number): string => {

  if (validityPeriod === 0) {
    return "永久有效";
  }

  // 将时间字符串转换为Date对象
  const shareTimeDate = new Date(shareTime);

  // 计算有效期结束时间（增加10天）
  shareTimeDate.setDate(shareTimeDate.getDate() + validityPeriod);

  // 获取当前时间
  const currentTimeDate = new Date();

  const remainingTime = shareTimeDate.getTime() - currentTimeDate.getTime();
  const remainingDays = Math.ceil(remainingTime / (1000 * 60 * 60 * 24));

  if (remainingDays <= 0) {
    return "已失效";
  } else {
    return remainingDays + "天后过期";
  }
}

// 拉取数据
loadDataList();
</script>

<template>
  <div>
    <div class="top">
      <el-button type="primary" v-show="selectedShareIds.length == 1" @click="copyBatch">
        <span class="iconfont icon-cancel"></span>
        复制链接
      </el-button>
      <el-button type="primary" v-show="selectedShareIds.length > 0" @click="cancelShareBatch">
        <span class="iconfont icon-cancel"></span>
        取消分享
      </el-button>
    </div>
    <div class="file-list" v-if="tableData.list && tableData.list.length > 0">
      <Table ref="dataTableRef" :columns="columns" :dataSource="tableData" :fetch="loadDataList" :initFetch="false"
             :options="tableOptions" @rowSelected="rowSelected">
        <template #fileName="{index, row}">
          <div class="file-item" @mouseenter="showActionBar(index)" @mouseleave="hideActionBar()">
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

            <span class="file-name" :title="row.fileName">{{ row.name }}</span>
            <span class="op">
              <template v-if="showActionBarIndex == index && row.id">
                <span class="iconfont icon-link" @click="copy(row)">复制链接</span>
                <span class="iconfont icon-ban" @click="cancelShare(row)">取消分享</span>
              </template>
            </span>
          </div>
        </template>
        <template #shareStatus="{row}">
          {{ calculateValidityPeriod(row.createTime, row.validityPeriod) }}
        </template>
        <template #viewNum="{row}">
          {{ row.viewNum }}次
        </template>
      </Table>
    </div>
    <div class="no-data" v-else>
      <div class="no-data-inner">
        <Icon iconName="no_data" :width="120" fit="fill"></Icon>
        <div class="tips">您还没有分享数据哦</div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
@import "@/styles/file.list.scss";

.file-list {
  margin-top: 10px;

  .file-item {
    .file-name {
      span {
        &:hover {
          color: #494944;
        }
      }
    }

    .op {
      width: 170px;
    }
  }
}
</style>