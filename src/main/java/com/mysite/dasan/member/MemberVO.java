package com.mysite.dasan.member;

import java.sql.Date;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 회원 정보 데이터 전송 객체
 */
@Getter
@Setter
@ToString
public class MemberVO {
	@NotBlank(message = "ID cannot be empty")
	@Size(min = 3, max = 50, message = "ID must be between 3 and 50 characters")
	private String id;

	@NotBlank(message = "Password cannot be empty")
	@Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
	private String password;

	@NotBlank(message = "Email cannot be empty")
	@Email(message = "Email format is invalid")
	private String email;

	private String role;
	private Date createDate;
}
