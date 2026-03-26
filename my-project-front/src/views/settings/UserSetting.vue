<script setup>
import {computed,ref,onMounted} from "vue";
import Card from "@/components/Card.vue";
import {Apple, Grid, Management, Message, Notebook, Phone, Refresh, Select, User} from "@element-plus/icons-vue";
import {useUserStore} from "@/store/userStore.js";
import {saveDetailsService,getAccountDetailsByIdService,modifyEmailService,askcode} from "@/api/userApi.js";
import {ElMessage} from "element-plus";
const accessHeader=()=>{
  return{
    'Authorization': `Bearer ${userstore.getToken()}`
  }
}
//冷却时间
const codetime=ref(0)
const userstore=useUserStore()
const loading=ref({
  base: false,
  formissuccess: true
})
//转换时间格式
const registerdate= computed(()=> new Date(userstore.user.registerTime).toLocaleString() )
//绑定表单，用来获取dom对象
const form = ref()
//个人简介
const desc=ref('')
//基础表单
const baseForm=ref({
  username: '',
  gender: 1,
  qq: '',
  wx: '',
  phone: '',
  desc: ''
})
//修改邮箱表单
const emailForm=ref({
  email: '',
  code: ''
})
//规则配置
const rules={
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 10, message: '用户名必须是 3-10位 的字符', trigger: 'blur' }
  ],

  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    {
      type: 'email',
      message: '请输入有效的邮箱地址',
      trigger: ['blur', 'change']
    }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    {
      pattern: /^1[3456789]\d{9}$/,
      message: '请输入正确的手机号格式',
      trigger: ['blur', 'change']
    }
  ],
  qq: [
    { required: true, message: '请输入QQ号码', trigger: 'blur' },
    {
      pattern: /^[1-9]\d{4,9}$/,
      message: '请输入正确的QQ号码格式',
      trigger: ['blur', 'change']
    }
  ],
  wx: [
    { required: true, message: '请输入微信号', trigger: 'blur' },
    {
      pattern:  /^\d{1,20}$/,
      message: '请输入1到20位的数字作为微信号',
      trigger: ['blur', 'change']
    }
  ]
}

//保存或者更新用户信息
const saveorupdate= async ()=>{
  // 先进行校验，校验成功 → 请求，校验失败 → 自动提示
  await form.value.validate()
  loading.value.base=true
  await saveDetailsService(baseForm.value)
  ElMessage.success('用户信息保存成功!')
  loading.value.base=false
  //更新用户名称
  userstore.user.username= baseForm.value.username
  desc.value= baseForm.value.desc
}
//根据id查询登录用户详细信息
 const accountdetails= async ()=>{
 const res= await getAccountDetailsByIdService()
   baseForm.value.username=userstore.user.username
   emailForm.value.email=userstore.user.email
   baseForm.value.gender=res.data.data.gender
   baseForm.value.phone=res.data.data.phone
   baseForm.value.wx=res.data.data.wx
   baseForm.value.qq=res.data.data.qq
   baseForm.value.desc=desc.value=res.data.data.desc
   loading.value.formissuccess=false
 }
 //获取验证码
