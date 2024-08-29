<script setup lang="ts">
import {nextTick, ref} from "vue";
import Verify from "@/utils/Verify";
import {ResultErrorMsgEnum} from "@/enums/ResultErrorMsgEnum";
import {updateUserPassword} from "@/api/user";

// 校验确认密码
const checkRePassword = (rule: any, value: string, callback: any) => {
  if (value !== formData.value.password) {
    callback(new Error(rule.message));
  } else {
    callback();
  }
};

const formData = ref({
  password: '',
  rePassword: ''
});
const formDataRef = ref();

// 规则对象
const rules = {
  password: [
    {required: true, message: ResultErrorMsgEnum.ERROR_PASSWARD_EMPTY, trigger: "blur"},
    {validator: Verify.password, message: ResultErrorMsgEnum.ERROR_PASSWARD_FORMAT, trigger: "blur"}
  ],
  rePassword: [
    {required: true, message: ResultErrorMsgEnum.ERROR_REPASSWARD_EMPTY, trigger: "blur"},
    {validator: checkRePassword, message: ResultErrorMsgEnum.ERROR_PASSWARD_NOT_MATCH, trigger: "blur"}
  ]
}

const show = () => {
  dialogConfig.value.show = true;
  nextTick(() => {
    formDataRef.value.resetFields();
    formData.value = {
      password: '',
      rePassword: ''
    };
  })
}

defineExpose({show})

const dialogConfig = ref({
  show: false,
  title: '修改密码',
  buttons: [
    {
      type: 'primary',
      text: '确定',
      click: () => {
        submitForm();
      }
    }
  ]
});

/**
 * 提交请求
 */
const submitForm = () => {
  formDataRef.value.validate((valid: boolean) => {
    // 所有表单都通过校验才为true
    if (valid) {
      updateUserPassword(formData.value.password)
          .then(() => {
            // 关闭弹窗
            dialogConfig.value.show = false;
          });
    }
  });
}
</script>

<template>
  <div>
    <Dialog :show="dialogConfig.show" :title="dialogConfig.title" :buttons="dialogConfig.buttons" width="500px"
            :show-cancel="false" @close="dialogConfig.show = false">
      <el-form :model="formData" :rules="rules" ref="formDataRef" label-width="80px" @submit.prevent>
        <el-form-item label="新密码" prop="password">
          <el-input type="password" size="large" placeholder="请输入密码" v-model.trim="formData.password"
                    show-password>
            <template #prefix>
              <span class="iconfont icon-password"></span>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="rePassword">
          <el-input type="password" size="large" placeholder="请再次输入密码" v-model.trim="formData.rePassword"
                    show-password>
            <template #prefix>
              <span class="iconfont icon-password"></span>
            </template>
          </el-input>
        </el-form-item>
      </el-form>
    </Dialog>
  </div>
</template>

<style scoped lang="scss">

</style>