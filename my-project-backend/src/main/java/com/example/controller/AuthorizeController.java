package com.example.controller;

import com.example.entity.dto.ConfirmResetDTO;
import com.example.entity.dto.EmailRegisterDTO;
import com.example.entity.dto.ResetPasswordDTO;
import com.example.result.ResultBean;
import com.example.service.AccountService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 处理验证请求
 */
@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthorizeController {
    @Autowired
    private AccountService accountService;
    /**
     * 发送验证码
     * @param email
     * @param type
     * @return
     */
    @GetMapping("/ask-code")
    public ResultBean askVerifyCode(@RequestParam @Email @NotEmpty String email,
                                    @RequestParam @Pattern(regexp = "(register|reset|modify)") String type,
                                    HttpServletRequest request){
        String message = accountService.registerEmailCode(type, email, request.getRemoteAddr());
        if(message==null)
            return ResultBean.success();
        return ResultBean.failure(400,message);
    }

    /**
     * 用户注册
     * @param emailRegisterDTO
     * @return
     */
    @PostMapping("/register")
    public ResultBean register(@RequestBody @Valid EmailRegisterDTO emailRegisterDTO){
        String message = accountService.registerEmailAccount(emailRegisterDTO);
        if(message==null)
            return ResultBean.success();
        return ResultBean.failure(400,message);

    }

    /**
     * 是否重置密码
     * @param confirmResetDTO
     * @return
     */
    @PostMapping("/reset-confirm")
    public ResultBean confirmReset(@RequestBody @Valid ConfirmResetDTO confirmResetDTO){
        String message = accountService.confirmReset(confirmResetDTO);
        if(message==null)
            return ResultBean.success();
        return ResultBean.failure(400,message);
    }

    /**
     * 重置密码
     * @param resetPasswordDTO
     * @return
     */
    @PostMapping("/reset-password")
    public ResultBean confirmReset(@RequestBody @Valid ResetPasswordDTO resetPasswordDTO){
        String message = accountService.resetPassword(resetPasswordDTO);
        if(message==null)
            return ResultBean.success();
        return ResultBean.failure(400,message);
    }
}
