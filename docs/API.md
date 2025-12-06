# 🎬 영화진흥위원회 API 연동

## 개요

MovieSync는 **영화진흥위원회(KOFIC) 오픈 API**를 사용하여 일일 박스오피스 정보를 가져옵니다.

### API 정보

| 항목 | 값 |
|------|-----|
| **제공처** | 영화진흥위원회 (KOFIC) |
| **API 종류** | 일별 박스오피스 조회 |
| **응답 형식** | XML |
| **인증 방식** | API Key (쿼리 파라미터) |
| **API 문서** | [KOFIC 오픈 API](https://www.kobis.or.kr/kobisopenapi/homepg/main/main.do) |

---

## API 엔드포인트

### 기본 URL

```
http://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.xml
```

### 요청 파라미터

| 파라미터 | 필수 | 설명 |
|----------|------|------|
| `key` | ✅ | API 발급 키 |
| `targetDt` | ✅ | 조회 날짜 (YYYYMMDD 형식) |

### 요청 예시

```
http://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.xml?key=YOUR_API_KEY&targetDt=20251205
```

---

## API 응답 구조

### XML 응답 예시

```xml
<?xml version="1.0" encoding="UTF-8"?>
<boxOfficeResult>
    <boxofficeType>일별 박스오피스</boxofficeType>
    <showRange>20251205~20251205</showRange>
    <dailyBoxOfficeList>
        <dailyBoxOffice>
            <rnum>1</rnum>
            <rank>1</rank>
            <rankInten>0</rankInten>
            <rankOldAndNew>OLD</rankOldAndNew>
            <movieCd>20234567</movieCd>
            <movieNm>위키드</movieNm>
            <openDt>2024-11-20</openDt>
            <salesAmt>1234567890</salesAmt>
            <salesShare>35.5</salesShare>
            <salesInten>123456</salesInten>
            <salesChange>10.5</salesChange>
            <salesAcc>50000000000</salesAcc>
            <audiCnt>123456</audiCnt>
            <audiInten>12345</audiInten>
            <audiChange>8.5</audiChange>
            <audiAcc>5000000</audiAcc>
            <scrnCnt>1234</scrnCnt>
            <showCnt>5678</showCnt>
        </dailyBoxOffice>
        <!-- ... 최대 10개 -->
    </dailyBoxOfficeList>
</boxOfficeResult>
```

### 주요 필드 설명

| 필드 | 타입 | 설명 |
|------|------|------|
| `rank` | int | 박스오피스 순위 |
| `movieCd` | String | 영화 코드 (고유 식별자) |
| `movieNm` | String | 영화 이름 (국문) |
| `openDt` | String | 개봉일 (YYYY-MM-DD) |
| `salesAcc` | long | 누적 매출액 (원) |
| `audiAcc` | long | 누적 관객 수 |
| `scrnCnt` | int | 상영 스크린 수 |
| `showCnt` | int | 상영 횟수 |

---

## APIManager 클래스

### 클래스 구조

```java
public class APIManager extends Thread {
    // 상수
    private static final String API_KEY = "ebd2ef0243c007f60e7f197614e7ce88";
    private static final String API_URL = "http://www.kobis.or.kr/...";
    
    // 필드
    private DatabaseManager dbManager;
    private boolean running = true;
    
    // 생성자
    public APIManager(DatabaseManager dbManager);
    
    // 메서드
    public void run();                      // 스레드 실행
    public void fetchAndSaveMovies();       // API 호출 및 DB 저장
    private String getTagValue(...);        // XML 태그 값 추출
    private String getYesterdayDate();      // 어제 날짜 계산
    private long getNextMondayMidnight();   // 스케줄 계산
    public void stopAPI();                  // 스레드 종료
}
```

### 동작 흐름

```
┌────────────────────────────────────────────────────────────────────────┐
│                          APIManager 동작 흐름                          │
├────────────────────────────────────────────────────────────────────────┤
│                                                                        │
│  [서버 시작]                                                           │
│       │                                                                │
│       ▼                                                                │
│  ┌─────────────────┐                                                   │
│  │ APIManager 생성 │                                                   │
│  │   & start()     │                                                   │
│  └────────┬────────┘                                                   │
│           │                                                            │
│           ▼                                                            │
│  ┌─────────────────┐                                                   │
│  │ 즉시 1회 호출   │──────────────────────────────────────┐            │
│  │fetchAndSaveMovies│                                     │            │
│  └────────┬────────┘                                      │            │
│           │                                               │            │
│           ▼                                               │            │
│  ┌─────────────────┐                                      │            │
│  │  Timer 설정     │                                      │            │
│  │ (주 1회 갱신)   │                                      │            │
│  └────────┬────────┘                                      │            │
│           │                                               │            │
│           ▼                                               ▼            │
│  ┌─────────────────┐     ┌─────────────────────────────────────────┐  │
│  │  대기 (7일)     │────▶│            fetchAndSaveMovies()         │  │
│  └─────────────────┘     │  1. 어제 날짜 계산                       │  │
│           ▲              │  2. API URL 생성                        │  │
│           │              │  3. HTTP GET 요청                       │  │
│           │              │  4. XML 파싱                            │  │
│           │              │  5. 각 영화 데이터 추출                  │  │
│           │              │  6. DB 저장 (UPSERT)                    │  │
│           │              └─────────────────────────────────────────┘  │
│           │                               │                           │
│           └───────────────────────────────┘                           │
│                                                                        │
└────────────────────────────────────────────────────────────────────────┘
```

---

## API 호출 코드

### fetchAndSaveMovies() 메서드

```java
public void fetchAndSaveMovies() {
    System.out.println("API> 박스오피스 데이터 가져오는 중...");
    
    try {
        // 1. 어제 날짜 구하기 (박스오피스는 전일 기준)
        String targetDate = getYesterdayDate();
        
        // 2. API URL 생성
        String urlString = API_URL + "?key=" + API_KEY + "&targetDt=" + targetDate;
        
        // 3. HTTP 연결
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        
        // 4. XML 파싱
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(conn.getInputStream());
        doc.getDocumentElement().normalize();
        
        // 5. <dailyBoxOffice> 태그들 가져오기
        NodeList movieList = doc.getElementsByTagName("dailyBoxOffice");
        
        // 6. 각 영화 데이터 파싱 및 DB 저장
        for (int i = 0; i < movieList.getLength(); i++) {
            Element movie = (Element) movieList.item(i);
            
            String movieCd = getTagValue("movieCd", movie);
            String movieNm = getTagValue("movieNm", movie);
            int rank = Integer.parseInt(getTagValue("rank", movie));
            String openDt = getTagValue("openDt", movie);
            long audiAcc = Long.parseLong(getTagValue("audiAcc", movie));
            long salesAcc = Long.parseLong(getTagValue("salesAcc", movie));
            
            // DB에 저장
            dbManager.saveMovie(movieCd, movieNm, rank, openDt, audiAcc, salesAcc);
        }
        
        conn.disconnect();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

---

## 데이터 저장

### UPSERT 전략

영화 데이터는 `REPLACE INTO` 구문을 사용하여 저장합니다:

```sql
REPLACE INTO Movies (movie_cd, movie_nm, rank, open_dt, audi_acc, sales_acc, update_dt) 
VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
```

- **새로운 영화**: INSERT 수행
- **기존 영화**: UPDATE 수행 (순위, 관객수, 매출액 갱신)

### 저장되는 데이터

| 필드 | API 필드 | 설명 |
|------|----------|------|
| movie_cd | movieCd | 영화 코드 |
| movie_nm | movieNm | 영화 제목 |
| rank | rank | 박스오피스 순위 |
| open_dt | openDt | 개봉일 |
| audi_acc | audiAcc | 누적 관객 수 |
| sales_acc | salesAcc | 누적 매출액 |

---

## 스케줄링

### 갱신 주기

| 이벤트 | 시점 |
|--------|------|
| 최초 호출 | 서버 시작 직후 |
| 정기 호출 | 매주 월요일 00:00 |

### Timer 설정

```java
Timer timer = new Timer();
timer.scheduleAtFixedRate(new TimerTask() {
    @Override
    public void run() {
        if (running) {
            fetchAndSaveMovies();
        }
    }
}, getNextMondayMidnight(),    // 첫 실행: 다음 월요일 자정
   7 * 24 * 60 * 60 * 1000L);  // 반복 주기: 7일
```

### 다음 월요일 자정 계산

```java
private long getNextMondayMidnight() {
    Calendar cal = Calendar.getInstance();
    
    // 다음 월요일로 이동
    int daysUntilMonday = (Calendar.MONDAY - cal.get(Calendar.DAY_OF_WEEK) + 7) % 7;
    if (daysUntilMonday == 0) {
        daysUntilMonday = 7; // 오늘이 월요일이면 다음 주
    }
    cal.add(Calendar.DAY_OF_MONTH, daysUntilMonday);
    
    // 자정으로 설정
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    
    return cal.getTimeInMillis() - System.currentTimeMillis();
}
```

---

## 에러 처리

| 예외 | 원인 | 처리 |
|------|------|------|
| `MalformedURLException` | URL 형식 오류 | 로그 출력 |
| `IOException` | 네트워크 오류 | 로그 출력, 다음 스케줄에서 재시도 |
| `ParserConfigurationException` | XML 파서 설정 오류 | 로그 출력 |
| `SAXException` | XML 파싱 오류 | 로그 출력 |

### 타임아웃 설정

```java
conn.setConnectTimeout(5000);  // 연결 타임아웃: 5초
conn.setReadTimeout(5000);     // 읽기 타임아웃: 5초
```

---

## API 키 관리

### 현재 설정

```java
private static final String API_KEY = "ebd2ef0243c007f60e7f197614e7ce88";
```

### 권장 사항

보안을 위해 API 키를 다음 방법으로 관리하는 것을 권장합니다:

1. **환경 변수 사용**
```java
private static final String API_KEY = System.getenv("KOFIC_API_KEY");
```

2. **설정 파일 사용**
```java
Properties props = new Properties();
props.load(new FileInputStream("config.properties"));
String apiKey = props.getProperty("kofic.api.key");
```

---

## 테스트

### 독립 실행 테스트

```java
public static void main(String[] args) {
    DatabaseManager dbManager = new DatabaseManager();
    APIManager apiManager = new APIManager(dbManager);
    
    // 즉시 실행 테스트
    apiManager.fetchAndSaveMovies();
    
    // DB 조회 확인
    ResultSet rs = dbManager.getTop10Movies();
    while (rs.next()) {
        System.out.println(rs.getInt("rank") + "위: " + rs.getString("movie_nm"));
    }
    
    dbManager.close();
}
```

---

[← 돌아가기](./README.md)
