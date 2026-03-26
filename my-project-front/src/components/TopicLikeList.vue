<script setup>
import {getLikeService,interactService} from "@/api/topicApi.js";
import {ref} from "vue";
import LightCard from "@/components/lightCard.vue";
import router from "@/router/index.js";
import TopcTag from "@/components/TopcTag.vue";
import {ElMessage} from "element-plus";

defineProps({
  show: Boolean
})
const emit=defineEmits(['close'])
const list=ref([])
//获取收藏列表
const getCollectList=async ()=>{
  const res= await getLikeService()

  list.value=res.data.data
}
//删除帖子
const deleteTopic=async (index,tid)=>{
  await interactService(tid,'like',false)
  ElMessage.success('已取消点赞!')
  list.value.splice(index,1)
}
</script>

<template>
  <el-drawer :model-value="show" @close="emit('close')" @open="getCollectList" title="点赞列表">
    <div class="collect-list">
      <light-card  v-for="(item,index) in list" class="topic-card" @click="router.push(`/index/topic-detail/${item.id}`)">
        <topc-tag :type="item.type"></topc-tag>
        <div class="title" style="margin-left: 15px">
          <b>{{item.title}}</b>
        </div>
        <el-button type="danger" @click.stop="deleteTopic(index,item.id)" size="small" plain style="border-radius: 8px">删除</el-button>
      </light-card>
    </div>
  </el-drawer>
</template>

<style scoped lang="less">
.collect-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.topic-card {
  background-color: rgba(128, 128, 128, 0.2);
  transition: .3s;
  display: flex;

  .title {
    margin-left: 5px;
    font-size: 14px;
    flex: 1;
    white-space: nowrap;
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &:hover {
    scale: 1.02;
    cursor: pointer;
  }
}
</style>