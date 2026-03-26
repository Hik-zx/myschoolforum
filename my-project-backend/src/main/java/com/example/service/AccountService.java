package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.Account;
import com.example.entity.dto.*;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AccountService extends IService<Account>, UserDetailsService {
    /**
     * 通过邮箱或者账号查找用户
     * @param text
     * @return
     */
    Account findAccountByNameOrEmail(String text);

    /**
     * 通过id查找用户信息
     * @param id
     * @return
     */
    Account fingAccountById(int id);
    /**
     * 邮箱验证码
     * @param type
     * @param email
     * @param ip
     * @return
     */
    String registerEmailCode(String type,String email,String ip);

    /**
     * 邮箱注册
     * @param emailRegisterDTO
     * @return
     */
    String registerEmailAccount(EmailRegisterDTO emailRegisterDTO);

    /**
     * 是否确认重置密码
     * @param confirmResetDTO
     * @return
     */
    String confirmReset(ConfirmResetDTO confirmResetDTO);

    /**
     * 重置密码
     * @param resetPasswordDTO
     * @return
     */
    String resetPassword(ResetPasswordDTO resetPasswordDTO);

    /**
     * 修改邮箱
     * @param id
     * @param modifyEmailDTO
     * @return
     */

    String modifEmail(int id, ModifyEmailDTO modifyEmailDTO);

    /**
     * 修改密码
     * @param id
     * @param modifyEmailDTO
     * @return
     */
    String modifyPassword(int id, ModifyPasswordDTO modifyEmailDTO);
}
