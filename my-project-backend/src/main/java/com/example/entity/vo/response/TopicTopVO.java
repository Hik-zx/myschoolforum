package com.example.entity.vo.response;

import lombok.Data;

import java.util.Date;

/**
 * 置顶帖子
 */
@Data
public class TopicTopVO {
    int id;
    String title;
    Date time;
}
