<script setup>

</script>

<template>
  <div class="light-card">
    <slot></slot>
  </div>
</template>

<style scoped lang="scss">
.light-card{
  border-radius: 8px;
  background-color: var(--el-bg-color);
  padding: 10px;
  box-shadow: 0 0 7px rgba(0, 0, 0, 0.3);
}
</style>