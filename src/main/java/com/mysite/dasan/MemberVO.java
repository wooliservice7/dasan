package com.mysite.dasan;

import java.sql.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MemberVO {
    private String id;          // ID
    private String password;    // PASSWORD
    private String email;       // EMAIL
    private String role;        // ROLE
    private Date createDate;    // CREATE_DATE
}	
