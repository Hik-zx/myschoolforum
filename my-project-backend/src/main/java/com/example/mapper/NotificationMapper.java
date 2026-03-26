package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息提醒模块
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}
