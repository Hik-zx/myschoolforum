<!--子组件，接受父组件传递的内容-->
<script setup>
import {SoldOut} from "@element-plus/icons-vue";

defineProps({
  icon: Object,
  title: String,
  desc: String
})
</script>

<template>
  <div class="card">
    <div class="card-header" v-if="title">
      <div>
        <el-icon style="margin-right: 3px;translate: 0 2px">
          <component :is="icon"/>
        </el-icon>
        {{ title }}
      </div>
      <div>{{ desc }}</div>
    </div>
<!--    默认插槽-->
    <slot></slot>
  </div>
</template>

<style scoped lang="scss">
  .card{
    border-radius: 8px;
   // border: solid 1px var(--el-border-color);
    background-color: var(--el-bg-color);
    box-sizing: border-box;
    min-height: 20px;
    padding: 10px;
    box-shadow: 0 0 7px rgba(0, 0, 0, 0.3); /* 水平偏移量为 0，垂直偏移量为 0，模糊半径为 10px，颜色为淡蓝色 */
  }
  .card-header{
    border-bottom: solid 1px var(--el-border-color);
    padding-bottom: 5px;
    margin-bottom: 10px;
   &>:first-child{
      font-size: 18px;
      font-weight: bold;
    }
    &>:last-child{
      font-size: 13px;
      color: gray;
    }
  }
</style>