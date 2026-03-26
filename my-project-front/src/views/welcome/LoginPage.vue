<script setup>
import {User,Lock,Message} from '@element-plus/icons-vue'
import { ref, watch } from 'vue'
import {  userLoginService } from '@/api/userApi.js'
import {askcode} from "@/api/userApi.js";
import {useUserStore} from '@/store/userStore.js'
import {registerUser} from "@/api/userApi.js";
import {ElMessage} from "element-plus";
import router from "@/router"
import {onMounted} from "vue";
//冷却时间
const codetime=ref(0)
const userStore=useUserStore()
//绑定表单，用来获取dom对象
const form = ref()
const isRegister = ref(false)
// 整个的用于提交的form数据对象
const formModel = ref({
  username: '',
  password: '',
  repassword: '',
  email: '',
  code: ''
})
//记住我
const rememberMe=ref(false)
//规则配置
const rules={
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 10, message: '用户名必须是 3-10位 的字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    {
      pattern: /^\S{4,15}$/,
      message: '密码必须是 6-15位 的非空字符',
      trigger: 'blur'
    }
  ],
  repassword: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    {
      pattern: /^\S{6,15}$/,
      message: '密码必须是 6-15位 的非空字符',
      trigger: 'blur'
    },
    {
      validator: (rule, value, callback) => {
        // 判断 value 和 当前 form 中收集的 password 是否一致
        if (value !== formModel.value.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback() // 就算校验成功，也需要callback
        }
      },
      trigger: 'blur'
    }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    {
      type: 'email',
      message: '请输入有效的邮箱地址',
      trigger: ['blur', 'change']
    }
  ],
}
// 切换的时候，重置表单内容
watch(isRegister, () => {
  formModel.value = {
    username: '',
    password: ''
  }
})

//登录
const login=async ()=>{
  // 登录成功之前，先进行校验，校验成功 → 请求，校验失败 → 自动提示
  await form.value.validate()
 const res= await userLoginService(
     formModel.value.username,
     formModel.value.password
 )
  ElMessage.success('登录成功!   欢迎您： '+res.data.data.username+'!')
  userStore.setToken(res.data.data.token)
  router.push('/index')
  if (rememberMe.value) {
    // 如果用户勾选了记住我，则保存凭据到本地存储中
    localStorage.setItem('savedCredentials', JSON.stringify({
      username: formModel.value.username,
      password: formModel.value.password
    }));
  } else {
    // 如果用户取消勾选记住我，则清除本地存储中的凭据
    localStorage.removeItem('savedCredentials');
  }
}
//获取验证码
const askcodea=async () =>{
  // 登录成功之前，先进行校验，校验成功 → 请求，校验失败 → 自动提示
  await form.value.validate()
  if (!formModel.value.email) {
    ElMessage.error('邮箱不能为空');
    return;
  }

  await askcode(formModel.value.email,'register')
  ElMessage.success('获取验证码成功!请注意查收!')
  codetime.value=60
 const timer=setInterval(() => {
    if (codetime.value > 0) {
      codetime.value--;
    } else {
      clearInterval(timer); // 倒计时结束时清除定时器
    }
  }, 1000);
}
//注册用户
const register=async () => {
  // 登录成功之前，先进行校验，校验成功 → 请求，校验失败 → 自动提示
  await form.value.validate()
  await registerUser(formModel.value.username,formModel.value.password,
                     formModel.value.email,formModel.value.code)
  ElMessage.success('注册成功')
  location.reload();
}
//初始话数据（记住我功能）
onMounted(()=> {
  // 检查 localStorage 中是否存在记住的用户信息
  const rememberedUser = localStorage.getItem('savedCredentials');
  if (rememberedUser) {
    // 如果存在，则解析用户信息并填充到表单中
    const { username, password } = JSON.parse(rememberedUser);
    formModel.value.username = username;
    formModel.value.password = password;
    // 同时将“记住我”选项设为 true
    rememberMe.value = true;
  }
})
</script>

<template>
  <div><h1>欢迎来到我的个人网站</h1></div>
<!--    <div class="formCss">-->
      <!-- 注册相关表单 -->
      <el-form
          :model="formModel"
          :rules="rules"
          ref="form"
          size="large"
          autocomplete="off"
          v-if="isRegister"
          label-width="82px"
      >
        <el-form-item>
          <h1>注册</h1>
        </el-form-item>
        <el-form-item label="用户" prop="username">
          <el-input
              v-model="formModel.username"
              :prefix-icon="User"
              placeholder="请输入用户名"
          ></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
              v-model="formModel.password"
              :prefix-icon="Lock"
              type="password"
              placeholder="请输入密码"
              show-password
          ></el-input>
        </el-form-item>
        <el-form-item label="重复密码" prop="repassword">
          <el-input
              v-model="formModel.repassword"
              :prefix-icon="Lock"
              type="password"
              placeholder="请再次输入密码"
              show-password
          ></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input
              v-model="formModel.email"
              :prefix-icon="Message"
              placeholder="请输入邮箱"
          ></el-input>
        </el-form-item>
        <el-form-item label="验证码" prop="">
          <el-input
              v-model="formModel.code"
              placeholder="请输入验证码"
              style="width: 180px;"
          ></el-input>
          <el-button
              type="primary"
              style="margin-left: 10px;"
              @click="askcodea"
              :disabled="codetime"
          >
            {{ codetime>0 ? `${codetime} s` : '获取验证码' }}
          </el-button>
        </el-form-item>

        <el-form-item>
          <el-button
              class="button"
              type="primary"
              @click="register"
              auto-insert-space
          >
            注册
          </el-button>
        </el-form-item>
        <el-form-item class="flex">
          <el-link type="primary" :underline="false" @click="isRegister = false" >
            ← 返回
          </el-link>
        </el-form-item>
      </el-form>
      <!-- 登录相关表单 -->
      <el-form
      size="large"
      ref="form"
      :model="formModel"
      :rules="rules"
      v-else
      >
        <el-form-item>
          <h1>登录</h1>
        </el-form-item>
        <el-form-item label="用户" prop="username">
          <el-input
              :prefix-icon="User"
              placeholder="请输入用户名"
              name="username"
              v-model="formModel.username"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
              :prefix-icon="Lock"
              type="password"
              placeholder="请输入密码"
              v-model="formModel.password"
              show-password
          />
        </el-form-item>
        <el-form-item class="flex">
          <div class="flex">
            <el-checkbox v-model="rememberMe" >记住我</el-checkbox>
            <el-link type="primary" :underline="false" @click="router.push('/reset')">忘记密码？</el-link>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button class="button" type="primary" @click="login">登录</el-button>
        </el-form-item>
        <el-form-item class="flex">
          <el-link type="primary" :underline="false" @click="isRegister = true">
           没有账号？立即注册 →
          </el-link>
        </el-form-item>
      </el-form>
<!--    </div>-->

</template>

<style lang="scss" scoped>
.title {
  margin: 0 auto;
}
.button {
  width: 100%;
}
.flex {
  width: 100%;
  display: flex;
  justify-content: space-between;
}

</style>