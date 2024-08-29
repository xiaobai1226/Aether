<script setup lang="ts">
import { onMounted, ref } from 'vue'
import DPlayer from 'dplayer'
import { getVideo } from '@/api/file'
import Utils from '@/utils/Utils'

const props = defineProps({
  // url: {
  //   type: String, default: ''
  // }

  // 文件ID
  fileId: Number
})

const videoInfo = ref({
  video: null
})

const player = ref()

const initPlayer = (videoUrl: string) => {
  const dp = new DPlayer({
    container: player.value,
    theme: 'b7daff',
    screenshot: true,
    video: {
      url: videoUrl,
      type: 'auto'
    }
  })
}

onMounted(() => {
  getFullVideo()
})

const getFullVideo = () => {
  if (props.fileId) {
    getVideo(props.fileId).then(({ data }) => {
      const videoUrl = URL.createObjectURL(new Blob([data]))
      initPlayer(videoUrl)
    })
  }
}
</script>

<template>
  <div ref="player" id="player"></div>
</template>

<style scoped lang="scss">
#player {
  width: 100%;

  :deep .dplayer-video-wrap {
    text-align: center;

    .dplayer-video {
      margin: 0 auto;
      max-height: calc(100vh - 41px);
    }
  }
}
</style>