package com.mysite.dasan;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class MemberController {

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	MemberService memberService;

	@Autowired
	MemberMapper memberMapper;
	
	
	
	@GetMapping("/login")
	public String login() {
		return "login"; // templates/login.html
	}

	@GetMapping("/")
	public String dashboard(Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		model.addAttribute("username", auth.getName());
		return "dashboard"; // templates/dashboard.html
	}

	// GET 요청: 회원가입 화면을 보여줍니다.
	// URL: http://localhost:8080/register
	@GetMapping("/register")
	public String showRegisterForm() {
		// src/main/resources/templates/register.html 파일을 반환합니다.
		return "register";
	}

	// POST 요청: 폼 데이터를 받아서 처리합니다.
	@PostMapping("/register")
	public String processRegistration(MemberVO memberVO) {

		// 1. 여기서 비밀번호 암호화 로직 수행 (필수)
		memberVO.setPassword(passwordEncoder.encode(memberVO.getPassword()));

		// 2. 서비스 계층을 호출하여 DB에 저장
		memberService.registerNewMember(memberVO);

		// 회원가입 성공 후 메인 페이지로 리다이렉트
		return "redirect:/";
	}

	@GetMapping("/api/members/org")
	@ResponseBody
	public List<MemberVO> getOrgMembers() {
		return memberMapper.selectMembersByRoleOrg();
	}

	@GetMapping("/api/excel/download/memory")
	public ResponseEntity<byte[]> downloadMemoryExcel(HttpServletResponse response, 
			@RequestParam(defaultValue = "50000") int rowCount) throws IOException {
		// 실제 데이터 조회 로직 (예시)
		List<String[]> data = memberService.fetchDataFromDatabase(rowCount);

		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			Sheet sheet = workbook.createSheet("HighMemoryMode");

			// 헤더 생성
			Row headerRow = sheet.createRow(0);

			String[] headers = { "id", "LEVEL_1", "LEVEL_2", "LEVEL_3", "LEVEL_4", "LEVEL_5", "LEVEL_6", "DEPARTMENT" };
			final int FIXED_WIDTH_10_CHARS = 3600;
			for (int i = 0; i < headers.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(headers[i]);

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
			byte[] bytes = out.toByteArray();

			String fileName = URLEncoder.encode("HighMemoryMode", StandardCharsets.UTF_8) + ".xlsx";

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
					.contentType(MediaType.APPLICATION_OCTET_STREAM).body(bytes);

		}
	}

	@GetMapping("/api/excel/download/harddisk")
	public void downloadHardDiskExcel(HttpServletResponse response, 
			@RequestParam(defaultValue = "50000") int rowCount) throws IOException {

		String fileName = "LowMemoryMode.xlsx";

		try {
			// 파일 이름 URL 인코딩
			String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

			// 1. HTTP 응답 헤더 설정
			// MIME 타입 설정: 엑셀 파일임을 알립니다.
			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			// Content-Disposition 설정: 브라우저에게 파일을 다운로드하게 합니다.
			response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");

			// 2. Service 계층의 스트림 생성 메소드 호출
			// HttpServletResponse에서 OutputStream을 얻어와 서비스로 전달합니다.
			// 서비스 메소드 내에서 SXSSFWorkbook이 이 스트림에 직접 데이터를 씁니다.
			memberService.createExcelStream(response.getOutputStream(), rowCount);

			// 응답 스트림 종료 (try-with-resources 사용 시 자동 처리될 수도 있습니다)
			// response.getOutputStream().flush();

		} catch (Exception e) {
			// 예외 발생 시 로그 기록 및 클라이언트에게 오류 메시지 전달
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500 에러 상태 설정
			response.getWriter().write("엑셀 다운로드 중 오류가 발생했습니다: " + e.getMessage());
		}
	}	
}
