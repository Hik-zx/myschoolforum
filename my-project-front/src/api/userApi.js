
//登录接口
import request from "@/utils/request.js";
import qs from 'qs';
import instance from "@/utils/request.js"; // 导入 qs 库，用于将对象转换为 URL 查询字符串格式
export const userLoginService = (username, password) => {
    // 直接以对象形式传递参数，axios 库会自动将其转换为 URL 查询字符串格式
    return request.post('/api/auth/login', {
        username,
        password
    }, {
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded' // 设置正确的 Content-Type,否则不能通过security检验
        }
    });
}
export const logout=()=>{
    return request.get('/api/auth/logout')
}


//获取验证码
export const askcode=(email,type)=>{
    return request.get('/api/auth/ask-code',{
        params:{
            email,
            type
        }
    })
}
//注册账号
export const registerUser = (username, password,email,code) => {
    // 直接以对象形式传递参数，axios 库会自动将其转换为 URL 查询字符串格式
    return request.post('/api/auth/register', {
        username,
        password,
        email,
        code
    });
}
//是否确认重置密码
export const confirmResetuserService=(email,code)=>{
    return request.post('/api/auth/reset-confirm',{
        email,
        code
    });
}

//重置密码
export const resetPasswordService=(email,code,password)=>{
    return request.post('/api/auth/reset-password',{
        email,
        code,
        password
    })
}
//根据id获取用户信息
export const getAccountByIdService =()=>{
    return request.get('/api/user/info')
}
//根据id获取用户详细信息
export const getAccountDetailsByIdService =()=>{
    return request.get('/api/user/details')
}
//保存或更新用户详细信息
export const saveDetailsService=(baseForm)=> {
    return request.post('/api/user/save-details',baseForm)
}
//修改用户邮箱
export const modifyEmailService=(emailForm)=>{
    return request.post('/api/user/modify-email',emailForm)
}
//重置密码
export const modifyPasswordService=(password,new_password)=>{
    return request.post('/api/user/modify-password',{
        password,
        new_password
    })
}
//获取用户隐私信息
export const getAccountPrivacyService =()=>{
    return request.get('/api/user/privacy')
}
//保存隐私状态
export const savePrivacyService=(type,status)=>{
    return request.post('/api/user/save-privacy',{
        type,
        status
    })
}
