<script setup>
import {topicDetailsService,interactService,updateTopicService,} from "@/api/topicApi.js";
import {deleteCommentService} from "@/api/topiccommentApi.js";
import {useRoute} from "vue-router";
import {getCommentsService} from "@/api/topiccommentApi.js";
import {reactive,computed,ref} from "vue";
import { QuillDeltaToHtmlConverter } from 'quill-delta-to-html';
import {
  ArrowLeft,
  CircleCheck,
  Female,
  Male,
  Star,
  Plus,
  ChatDotSquare,
  ChatSquare,
  Delete
} from "@element-plus/icons-vue";
import Card from "@/components/Card.vue";
import router from "@/router/index.js";
import TopcTag from "@/components/TopcTag.vue";
import InteractButton from "@/components/InteractButton.vue";
import {ElMessage} from "element-plus";
import {useUserStore} from "@/store/userStore.js";
import TopicEditor from "@/components/TopicEditor.vue";
const usestore=useUserStore()
const route=useRoute()
const edit=ref(false)
const topic=reactive({
  data: null,
  like: false,
  collect: false,
  comments: [],
  page: 1
})
const comment = reactive({
  show: false,
  text: '',
  quote: null
})
//获取路由参数
const tid=route.params.tid
//获取帖子详细信息
const gettopicDetail=async ()=>{
const res=  await topicDetailsService(tid)
  topic.data=res.data.data
  topic.like=res.data.data.interact.like
  topic.collect=res.data.data.interact.collect
  //获取帖子详细时获取评论
  loadComments(1)
  console.log(res.data.data)
}
gettopicDetail()
//转换数据格式
function converttoHtml (content){
  const ops = JSON.parse(content).ops
  const converter = new QuillDeltaToHtmlConverter(ops, { inlineStyles: true });
  return converter.convert();
}
//点赞收藏
//，topic[type] 是访问 topic 对象中与 type 变量相对应的属性的表达式
const interact=async (type,messgae)=>{
 await interactService(tid,type,!topic[type])
  //赋值
  topic[type]=!topic[type]
   if(topic[type]){
     ElMessage.success(`${messgae}成功!`)
   }else {
     ElMessage.success(`已取消${messgae}!`)
   }
}
//更新帖子
const updateTopic=async (editor)=>{
  await updateTopicService(tid,editor.type.id,editor.title,editor.text)
  ElMessage.success('内容更新成功！')
  edit.value=false
  await gettopicDetail()
}
//获取评论
const getComments=async (page)=>{
 const res= await getCommentsService(page-1,tid)
  topic.comments=res.data.data
}
function loadComments(page) {
  topic.comments = []
  topic.page = page
  getComments(page)
  //get(`/api/forum/comments?tid=${tid}&page=${page - 1}`, data => topic.comments = data)
}
function onCommentAdd() {
  comment.show = false
  loadComments(Math.floor(++topic.data.comment / 10)+1)
}
//删除评论
const deletecomments=async (id)=>{
  await deleteCommentService(id)
  ElMessage.success('评论删除成功!')
  loadComments(topic.page)
}
</script>

