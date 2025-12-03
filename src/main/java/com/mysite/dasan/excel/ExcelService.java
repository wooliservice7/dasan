package com.mysite.dasan.excel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

/**
 * Excel 파일 생성 및 관련 비즈니스 로직을 담당하는 서비스
 */
@Service
@AllArgsConstructor
public class ExcelService {

	private static final Logger logger = LoggerFactory.getLogger(ExcelService.class);

	private static final int FIXED_WIDTH_10_CHARS = 3600;
	private static final String[] EXCEL_HEADERS = { "id", "LEVEL_1", "LEVEL_2", "LEVEL_3", "LEVEL_4", "LEVEL_5", "LEVEL_6", "DEPARTMENT" };

	private final UnitTaskMapper unitTaskMapper;

	/**
	 * 데이터베이스에서 데이터 조회
	 */
	public List<String[]> fetchDataFromDatabase(int rowCount) {
		logger.info("Fetching {} rows from database", rowCount);
		try {
			List<UnitTaskVO> taskList = unitTaskMapper.selectAllUnitTask(rowCount);
			List<String[]> excelData = new ArrayList<>();

			for (UnitTaskVO task : taskList) {
				String[] rowArray = new String[8];
				rowArray[0] = String.valueOf(task.getId());
				rowArray[1] = (task.getLevel1() != null) ? task.getLevel1() : "";
				rowArray[2] = (task.getLevel2() != null) ? task.getLevel2() : "";
				rowArray[3] = (task.getLevel3() != null) ? task.getLevel3() : "";
				rowArray[4] = (task.getLevel4() != null) ? task.getLevel4() : "";
				rowArray[5] = (task.getLevel5() != null) ? task.getLevel5() : "";
				rowArray[6] = (task.getLevel6() != null) ? task.getLevel6() : "";
				rowArray[7] = (task.getDepartment() != null) ? task.getDepartment() : "";
				excelData.add(rowArray);
			}
			logger.debug("Fetched {} records successfully", excelData.size());
			return excelData;
		} catch (Exception e) {
			logger.error("Error fetching data from database", e);
			throw new RuntimeException("Failed to fetch data from database", e);
		}
	}

	/**
	 * 메모리 기반 Excel 파일 생성 (바이트 배열로 반환)
	 */
	public byte[] generateExcelBytes(List<String[]> data, String sheetName) throws IOException {
		logger.info("Generating Excel file in memory mode");
		try (Workbook workbook = new XSSFWorkbook(); 
		     ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			
			Sheet sheet = workbook.createSheet(sheetName);
			Row headerRow = sheet.createRow(0);
			
			// 헤더 행 생성
			for (int i = 0; i < EXCEL_HEADERS.length; i++) {
				headerRow.createCell(i).setCellValue(EXCEL_HEADERS[i]);
				sheet.setColumnWidth(i, FIXED_WIDTH_10_CHARS);
			}

			// 데이터 행 생성
			int rowNum = 1;
			for (String[] rowData : data) {
				Row row = sheet.createRow(rowNum++);
				for (int i = 0; i < rowData.length; i++) {
					row.createCell(i).setCellValue(rowData[i]);
				}
			}

			workbook.write(out);
			logger.debug("Excel file generated successfully with {} rows", rowNum - 1);
			return out.toByteArray();
		} catch (IOException e) {
			logger.error("Error generating Excel file", e);
			throw e;
		}
	}

	/**
	 * 스트림 기반 Excel 파일 생성 (디스크 모드 - 대용량 처리)
	 */
	public void createExcelStream(OutputStream outputStream, int rowCount) {
		logger.info("Creating Excel stream with row count: {}", rowCount);
		try (SXSSFWorkbook workbook = new SXSSFWorkbook(5000)) {
			
			Sheet sheet = workbook.createSheet("Data");
			Row headerRow = sheet.createRow(0);
			
			// 헤더 행 생성
			for (int i = 0; i < EXCEL_HEADERS.length; i++) {
				headerRow.createCell(i).setCellValue(EXCEL_HEADERS[i]);
				sheet.setColumnWidth(i, FIXED_WIDTH_10_CHARS);
			}

			// 스트림 방식으로 데이터 기록
			ExcelResultHandler handler = new ExcelResultHandler(sheet);
			unitTaskMapper.streamAllUnitTasks(handler, rowCount);
			
			workbook.write(outputStream);
			outputStream.flush();
			logger.info("Excel stream created successfully");
		} catch (Exception e) {
			logger.error("Error creating Excel stream", e);
			throw new RuntimeException("Failed to create Excel stream", e);
		}
	}
}
