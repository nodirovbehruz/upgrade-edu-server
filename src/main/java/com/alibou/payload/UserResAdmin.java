package com.alibou.payload;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
public class UserResAdmin {
    private Integer id;
    private String firstname;
    private String lastname;
    private String email;
    private String phoneNumber;
    private Boolean coursePaid;
    private Date registrationDate;
    private Date dateOfBirth;

}
