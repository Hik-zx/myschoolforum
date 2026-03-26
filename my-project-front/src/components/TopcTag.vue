<script setup>
import {useUserStore} from "@/store/userStore.js";
const usestore=useUserStore()
defineProps({
  type: Number
})
</script>

<template>
    <div class="topic-type"
         :style="{
         color: usestore.findTypeByid(type).color+'EE',
         'border-color': usestore.findTypeByid(type).color+'77',
         'background': usestore.findTypeByid(type).color+'33'
         }"
    >
      {{usestore.findTypeByid(type).name}}
    </div>

</template>

<style scoped lang="scss">
    .topic-type{
      display: inline-block;
      border: solid 0.5px grey;
      border-radius: 4px;
      font-size: 12px;
      padding: 0 5px;
      height: 18px;
      translate:  0 -2px ;

    }
</style>