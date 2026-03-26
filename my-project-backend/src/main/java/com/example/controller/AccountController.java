package com.example.controller;

import com.example.entity.Account;
import com.example.entity.AccountDetails;
import com.example.entity.AccountPrivacy;
import com.example.entity.dto.DetailsSaveVO;
import com.example.entity.dto.ModifyEmailDTO;
import com.example.entity.dto.ModifyPasswordDTO;
import com.example.entity.dto.SavePrivacyDTO;
import com.example.entity.vo.response.AccountDetailsVO;
import com.example.entity.vo.response.AccountPrivacyVO;
import com.example.entity.vo.response.AccountVo;
import com.example.result.ResultBean;
import com.example.service.AccountDetailsService;
import com.example.service.AccountPrivacyService;
import com.example.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * 处理关于用户的请求
 */
@RestController
@RequestMapping("/api/user")
public class AccountController {
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountDetailsService accountDetailsService;
    @Autowired
    private AccountPrivacyService accountPrivacyService;
    /**
     * 获取用户信息
     * @return
     */
    @GetMapping("/info")
    public ResultBean<AccountVo> getAccountInfo(HttpServletRequest request){
        AccountVo accountVo=new AccountVo();
        //JwtAuthorizeFilter类存入的
        int id = (int) request.getAttribute("id");
        Account account = accountService.fingAccountById(id);
        if (account != null) {
            BeanUtils.copyProperties(account,accountVo);
        } else {
            // 处理account为null的情况
            // 比如返回一个错误信息或空对象等
            return ResultBean.failure(400,"账号不存在");
        }
        return ResultBean.success(accountVo);
    }

    /**
     * 获取用户详细信息
     * @param request
     * @return
     */
    @GetMapping("/details")
    public ResultBean<AccountDetailsVO> getAccountDetails(HttpServletRequest request){
        int id = (int) request.getAttribute("id");
        AccountDetails accountDetails = Optional
                .ofNullable(accountDetailsService.findAccountDetailsById(id))
                .orElseGet(AccountDetails::new);
        AccountDetailsVO accountDetailsVO=new AccountDetailsVO();
        BeanUtils.copyProperties(accountDetails,accountDetailsVO);
        return ResultBean.success(accountDetailsVO);
    }

    /**
     * 保存或更新用户详细信息
     * @param id
     * @param detailsSaveVO
     * @return
     */
    @PostMapping("/save-details")
    public ResultBean saveDetails(@RequestAttribute("id") int id, @RequestBody @Valid DetailsSaveVO detailsSaveVO){
        boolean message = accountDetailsService.saveAccountDetails(id, detailsSaveVO);
        return message? ResultBean.success():ResultBean.failure(400,"此用户名已被其他用户使用，请重新输入！");
    }

    /**
     * 修改邮箱
     * @param id
     * @param modifyEmailDTO
     * @return
     */
    @PostMapping("/modify-email")
    public  ResultBean modifyEmail(@RequestAttribute("id") int id,@RequestBody @Valid ModifyEmailDTO modifyEmailDTO){

        String message = accountService.modifEmail(id, modifyEmailDTO);
        return message==null? ResultBean.success():ResultBean.failure(400,message);
    }

    /**
     * 修改密码
     * @param id
     * @param modifyEmailDTO
     * @return
     */
    @PostMapping("/modify-password")
    public  ResultBean modifyPassword(@RequestAttribute("id") int id, @RequestBody @Valid ModifyPasswordDTO modifyEmailDTO){
        String message = accountService.modifyPassword(id,modifyEmailDTO);
        return message==null? ResultBean.success():ResultBean.failure(400,message);
    }

    /**
     * 隐私状态设置
     * @param id
     * @param savePrivacyDTO
     * @return
     */
    @PostMapping("/save-privacy")
    public ResultBean savePrivacy(@RequestAttribute("id") int id, @RequestBody @Valid SavePrivacyDTO savePrivacyDTO){
         accountPrivacyService.savePrivacy(id,savePrivacyDTO);
         return ResultBean.success();

    }

    /**
     * 获取隐私状态
     * @param id
     * @return
     */
    @GetMapping("/privacy")
    public ResultBean<AccountPrivacyVO> privacy(@RequestAttribute("id") int id){
        AccountPrivacy privacy = accountPrivacyService.accountPrivacy(id);
        AccountPrivacyVO accountPrivacyVO=new AccountPrivacyVO();
        BeanUtils.copyProperties(privacy,accountPrivacyVO);
        return ResultBean.success(accountPrivacyVO);

    }
}
