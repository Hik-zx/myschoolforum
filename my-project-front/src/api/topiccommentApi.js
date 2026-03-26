import request from "@/utils/request.js";
//发表评论
export const addCommentService = (tid,quote,content) => {
    // 直接以对象形式传递参数，axios 库会自动将其转换为 URL 查询字符串格式
    return request.post('/api/forum/add-comment', {
        tid,
        quote,
        content
    });
}
//获取评论
export const getCommentsService=(page,tid)=>{
    return request.get('/api/forum/comments',{
        params: {
            page,
            tid
        }
    })
}
//删除评论
export const deleteCommentService=(id)=>{
    return request.get('/api/forum/delete-comment',{
        params: {
            id
        }
    })
}