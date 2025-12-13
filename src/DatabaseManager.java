//프로젝트 이름 : MovieSync
//개발자 : 권미리
//개발 기간: 2025.12.01 ~ 2025.12.13

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

//SQLite 데이터베이스를 관리하는 클래스 - ai 도움 받았습니다.
public class DatabaseManager {
    //데이터베이스 연결 정보
    private static final String DB_URL = "jdbc:sqlite:moviesync.db";  //SQLite DB 파일 경로
    private Connection conn = null;  //DB 연결 객체
    
    //생성자 - DB 연결 및 테이블 생성
    public DatabaseManager() {
        try {
            //SQLite JDBC 드라이버 로드
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(DB_URL); //DB 연결
            System.out.println("DB> 데이터베이스 연결 성공");
            
            //테이블 생성
            createTables();
        } catch (ClassNotFoundException e) { //JDBC 드라이버 없음
            System.err.println("DB> JDBC 드라이버를 찾을 수 없습니다: " + e.getMessage());
        } catch (SQLException e) { //DB 연결 실패
            System.err.println("DB> 데이터베이스 연결 실패: " + e.getMessage());
        }
    }
    
    //테이블 생성 메소드 - Users, Movies, Reviews, Bookmarks
    private void createTables() {
        try {
            Statement stmt = conn.createStatement(); //SQL 실행용 Statement 생성
            
            //Users 테이블 - 사용자 정보 저장
            String createUsers = "CREATE TABLE IF NOT EXISTS Users (" +
                    "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " + //사용자 ID (자동 증가)
                    "username TEXT NOT NULL UNIQUE, " + //닉네임 (중복 불가)
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)"; //가입 일시
            stmt.execute(createUsers);
            
            //Movies 테이블 - 영화 정보 저장 (API에서 받은 데이터)
            String createMovies = "CREATE TABLE IF NOT EXISTS Movies (" +
                    "movie_cd TEXT PRIMARY KEY, " + //영화 코드 (API 제공)
                    "movie_nm TEXT NOT NULL, " + //영화 제목
                    "rank INTEGER, " + //박스오피스 순위
                    "open_dt TEXT, " + //개봉일
                    "audi_acc INTEGER, " + //누적 관객수
                    "sales_acc INTEGER, " + //누적 매출액
                    "update_dt DATETIME DEFAULT CURRENT_TIMESTAMP)"; //갱신 일시
            stmt.execute(createMovies);
            
            //Reviews 테이블 - 감상평 저장
            String createReviews = "CREATE TABLE IF NOT EXISTS Reviews (" +
                    "review_id INTEGER PRIMARY KEY AUTOINCREMENT, " + //감상평 ID (자동 증가)
                    "user_id INTEGER NOT NULL, " + //작성자 ID (FK)
                    "movie_cd TEXT NOT NULL, " + //영화 코드 (FK)
                    "rating INTEGER NOT NULL CHECK(rating >= 1 AND rating <= 5), " + //별점 (1-5 제약)
                    "content TEXT, " + //감상평 내용
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " + //작성 일시
                    "FOREIGN KEY (user_id) REFERENCES Users(user_id), " + //외래키 제약
                    "FOREIGN KEY (movie_cd) REFERENCES Movies(movie_cd))"; //외래키 제약
            stmt.execute(createReviews);
            
            //Bookmarks 테이블 (선택 기능) - 북마크 저장
            String createBookmarks = "CREATE TABLE IF NOT EXISTS Bookmarks (" +
                    "bookmark_id INTEGER PRIMARY KEY AUTOINCREMENT, " + //북마크 ID (자동 증가)
                    "user_id INTEGER NOT NULL, " + //사용자 ID (FK)
                    "movie_cd TEXT NOT NULL, " + //영화 코드 (FK)
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " + //추가 일시
                    "FOREIGN KEY (user_id) REFERENCES Users(user_id), " + //외래키 제약
                    "FOREIGN KEY (movie_cd) REFERENCES Movies(movie_cd), " + //외래키 제약
                    "UNIQUE(user_id, movie_cd))"; //중복 방지 (사용자+영화 조합 유일)
            stmt.execute(createBookmarks);
            
            stmt.close(); //Statement 닫기
            System.out.println("DB> 테이블 생성 완료");
        } catch (SQLException e) { //테이블 생성 실패
            System.err.println("DB> 테이블 생성 실패: " + e.getMessage());
        }
    }
    
    //Users 테이블 관련 메소드
    //사용자 생성 또는 기존 사용자 조회 (로그인)
    public int createUser(String username) {
        try {
            //기존 사용자 확인 - 있으면 기존 userId 반환
            int existingUserId = getUserIdByUsername(username);
            if (existingUserId > 0) { //기존 사용자면
                System.out.println("DB> 기존 사용자 로그인: " + username + " (ID: " + existingUserId + ")");
                return existingUserId; //기존 ID 반환
            }
            
            //새 사용자 생성 - INSERT
            String sql = "INSERT INTO Users (username) VALUES (?)";
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); //생성된 키 반환 옵션
            pstmt.setString(1, username); //? 에 username 값 바인딩
            pstmt.executeUpdate(); //쿼리 실행
            
            //생성된 user_id 가져오기
            ResultSet rs = pstmt.getGeneratedKeys(); //자동 생성된 키 가져오기
            if (rs.next()) {
                int userId = rs.getInt(1); //첫 번째 컬럼 (user_id)
                rs.close();
                pstmt.close();
                System.out.println("DB> 사용자 생성: " + username + " (ID: " + userId + ")");
                return userId; //새로 생성된 ID 반환
            }
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("DB> 사용자 생성 실패: " + e.getMessage());
        }
        return -1; //실패
    }
    
    //닉네임으로 userId 조회
    public int getUserIdByUsername(String username) {
        try {
            String sql = "SELECT user_id FROM Users WHERE username = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username); //? 에 username 값 바인딩
            ResultSet rs = pstmt.executeQuery(); //쿼리 실행
            
            if (rs.next()) { //결과가 있으면
                int userId = rs.getInt("user_id"); //user_id 컬럼 값 가져오기
                rs.close();
                pstmt.close();
                return userId;
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("DB> 사용자 조회 실패: " + e.getMessage());
        }
        return -1; //없음 또는 실패
    }
    
    //닉네임 중복 체크
    public boolean isUsernameTaken(String username) {
        try {
            String sql = "SELECT COUNT(*) FROM Users WHERE username = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username); //? 에 username 값 바인딩
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt(1); //첫 번째 컬럼 (개수)
                rs.close();
                pstmt.close();
                return count > 0; //있으면 true, 없으면 false
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("DB> 닉네임 중복 체크 실패: " + e.getMessage());
        }
        return false;
    }
    
    //userId로 닉네임 가져오기
    public String getUsername(int userId) {
        try {
            String sql = "SELECT username FROM Users WHERE user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId); //? 에 userId 값 바인딩
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String username = rs.getString("username"); //username 컬럼 값 가져오기
                rs.close();
                pstmt.close();
                return username;
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("DB> 닉네임 조회 실패: " + e.getMessage());
        }
        return null; //없음 또는 실패
    }
    
    // ========== Movies 테이블 관련 메소드 ==========
    
    //영화 저장 또는 갱신 (UPSERT) - API에서 받은 데이터 저장
    public boolean saveMovie(String movieCd, String movieNm, int rank, String openDt, 
                             long audiAcc, long salesAcc) {
        try {
            //SQLite는 REPLACE INTO 사용 (INSERT OR REPLACE) - 기존 데이터 덮어쓰기
            String sql = "REPLACE INTO Movies (movie_cd, movie_nm, rank, open_dt, audi_acc, sales_acc, update_dt) " +
                         "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, movieCd); //영화 코드
            pstmt.setString(2, movieNm); //영화 제목
            pstmt.setInt(3, rank); //순위
            pstmt.setString(4, openDt); //개봉일
            pstmt.setLong(5, audiAcc); //누적 관객수 (long 타입)
            pstmt.setLong(6, salesAcc); //누적 매출액 (long 타입)
            pstmt.executeUpdate(); //쿼리 실행
            pstmt.close();
            System.out.println("DB> 영화 저장: " + movieNm);
            return true;
        } catch (SQLException e) {
            System.err.println("DB> 영화 저장 실패: " + e.getMessage());
            return false;
        }
    }
    
    //박스오피스 Top 10 조회 - 순위 순으로 최대 10개
    public ResultSet getTop10Movies() {
        try {
            String sql = "SELECT * FROM Movies WHERE rank <= 10 ORDER BY rank, movie_cd LIMIT 10";
            Statement stmt = conn.createStatement();
            return stmt.executeQuery(sql); //ResultSet 반환 (호출자가 닫아야 함)
        } catch (SQLException e) {
            System.err.println("DB> 영화 목록 조회 실패: " + e.getMessage());
            return null;
        }
    }
    
    //영화 상세 정보 조회 - 영화 코드로 조회
    public ResultSet getMovieDetail(String movieCd) {
        try {
            String sql = "SELECT * FROM Movies WHERE movie_cd = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, movieCd); //? 에 movieCd 값 바인딩
            return pstmt.executeQuery(); //ResultSet 반환 (호출자가 닫아야 함)
        } catch (SQLException e) {
            System.err.println("DB> 영화 상세 조회 실패: " + e.getMessage());
            return null;
        }
    }
    
    //영화 검색 - 제목으로 부분 검색 (LIKE 사용)
    public ResultSet searchMovies(String keyword) {
        try {
            String sql = "SELECT * FROM Movies WHERE movie_nm LIKE ? ORDER BY rank";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + keyword + "%"); //앞뒤에 % 추가 (부분 검색)
            return pstmt.executeQuery(); //ResultSet 반환
        } catch (SQLException e) {
            System.err.println("DB> 영화 검색 실패: " + e.getMessage());
            return null;
        }
    }
    
    // ========== Reviews 테이블 관련 메소드 ==========
    
    //감상평 작성 - userId, movieCd, rating, content로 저장
    public int submitReview(int userId, String movieCd, int rating, String content) {
        try {
            String sql = "INSERT INTO Reviews (user_id, movie_cd, rating, content) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, userId); //작성자 ID
            pstmt.setString(2, movieCd); //영화 코드
            pstmt.setInt(3, rating); //별점 (1-5)
            pstmt.setString(4, content); //감상평 내용
            pstmt.executeUpdate(); //쿼리 실행
            
            ResultSet rs = pstmt.getGeneratedKeys(); //생성된 review_id 가져오기
            if (rs.next()) {
                int reviewId = rs.getInt(1); //첫 번째 컬럼 (review_id)
                rs.close();
                pstmt.close();
                System.out.println("DB> 감상평 작성: reviewId=" + reviewId);
                return reviewId;
            }
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("DB> 감상평 작성 실패: " + e.getMessage());
        }
        return -1; //실패
    }
    
    //영화별 감상평 조회 (작성자명 포함 - JOIN) - 최신순 정렬
    public ResultSet getReviews(String movieCd) {
        try {
            String sql = "SELECT r.review_id, u.username, r.rating, r.content, r.created_at " +
                         "FROM Reviews r JOIN Users u ON r.user_id = u.user_id " + //JOIN으로 작성자명 가져오기
                         "WHERE r.movie_cd = ? ORDER BY r.created_at DESC"; //최신순 정렬
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, movieCd); //? 에 movieCd 값 바인딩
            return pstmt.executeQuery(); //ResultSet 반환
        } catch (SQLException e) {
            System.err.println("DB> 감상평 조회 실패: " + e.getMessage());
            return null;
        }
    }
    
    //전체 감상평 조회 (영화 정보 포함) - Reviews + Users + Movies 3개 테이블 JOIN
    public ResultSet getAllReviews() {
        try {
            String sql = "SELECT r.review_id, m.movie_nm, u.username, r.rating, r.content, r.created_at " +
                         "FROM Reviews r " +
                         "JOIN Users u ON r.user_id = u.user_id " + //작성자명
                         "JOIN Movies m ON r.movie_cd = m.movie_cd " + //영화명
                         "ORDER BY r.created_at DESC"; //최신순 정렬
            PreparedStatement pstmt = conn.prepareStatement(sql);
            return pstmt.executeQuery(); //ResultSet 반환
        } catch (SQLException e) {
            System.err.println("DB> 전체 감상평 조회 실패: " + e.getMessage());
            return null;
        }
    }
    
    //평균 평점 계산 - AVG 함수 사용
    public double getAverageRating(String movieCd) {
        try {
            String sql = "SELECT AVG(rating) as avg_rating FROM Reviews WHERE movie_cd = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, movieCd); //? 에 movieCd 값 바인딩
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                double avgRating = rs.getDouble("avg_rating"); //평균 평점
                rs.close();
                pstmt.close();
                return avgRating;
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("DB> 평균 평점 조회 실패: " + e.getMessage());
        }
        return 0.0; //실패 또는 감상평 없음
    }
    
    //감상평 개수 조회 - COUNT 함수 사용
    public int getReviewCount(String movieCd) {
        try {
            String sql = "SELECT COUNT(*) as count FROM Reviews WHERE movie_cd = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, movieCd); //? 에 movieCd 값 바인딩
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt("count"); //개수
                rs.close();
                pstmt.close();
                return count;
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("DB> 감상평 개수 조회 실패: " + e.getMessage());
        }
        return 0; //실패 또는 감상평 없음
    }
    
    //감상평 삭제 (권한 확인 포함) - 본인 작성 감상평만 삭제 가능
    public boolean deleteReview(int reviewId, int userId) {
        try {
            //본인 작성 여부 확인 - 권한 검증
            String checkSql = "SELECT user_id FROM Reviews WHERE review_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, reviewId); //? 에 reviewId 값 바인딩
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                int authorId = rs.getInt("user_id"); //작성자 ID
                rs.close();
                checkStmt.close();
                
                if (authorId != userId) { //작성자와 요청자가 다르면
                    System.out.println("DB> 권한 없음: 다른 사용자의 감상평");
                    return false; //권한 없음
                }
                
                //삭제 실행 - DELETE
                String deleteSql = "DELETE FROM Reviews WHERE review_id = ?";
                PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
                deleteStmt.setInt(1, reviewId); //? 에 reviewId 값 바인딩
                deleteStmt.executeUpdate(); //쿼리 실행
                deleteStmt.close();
                System.out.println("DB> 감상평 삭제: reviewId=" + reviewId);
                return true; //삭제 성공
            }
            rs.close();
            checkStmt.close();
        } catch (SQLException e) {
            System.err.println("DB> 감상평 삭제 실패: " + e.getMessage());
        }
        return false; //실패
    }
    
    //Bookmarks 테이블 관련 메소드
    //북마크 추가 - INSERT (중복 시 실패)
    public boolean addBookmark(int userId, String movieCd) {
        try {
            String sql = "INSERT INTO Bookmarks (user_id, movie_cd) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId); //사용자 ID
            pstmt.setString(2, movieCd); //영화 코드
            pstmt.executeUpdate(); //쿼리 실행
            pstmt.close();
            System.out.println("DB> 북마크 추가: userId=" + userId + ", movieCd=" + movieCd);
            return true; //추가 성공
        } catch (SQLException e) { //UNIQUE 제약 위반 시 실패
            System.err.println("DB> 북마크 추가 실패 (중복 가능): " + e.getMessage());
            return false; //실패 (이미 존재)
        }
    }
    
    //북마크 삭제 - DELETE
    public boolean deleteBookmark(int userId, String movieCd) {
        try {
            String sql = "DELETE FROM Bookmarks WHERE user_id = ? AND movie_cd = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId); //사용자 ID
            pstmt.setString(2, movieCd); //영화 코드
            int rows = pstmt.executeUpdate(); //삭제된 행 개수 반환
            pstmt.close();
            System.out.println("DB> 북마크 삭제: " + rows + "개");
            return rows > 0; //삭제된 행이 있으면 true
        } catch (SQLException e) {
            System.err.println("DB> 북마크 삭제 실패: " + e.getMessage());
            return false;
        }
    }
    
    //사용자의 북마크 목록 조회 - Bookmarks + Movies JOIN
    public ResultSet getBookmarks(int userId) {
        try {
            String sql = "SELECT m.* FROM Bookmarks b JOIN Movies m ON b.movie_cd = m.movie_cd " + //JOIN으로 영화 정보 가져오기
                         "WHERE b.user_id = ? ORDER BY b.created_at DESC"; //최신순 정렬
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId); //? 에 userId 값 바인딩
            return pstmt.executeQuery(); //ResultSet 반환
        } catch (SQLException e) {
            System.err.println("DB> 북마크 조회 실패: " + e.getMessage());
            return null;
        }
    }
    
    //DB 연결 종료
    public void close() {
        try {
            if (conn != null && !conn.isClosed()) { //연결이 열려있으면
                conn.close(); //연결 닫기
                System.out.println("DB> 데이터베이스 연결 종료");
            }
        } catch (SQLException e) {
            System.err.println("DB> 연결 종료 실패: " + e.getMessage());
        }
    }
}