package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.Topic;
import com.example.entity.TopicType;
import com.example.entity.dto.AddCommentDTO;
import com.example.entity.dto.InteractDTO;
import com.example.entity.dto.TopicCreatDTO;
import com.example.entity.dto.TopicUpdateDTO;
import com.example.entity.vo.response.CommentVO;
import com.example.entity.vo.response.TopicDetailsVO;
import com.example.entity.vo.response.TopicPreviewVO;
import com.example.entity.vo.response.TopicTopVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface TopicService extends IService<Topic> {
    List<TopicType> listTypes();

    /**
     * 创建帖子
     * @param topicCreatDTO
     * @param uid
     * @return
     */
    String creatTopic(TopicCreatDTO topicCreatDTO, int uid);

    /**
     * 获取帖子
     * @param page
     * @param type
     * @return
     */
    List<TopicPreviewVO> listTopicByPage(int page, int type);

    /**
     * 获取置顶帖子
     * @return
     */
    List<TopicTopVO> listtopTopic();

    /**
     * 获取帖子详细信息
     * @param tid
     * @return
     */
    TopicDetailsVO getTopicDetails(int tid,int uid);

    /**
     * 点赞收藏
     * @param interactDTO
     * @param state
     */
    void interact(InteractDTO interactDTO,boolean state);

    /**
     * 获取收藏列表
     * @param uid
     * @return
     */
    List<TopicPreviewVO> getCollectList(int uid);

    /**
     * 获取点赞视频
     * @param uid
     * @return
     */
    List<TopicPreviewVO> getLikeList(int uid);

    /**
     * 修改帖子
     * @param dto
     * @param uid
     */
    String updateTopic(TopicUpdateDTO dto, int uid);

    /**
     * 发表评论
     * @param dto
     * @param uid
     * @return
     */
    String addcomment(AddCommentDTO dto, int uid);

    /**
     * 获取评论
     * @param tid
     * @param page
     * @return
     */
    List<CommentVO> comments(int tid, int page);

    /**
     * 删除评论
     * @param id
     * @param uid
     */
    void deleteComment(int id, int uid);

    /**
     * 获取我的帖子
     * @param uid
     * @return
     */
    List<TopicPreviewVO> getMyTopicList(int uid);

    /**
     * 删除我的帖子
     * @param id
     * @param uid
     */
    void deleteMyTopic(int id, int uid);

    /**
     * 通过标题搜索帖子
     * @param page
     * @param title
     * @return
     */
    List<TopicPreviewVO> listTopicByTitle( String title);
}
