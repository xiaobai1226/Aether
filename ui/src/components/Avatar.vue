<script setup lang="ts">
import { onMounted, watch, ref } from 'vue'
import { getAvatar } from '@/api/user'

const props = defineProps({
  width: {
    type: Number,
    default: 40
  },
  avatar: {
    type: String
  }
})

// 用于存储头像的URL数据
const avatarUrl = ref('')

/**
 * 更新头像图片
 */
const updateAvatarImage = () => {
  getAvatar().then(({ data }) => {
    avatarUrl.value = URL.createObjectURL(new Blob([data]))
  })
}

defineExpose({ updateAvatarImage })

onMounted(() => {
  if (props.avatar === undefined) {
    updateAvatarImage()
  } else {
    avatarUrl.value = props.avatar
  }
})

watch(() => props.avatar, (newAvatar) => {
  if (newAvatar) {
    avatarUrl.value = newAvatar
  }
})
</script>

<template>
<span class="avatar" :style="{ width: width + 'px', height: width + 'px' }">
  <img :src="avatarUrl" alt="" />
</span>
</template>

<style scoped lang="scss">
.avatar {
  display: flex;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;

  img {
    width: 100%;
    object-fit: cover;
  }
}
</style>