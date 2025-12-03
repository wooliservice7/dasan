package com.mysite.dasan.member;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.validation.Valid;

/**
 * 회원 인증 및 관리 관련 요청을 처리하는 컨트롤러
 */
@Controller
@RequestMapping
public class MemberController {

	private static final Logger logger = LoggerFactory.getLogger(MemberController.class);

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private MemberService memberService;

	@Autowired
	private MemberMapper memberMapper;
	
	/**
	 * 로그인 화면 표시
	 */
	@GetMapping("/login")
	public String login() {
		logger.info("Accessing login page");
		return "login";
	}

	/**
	 * 대시보드 화면 표시
	 */
	@GetMapping("/")
	public String dashboard(Model model) {		
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			String username = auth.getName();
			model.addAttribute("username", username);
			logger.info("Dashboard accessed by user: {}", username);
			return "dashboard";
		} catch (Exception e) {
			logger.error("Error accessing dashboard", e);
			return "redirect:/login";
		}
	}

	/**
	 * 회원가입 화면 표시
	 */
	@GetMapping("/register")
	public String showRegisterForm() {
		logger.info("Accessing registration page");
		return "register";
	}

	/**
	 * 회원가입 처리
	 */
	@PostMapping("/register")
	public String processRegistration(@Valid MemberVO memberVO, BindingResult bindingResult) {
		logger.info("Registration attempt for user: {}", memberVO.getId());		
		if (bindingResult.hasErrors()) {
			logger.warn("Validation failed for registration: {}", bindingResult.getFieldError());
			return "register";
		}
		
		// 비밀번호 암호화
		memberVO.setPassword(passwordEncoder.encode(memberVO.getPassword()));			
		// DB에 저장
		memberService.registerNewMember(memberVO);
		logger.info("User registered successfully: {}", memberVO.getId());			
		return "redirect:/login";
	}

	/**
	 * 역할별 회원 조회 (API)
	 */
	@GetMapping("/api/members/org")
	@ResponseBody
	public List<MemberVO> getOrgMembers() {
		logger.info("Fetching ORG role members");
		return memberMapper.selectMembersByRoleOrg();
	}
}
