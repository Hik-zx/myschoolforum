package com.example.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("db_topic_type")
public class TopicType {
    int id;
    String name;
    @TableField("`desc`")
    String desc;
    String color;
}
