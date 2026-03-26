package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.AccountPrivacy;
import com.example.entity.dto.SavePrivacyDTO;

/**
 * 用户隐私
 */
public interface AccountPrivacyService extends IService<AccountPrivacy> {
    /**
     * 保存隐私状态
     * @param id
     * @param savePrivacyDTO
     */
    void savePrivacy(int id, SavePrivacyDTO savePrivacyDTO);

    /**
     * 根据id查询用户隐私状态
     * @param id
     * @return
     */
    AccountPrivacy accountPrivacy(int id);
}
