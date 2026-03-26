import request from "@/utils/request.js";
//获取消息提醒
export const getNotificationService=()=>{
    return request.get('/api/notification/list')
}
//跳转到消息提醒对应的帖子，并且删除消息提醒
export const comfireService=(id)=>{
    return request.get('/api/notification/delete',{
        params: {
            id
        }
    })
}
//删除所有消息提醒
export const deleteAllNotificationService=()=>{
    return request.get('/api/notification/delete-all')
}