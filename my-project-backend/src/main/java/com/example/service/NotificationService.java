package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.Notification;
import com.example.entity.vo.response.NotificationVO;

import java.util.List;

public interface NotificationService extends IService<Notification> {
    /**
     * 获取消息提醒
     * @param uid
     * @return
     */
    List<NotificationVO> findUserNotification(int uid);

    /**
     * 删除消息提醒
     * @param id
     * @param uid
     */
    void deleteUserNotification(int id, int uid);
    void deleteUserAllNotification(int uid);

    /**
     * 增加消息提醒
     * @param uid
     * @param title
     * @param content
     * @param type
     * @param url
     */
    void addNotification(int uid, String title, String content, String type, String url);
}
