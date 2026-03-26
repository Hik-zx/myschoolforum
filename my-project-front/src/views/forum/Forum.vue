<script setup>
import {getTopictypeService} from "@/api/topicApi.js";
import {useUserStore} from "@/store/userStore.js";
const usestore=useUserStore()
//获取帖子类型
const getTopicType=async ()=>{
  const res= await getTopictypeService();
  const array=[]
  array.push({name: '全部',id: 0,color: 'linear-gradient(45deg, white,red,orange,gold,green,blue)'})
  res.data.data.forEach(d=>array.push(d))
  usestore.forum.types= array
}
getTopicType()

</script>

<template>
  <div>
    <router-view v-slot="{Component}">
      <transition name="el-fade-in-linear" mode="out-in">
        <!--      返回帖子广场不用重新获取-->
        <keep-alive include="TopicList">
          <component :is="Component"></component>
        </keep-alive>
      </transition>
    </router-view>
<!--    返回顶部-->
    <el-backtop target=".main-content-page .el-scrollbar__wrap" :right="20" :bottom="70"/>
  </div>

</template>
