package com.mysite.dasan;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MemberService implements UserDetailsService {

	private final MemberMapper memberMapper;
	private final PasswordEncoder passwordEncoder; // SecurityConfig에서 주입받아야 함

	// --- [UserDetailsService 구현: 로그인 처리 시 사용됨] ---
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		MemberVO member = memberMapper.selectMemberById(username);

		if (member == null) {
			throw new UsernameNotFoundException("User not found with ID: " + username);
		}

		// DB의 ROLE 컬럼 값(예: "ADMIN", "USER")을 사용하여 UserDetails 생성
		List<String> roles = Arrays.asList(member.getRole());

		return User.builder().username(member.getId()).password(member.getPassword()) // 이미 암호화된 비밀번호
				.roles(roles.toArray(new String[0])) // 스프링 시큐리티 권한으로 변환 (ROLE_ prefix 자동 추가)
				.build();
	}

	// --- [회원가입 비즈니스 로직] ---

	/**
	 * 새로운 회원을 등록하는 비즈니스 로직
	 * 
	 * @param memberVO 회원가입 폼에서 입력받은 정보 (평문 비밀번호 포함)
	 */
	@Transactional // 트랜잭션 처리
	public void registerNewMember(MemberVO memberVO) {

		// 1. 아이디 중복 확인 (선택 사항이지만 권장)
		if (memberMapper.selectMemberById(memberVO.getId()) != null) {
			throw new IllegalStateException("이미 존재하는 아이디입니다: " + memberVO.getId());
		}

		// 2. 비밀번호 암호화 (필수)
		String encodedPassword = passwordEncoder.encode(memberVO.getPassword());
		memberVO.setPassword(encodedPassword);

		// 3. DB에 저장
		memberMapper.insertMember(memberVO);
	}

	public List<String[]> fetchDataFromDatabase(int rowCount) {

		// 1. MyBatis 매퍼를 통해 DB에서 데이터를 DTO 리스트로 조회
		List<UnitTaskVO> taskList = memberMapper.selectAllUnitTask(rowCount);

		// 2. List<UnitTask>를 List<String[]> 형태로 변환
		List<String[]> excelData = new ArrayList<>();

		for (UnitTaskVO task : taskList) {
			// String 배열을 선언하고 크기를 8로 지정합니다.
			String[] rowArray = new String[8];

			// DTO의 Getter를 사용하여 데이터를 순서대로 String 배열의 각 인덱스에 할당합니다.
			// null 값에 대한 예외 처리를 추가하여 NullPointerException을 방지할 수 있습니다.

			// ID는 Long 타입이므로 String.valueOf()로 변환
			rowArray[0] = String.valueOf(task.getId());

			// 나머지 필드는 String 타입이므로 바로 할당 (null 체크 포함)
			rowArray[1] = (task.getLevel1() != null) ? task.getLevel1() : "";
			rowArray[2] = (task.getLevel2() != null) ? task.getLevel2() : "";
			rowArray[3] = (task.getLevel3() != null) ? task.getLevel3() : "";
			rowArray[4] = (task.getLevel4() != null) ? task.getLevel4() : "";
			rowArray[5] = (task.getLevel5() != null) ? task.getLevel5() : "";
			rowArray[6] = (task.getLevel6() != null) ? task.getLevel6() : "";
			rowArray[7] = (task.getDepartment() != null) ? task.getDepartment() : "";

			// 완성된 행 배열을 최종 리스트에 추가
			excelData.add(rowArray);
		}

		return excelData;
	}

	@Transactional(readOnly = true) // 읽기 전용 트랜잭션으로 성능 최적화
	public void createExcelStream(OutputStream os, int rowCount) throws Exception {

		// 1. SXSSFWorkbook 생성
		try (SXSSFWorkbook workbook = new SXSSFWorkbook(5000)) {
			Sheet sheet = workbook.createSheet("LowMemoryMode");

			// 헤더 생성 (이전 요청의 헤더 너비 설정은 생략)
			Row headerRow = sheet.createRow(0);
			String[] headers = { "ID", "Level 1", "Level 2", "Level 3", "Level 4", "Level 5", "Level 6", "Department" };
			final int FIXED_WIDTH_10_CHARS = 3600;
			for (int i = 0; i < headers.length; i++) {
				headerRow.createCell(i).setCellValue(headers[i]);
				sheet.setColumnWidth(i, FIXED_WIDTH_10_CHARS);
			}

			// 2. ResultHandler 인스턴스 생성
			ExcelResultHandler handler = new ExcelResultHandler(sheet);

			// 3. Mapper 메소드 호출 (트랜잭션 내부에서 스트리밍 시작)
			// MyBatis가 스프링 트랜잭션 내에서 ResultHandler를 감지하고 스트리밍 모드를 활성화합니다.
			memberMapper.streamAllUnitTasks(handler, rowCount);

			// 4. 엑셀 파일 작성 및 스트림 플러시
			workbook.write(os);
			os.flush();
		}
		// finally 블록에서 workbook.dispose()를 호출하여 임시 파일 정리 필요 (SXSSF 특징)
		// try-with-resources 사용 시 자동으로 dispose 됩니다.
	}
}
