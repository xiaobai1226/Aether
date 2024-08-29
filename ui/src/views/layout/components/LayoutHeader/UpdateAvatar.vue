<script setup lang="ts">
import {ref} from "vue";
import AvatarUpload from "@/components/AvatarUpload.vue";
import Dialog from "@/components/Dialog.vue";
import {updateUserAvatar} from "@/api/user";

const emit = defineEmits(["updateAvatar"]);

const formData = ref({
  avatar: null,
  nickname: ''
});
const formDataRef = ref();
const rules = {
  title: [{required: true, message: '请输入内容', trigger: 'blur'}],
}

const show = (nickname: string) => {
  formData.value.nickname = nickname;
  dialogConfig.value.show = true;
}

defineExpose({show})

const dialogConfig = ref({
  show: false,
  title: '修改头像',
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
 * 提交更新请求
 */
const submitForm = () => {
  if (!(formData.value.avatar instanceof File)) {
    dialogConfig.value.show = false;
    return;
  }

  updateUserAvatar(formData.value.avatar).then(() => {
    dialogConfig.value.show = false;
    emit("updateAvatar");
  });
}
</script>

<template>
  <div>
    <Dialog :show="dialogConfig.show" :title="dialogConfig.title" :buttons="dialogConfig.buttons" width="500px"
            :show-cancel="false" @close="dialogConfig.show = false">
      <el-form :model="formData" :rules="rules" ref="formDataRef" label-width="80px" @submit.prevent>
        <el-form-item label="昵称">
          {{ formData.nickname }}
        </el-form-item>
        <el-form-item label="头像" props="">
          <AvatarUpload v-model:model-value="formData.avatar"/>
        </el-form-item>
      </el-form>
    </Dialog>
  </div>
</template>

<style scoped lang="scss">

</style>