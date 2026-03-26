package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.AccountDetails;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户详细信息
 */
@Mapper
public interface AccountDetailsMapper extends BaseMapper<AccountDetails> {
}
