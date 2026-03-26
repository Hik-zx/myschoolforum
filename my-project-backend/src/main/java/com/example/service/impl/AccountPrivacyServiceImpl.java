package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.AccountPrivacy;
import com.example.entity.dto.SavePrivacyDTO;
import com.example.mapper.AccountMapper;
import com.example.mapper.AccountPrivacyMapper;
import com.example.service.AccountPrivacyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 用户隐私
 */
@Service
public class AccountPrivacyServiceImpl extends ServiceImpl<AccountPrivacyMapper, AccountPrivacy> implements AccountPrivacyService {
    /**
     * 保存隐私状态
     * @param id
     * @param savePrivacyDTO
     */
    @Override
    @Transactional
    public void savePrivacy(int id, SavePrivacyDTO savePrivacyDTO) {
        //确保始终有一个AccountPrivacy对象，即使数据库中没有找到匹配的记录，也会返回一个新的实例，而不是null。默认隐私全部true
        AccountPrivacy privacy= Optional.ofNullable(baseMapper.selectById(id))
                                        .orElse(new AccountPrivacy(id));
        //获取status
        boolean status = savePrivacyDTO.isStatus();
        //根据SavePrivacyDTO类型和status更新AccountPrivacy字段的布尔值
        switch (savePrivacyDTO.getType()){
            case "phone" -> privacy.setPhone(status);
            case "email" -> privacy.setEmail(status);
            case "wx" -> privacy.setWx(status);
            case "qq" -> privacy.setQq(status);
            case "gender" -> privacy.setGender(status);
        }
        this.saveOrUpdate(privacy);
    }

    /**
     * 根据id查询用户隐私转态
     * @param id
     * @return
     */
    public AccountPrivacy accountPrivacy(int id){
        //确保始终有一个AccountPrivacy对象，即使数据库中没有找到匹配的记录，也会返回一个新的实例，而不是null。
        return Optional.ofNullable(this.getById(id))
                .orElse(new AccountPrivacy(id));
    }
}
