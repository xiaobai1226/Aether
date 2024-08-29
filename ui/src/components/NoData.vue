<script setup lang="ts">
const props = defineProps({
  msg: {
    type: String
  }
});
</script>

<template>
  <div class="no-data">
    <div class="iconfont icon-empty"></div>
    <!--    <div>-->
    <!--      <Icon iconName="no_data" :width="120" fit="fill"></Icon>-->
    <!--    </div>-->
    <div class="msg">{{ msg }}</div>
  </div>
</template>

<style scoped lang="scss">
.no-data {
  text-align: center;
  padding: 10px 0;

  .icon-empty {
    font-size: 50px;
    color: #bbb;
  }

  .msg {
    margin-top: 10px;
    color: #909399;
    font-size: 14px;
  }
}
</style>