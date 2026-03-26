package com.example.controller;

import com.example.entity.vo.response.NotificationVO;
import com.example.result.ResultBean;
import com.example.service.NotificationService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息提醒类
 */
@RestController
@RequestMapping("/api/notification")
public class NotificationController {
    @Resource
    NotificationService service;

    @GetMapping("/list")
    public ResultBean<List<NotificationVO>> listNotification(@RequestAttribute("id") int id) {
        return ResultBean.success(service.findUserNotification(id));
    }

    @GetMapping("/delete")
    public ResultBean<List<NotificationVO>> deleteNotification(@RequestParam @Min(0) int id,
                                                             @RequestAttribute("id") int uid) {
        service.deleteUserNotification(id, uid);
        return ResultBean.success();
    }

    @GetMapping("/delete-all")
    public ResultBean<List<NotificationVO>> deleteAllNotification(@RequestAttribute("id") int uid) {
        service.deleteUserAllNotification(uid);
        return ResultBean.success();
    }
}