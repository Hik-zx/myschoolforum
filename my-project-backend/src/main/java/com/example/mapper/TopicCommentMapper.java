package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.TopicComments;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TopicCommentMapper extends BaseMapper<TopicComments> {
}
