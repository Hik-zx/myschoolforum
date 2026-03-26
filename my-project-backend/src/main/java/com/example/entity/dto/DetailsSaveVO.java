package com.example.entity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 个人设置类
 */
@Data
public class DetailsSaveVO {
    @Size(min = 3, max = 10)
    String username;
    @Min(0)
    @Max(1)
    int gender;
    @Pattern(regexp = "^1[3456789]\\d{9}$")
    String phone;
    @Length(max = 10,min=5)
    String qq;
    @Length(max = 20)
    String wx;
    @Length(max = 200)
    String desc;

}
