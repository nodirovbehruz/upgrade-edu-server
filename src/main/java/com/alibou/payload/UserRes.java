package com.alibou.payload;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class UserRes {
    private String firstname;
    private String lastname;
    private String phoneNumber;
    private String email;
    private String  registrationDate;
    private String  dateOfBirth;
    private UUID attachmentId;
}
