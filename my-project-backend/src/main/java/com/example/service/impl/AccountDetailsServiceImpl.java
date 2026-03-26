package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.Account;
import com.example.entity.AccountDetails;
import com.example.entity.dto.DetailsSaveVO;
import com.example.mapper.AccountDetailsMapper;
import com.example.mapper.AccountMapper;
import com.example.service.AccountDetailsService;
import com.example.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户详细信息实现类
 */
@Service
public class AccountDetailsServiceImpl extends ServiceImpl<AccountDetailsMapper, AccountDetails> implements AccountDetailsService {
    @Autowired
    AccountService accountService;

    /**
     * 根据id查询用户详细信息
     * @param id
     * @return
     */
    @Override
    public AccountDetails findAccountDetailsById(int id) {
        return this.getById(id);
    }

    /**
     * 保存用户详细信息
     * @param id
     * @param detailsSaveVO
     * @return
     */
    @Override
    @Transactional
    public boolean saveAccountDetails(int id, DetailsSaveVO detailsSaveVO) {
        Account account = accountService.findAccountByNameOrEmail(detailsSaveVO.getUsername());
        if(account==null || account.getId()==id){
            accountService.update()
                    .eq("id",id)
                    .set("username", detailsSaveVO.getUsername())
                    .update();
            //存在则更新，不存在则插入
            this.saveOrUpdate(new AccountDetails(
                    id, detailsSaveVO.getGender(), detailsSaveVO.getPhone(),
                    detailsSaveVO.getQq(), detailsSaveVO.getWx(), detailsSaveVO.getDesc()));
            return true;
        }
        return false;
    }
}
