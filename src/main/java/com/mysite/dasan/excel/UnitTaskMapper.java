package com.mysite.dasan.excel;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.ResultHandler;

/**
 * UNIT_TASK 테이블 관련 데이터 접근 객체
 */
@Mapper
public interface UnitTaskMapper {
	 List<UnitTaskVO> selectAllUnitTask(int rowCount);
	 void streamAllUnitTasks(ResultHandler<UnitTaskVO> handler, @Param("rowCount") int rowCount);
}
