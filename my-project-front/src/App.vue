<script setup>
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import { useDark, useToggle } from '@vueuse/core'

useDark({
  selector: 'html',
  attribute: 'class',
  valueDark: 'dark',
  valueLight: 'light'
})

useDark({
  onChanged(dark) { useToggle(dark) }
})
</script>

<template>
  <div>
    <!-- App.vue的路由出口 -->
    <!-- 全局中文处理 -->
    <el-config-provider :locale="zhCn">
      <router-view></router-view>
    </el-config-provider>
  </div>
</template>
<style scoped>

</style>
