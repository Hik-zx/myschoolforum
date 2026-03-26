package com.example.entity.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

/**
 * 重置密码DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordDTO {
    @Email
    String email;
    @Length(min = 6,max=6)
    String code;
    @Length(min = 6,max = 10)
    String password;
}
