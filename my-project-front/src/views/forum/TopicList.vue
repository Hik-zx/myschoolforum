<script setup>
import {computed, onMounted, ref,watch,reactive} from "vue";
import {
  ArrowRightBold,
  Clock,
  FolderOpened,
  Apple,
  Comment,
  ChatDotSquare,
  MilkTea,
  Search
} from "@element-plus/icons-vue";
import {useUserStore} from "@/store/userStore.js";
import {getTopictypeService} from "@/api/topicApi.js";
import LightCard from "@/components/lightCard.vue";
import {Calendar, CollectionTag, EditPen,Edit,Document,Compass,Picture,Microphone,CircleCheck,Star} from "@element-plus/icons-vue";
import Weather from "@/components/Weather.vue";
import {getWeatherService} from "@/api/weatherApi.js";
import {getTopicService,toptopicService,getTopicByTitleService} from "@/api/topicApi.js";
import TopicEditor from "@/components/TopicEditor.vue";
import ColorDot from "@/components/colorDot.vue";
import router from "@/router/index.js";
import TopcTag from "@/components/TopcTag.vue";
import TopicCollectList from "@/components/TopicCollectList.vue";
import TopicLikeList from "@/components/TopicLikeList.vue";
import MyTopicList from "@/components/MyTopicList.vue";
const usestore=useUserStore()
//转换时间格式
const registerDate = computed(() => {
  const date = new Date();
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];
  const weekday = weekdays[date.getDay()];
  return `${date.getFullYear()} 年 ${date.getMonth() + 1} 月 ${date.getDate()} 日  ${weekday}`;
});
const weather=ref({
    location: {},
    now: {},
    hourly: {},
    success: false
})
const topics=reactive({
  list: [],
  type: 0,
  page: 0,
  end: false,
  top: [],
  title: null
})
const editor=ref(false)
const collects=ref(false)
const likes=ref(false)
const mytopic=ref(false)
const searchInput=ref('')
//根据标题查找帖子
const getTopicByTitle= async ()=>{
  topics.list=null
  const res= await getTopicByTitleService(searchInput.value)
  //无限刷新
  if(res.data.data){
    res.data.data.forEach(d=>topics.list.push(d))
    topics.page++
  }
  if(!res.data.data || res.data.data.length<10){
    topics.end=true
  }
}

//获取帖子
const getTopic= async ()=>{
  if(topics.end) return
  const res= await getTopicService(topics.page,topics.type)
  //无限刷新
  if(res.data.data){
    res.data.data.forEach(d=>topics.list.push(d))
    topics.page++
  }
  if(!res.data.data || res.data.data.length<10){
    topics.end=true
  }

  //topics.list=res.data.data
}

//获取置顶帖子
const toptopic=async ()=>{
  const res=await toptopicService()
  topics.top=res.data.data
}
getTopic()
toptopic()
//监听type，改变时获取对应帖子类型
watch(() => topics.type, () => resetList(),)
function resetList() {
  topics.page = 0
  topics.end = false
  topics.list = []
  getTopic()
}

function onTopicCreat() {
  editor.value=false
  topics.page=0
  topics.list=[]
  topics.end=false
  getTopic()
}
// 获取用户位置信息
navigator.geolocation.getCurrentPosition(async position => {
  const longitude = position.coords.longitude; // 经度
  const latitude = position.coords.latitude; // 纬度

  // 在获取到位置信息后立即调用天气服务函数
  try {
  const res= await getWeatherService(longitude, latitude);
    Object.assign(weather.value,res.data.data)
    weather.value.success=true
  } catch (error) {
    console.error('获取天气数据出错：', error);
    weather.value.success = false;
  }
});
// onMounted(()=>{
//
//   topics.page=0
// })


</script>

