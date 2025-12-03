package com.mysite.dasan.member;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

/**
 * MEMBER 테이블 관련 데이터 접근 객체
 */
@Mapper
public interface MemberMapper {
	 void insertMember(MemberVO member);
	 MemberVO selectMemberById(String id);
	 List<MemberVO> selectMembersByRoleOrg();
}
