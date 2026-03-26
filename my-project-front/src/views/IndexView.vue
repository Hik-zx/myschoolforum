<script setup>
import {ref,onMounted,reactive} from "vue";
import {useUserStore} from "@/store/userStore.js";
import router from "@/router/index.js";
import {ElMessage, ElMessageBox} from "element-plus";
import {
  getNotificationService,
  comfireService,
  deleteAllNotificationService,
} from "@/api/notificationApi.js";
import {logout} from "@/api/userApi.js";
import {getAccountByIdService} from "@/api/userApi.js";
import {
  Back,
  Bell,
  ChatDotSquare, Check, Collection, DataLine,
  Document, Files,
  Location, Lock, Message, Monitor,
  Notification, Operation,
  Position,
  School, Search,
  Umbrella, User
} from "@element-plus/icons-vue";
import LightCard from "@/components/lightCard.vue";

const userstore=useUserStore()
const loding=ref(true)
const notification=ref([])
//reactive无需.value访问属性值
const searchInput=reactive({
  type: '1',
  text: ''
})

const logoutI= async ()=>{
  // //退出操作 await等待操作完毕执行
  await logout()
  ElMessage.success( '退出成功！')
  router.push('/')
  //清除本地数据（token和user信息）
  userstore.removeToken()
}
//根据id查询登录用户信息
const getAccountInfo=async ()=>{
  const res = await getAccountByIdService()
  userstore.setUser(res.data.data)
  loding.value=false
}
//获取消息提醒
const getNotification=async ()=>{
 const res= await getNotificationService()
  notification.value=res.data.data
}
getNotification()
//跳转到消息提醒对应的帖子，并且删除消息提醒
const confirmNotification=async (id,url)=>{
  await comfireService(id)
  await getNotification()
  //window.open(url)
  router.push(url)
}
//删除所有消息提醒
const deleteAllNotification=async ()=>{
  await deleteAllNotificationService()
  await getNotification()
  ElMessage.success("清除成功!")
}
//进去就加载登录用户数据
onMounted(() => {
 getAccountInfo()
})
</script>

<template>
  <div class="main-content" v-loading="loding" element-loading-text="正在加载...">
    <el-container style="height: 100%" v-if="!loding">
      <el-header class="main-content-header">
        <div style="height: 32px;font-size: 28px">
          <i class="qi-1075"></i>
          <i class="qi-1075"></i>
          <i class="qi-1075"></i>
          <i class="qi-1075"></i>
          <i class="qi-1075"></i>
          <i class="qi-1075"></i>
        </div>
        <div style="flex: 1; padding: 0 20px; text-align: center;">
          <el-input v-model="searchInput.text"
                    size="small"
                    style="width: 100%;max-width: 500px;"
                    placeholder="搜索论坛相关内容...">
            <template #prefix>
              <el-icon><Search></Search></el-icon>
            </template>