<template>
    <div style="display: flex;margin: 20px auto;gap: 20px;max-width: 1050px">
      <div style="flex: 1;">
            <light-card>
              <div class="creat-topic" @click="editor=true">
                <el-icon><EditPen> </EditPen></el-icon> 点击发表主题...
              </div>
              <div style="margin-top: 10px;display: flex;gap: 13px;font-size: 18px;color: grey">
                <el-icon><Edit /></el-icon>
                <el-icon><Document /></el-icon>
                <el-icon><Compass /></el-icon>
                <el-icon><Picture /></el-icon>
                <el-icon><Microphone /></el-icon>
              </div>
            </light-card>
        <light-card style="margin-top: 20px;display: flex;flex-direction: column;gap: 10px">
          <div v-for="item in topics.top" class="top-topic" @click="router.push('/index/topic-detail/'+item.id)">
            <el-tag type="info" size="small" style="color: dodgerblue">置顶</el-tag>
            <div>{{item.title}}</div>
            <div>{{new Date(item.time).toLocaleString()}}</div>
          </div>
        </light-card>
          <light-card style="margin-top: 20px;display: flex;gap: 7px">
            <div :class="`type-select-card ${topics.type === item.id ? 'active' : ''}`"
                 v-for="item in usestore.forum.types"
                 @click="topics.type = item.id">
              <color-dot :color="item.color"/>
              <span style="margin-left: 5px">{{item.name}}</span>
            </div>

          </light-card>
        <transition name="el-fade-in" mode="out-in">
          <div v-if="topics.list.length">
            <div style="margin-top: 20px;display: flex;flex-direction: column;gap: 10px"
            v-infinite-scroll="getTopic">
              <light-card class="topic-card" v-for="item in topics.list" @click="router.push('/index/topic-detail/'+item.id)">
                <div>
                  <div style="display: flex">
                    <div>
                      <el-avatar :size="30" :src="usestore.avatarUserUrl(item.avatar)"></el-avatar>
                    </div>

                    <div style="margin-left: 7px;transform: translateY(-2px)">
                      <div style="font-size: 13px;font-weight: bold">{{item.username}}</div>
                      <div style="font-size: 12px;color: grey;">
                        <el-icon style="color: dodgerblue"><Clock/></el-icon>
                        <div style="margin-left: 2px; margin-top: 2px ;display: inline-block;transform: translateY(-2px);color: dodgerblue">
                          {{new Date(item.time).toLocaleString()}}
                        </div>
                      </div>
                    </div>
                  </div>

                </div>
                <div style="margin-top: 5px">
                 <topc-tag :type="item.type"></topc-tag>
                  <span style="font-weight: bold;margin-left: 7px">{{item.title}}</span>
                </div>
                <div class="topic-content">{{item.text}}</div>
                <div style="display: grid;grid-template-columns: repeat(3, 1fr);grid-gap: 10px">
                  <el-image class="topic-image" v-for="img in item.images" :src="img" fit="cover"></el-image>
                </div>
                <div style="display: flex;gap: 20px;font-size: 13px;margin-top: 10px;opacity: 0.8">
                  <div>
                    <el-icon style="vertical-align: middle ;translate: 0 -1px"><CircleCheck/></el-icon> 点赞：{{item.like}}
                  </div>
                  <div>
                    <el-icon style="vertical-align: middle;translate: 0 -1px"><Star/></el-icon> 收藏：{{item.collect}}
                  </div>
                  <div>
                    <el-icon style="vertical-align: middle ;translate: 0 -1px"><ChatDotSquare/></el-icon> 评论：{{item.comments}}
                  </div>
                </div>
              </light-card>
            </div>
          </div>
        </transition>

        </div>
      <div style="width: 280px">
        <div style="position: sticky;top: 20px">
          <light-card>
            <div class="collect-list-button" @click="collects=true">
                <span><el-icon style="margin-right: 5px;color: #FEA233"><FolderOpened></FolderOpened></el-icon>查看我的收藏</span>
              <el-icon style="transform: translateY(3px)"><ArrowRightBold></ArrowRightBold></el-icon>
            </div>
          </light-card>
          <light-card style="margin-top: 15px">
            <div class="collect-list-button" @click="likes=true" >
              <span><el-icon style="margin-right: 5px;color: red"><Apple /></el-icon>查看我的点赞</span>
              <el-icon style="transform: translateY(3px)"><ArrowRightBold></ArrowRightBold></el-icon>
            </div>
          </light-card>
          <light-card style="margin-top: 15px">
            <div class="collect-list-button" @click="mytopic=true">
              <span><el-icon style="margin-right: 5px;color: gold"><MilkTea /></el-icon>查看我的帖子</span>
              <el-icon style="transform: translateY(3px)"><ArrowRightBold></ArrowRightBold></el-icon>
            </div>
          </light-card>
          <light-card style="margin-top: 15px">
            <div style="font-weight: bold">
              <el-icon style="translate: 0 2px"><CollectionTag> </CollectionTag></el-icon>
              论坛公告
              <el-divider style="margin: 10px 0"></el-divider>
            </div>
            <div style="font-size: 14px;margin: 10px;color: grey">
              小伙伴们可以发送自己的帖子进行交友，但是请遵守相关法律法规!
            </div>
          </light-card>
          <light-card style="margin-top: 20px;height: 230px" >
            <div style="font-weight: bold">
              <el-icon style="translate: 0 2px"><Calendar> </Calendar></el-icon>
              天气信息
              <el-divider style="margin: 10px 0"></el-divider>
            </div>
           <weather :data="weather"></weather>
          </light-card>
          <light-card style="margin-top: 20px">
            <div style="display: flex;justify-content: space-between;color: grey;font-size: 14px">
              今天是 :
              <div >{{registerDate}}</div>
            </div>

          </light-card>
        </div>

      </div>
      <TopicEditor :show="editor" @close="onTopicCreat"></TopicEditor>
      <topic-collect-list :show="collects" @close="collects=false"></topic-collect-list>
      <topic-like-list :show="likes" @close="likes=false"></topic-like-list>
      <my-topic-list :show="mytopic" @close="mytopic=false"></my-topic-list>
    </div>