const askcodea=async () =>{
  // 登录成功之前，先进行校验，校验成功 → 请求，校验失败 → 自动提示
  await form.value.validate()
  if (!emailForm.value.email) {
    ElMessage.error('邮箱不能为空');
    return;
  }

  await askcode(emailForm.value.email,'modify')
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
//修改电子邮箱
const modifyEmail=async ()=>{
  await form.value.validate()
  if (!emailForm.value.code) {
    ElMessage.error('验证码不能为空');
    return;
  }
  await modifyEmailService(emailForm.value)
  ElMessage.success("修改成功!")
  //更新邮箱
  userstore.user.email= emailForm.value.email
  emailForm.value.code=''
}
//判断图片准确性
function beforeAvatarUpload(rawFile){
  if(rawFile.type !=='image/jpeg' && rawFile.type !=='image/png'){
    ElMessage.error('头像只能是JPG/PNG格式')
    return false
  }else if(rawFile.size /1024 >200){
    ElMessage.error('头像大小不能大于200KB')
    return false
  }
  return true
}
//上传成功
function uploadSuccess(reponse){
  ElMessage.success('头像上传成功!')
  userstore.user.avator=reponse.data.data.avatar
  location.reload(); // 刷新页面
}
onMounted(() => {
  accountdetails()
})



</script>

<template>
  <div style="display: flex;max-width: 1050px;margin: auto">
    <div class="setting-left">
      <!--父传子-->
      <Card :icon="User" title="账号信息设置" desc="在这里编辑您的个人信息，您可以在隐私设置中选择是否展示信息" v-loading="loading.formissuccess">
        <el-form
            :model="baseForm"
            :rules="rules"
            ref="form"
            label-position="top"
            style="margin: 0 10px 10px 10px">
          <el-form-item label="用户名" prop="username" >
            <el-input :prefix-icon="User" v-model="baseForm.username" maxlength="10"></el-input>
          </el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="baseForm.gender">
              <el-radio :value="1">男</el-radio>
              <el-radio :value="0">女</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input :prefix-icon="Phone" v-model="baseForm.phone" maxlength="11"></el-input>
          </el-form-item>
          <el-form-item label="QQ号" prop="qq">
            <el-input :prefix-icon="Apple" v-model="baseForm.qq" maxlength="13"></el-input>
          </el-form-item>
          <el-form-item  label="微信号" prop="wx">
            <el-input :prefix-icon="Grid" v-model="baseForm.wx" maxlength="20"></el-input>
          </el-form-item>
          <el-form-item label="个人简介">
            <el-input v-model="baseForm.desc" type="textarea" :rows="6" maxlength="200" ></el-input>
          </el-form-item>
          <div>
            <el-button :icon="Select" type="success" @click="saveorupdate" :loading="loading.base" plain>保存用户信息</el-button>
          </div>
        </el-form>
      </Card>

      <Card style="margin-top: 10px" :icon="Message" title="电子邮件设置" desc="您可以在这里修改默认的绑定邮件地址">
        <el-form ref="form" :rules="rules" :model="emailForm" label-position="top" style="margin: 0 10px 10px 10px">
          <el-form-item label="电子邮件" prop="email">
            <el-input :prefix-icon="Message" v-model="emailForm.email"></el-input>
          </el-form-item>
          <el-form-item label="验证码">
            <el-row style="width: 100%" :gutter="10">
              <el-col :span="18">
                <el-input placeholder="请获取验证码" v-model="emailForm.code"></el-input>
              </el-col>
              <el-col :span="6">
                <el-button type="success" style="width: 80%" @click="askcodea" :disabled="codetime" plain>
                  {{ codetime>0 ? `${codetime} s` : '获取验证码' }}
                </el-button>
              </el-col>
            </el-row>
          </el-form-item>
          <div>
            <el-button :icon="Refresh" type="success" @click="modifyEmail" plain>更新电子邮件</el-button>
          </div>
        </el-form>
      </Card>
    </div>

    <div class="setting-right">
<!-- 当页面滚动时,这个元素会在距离页面顶部20像素的位置保持固定，直到页面继续向下滚动，元素不再可见-->
      <div style="position: sticky;top: 20px">
        <Card>
          <div style="text-align: center;padding: 5px 15px 0 15px">
              <el-avatar :size="70" :src="userstore.avatarUrl()"></el-avatar>
            <div style="margin: 5px 0">
                <el-upload
                    :action=" 'http://localhost:8080/api/image/avatar'"
                    :before-upload="beforeAvatarUpload"
                    :on-success="uploadSuccess"
                    :show-file-list="false"
                    :headers="{ Authorization:  'Bearer '+userstore.getToken()}"
                >
                  <el-button type="primary" size="small" round plain>修改头像</el-button>

                </el-upload>
            </div>
              <div style="font-weight: bold">你好， {{userstore.user.username}}</div>
          </div>
          <el-divider style="margin: 10px 0"></el-divider>
          <div style="font-size: 14px;color: grey;padding: 10px;margin: 10px">
           {{desc? '个人简介：' +desc:'该用户很懒，还没填写任何简介。'}}
          </div>
        </Card>
        <Card style="margin-top: 20px;font-size: 14px;font-weight: bold" >
          <div>账号注册时间 :  {{registerdate}}</div>
          <div style="margin-top: 10px">欢迎加入我们的校园论坛!</div>
        </Card>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
  .setting-left{
    flex: 1;
    margin: 20px;
  }
  .setting-right{
    width: 350px;
    margin: 20px 20px 20px 0;
    word-break: break-all; /* 强制在单词内部换行 */
  }
</style>