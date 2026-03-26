package com.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

/**
 * 修改密码
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModifyPasswordDTO {
    @Length(min = 6,max = 15)
    String password;
    @Length(min = 6,max = 15)
    String new_password;
}