</template>

<style scoped lang="less">
.creat-topic{
  background-color: #efefef;
  border-radius: 8px;
  height: 40px;
  color: grey;
  font-size: 14px;
  line-height: 40px;
  padding: 0 10px;
  &:hover {
    cursor: pointer;
  }
}
.topic-card{
  padding: 15px;
  transition: scale .3s;
  &:hover{
    scale: 1.015;
    cursor: pointer;
  }
  .topic-content{
    font-size: 13px;
    //color: grey;
    margin: 5px 0;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 3;
    overflow: hidden;
    text-overflow: ellipsis;
    padding-right: 10px; /* 添加额外的空白 */
  }

  .topic-image {
    width: 100%;
    height: 100%;
    max-height: 110px;
    border-radius: 5px;
  }

}
.type-select-card{
  background-color: #f5f5f5;
  padding: 2px 7px;
  font-size: 14px;
  border-radius: 5px;
  transition: background-color .3s;
  //box-sizing: border-box;
  &.active {
    border: solid 1px dodgerblue;
  }
  &:hover{
    scale: 1.015;
    cursor: pointer;
    background-color: #dadada;
  }
}
.top-topic{
  display: flex;
  div:first-of-type{
    font-size: 14px;
    font-weight: bold;
    margin-left: 10px;
    opacity: 0.8;
    transition: color .3s;
    &:hover{
      color: dodgerblue;
    }
  }
  div:nth-of-type(2) {
    flex: 1;
    color: grey;
    font-size: 13px;
    text-align: right;
  }
  &:hover{
    cursor: pointer;
  }
}
.collect-list-button{
  font-size: 14px;
  display: flex;
  justify-content: space-between;
  transition: .3s;
  &:hover{
    scale: 1.015;
    cursor: pointer;
    opacity: 0.6;
    color: dodgerblue;
  }
}
</style>