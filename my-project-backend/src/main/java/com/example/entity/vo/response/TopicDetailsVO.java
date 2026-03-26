package com.example.entity.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

/**
 * 帖子详细信息
 */
@Data
public class TopicDetailsVO {
    int id;
    String title;
    String content;
    int type;
    Date time;
    User user;
    Interact interact;
    //评论数
    Long comment;
    @Data
    public static class User {
        int id;
        String username;
        String avatar;
        String desc;
        Integer gender;//int不能存储null，Integer可以
        String qq;
        String wx;
        String phone;
        String email;
    }
    @Data
    @AllArgsConstructor
    public static class Interact{
        Boolean like;
        Boolean collect;
    }
}
