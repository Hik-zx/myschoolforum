package com.example.entity.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountPrivacyVO {
    boolean phone;
    boolean email;
    boolean wx ;
    boolean qq;
    boolean gender;
}
