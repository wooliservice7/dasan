package com.mysite.dasan.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;

import com.mysite.dasan.member.MemberService;
import lombok.AllArgsConstructor;

/**
 * Spring Security 설정
 */
@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

	private final MemberService memberService;

	/**
	 * 사용자 전환 필터
	 */
	@Bean
	public SwitchUserFilter switchUserFilter() {
		SwitchUserFilter filter = new SwitchUserFilter();
		filter.setUserDetailsService(memberService);
		filter.setSwitchUserUrl("/switch-user");
		filter.setExitUserUrl("/switch-user-exit");
		filter.setTargetUrl("/");
		return filter;
	}

	/**
	 * Security Filter Chain 설정
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/login", "/register", "/webjars/**", "/css/**", "/js/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/members/org").hasAnyRole("ADMIN", "ORG")
				.requestMatchers(HttpMethod.GET, "/api/excel/**").hasAnyRole("ADMIN", "USER")
				.anyRequest().authenticated()
			)
			.formLogin(form -> form
				.loginPage("/login")
				.defaultSuccessUrl("/", true)
				.permitAll()
			)
			.logout(logout -> logout
				.logoutSuccessUrl("/login")
				.permitAll()
			)
			.csrf(csrf -> csrf.disable());

		return http.build();
	}
}
