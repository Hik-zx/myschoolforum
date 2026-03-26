<script setup>

</script>

<template>
  <el-row style="height: 100vh">
    <el-col :span="12" class="bg" >
    </el-col>
    <el-col :span="6" :offset="3" class="formCss" >
      <router-view></router-view>
    </el-col>
  </el-row>
</template>

<style lang="scss" scoped>
.bg {
  background:
      url('@/assets/333.jpg') no-repeat 60% center / cover;
      border-radius: 0 20px 20px 0;
}

  .formCss {
    display: flex;
    flex-direction: column;
    justify-content: center;
    user-select: none;
    background-color: var(--el-bg-color);
  }

</style>