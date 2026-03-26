package com.example.entity.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

/**
 * 修改邮箱
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModifyEmailDTO {
    @Email
    String email;
    @Length(max = 6, min = 6)
    String code;
}
