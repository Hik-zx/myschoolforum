<script setup>
import {ref} from "vue";
import {Lock, Message} from "@element-plus/icons-vue";
import {ElMessage} from "element-plus";
import {confirmResetuserService, resetPasswordService} from "@/api/userApi.js";
import {askcode} from "@/api/userApi.js";
import router from "@/router/index.js";
//冷却时间
const codetime=ref(0)
const active=ref(0)
const form = ref()
const formModel=ref({
  email: '',
  code: '',
  password: '',
  repassword: '',
})
const rules={
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    {
      type: 'email',
      message: '请输入有效的邮箱地址',
      trigger: ['blur', 'change']
    }
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
}
//获取验证码
const askcodea=async () =>{
  // 登录成功之前，先进行校验，校验成功 → 请求，校验失败 → 自动提示
  await form.value.validate()
  if (!formModel.value.email) {
    ElMessage.error('邮箱不能为空');
    return;
  }

  await askcode(formModel.value.email,'reset')
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
//判断是否确认重置密码
const confirmreset= async ()=>{
  // 登录成功之前，先进行校验，校验成功 → 请求，校验失败 → 自动提示
  await form.value.validate()
  await confirmResetuserService(formModel.value.email,formModel.value.code)
  active.value++
  ElMessage.success("开始重置密码")
}
//重置密码
const resetpassword=async ()=>{
  await form.value.validate()
  await resetPasswordService(formModel.value.email,formModel.value.code,formModel.value.password)
  active.value++
  ElMessage.success("重置密码成功，请登录！")
  router.push('/')
}
</script>

<template>
<div>
  <div style="margin-top: 10px ">
    <el-steps :active="active" process-status="process" finish-status="success">
    <el-step title="验证电子邮件" />
    <el-step title="重新设定密码" />
    </el-steps>
  </div>

  <div v-if="active===0">
  <div style="margin-top: 60px">
    <h3>请输入您要重置密码的电子邮箱:</h3>
  </div>
  <el-form :rules="rules" ref="form" :model="formModel">
    <el-form-item prop="email">
      <el-input
          v-model="formModel.email"
          :prefix-icon="Message"
          placeholder="请输入邮箱"
      ></el-input>
    </el-form-item>
    <div style="margin-top: 30px">
      <h3>请输入邮箱验证码:</h3>
    </div>
    <el-form-item  prop="">
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
          @click="confirmreset"
         style="margin-top:10px"
      >
        开始重置密码
      </el-button>
    </el-form-item>
  </el-form>
  </div>
  <div v-if="active===1">
    <div style="margin-top: 60px">
      <h3>请输入您要重置的密码:</h3>
    </div>
    <el-form :rules="rules" ref="form" :model="formModel">
      <el-form-item prop="password">
        <el-input
            v-model="formModel.password"
            :prefix-icon="Lock"
            placeholder="请输入密码"
        ></el-input>
      </el-form-item>
      <div style="margin-top: 30px">
        <h3>请再次输入您要重置的密码:</h3>
      </div>
      <el-form-item  prop="repassword">
        <el-input
            v-model="formModel.repassword"
            :prefix-icon="Lock"
            placeholder="请再次输入密码"
        ></el-input>
      </el-form-item>
      <el-form-item>
        <el-button
            class="button"
            type="primary"
            @click="resetpassword"
            style="margin-top:10px"
            auto-insert-space
        >
          重置密码
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</div>
</template>

<style scoped lang="scss">

</style>