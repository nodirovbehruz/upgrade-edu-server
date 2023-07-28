package com.alibou.auth;

import com.alibou.entities.Enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    private String firstname;
    private String lastname;
    private String email;
    private String phoneNumber;
    private String password;
    private Date dateOfBirth;
    private Role role = Role.USER;
}
