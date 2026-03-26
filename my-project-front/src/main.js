import { createApp } from 'vue'
import App from './App.vue'
import router from "@/router/index.js";
import pinia from "@/store/index.js";
import { ElMessage } from 'element-plus'
import 'element-plus/theme-chalk/el-message.css';
const app=createApp(App)

app.use(pinia)
app.use(ElMessage)
app.use(router)
app.mount('#app')

