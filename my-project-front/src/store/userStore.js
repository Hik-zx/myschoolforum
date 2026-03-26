// 状态管理模块
import { defineStore } from 'pinia'
import { ref } from 'vue'

// 用户模块 token,setToken,removeToken
export const useUserStore = defineStore(
    'token',
    () => {
        const token = ref('')
        const setToken = (newToken) => {
            token.value = newToken
        }
        const getToken = () => {
            return token.value
        }
        const removeToken = () => {
            token.value = ''
        }

        const user = ref({})
        const getUser = async () => {
            const res = await userGetInfoService() // 请求获取数据
            user.value = res.data.data
        }
        const setUser = (obj) => {
            user.value = obj
        }
        //获取头像
        const avatarUrl = () => {
            if (user.value.avatar) {
                return `http://localhost:8080/image${user.value.avatar}`
            } else {
                return 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
            }
        }
        const avatarUserUrl = (avatar) => {
            if (avatar) {
                return `http://localhost:8080/image${avatar}`
            } else {
                return 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
            }
        }
        const forum = ref({
            types: []
        })
        const setForum = (obj) => {
            forum.value = obj
        }
        //通过id获取类型
        const findTypeByid=(id)=>{
                for (let type of forum.value.types){
                    if(type.id === id)
                        return type
                }
        }
        return {
            token,
            setToken,
            removeToken,
            user,
            getUser,
            setUser,
            getToken,
            avatarUrl,
            avatarUserUrl,
            forum,
            setForum,
            findTypeByid
        }
    },
    {
        persist: true ,//默认所有都持久化

    }
)
