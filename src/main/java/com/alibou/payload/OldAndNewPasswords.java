package com.alibou.payload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OldAndNewPasswords {
    private String newPassword;
    private String oldPassword;
}
