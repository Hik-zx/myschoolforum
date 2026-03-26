package com.example.controller;
import com.example.entity.TopicType;
import com.example.entity.dto.AddCommentDTO;
import com.example.entity.dto.InteractDTO;
import com.example.entity.dto.TopicCreatDTO;
import com.example.entity.dto.TopicUpdateDTO;
import com.example.entity.vo.response.*;
import com.example.result.ResultBean;
import com.example.service.TopicService;
import com.example.service.WeatherService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 论坛相关
 */
@RestController
@RequestMapping("/api/forum")
public class ForumController {
    @Autowired
    private WeatherService weatherService;
    @Autowired
    private TopicService topicService;
    /**
     * 获取天气信息
     * @param longitude
     * @param latitude
     * @return
     */
    @GetMapping("/weather")
    public ResultBean<WeatherVO> weather(double longitude,double latitude){
        WeatherVO weatherVO = weatherService.fetchWeather(longitude, latitude);
        return weatherVO==null? ResultBean.failure(400,"获取地理位置信息与天气失败!"):ResultBean.success(weatherVO);
    }

    /**
     * 获取帖子类型
     * @return
     */
    @GetMapping ("/types")
    public ResultBean<List<TopicType>> listTypes(){
      return ResultBean.success(topicService.listTypes());
    }

    /**
     * 创建帖子
     * @param topicCreatDTO
     * @return
     */
    @PostMapping("/creat-topic")
    public ResultBean creatTopic(@Valid @RequestBody TopicCreatDTO topicCreatDTO,@RequestAttribute("id") int uid){
       String message= topicService.creatTopic(topicCreatDTO,uid);
        if(message !=null){
            return ResultBean.failure(400,"帖子上传失败，请联系管理员");
        }
        return ResultBean.success();
    }

    /**
     * 获取帖子
     * @param page
     * @param type
     * @return
     */
    @GetMapping("/list-topic")
    public ResultBean<List<TopicPreviewVO>> listTopic(@RequestParam @Min(0) int page,@RequestParam @Min(0) int type){
      List<TopicPreviewVO> list=topicService.listTopicByPage(page+1,type);
      return ResultBean.success(list);
    }
    /**
     * 通过标题搜索帖子
     * @param page
     * @param title
     * @return
     */
    @GetMapping("/list-topicbytitle")
    public ResultBean<List<TopicPreviewVO>> listTopicByTitle(@RequestParam  String title){
        List<TopicPreviewVO> list=topicService.listTopicByTitle(title);
        return ResultBean.success(list);
    }

    /**
     * 获取置顶帖子
     * @return
     */
    @GetMapping("/top-topic")
    public ResultBean<List<TopicTopVO>> getTopTopic(){
    List<TopicTopVO>  list  =topicService.listtopTopic();
    return ResultBean.success(list);
    }

    /**
     * 获取帖子详细信息
     * @param tid
     * @return
     */
    @GetMapping("/topic")
    public ResultBean<TopicDetailsVO> topic(@RequestParam @Min(0) int tid,@RequestAttribute("id") int uid){
        TopicDetailsVO vo=  topicService.getTopicDetails(tid,uid);
      return ResultBean.success(vo);
    }

    /**
     * 点赞收藏
     * @param tid
     * @param type
     * @param state
     * @param id
     * @return
     */
    @GetMapping("/interact")
    public ResultBean interact(@RequestParam @Min(0) int tid,
                               @RequestParam @Pattern(regexp = "(like|collect)") String type,
                               @RequestParam boolean state,@RequestAttribute("id") int id){

        topicService.interact(new InteractDTO(tid,id,new Date(),type),state);
        return ResultBean.success();
    }

    /**
     * 获取收藏列表
     * @param uid
     * @return
     */
    @GetMapping("/collects")
    public ResultBean<List<TopicPreviewVO>> getCollectTopicList(@RequestAttribute("id") int uid){
     List<TopicPreviewVO>  vo= topicService.getCollectList(uid);
     return ResultBean.success(vo);
    }

    /**
     * 获取点赞列表
     * @param uid
     * @return
     */
    @GetMapping("/likes")
    public ResultBean<List<TopicPreviewVO>> getLikeTopicList(@RequestAttribute("id") int uid){
        List<TopicPreviewVO>  vo= topicService.getLikeList(uid);
        return ResultBean.success(vo);
    }

    /**
     * 获取我的帖子
     * @param uid
     * @return
     */
    @GetMapping("/my-topic")
    public ResultBean<List<TopicPreviewVO>> getMyTopicList(@RequestAttribute("id") int uid){
        List<TopicPreviewVO>  vo= topicService.getMyTopicList(uid);
        return ResultBean.success(vo);
    }

    /**
     * 修改帖子
     * @param dto
     * @param uid
     * @return
     */
    @PostMapping("/update-topic")
    public ResultBean updateTopic(@Valid @RequestBody TopicUpdateDTO dto,@RequestAttribute("id") int uid){
        String s = topicService.updateTopic(dto, uid);
        if(s !=null){
            return ResultBean.failure(400,"帖子修改失败，请联系管理员");
        }
        return ResultBean.success();
    }

    /**
     * 发表评论
     * @param dto
     * @param uid
     * @return
     */
    @PostMapping("/add-comment")
    public ResultBean addComment(@Valid @RequestBody AddCommentDTO dto, @RequestAttribute("id") int uid){
      String message=  topicService.addcomment(dto,uid);
        if(message !=null){
            return ResultBean.failure(400,"发表评论失败，请联系管理员");
        }
        return ResultBean.success();
    }

    /**
     * 获取评论
     * @param tid
     * @param page
     * @return
     */
    @GetMapping("/comments")
    public ResultBean<List<CommentVO>> comments(@RequestParam @Min(0) int tid,@RequestParam @Min(0) int page){
       List <CommentVO> list=topicService.comments(tid,page+1);
        return ResultBean.success(list);
    }

    /**
     * 删除评论
     * @param id 评论id
     * @param uid 用户id
     * @return
     */
    @GetMapping("/delete-comment")
    public ResultBean deleteComment(@RequestParam @Min(0) int id,@RequestAttribute("id") int uid){
        topicService.deleteComment(id,uid);
        return ResultBean.success();
    }

    /**
     * 删除我的帖子
     * @param id
     * @param uid
     * @return
     */
    @GetMapping("/delete-mytopic")
    public ResultBean deleteMyTopic(@RequestParam @Min(0) int id,@RequestAttribute("id") int uid){
        topicService.deleteMyTopic(id,uid);
        return ResultBean.success();
    }
}
