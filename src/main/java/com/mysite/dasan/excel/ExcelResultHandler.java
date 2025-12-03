package com.mysite.dasan.excel;

import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import lombok.extern.slf4j.Slf4j;

/**
 * MyBatis 결과 핸들러로 데이터베이스 쿼리 결과를 직접 Excel 시트에 작성합니다.
 * 대용량 데이터 처리 시 메모리 효율성을 위해 스트리밍 방식으로 동작합니다.
 */
@Slf4j
public class ExcelResultHandler implements ResultHandler<UnitTaskVO> {
    
    private final Sheet sheet;
    private int rowNum = 1; // 0번 행은 헤더이므로 1부터 시작
    
    // Excel 셀 인덱스 상수
    private static final int CELL_ID = 0;
    private static final int CELL_LEVEL1 = 1;
    private static final int CELL_LEVEL2 = 2;
    private static final int CELL_LEVEL3 = 3;
    private static final int CELL_LEVEL4 = 4;
    private static final int CELL_LEVEL5 = 5;
    private static final int CELL_LEVEL6 = 6;
    private static final int CELL_DEPARTMENT = 7;

    /**
     * ExcelResultHandler 생성자
     * @param sheet Excel 데이터를 작성할 시트 객체
     */
    public ExcelResultHandler(Sheet sheet) {
        this.sheet = sheet;
    }

    /**
     * MyBatis 쿼리 결과를 처리하여 Excel 행에 데이터를 작성합니다.
     * 각 UnitTaskVO 객체를 해당 행에 매핑하고 셀 값을 설정합니다.
     * 
     * @param context 현재 처리 중인 결과 컨텍스트
     * @throws RuntimeException 행 생성 또는 셀 작성 중 오류 발생 시
     */
    @Override
    public void handleResult(ResultContext<? extends UnitTaskVO> context) {
        try {
            UnitTaskVO task = context.getResultObject();
            Row row = sheet.createRow(rowNum++);

            // UnitTask DTO의 데이터를 순서대로 셀에 입력
            row.createCell(CELL_ID).setCellValue(String.valueOf(task.getId() != null ? task.getId() : ""));
            row.createCell(CELL_LEVEL1).setCellValue(task.getLevel1() != null ? task.getLevel1() : "");
            row.createCell(CELL_LEVEL2).setCellValue(task.getLevel2() != null ? task.getLevel2() : "");
            row.createCell(CELL_LEVEL3).setCellValue(task.getLevel3() != null ? task.getLevel3() : "");
            row.createCell(CELL_LEVEL4).setCellValue(task.getLevel4() != null ? task.getLevel4() : "");
            row.createCell(CELL_LEVEL5).setCellValue(task.getLevel5() != null ? task.getLevel5() : "");
            row.createCell(CELL_LEVEL6).setCellValue(task.getLevel6() != null ? task.getLevel6() : "");
            row.createCell(CELL_DEPARTMENT).setCellValue(task.getDepartment() != null ? task.getDepartment() : "");
            
            log.debug("Excel row created successfully at rowNum: {}", rowNum - 1);
        } catch (Exception e) {
            log.error("Error occurred while creating Excel row for UnitTaskVO", e);
            throw new RuntimeException("Failed to handle result and create Excel row", e);
        }
    }
}