<!--            <template #append>-->
<!--              <el-select style="width: 120px" v-model="searchInput.type" size="small">-->
<!--                <el-option value="1" label="帖子广场"></el-option>-->
<!--                <el-option value="2" label="校园活动"></el-option>-->
<!--                <el-option value="3" label="表白墙"></el-option>-->
<!--                <el-option value="4" label="教务通知"></el-option>-->
<!--              </el-select>-->
<!--            </template>-->
          </el-input>
          <el-button size="small" type="primary" round plain style="margin-left: 10px">搜索</el-button>
        </div>
        <div style="flex: 1" class="user-info">
          <el-popover placement="bottom" width="350" trigger="click">
            <template #reference>
              <el-badge style="margin-right: 15px" is-dot :hidden="!notification.length">
                <div class="notifacition">
                  <el-icon><Bell></Bell></el-icon>
                  <div style="font-size: 10px">消息</div>
                </div>
              </el-badge>
            </template>
            <el-empty :image-size="80" description="暂时没有消息！" v-if="!notification.length"></el-empty>
            <el-scrollbar :max-height="500" v-else>
              <light-card v-for="item in notification" class="notification-item"
                          @click="confirmNotification(item.id, item.url)">
                <div>
                  <el-tag size="small" :type="item.type">消息</el-tag>&nbsp;
                  <span style="font-weight: bold">{{item.title}}</span>
                </div>
                <el-divider style="margin: 7px 0 3px 0"/>
                <div style="font-size: 13px;color: grey">
                  {{item.content}}
                </div>
              </light-card>
            </el-scrollbar>
            <div style="margin-top: 10px" v-if="notification.length">
              <el-button size="small" type="warning" :icon="Check" @click="deleteAllNotification"
                         style="width: 100%" plain>清除全部未读消息</el-button>
            </div>
          </el-popover>
          <div class="profile">
            <div>{{ userstore.user.username }}</div>
            <div>{{ userstore.user.email }}</div>
          </div>
          <el-dropdown>
            <el-avatar :src="userstore.avatarUrl()" style="border: 1px solid dodgerblue"></el-avatar>
            <template #dropdown>
                <el-dropdown-item @click="router.push('/index/user-setting')">
                  <el-icon><Operation></Operation></el-icon>
                  个人设置
                </el-dropdown-item>
              <el-dropdown-item>
                <el-icon><Message></Message></el-icon>
                消息列表
              </el-dropdown-item>
              <el-dropdown-item @click="logoutI" divided>
                <el-icon><Back></Back></el-icon>
                退出登录
              </el-dropdown-item>
            </template>
          </el-dropdown>

        </div>
      </el-header>
      <el-container>
        <el-aside width="230px">
          <el-scrollbar style="height: calc(100vh - 55px)">
          <el-menu
              router
              style="min-height: calc(100vh - 55px)"
              :default-active="$route.path"
              :default-openeds="['1','2','3']"
          >
            <el-sub-menu index="1">
              <template #title>
                <el-icon><Location></Location></el-icon>
                <span><b>校园论坛</b></span>
              </template>
              <el-menu-item index="/index">
                <template #title>
                  <el-icon><ChatDotSquare/></el-icon>
                 帖子广场
                </template>
              </el-menu-item>
              <el-menu-item>
                <template #title>
                  <el-icon><Bell/></el-icon>
                  失物招领
                </template>
              </el-menu-item>
              <el-menu-item>
                <template #title>
                  <el-icon><Notification/></el-icon>
                  校园活动
                </template>
              </el-menu-item>
              <el-menu-item>
                <template #title>
                  <el-icon><Umbrella/></el-icon>
                  表白墙
                </template>
              </el-menu-item>
              <el-menu-item>
                <template #title>
                  <el-icon><School/></el-icon>
                  海文考研
                  <el-tag style="margin-left: 10px" size="small">合作机构</el-tag>
                </template>
              </el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="2">
              <template #title>
                <el-icon><Position></Position></el-icon>
                <span><b>探索与发现</b></span>
              </template>
              <el-menu-item>
                <template #title>
                  <el-icon><Document/></el-icon>
                  成绩查询
                </template>
              </el-menu-item>
              <el-menu-item>
                <template #title>
                  <el-icon><Monitor/></el-icon>
                  教务通知
                </template>
              </el-menu-item>
              <el-menu-item>
                <template #title>
                  <el-icon><Files/></el-icon>
                  班级课程表
                </template>
              </el-menu-item>
              <el-menu-item>
                <template #title>
                  <el-icon><Collection/></el-icon>
                  在线图书馆
                </template>
              </el-menu-item>
              <el-menu-item>
                <template #title>
                  <el-icon><DataLine/></el-icon>
                  教师预约
                </template>
              </el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="3">
              <template #title>
                <el-icon><Operation></Operation></el-icon>
                <span><b>个人设置</b></span>
              </template>
              <el-menu-item index="/index/user-setting">
                <template #title>
                  <el-icon><User/></el-icon>
                  个人信息设置
                </template>
              </el-menu-item>
              <el-menu-item index="/index/user-privacy">
                <template #title>
                  <el-icon><Lock/></el-icon>
                  账号安全设置
                </template>
              </el-menu-item>
              </el-sub-menu>
          </el-menu>
          </el-scrollbar>
        </el-aside>
        <el-main class="main-content-page" >
          <el-scrollbar style="height: calc(100vh - 55px)">
            <router-view v-slot="{Component}">
              <transition name="el-fade-in-linear" mode="out-in">
                <component :is="Component" style="height: 100%"></component>
              </transition>
            </router-view>
          </el-scrollbar>
        </el-main>
      </el-container>
    </el-container>
  </div>

</template>

<style scoped lang="less">
.notification-item {
  transition: .3s;
  margin-bottom: 15px;
  &:hover {
    cursor: pointer;
    opacity: 0.7;
  }
}
.notifacition{
  font-size: 22px;
  line-height: 14px;
  text-align: center;
  transition: color .3s;
  &:hover{
    color: grey;
    cursor: pointer;
  }
}
.main-content{
  height: 100vh;
  width: 100vw;
}
.main-content-page{
  padding: 0;
  background-color: #f7f8fa;
}
.main-content-header{
  border-bottom: solid 1px var(--el-border-color);
  height: 55px;
  display: flex;
  align-items: center;
  box-sizing: border-box;
  .logo{
    height: 32px;
  }

  .user-info{
    display: flex;
    justify-content: flex-end;//如果主轴是水平方向（默认情况），justify-content: flex-end; 将把所有子元素向右对齐，靠近容器的右侧边缘。
    align-items: center;
    .el-avatar:hover{
      cursor: pointer;
    }

    .profile{
      text-align: right;
      margin-right: 20px;

      &>:first-child{
        font-size: 18px;
        font-weight: bold;
        line-height: 20px;
      }

      &>:last-child{
        font-size: 12px;
        color: #3b3636;
      }
    }
  }
}
</style>