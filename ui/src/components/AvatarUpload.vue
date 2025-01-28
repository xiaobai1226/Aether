<script setup lang="ts">
import {ref, onMounted} from "vue";

import {getAvatar} from "@/api/v1/user";

const props = defineProps({
  modelValue: {
    type: Object,
    default: null
  }
});

// 上传新头像文件后，临时存储的变量
const localFile = ref<string | null>(null);
const emit = defineEmits(["update:modelValue"]);
const uploadImage = (fileObject: { file: File }) => {
  const file = fileObject.file;
  let img = new FileReader();
  img.readAsDataURL(file);
  img.onload = ({target}) => {
    if (target != null) {
      localFile.value = target.result as string;
    }
  };
  emit("update:modelValue", file)
}

// 用于存储头像的URL数据
const avatarUrl = ref('');

const getAvatarImage = () => {
  getAvatar().then(({data}) => {
    avatarUrl.value = URL.createObjectURL(new Blob([data]));
  });
}

onMounted(() => {
  getAvatarImage();
});
</script>

<template>
  <div class="avatar-upload">
    <div class="avatar-show">
      <template v-if="localFile">
        <img :src="localFile"/>
      </template>
      <template v-else>
        <img :src="avatarUrl"/>
      </template>
    </div>
    <div class="select-btn">
      <el-upload name="file" :show-file-list="false" accept=".png,.PNG,.jpg,.JPG,.jepg,.JEPG,.gif,.GIF,.bmp,.BMP"
                 :multiple="false" :http-request="uploadImage">
        <el-button type="primary">选择</el-button>
      </el-upload>
    </div>
  </div>
</template>

<style scoped lang="scss">
.avatar-upload {
  display: flex;
  justify-content: center;
  align-items: end;

  .avatar-show {
    background: rgb(245, 245, 245);
    width: 150px;
    height: 150px;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    position: relative;

    .iconfont {
      font-size: 50px;
      color: #ddd;
    }

    img {
      width: 100%;
      height: 100%;
    }

    .op {
      position: absolute;
      color: #0e8aef;
      top: 80px;
    }
  }

  .select-btn {
    margin-left: 10px;
    vertical-align: bottom;
  }
}
</style>