package com.alibou.payload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailVerifyPasswordReq {
    private String email;
    private String emailCode;
    private String newPassword;
}
