<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import APlayer from 'aplayer'
import 'aplayer/dist/APlayer.min.css'
import { getFile } from '@/api/file'

const props = defineProps({
  // 文件ID
  fileId: Number,
  fileName: {
    type: String
  }
})

const playerRef = ref()
const player = ref()

onMounted(() => {
  if (props.fileId) {
    getFile(props.fileId).then(({ data }) => {
      const audioUrl = URL.createObjectURL(new Blob([data]))

      player.value = new APlayer({
        container: playerRef.value,
        audio: {
          url: audioUrl,
          name: props.fileName,
          cover: new URL('@/assets/music_icon.png', import.meta.url).href,
          artist: ''
        }

      })
    })
  }
})

onUnmounted(() => {
  player.value.destroy()
})
</script>

<template>
  <div class="music">
    <div class="body-content">
      <div class="cover">
        <img src="@/assets/music_cover.png" />
      </div>
      <div ref="playerRef" class="music-player"></div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.music {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;

  .body-content {
    text-align: center;
    width: 80%;

    .cover {
      margin: 0 auto;
      width: 200px;
      text-align: center;

      img {
        width: 100%;
      }
    }

    .music-player {
      margin-top: 20px;
    }
  }
}
</style>