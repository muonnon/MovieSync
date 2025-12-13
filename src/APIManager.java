//프로젝트 이름 : MovieSync
//개발자 : 권미리
//개발 기간: 2025.12.01 ~ 2025.12.13
//영화진흥위원회(KOFIC) API 호출 및 XML 파싱 클래스

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import javax.xml.parsers.*;
import org.w3c.dom.*;

/* 영화진흥위원회 API를 호출, 박스오피스 데이터를 가져오는 클래스 스레드를 상속받아
 * 백그라운드에서 주기적으로 데이터를 갱신*/
public class APIManager extends Thread {
	// API 연결 정보
	private static final String API_KEY = "ebd2ef0243c007f60e7f197614e7ce88"; // KOFIC API 키
	private static final String API_URL = "http://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.xml";

	private DatabaseManager dbManager; // DB 관리자
	private boolean running = true; // 스레드 실행 상태

	//생성자
	public APIManager(DatabaseManager dbManager) {
		this.dbManager = dbManager;
	}

	// 스레드 실행 메소드
	@Override
	public void run() {
		System.out.println("API> APIManager 시작");

		// 서버 시작 시 즉시 1회 호출
		fetchAndSaveMovies();

		// 주 1회 갱신 타이머 설정 (매주 월요일 00:00에 갱신) - ai 도움 받았습니다.
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (running) {
					fetchAndSaveMovies();
				}
			}
		}, getNextMondayMidnight(), 7 * 24 * 60 * 60 * 1000L); // 7일마다
	}

	// 다음 월요일 자정까지의 시간 계산 - ai 도움받았습니다.
	private long getNextMondayMidnight() {
		Calendar cal = Calendar.getInstance();

		// 다음 월요일로 이동
		int daysUntilMonday = (Calendar.MONDAY - cal.get(Calendar.DAY_OF_WEEK) + 7) % 7;
		if (daysUntilMonday == 0) {
			daysUntilMonday = 7; // 오늘이 월요일이면 다음 주 월요일
		}
		cal.add(Calendar.DAY_OF_MONTH, daysUntilMonday);

		// 자정으로 설정
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis() - System.currentTimeMillis();
	}

	// API 호출 및 DB 저장 메소드
	public void fetchAndSaveMovies() {
		System.out.println("API> 박스오피스 데이터 가져오는 중...");

		try {
			// 1. 어제 날짜 구하기 - 박스오피스는 하루 전 기준
			String targetDate = getYesterdayDate();

			// 2. API URL 생성
			String urlString = API_URL + "?key=" + API_KEY + "&targetDt=" + targetDate;
			System.out.println("API> 요청 URL: " + urlString);

			// 3. HTTP 연결 - ai 도움 받았습니다.
			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5000); // 5초 타임아웃
			conn.setReadTimeout(5000);

			int responseCode = conn.getResponseCode();
			if (responseCode != 200) {
				System.err.println("API> HTTP 오류: " + responseCode);
				return;
			}

			// 4. XML 파싱 준비 - 검색 도움받았습니다.
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); //XML 파서를 생성하는 팩토리 클래스의 인스턴스 생성
			DocumentBuilder builder = factory.newDocumentBuilder(); //실제 MXL을 파싱할 수 있는 빌더 객체 생성
			Document doc = builder.parse(conn.getInputStream()); //HTTP 연결의 InputStream을 읽어서 XML 문서 객체로 파싱

			// 5. XML 정규화 (선택사항, 파싱 안정성 향상) - ai 사용했습니다.
			doc.getDocumentElement().normalize(); //XML 문서의 텍스트 노드를 정규화, 파싱 결과의 일관성을 보장해 데이터 추출을 안정적으로

			// 6. <dailyBoxOffice> 태그들 가져오기
			NodeList movieList = doc.getElementsByTagName("dailyBoxOffice"); //문서 전체에서 지정한 태그 이름을 가진 모든 요소를 NodeList로 반환
			System.out.println("API> 총 " + movieList.getLength() + "개 영화 데이터 수신");
			System.out.println("API> API에서 받은 영화 목록:");

			// 7. 각 영화 데이터 파싱 및 DB 저장
			int maxMovies = Math.min(10, movieList.getLength()); //10개 이상 받아도 최대 10개까지만 처리
			for (int i = 0; i < maxMovies; i++) { //NodeList의 각 영화 요소를 순화하며 데이터 추출
				Element movie = (Element) movieList.item(i); //Node를 Element로 형변환

				// 각 필드 추출
				String movieCd = getTagValue("movieCd", movie); //영화고유코드
				String movieNm = getTagValue("movieNm", movie); //영화제목
				int rank = Integer.parseInt(getTagValue("rank", movie)); //박스오피스 순위
				String openDt = getTagValue("openDt", movie); //개봉일
				long audiAcc = Long.parseLong(getTagValue("audiAcc", movie)); //누적관객수(큰 숫자 처리)
				long salesAcc = Long.parseLong(getTagValue("salesAcc", movie)); //누적매출액

				System.out.println("  " + rank + "위. " + movieNm + " (" + movieCd + ")"); //콘솔에 영화 정보 출력
				// DB에 저장 - DatabaseManager의 saveMovie() 호출, UPSERT로 저장
				dbManager.saveMovie(movieCd, movieNm, rank, openDt, audiAcc, salesAcc);
			}

			System.out.println("API> 박스오피스 데이터 저장 완료 (" + maxMovies + "개)");
			conn.disconnect();

		} catch (MalformedURLException e) { //일부 오류 유형 구글 검색
			System.err.println("API> 잘못된 URL: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("API> 네트워크 오류: " + e.getMessage());
		} catch (ParserConfigurationException e) {
			System.err.println("API> XML 파서 설정 오류: " + e.getMessage());
		} catch (org.xml.sax.SAXException e) {
			System.err.println("API> XML 파싱 오류: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("API> 예상치 못한 오류: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// XML에서 특정 태그의 값을 추출하는 헬퍼 메소드 - ai + 검색 도움 받았습니다.
	private String getTagValue(String tag, Element element) {
		try {
			NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes(); //주어진 Element 내에서 지정한 태그 이름을 가진 모든 자식 요소 찾기
			Node node = nodeList.item(0); //자식노드 중에서 첫번째 노드 가져오기
			if (node != null) { //null이 아닌지 확인
				return node.getNodeValue();
			}
		} catch (Exception e) {
			System.err.println("API> 태그 추출 실패: " + tag);
		}
		return ""; // 값이 없으면 빈 문자열 반환
	}

	// 어제 날짜를 yyyyMMdd 형식으로 반환
	private String getYesterdayDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1); // 하루 전
		return sdf.format(cal.getTime());
	}

	// 스레드 종료 메소드
	public void stopAPI() {
		running = false;
		System.out.println("API> APIManager 종료");
	}

	// 테스트용 main 메소드 (나중에 삭제)
	public static void main(String[] args) {
		System.out.println("=== APIManager 테스트 ===");
		DatabaseManager dbManager = new DatabaseManager();
		APIManager apiManager = new APIManager(dbManager);

		// 즉시 실행 테스트
		apiManager.fetchAndSaveMovies();

		// DB 조회 테스트
		try {
			java.sql.ResultSet rs = dbManager.getTop10Movies();
			System.out.println("\n=== DB에 저장된 영화 목록 ===");
			while (rs.next()) {
				System.out.println(
						rs.getInt("rank") + "위: " + rs.getString("movie_nm") + " (" + rs.getString("movie_cd") + ")");
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		dbManager.close();
	}
}