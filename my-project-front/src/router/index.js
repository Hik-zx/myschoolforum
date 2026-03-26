import {createRouter,createWebHistory} from "vue-router";
import {useUserStore} from "@/store/userStore.js";
import {ElMessage} from "element-plus";
const router=createRouter({
    //history模式，地址栏不带#
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'welcome',
            component: () => import('@/views/WelcomeView.vue'),
            children: [
                {
                    path: '',
                    name: 'welcome-login',
                    component: ()=> import('@/views/welcome/LoginPage.vue')
                },
                {
                    path: 'reset',
                    name: 'welcome-reset',
                    component: ()=> import('@/views/welcome/ResetPassword.vue')
                },

            ]
        },
        {
            path: '/index',
            name: 'indexPage',
            component: ()=>import('@/views/IndexView.vue'),
            children: [
                {
                    path: '',
                    name: 'topics',
                    component: ()  => import('@/views/forum/Forum.vue'),
                    children: [
                        {
                            path: '',
                            name: 'topic-list',
                            component: ()  => import('@/views/forum/TopicList.vue')
                        },
                        {
                            path: 'topic-detail/:tid',
                            name: 'topic-detail',
                            component: ()  => import('@/views/forum/TopicDetail.vue')
                        }
                    ]
                },
                {
                    path: 'user-setting',
                    name: 'user-setting',
                    component: ()  => import('@/views/settings/UserSetting.vue')
                },
                {
                    path: 'user-privacy',
                    name: 'user-privacy',
                    component: ()  => import('@/views/settings/Privacy.vue')
                }
            ]

        }
    ]
})
/**
 * 路由守卫
 */
//登录访问拦截
router.beforeEach((to) => {
    //判断是否有token，没有则返回登录页
    const usestore = useUserStore()
    if (!usestore.token && (to.path !== '/'&& to.path !== '/reset')) {
        ElMessage.error("登录失效，请重新登录!")
        return '/'
    }

    return true
})
export default router