//将Pinia进行统一管理,方便维护
import { createPinia } from 'pinia'
//导入pinia持久化插件
import persist from 'pinia-plugin-persistedstate'

const pinia = createPinia()
pinia.use(persist)
export default pinia
