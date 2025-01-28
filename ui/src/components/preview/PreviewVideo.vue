<template>
  <div ref="player" id="player"></div>
</template>

<script setup lang="ts">
import { onMounted, ref, onBeforeUnmount } from 'vue'
import Artplayer from 'artplayer'
import { getVideoUrl } from '@/api/v1/file'
import Utils from '@/utils/Utils'

const props = defineProps({
  // url: {
  //   type: String, default: ''
  // }

  // 文件ID
  fileId: Number,

  // 文件名
  name: String
})

// const videoInfo = ref({
//   video: null
// })

const player = ref()

let instance: Artplayer | null = null

const initPlayer = () => {
  if (!props.fileId) {
    return
  }
  
  instance = new Artplayer({
    container: player.value,
    theme: '#b7daff',
    screenshot: true,
    fullscreen: true,
    fullscreenWeb: true,
    miniProgressBar: true,
    url: getVideoUrl(props.fileId),
    type: Utils.extName(props.name)
  })
}

onMounted(() => {
  initPlayer()
})

onBeforeUnmount(() => {
  if (instance && instance.destroy) {
    instance.destroy(false)
  }
})

// const getFullVideo = () => {
//   if (props.fileId) {
//     getVideo(props.fileId).then(({ data }) => {
//       const videoUrl = URL.createObjectURL(new Blob([data]))
//       initPlayer(videoUrl)
//     })
//   }
// }
</script>

<style scoped lang="scss">
#player {
  width: 100%;

  .artplayer-app {
    margin: 0 auto;
    max-height: calc(100vh - 41px);
  }
}
</style>