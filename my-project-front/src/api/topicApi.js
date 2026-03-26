import request from "@/utils/request.js";
//获取帖子类型
export const getTopictypeService=()=>{
    return request.get('/api/forum/types')
}
//发送帖子
export const submitTopic = (type, title,content) => {
    // 直接以对象形式传递参数，axios 库会自动将其转换为 URL 查询字符串格式
    return request.post('/api/forum/creat-topic', {
        type,
        title,
        content
    });
}
//更新帖子
export const updateTopicService = (id,type, title,content) => {
    // 直接以对象形式传递参数，axios 库会自动将其转换为 URL 查询字符串格式
    return request.post('/api/forum/update-topic', {
        id,
        type,
        title,
        content
    });
}
//获取帖子
export const getTopicService=(page,type)=>{
    return request.get('/api/forum/list-topic',{
        params: {
            page,
            type
        },
    })
}
//通过标题搜索帖子
export const getTopicByTitleService=(title)=>{
    return request.get('/api/forum/list-topicbytitle',{
        params: {
            title
        },headers: {
            'Content-Type': 'application/x-www-form-urlencoded' // 设置正确的 Content-Type,否则不能通过security检验
        }
    })
}
//获取置顶帖子
export const toptopicService=()=>{
    return request.get('/api/forum/top-topic')
}
//获取帖子详细信息
export const topicDetailsService=(tid)=>{
    return request.get('/api/forum/topic',{
        params: {
            tid
        }
    })
}
//点按，收藏帖子
export const interactService=(tid,type,state)=>{
    return request.get('/api/forum/interact',{
        params: {
            tid,
            type,
            state
        }
    })
}
//获取收藏列表
export const getCollectService=()=>{
    return request.get('/api/forum/collects')
}
//获取点赞列表
export const getLikeService=()=>{
    return request.get('/api/forum/likes')
}
//获取我的帖子
export const getMyTopicService=()=>{
    return request.get('/api/forum/my-topic')
}
//删除我的帖子
export const deleteMyTopicService=(id)=>{
    return request.get('/api/forum/delete-mytopic',{
        params: {
            id
        }
    })
}