package com.mysite.dasan;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.ResultHandler;

@Mapper
public interface MemberMapper {
	 void insertMember(MemberVO member);
	 MemberVO selectMemberById(String id);
	 List<MemberVO> selectMembersByRoleOrg();
	 List<UnitTaskVO> selectAllUnitTask(int rowCount);
	 void streamAllUnitTasks(ResultHandler<UnitTaskVO> handler, @Param("rowCount") int rowCount);
}
