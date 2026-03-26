package com.example.entity.vo.response;

import lombok.Data;

import java.util.Date;

/**
 * 返给前端的用户VO
 */
@Data
public class AccountVo {
    int id;
    String username;
    String email;
    String role;
    String avatar;
    Date registerTime;
}
