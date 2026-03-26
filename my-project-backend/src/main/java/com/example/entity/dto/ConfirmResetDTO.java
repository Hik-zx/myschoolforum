package com.example.entity.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

/**
 * 是否确认重置密码DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmResetDTO {
    @Email
    String email;
    @Length(max = 6,min = 6)
    String code;
}
