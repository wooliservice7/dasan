package com.mysite.dasan;

import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

// 별도 파일 또는 ExcelService 내부에 정의
public class ExcelResultHandler implements ResultHandler<UnitTaskVO> {
    private final Sheet sheet;
    private int rowNum = 1; // 0번 행은 헤더이므로 1부터 시작

    // 생성자 수정: Workbook 대신 Sheet만 받음
    public ExcelResultHandler(Sheet sheet) {
        this.sheet = sheet;
    }

    @Override
    public void handleResult(ResultContext<? extends UnitTaskVO> context) {
        UnitTaskVO task = context.getResultObject();
        Row row = sheet.createRow(rowNum++);

        // UnitTask DTO의 데이터를 순서대로 셀에 입력
        row.createCell(0).setCellValue(String.valueOf(task.getId() != null ? task.getId() : ""));
        row.createCell(1).setCellValue(task.getLevel1() != null ? task.getLevel1() : "");
        row.createCell(2).setCellValue(task.getLevel2() != null ? task.getLevel2() : "");
        row.createCell(3).setCellValue(task.getLevel3() != null ? task.getLevel3() : "");
        row.createCell(4).setCellValue(task.getLevel4() != null ? task.getLevel4() : "");
        row.createCell(5).setCellValue(task.getLevel5() != null ? task.getLevel5() : "");
        row.createCell(6).setCellValue(task.getLevel6() != null ? task.getLevel6() : "");
        row.createCell(7).setCellValue(task.getDepartment() != null ? task.getDepartment() : "");
    }
}
