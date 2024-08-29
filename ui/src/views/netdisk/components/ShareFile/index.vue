<script setup lang="ts">
import {ref, nextTick} from "vue";
import Dialog from "@/components/Dialog.vue";
import {ElMessage} from "element-plus";
import Verify from "@/utils/Verify";
import type {CreateShareFileRequest, CreateShareFileResponse} from "@/api/share/types";
import {create} from "@/api/share";
import {ResultErrorMsgEnum} from "@/enums/ResultErrorMsgEnum";

const shareUrl = ref(document.location.origin + '/share/');

/**
 * 是否展示分享表单 0：分享表单 1： 分享结果
 */
const showType = ref(0);

/**
 * 分享文件id集合
 */
const ids = ref<Array<number>>([]);

/**
 * 表单内容
 */
const formData = ref({
  // 有效期 0：1天 1：7天 2：30天 3：永久有效
  validType: 0,
  // 提取码类型 0：系统生成 1：自定义
  codeType: 0,
  code: '',
});

const formDataRef = ref();

const rules = ref({
  validType: [
    {required: true, message: ResultErrorMsgEnum.ERROR_SHARE_VALIDITY_PERIOD_EMPTY}
  ],
  codeType: [
    {required: true, message: ResultErrorMsgEnum.ERROR_SHARE_EXTRACTION_CODE_TYPE_EMPTY}
  ],
  code: [
    {required: true, message: ResultErrorMsgEnum.ERROR_SHARE_EXTRACTION_CODE_EMPTY},
    {validator: Verify.extractionCode, message: ResultErrorMsgEnum.ERROR_SHARE_EXTRACTION_CODE_FORMAT},
    {length: 4, message: '提取码长度为4位'}
  ]
});

/**
 * 弹窗配置
 */
const dialogConfig = ref({
  show: false,
  title: '分享文件(夹):',
  buttons: [
    {
      text: '确定',
      type: 'primary',
      click: (e) => {
        share();
      }
    }
  ]
});

/**
 * 展示分享文件弹窗方法
 * @param userFileIds
 * @param title
 */
const show = (userFileIds: Array<number>, title: string) => {
  showType.value = 0;
  dialogConfig.value.title = "分享文件(夹):" + title;
  dialogConfig.value.show = true;
  showCancel.value = true;
  resultInfo.value = null;
  ids.value = userFileIds;
  dialogConfig.value.buttons[0].text = "确定";

  nextTick(() => {
    formDataRef.value.resetFields();
    formData.value.validType = 0;
    formData.value.codeType = 0;
  });
};

defineExpose({show});


// 展示取消
const showCancel = ref(true);

// 分享结果
const resultInfo = ref<CreateShareFileResponse | null>(null);

/**
 * 分享文件
 */
const share = () => {
  // 如果已经分享成功，直接关闭弹窗
  if (resultInfo.value && Object.keys(resultInfo.value).length > 0) {
    dialogConfig.value.show = false;
    return;
  }

  // 校验参数
  formDataRef.value.validate((valid: boolean) => {
    // 所有表单都通过校验才为true
    if (!valid) {
      return;
    }

    let validityPeriod = 1;
    if (formData.value.validType == 0) {
      validityPeriod = 1;
    } else if (formData.value.validType == 1) {
      validityPeriod = 7;
    } else if (formData.value.validType == 2) {
      validityPeriod = 30;
    } else if (formData.value.validType == 3) {
      validityPeriod = 0;
    }

    const params: CreateShareFileRequest = {
      ids: ids.value.join(","),
      validityPeriod: validityPeriod
    };

    if (formData.value.codeType == 1) {
      params.extractionCode = formData.value.code;
    }

    create(params).then(({data}) => {
      if (!data) {
        return;
      }

      showType.value = 1;
      resultInfo.value = data;
      dialogConfig.value.buttons[0].text = "关闭";
      showCancel.value = false;
    });
  });
}

/**
 * 复制分享链接及提取码
 */
const copy = () => {
  // await toClipboard(`链接：${shareUrl.value}${resultInfo.value.shareId} 提取码：${resultInfo.value.code}`);
  if (resultInfo.value) {
    navigator.clipboard.writeText(`链接：${shareUrl.value}${resultInfo.value.shareId} 提取码：${resultInfo.value.extractionCode}`)
        .then(() => {
          ElMessage.success("复制成功");
        })
        .catch(error => {
          // 如果用户拒绝访问或某些原因无法复制,捕获异常
          ElMessage.success("复制失败");
        });
  }
}
</script>

<template>
  <div>
    <Dialog :show="dialogConfig.show" :title="dialogConfig.title" :buttons="dialogConfig.buttons" width="600px"
            :showCancel="showCancel" @close="dialogConfig.show = false">
      <el-form :model="formData" :rules="rules" ref="formDataRef" label-width="100px" @submit.prevent>
        <template v-if="showType == 0">
          <el-form-item label="有效期" prop="validType">
            <el-radio-group v-model="formData.validType">
              <el-radio :label="0">1天</el-radio>
              <el-radio :label="1">7天</el-radio>
              <el-radio :label="2">30天</el-radio>
              <el-radio :label="3">永久有效</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="提取码" prop="codeType">
            <el-radio-group v-model="formData.codeType">
              <el-radio :label="0">系统生成</el-radio>
              <el-radio :label="1">自定义</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item prop="code" v-if="formData.codeType == 1">
            <el-input clearable placeholder="请输入4位提取码" v-model="formData.code" maxLength="4"
                      :style="{width: '130px'}"></el-input>
          </el-form-item>
        </template>

        <template v-else>
          <el-form-item label="分享链接">
            {{ shareUrl }}{{ resultInfo != null ? resultInfo.shareId : "" }}
          </el-form-item>
          <el-form-item label="提取码">
            {{ resultInfo != null ? resultInfo.extractionCode : "" }}
          </el-form-item>
          <div class="validity_period" v-if="resultInfo">
            <span v-if="resultInfo.validityPeriod > 0">
              链接<span class="validity_period_day">{{ resultInfo.validityPeriod }}天</span>后失效
            </span>
            <span v-else>
              链接<span class="validity_period_day">永久</span>有效
            </span>
          </div>
          <el-form-item>
            <el-button type="primary" @click="copy">复制链接及提取码</el-button>
          </el-form-item>
        </template>
      </el-form>
    </Dialog>
  </div>
</template>

<style scoped lang="scss">
.validity_period {
  margin-left: 30px;
  margin-bottom: 25px;
  color: #999;

  .validity_period_day {
    color: #409EFF
  }
}
</style>