<template>
  <div class="topic-page" v-if="topic.data">
    <div class="topic-main" style="position: sticky;top: 0;z-index: 10">
      <card style="display: flex;width: 100%;">
        <el-button :icon="ArrowLeft" type="primary" size="small"
                   plain round @click="router.push('/index')">返回列表</el-button>
                <div style="text-align: center;flex: 1">
                  <topc-tag :type="topic.data.type"></topc-tag>
                  <span style="font-weight: bold;margin-left: 10px">{{topic.data.title}}</span>
                </div>
      </card>
    </div>
    <div class="topic-main">
      <div class="topic-main-left">
        <el-avatar :size="60" :src="usestore.avatarUserUrl(topic.data.user.avatar)"></el-avatar>
        <div>
          <div style="font-size: 18px;font-weight: bold">
            {{topic.data.user.username}}
            <span style="color: hotpink" v-if="topic.data.user.gender === 0">
                            <el-icon style="translate: 0 4px"><Female/></el-icon>
                        </span>
            <span style="color: dodgerblue" v-if="topic.data.user.gender === 1">
                            <el-icon style="translate: 0 4px"><Male/></el-icon>
                        </span>
          </div>
          <div class="desc">{{topic.data.user.email}}</div>
        </div>
        <el-divider style="margin: 10px 0"></el-divider>
        <div style="text-align: left;margin: 0 5px">
          <div class="desc">微信： {{topic.data.user.wx || '已隐藏或未填写'}}</div>
          <div class="desc">QQ： {{topic.data.user.qq || '已隐藏或未填写'}}</div>
          <div class="desc">手机： {{topic.data.user.phone || '已隐藏或未填写'}}</div>
        </div>
        <el-divider style="margin: 10px 0"/>
        <div class="desc" style="margin: 0 5px">{{topic.data.user.desc}}</div>
      </div>
      <div class="topic-main-right">
          <div class="topic-content" v-html="converttoHtml(topic.data.content)"></div>
        <el-divider/>
        <div style="font-size: 13px;color: grey;text-align: center">
          <div>发帖时间: {{new Date(topic.data.time).toLocaleString()}}</div>
        </div>
        <div style="text-align: right;margin-top: 30px">
          <interact-button name="编辑" color="dodgerblue"
                           :check="false"  @check="edit=true"
                           style="margin-right: 20px"
                           v-if="usestore.user.id===topic.data.user.id">
            <el-icon><CircleCheck></CircleCheck></el-icon>
          </interact-button>
          <interact-button name="点赞" color="pink" :check="topic.like" check-name="已点赞" @check="interact('like','点赞')">
            <el-icon><CircleCheck></CircleCheck></el-icon>
          </interact-button>
          <interact-button name="收藏" style="margin-left: 20px" color="orange" :check="topic.collect" check-name="已收藏" @check="interact('collect','收藏')">
            <el-icon><Star></Star></el-icon>
          </interact-button>
          <interact-button name="评论" style="margin-left: 20px" color="green" @click="comment.show = true;comment.quote = null">
            <el-icon><ChatDotSquare /></el-icon>
          </interact-button>
        </div>
      </div>
    </div>
    <div style="display: flex;margin: 0 auto;width: 800px;font-weight: bold">评论：</div>
    <transition name="el-fade-in-linear" mode="out-in" >
      <div v-if="topic.comments.length">
        <div class="topic-main" style="margin-top: 10px" v-for="item in topic.comments">
          <div class="topic-main-left">
            <el-avatar :size="60" :src="usestore.avatarUserUrl(item.user.avatar)"></el-avatar>
            <div>
              <div style="font-size: 18px;font-weight: bold">
                {{item.user.username}}
                <span style="color: hotpink" v-if="item.user.gender === true">
                            <el-icon style="translate: 0 4px"><Female/></el-icon>
                        </span>
                <span style="color: dodgerblue" v-if="item.user.gender === false">
                            <el-icon style="translate: 0 4px"><Male/></el-icon>
                        </span>
              </div>
              <div class="desc">{{item.user.email}}</div>
            </div>
            <el-divider style="margin: 10px 0"></el-divider>
            <div style="text-align: left;margin: 0 5px">
              <div class="desc">微信： {{item.user.wx || '已隐藏或未填写'}}</div>
              <div class="desc">QQ： {{item.user.qq || '已隐藏或未填写'}}</div>
              <div class="desc">手机： {{item.user.phone || '已隐藏或未填写'}}</div>
            </div>
          </div>
          <div class="topic-main-right">

            <div style="font-size: 13px;color: grey;">
              <div>评论时间: {{new Date(item.time).toLocaleString()}}</div>
            </div>
            <div v-if="item.quote" class="comment-quote">
              回复: {{item.quote}}
            </div>
            <div class="topic-content" v-html="converttoHtml(item.content)"></div>
            <div style="text-align: right">
              <el-link :icon="ChatSquare" @click="comment.show =true;comment.quote=item" type="primary">
                 &nbsp;回复评论
              </el-link>
              <el-link :icon="Delete"  type="danger"
                       @click="deletecomments(item.id)"
                       v-if="item.user.id ===usestore.user.id" style="margin-left: 20px">
                &nbsp;删除评论
              </el-link>
            </div>
          </div>
        </div>
        <div style="width: fit-content;margin: 20px auto">
          <el-pagination background layout="prev, pager, next"
                         v-model:current-page="topic.page" @current-change="loadComments"
                         :total="topic.data.comment" :page-size="10" hide-on-single-page/>
        </div>
      </div>

        <el-empty :image-size="80" description="暂时没有任何评论！ 快来发表吧！" v-else></el-empty>

    </transition>

   <topic-editor :show="edit" @close="edit=false" v-if="topic.data && usestore.forum.types"
                 :default-type="topic.data.type" :default-text="topic.data.content"
                 :default-title="topic.data.title" submit-button="更新帖子内容" :submit="updateTopic">
   </topic-editor>
    <topic-comment-editor :show="comment.show" @close="comment.show = false" :tid="tid"
                          :quote="comment.quote" @comment="onCommentAdd"/>
<!--    <div class="add-comment" @click="comment.show = true;comment.quote = null">-->
<!--      <el-icon><Plus/></el-icon>-->
<!--    </div>-->
  </div>
</template>

<style  lang="less" scoped>
.comment-quote {
  font-size: 13px;
  color: grey;
  background-color: rgba(94, 94, 94, 0.1);
  padding: 10px;
  margin-top: 10px;
  border-radius: 5px;
}
.add-comment {
  position: fixed;
  bottom: 20px;
  right: 20px;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  font-size: 18px;
  color: var(--el-color-primary);
  text-align: center;
  line-height: 45px;
  background: var(--el-bg-color-overlay);
  box-shadow: var(--el-box-shadow-lighter);

  &:hover {
    background: var(--el-border-color-extra-light);
    cursor: pointer;
  }
}
.topic-page {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 10px 0;
}

.topic-main {
  display: flex;
  border-radius: 7px;
  margin: 0 auto;
  background-color: var(--el-bg-color);
  width: 800px;

  .topic-main-left {
    width: 200px;
    padding: 10px;
    text-align: center;
    border-right: solid 1px var(--el-border-color);

    .desc {
      font-size: 12px;
      color: grey;
    }
  }

  .topic-main-right {
    width: 600px;
    padding: 10px 20px;
    display: flex;
    flex-direction: column;

    .topic-content {
      font-size: 14px;
      line-height: 22px;
      opacity: 0.8;
      flex: 1;
    }
  }
}
</style>