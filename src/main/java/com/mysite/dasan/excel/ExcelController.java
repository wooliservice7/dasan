package com.mysite.dasan.excel;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Excel 파일 다운로드 및 조회 관련 API를 처리하는 컨트롤러
 */
@Controller
@RequestMapping("/api/excel")
public class ExcelController {

	private static final Logger logger = LoggerFactory.getLogger(ExcelController.class);
	private static final String EXCEL_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

	@Autowired
	private ExcelService excelService;

	/**
	 * 메모리 기반 Excel 다운로드
	 * 중소 규모 데이터셋(50,000건 이하)에 사용
	 */
	@GetMapping("/download/memory")
	public ResponseEntity<byte[]> downloadMemoryExcel(@RequestParam(defaultValue = "50000") int rowCount) throws IOException {
		logger.info("Downloading Excel in memory mode with row count: {}", rowCount);
		
		try {
			List<String[]> data = excelService.fetchDataFromDatabase(rowCount);
			byte[] excelBytes = excelService.generateExcelBytes(data, "HighMemoryMode");
			
			String fileName = URLEncoder.encode("HighMemoryMode", StandardCharsets.UTF_8) + ".xlsx";
			logger.info("Excel file prepared for download: {} with {} rows", fileName, data.size());
			
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
					.contentType(MediaType.parseMediaType(EXCEL_CONTENT_TYPE))
					.body(excelBytes);
		} catch (IOException e) {
			logger.error("Error creating Excel file", e);
			throw e;
		}
	}

	/**
	 * 디스크 기반 Excel 다운로드 (스트림 방식)
	 * 대규모 데이터셋(50,000건 이상)에 사용 - 메모리 효율적
	 */
	@GetMapping("/download/harddisk")
	public void downloadHardDiskExcel(HttpServletResponse response, 
			@RequestParam(defaultValue = "50000") int rowCount) throws Exception {
		
		logger.info("Downloading Excel in disk mode with row count: {}", rowCount);
		String fileName = "LowMemoryMode.xlsx";

		try {
			String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
			response.setContentType(EXCEL_CONTENT_TYPE);
			response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");

			excelService.createExcelStream(response.getOutputStream(), rowCount);
			logger.info("Excel file downloaded successfully in disk mode");
		} catch (IOException e) {
			logger.error("Error during Excel download", e);
			try {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().write("엑셀 다운로드 중 오류가 발생했습니다: " + e.getMessage());
			} catch (IOException ioException) {
				logger.error("Error writing error response", ioException);
			}
		}
	}
}
