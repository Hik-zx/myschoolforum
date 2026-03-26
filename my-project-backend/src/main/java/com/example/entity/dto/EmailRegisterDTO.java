package com.example.entity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NonNull;
import org.hibernate.validator.constraints.Length;

/**
 * 邮箱注册DTO
 */
@Data
public class EmailRegisterDTO {
    @Email
    @NotEmpty
    String email;
    @Length(max = 6,min = 6)
    String code;
    @Size(min = 3, max = 10)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$")
    String username;
    @Length(min = 6,max = 15)
    String password;
}
