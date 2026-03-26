import { useUserStore } from '@/store/userStore.js'
// 导入 element-plus 包
import { ElMessage } from 'element-plus'
import router from '@/router'
import axios from 'axios'
const baseURL = 'http://localhost:8080'
const instance = axios.create({
    baseURL,
    timeout: 10000
})

// 添加请求拦截器
instance.interceptors.request.use(
    async (config) => {
        const userStore = useUserStore()
        //登录时请求头不会有token，执行下一个请求才会把token带入请求头
        if (userStore.token) {
            //设置 HTTP 请求头中的 Authorization 字段，用于携带用户的令牌（token）
            config.headers.Authorization ='Bearer ' +userStore.getToken()
            console.log('后面：' + userStore.token)
        }

        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

// 添加响应拦截器
instance.interceptors.response.use(
    (response) => {
        // 2xx 范围内的状态码都会触发该函数。
        // 对响应数据做点什么
        if (response.data.code === 200) {
            return response
        }
        // 处理失败,给错误提示，抛出错误
        ElMessage.error(response.data.msg || '服务异常')
        return Promise.reject(response.data)
    },
    (error) => {
        // 超出 2xx 范围的状态码都会触发该函数。
        // 对响应错误做点什么
        // 处理 401 错误 权限不足 或 token 过期 => 拦截到登录
        if (error.response?.status === 401) {
            router.push('/login')
        }
        // 错误的默认情况
        ElMessage.error(error.response.data.msg || '服务异常')
        return Promise.reject(error)
    }
)
// 发送登录请求时跳过请求拦截器
// 导出 instance
export default instance
export { baseURL }
