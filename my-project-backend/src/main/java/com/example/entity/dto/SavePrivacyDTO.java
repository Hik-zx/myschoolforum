package com.example.entity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

/**
 * 保存隐私DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SavePrivacyDTO {
    @Pattern(regexp = "(phone|email|wx|qq|gender)")
    String type;
    boolean status;

}
