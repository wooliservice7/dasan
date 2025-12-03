package com.mysite.dasan.member;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;

/**
 * 회원 관련 비즈니스 로직을 담당하는 서비스
 */
@Service
@AllArgsConstructor
public class MemberService implements UserDetailsService {

	private static final Logger logger = LoggerFactory.getLogger(MemberService.class);

	private final MemberMapper memberMapper;

	/**
	 * UserDetailsService 구현: 로그인 처리
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		logger.info("Loading user: {}", username);
		try {
			MemberVO member = memberMapper.selectMemberById(username);
			if (member == null) {
				logger.warn("User not found: {}", username);
				throw new UsernameNotFoundException("User not found with ID: " + username);
			}
			logger.debug("User loaded successfully: {}", username);
			List<String> roles = Arrays.asList(member.getRole());
			return User.builder()
					.username(member.getId())
					.password(member.getPassword())
					.roles(roles.toArray(new String[0]))
					.build();
		} catch (Exception e) {
			logger.error("Error loading user: {}", username, e);
			throw new UsernameNotFoundException("Error loading user", e);
		}
	}

	/**
	 * 새로운 회원 등록
	 */
	@Transactional
	public void registerNewMember(MemberVO memberVO) {
		logger.info("Registering new member: {}", memberVO.getId());
		try {
			if (memberMapper.selectMemberById(memberVO.getId()) != null) {
				logger.warn("Duplicate ID: {}", memberVO.getId());
				throw new IllegalStateException("이미 존재하는 아이디입니다: " + memberVO.getId());
			}
			memberMapper.insertMember(memberVO);
			logger.info("Member registered: {}", memberVO.getId());
		} catch (Exception e) {
			logger.error("Registration failed: {}", memberVO.getId(), e);
			throw e;
		}
	}
}
