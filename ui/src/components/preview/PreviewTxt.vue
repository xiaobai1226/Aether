<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getFile } from '@/api/file'

const props = defineProps({
  // 文件ID
  fileId: Number
})

const txtContent = ref('')
const blobResult = ref()
const encode = ref('utf8')

const readTxt = () => {
  if (props.fileId) {
    getFile(props.fileId).then(({ data }) => {
      blobResult.value = new Blob([data])
      showTxt()
    })
  }
}

const changeEncode = (e: any) => {
  encode.value = e
  showTxt()
}

const showTxt = () => {
  let reader = new FileReader()
  reader.onload = () => {
    txtContent.value = reader.result
  }
  reader.readAsText(blobResult.value, encode.value)
}

onMounted(() => {
  readTxt()
})

const copy = async () => {
  navigator.clipboard.writeText(txtContent.value)
    .then(() => {
      ElMessage.success('复制成功')
    })
    .catch(error => {
      // 如果用户拒绝访问或某些原因无法复制,捕获异常
      ElMessage.success('复制失败')
    })
}
</script>

<template>
  <div class="code">
    <div class="top-op">
      <div class="encode-select">
        <el-select clearable v-model="encode" placeholder="选择编码" @change="changeEncode">
          <!--          <el-option v-for="item in encodeList" :key="item.value" :label="item.label" :value="item.value"></el-option>-->
          <el-option value="utf8" label="utf8编码" />
          <el-option value="gbk" label="gbk编码" />
        </el-select>
        <div class="tips">乱码了？切换编码</div>
      </div>
      <div class="copy-btn">
        <!--        <el-button type="primary" icon="el-icon-document-copy">复制代码</el-button>-->
        <el-button type="primary" @click="copy">复制</el-button>
      </div>
    </div>
    <highlightjs autodetect :code="txtContent" />
  </div>
</template>

<style scoped lang="scss">
.code {
  width: 100%;

  .top-op {
    display: flex;
    align-items: center;
    justify-content: space-around;
  }

  .encode-select {
    flex: 1;
    display: flex;
    align-items: center;
    margin: 5px 10px;

    .tips {
      margin-left: 10px;
      color: #828282;
    }
  }

  .copy-btn {
    margin-right: 10px;
  }

  pre {
    margin: 0;
  }
}
</style>