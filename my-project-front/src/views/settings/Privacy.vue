<script setup>
import {ref,onMounted} from "vue";
import {modifyPasswordService,getAccountPrivacyService,savePrivacyService} from "@/api/userApi.js";
import Card from "@/components/Card.vue";
import {Lock, Setting, Switch} from "@element-plus/icons-vue";
import {ElMessage} from "element-plus";
const form=ref()
const saving=ref(true)
//修改密码表单
const formmodel=ref({
  password: '',
  new_password: '',
  new_password_repeat: '',
})
//隐私设置表单
const privacyform=ref({
  phone: false,
  wx: false,
  qq: false,
  email: false,
  gender: false,
})
//规则
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
  new_password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    {
      pattern: /^\S{4,15}$/,
      message: '密码必须是 6-15位 的非空字符',
      trigger: 'blur'
    }
  ],
  new_password_repeat: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    {
      pattern: /^\S{6,15}$/,
      message: '密码必须是 6-15位 的非空字符',
      trigger: 'blur'
    },
    {
      validator: (rule, value, callback) => {
        // 判断 value 和 当前 form 中收集的 password 是否一致
        if (value !== formmodel.value.new_password) {
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
//重置密码
const modifyPassword=async ()=>{
  await form.value.validate()
  await modifyPasswordService(formmodel.value.password,formmodel.value.new_password)
  ElMessage.success("密码修改成功!")
  formmodel.value.password=''
  formmodel.value.new_password=''
  formmodel.value.new_password_repeat=''

}
//获取用户隐私转态
const getPrivacy=async ()=>{
 const res= await getAccountPrivacyService();
  privacyform.value.phone=res.data.data.phone
  privacyform.value.gender=res.data.data.gender
  privacyform.value.qq=res.data.data.qq
  privacyform.value.wx=res.data.data.wx
  privacyform.value.email=res.data.data.email
  saving.value=false
}
//保存隐私设置
const savePrivacy=async (type,status)=>{
  saving.value=true
  await savePrivacyService(type,status)
  ElMessage.success("修改成功")
  saving.value=false
}
onMounted(()=>{
  getPrivacy()
})
</script>

<template>
  <div style="max-width: 800px;margin: auto">
    <div style="margin-top: 20px">
      <Card :icon="Setting" title="隐私设置" desc="在这里设置哪些内容可见，请各位小伙伴注重个人隐私" v-loading="saving">
        <div class="check-list">
          <el-checkbox v-model="privacyform.phone" @change="savePrivacy('phone',privacyform.phone)">公开展示我的手机号</el-checkbox>
          <el-checkbox v-model="privacyform.email" @change="savePrivacy('email',privacyform.email)">公开展示我的邮箱</el-checkbox>
          <el-checkbox v-model="privacyform.wx" @change="savePrivacy('wx',privacyform.wx)">公开展示我的微信号</el-checkbox>
          <el-checkbox v-model="privacyform.qq" @change="savePrivacy('qq',privacyform.qq)">公开展示我的QQ号</el-checkbox>
          <el-checkbox v-model="privacyform.gender" @change="savePrivacy('gender',privacyform.gender)">公开展示我的性别</el-checkbox>
        </div>
      </Card>

      <Card style="margin: 20px 0" :icon="Setting" title="修改密码" >
        <p slot="desc" style="color: red; font-size: 12px;font-weight: bold">在此修改密码，请务必牢记您的密码</p>
        <el-form
            :rules="rules"
            ref="form"
            :model="formmodel"
            label-width="100px"
            style="margin-left: 15px">
          <el-form-item label="当前密码" prop="password">
            <el-input show-password type="password" v-model="formmodel.password" :prefix-icon="Lock" placeholder="当前密码" maxlength="16"></el-input>
          </el-form-item>
          <el-form-item label="新密码" prop="new_password">
            <el-input show-password type="password" v-model="formmodel.new_password" :prefix-icon="Lock" placeholder="新密码" maxlength="16"></el-input>
          </el-form-item>
          <el-form-item label="重复新密码" prop="new_password_repeat">
            <el-input show-password type="password" v-model="formmodel.new_password_repeat" :prefix-icon="Lock" placeholder="重新输入新密码" maxlength="16"></el-input>
          </el-form-item>
          <div style="text-align: center">
            <el-button :icon="Switch" type="success" @click="modifyPassword" plain>立即重置密码</el-button>
          </div>

        </el-form>
      </Card>
    </div>
  </div>
</template>

<style scoped lang="scss">
.check-list{
  margin: 10px 0 0 10px ;
  display: flex;
  flex-direction: column;
}
</style>