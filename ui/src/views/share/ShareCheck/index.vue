<script setup lang="ts">
import {ref} from "vue";
import {useRouter, useRoute} from "vue-router";
import {getShareInfo, checkExtractionCode} from "@/api/v1/share";
import type {ShareInfoResponse, CheckExtractionCodeRequest} from "@/api/v1/share/types";

const router = useRouter();
const route = useRoute();

const shareId = route.params.shareId as string;
const shareInfo = ref<ShareInfoResponse | null>(null);

/**
 * 获取分享信息
 */
const handleGetShareInfo = () => {
  getShareInfo(shareId).then(({data}) => {
    if (data) {
      shareInfo.value = data;
    }
  });
}

handleGetShareInfo();

const formData = ref({
  code: ""
});

const formDataRef = ref();

const rules = ref({
  code: [
    {required: true, message: "请输入提取码"},
    {min: 4, max: 4, message: "提取码长度为4位"}
  ]
});

/**
 * 检查提取码
 */
const handleCheckExtractionCode = () => {
  if (!formDataRef.value.validate()) {
    return;
  }

  const data: CheckExtractionCodeRequest = {
    shareId: shareId,
    extractionCode: formData.value.code
  }

  checkExtractionCode(data).then(({data}) => {
    router.push({
      path: "/share/" + shareId
    });
  });
}
</script>

<template>
  <div class="share">
    <div class="body-content">
      <div class="logo">
        <span class="iconfont icon-NAS"></span>
        <span class="name">Netdisk</span>
      </div>
      <div v-if="shareInfo" class="code-panel">
        <div class="file-info">
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
        <div class="code-body">
          <div class="tips">请输入提取码：</div>
          <div class="input-area">
            <el-form :model="formData" :rules="rules" ref="formDataRef" @submit.prevent>
              <el-form-item prop="code">
                <el-input class="input" clearable v-model.trim="formData.code"
                          @keyup.enter="handleCheckExtractionCode"></el-input>
                <el-button type="primary" @click="handleCheckExtractionCode">提取文件</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>

      <div v-else>
        <span>分享的文件不存在或已失效</span>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.share {
  height: calc(100vh);
  background: #eef2f6 url("@/assets/images/share_bg.png") repeat-x 0 bottom;
  display: flex;
  justify-content: center;

  .body-content {
    margin-top: calc(100vh / 5);
    width: 500px;

    .logo {
      display: flex;
      align-items: center;
      justify-content: center;

      .icon-NAS {
        font-size: 60px;
        color: #409eff;
      }

      .name {
        font-weight: bold;
        margin-left: 5px;
        font-size: 25px;
        color: #409eff;
      }
    }

    .code-panel {
      margin-top: 20px;
      background: #fff;
      border-radius: 5px;
      overflow: hidden;
      box-shadow: 0 0 7px 1px #5757574f;

      .file-info {
        padding: 10px 20px;
        background: #409eff;
        color: #fff;
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

      .code-body {
        padding: 30px 20px 60px 20px;

        .tips {
          font-weight: bold;
        }

        .input-area {
          margin-top: 10px;

          .input {
            flex: 1;
            margin-right: 10px;
          }
        }
      }
    }
  }
}
</style>