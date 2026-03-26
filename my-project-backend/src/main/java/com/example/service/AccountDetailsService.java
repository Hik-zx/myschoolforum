package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.AccountDetails;
import com.example.entity.dto.DetailsSaveVO;

public interface AccountDetailsService extends IService<AccountDetails> {
    /**
     * 根据id查询用户详细信息
     * @param id
     * @return
     */
    AccountDetails findAccountDetailsById(int id);

    /**
     * 保存用户详细信息
     * @param id
     * @param detailsSaveVO
     * @return
     */
    boolean saveAccountDetails(int id, DetailsSaveVO detailsSaveVO);
}
