//프로젝트 이름 : MovieSync
//개발자 : 권미리
//개발 기간: 2025.12.01 ~ 2025.12.13
// SQLite 데이터베이스 연결 및 쿼리 관리 클래스

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:moviesync.db";
    private Connection conn = null;
    
    // 생성자 - DB 연결 및 테이블 생성
    public DatabaseManager() {
        try {
            // SQLite JDBC 드라이버 로드
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(DB_URL);
            System.out.println("DB> 데이터베이스 연결 성공");
            
            // 테이블 생성
            createTables();
        } catch (ClassNotFoundException e) {
            System.err.println("DB> JDBC 드라이버를 찾을 수 없습니다: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("DB> 데이터베이스 연결 실패: " + e.getMessage());
        }
    }
    
    // 테이블 생성 메소드
    private void createTables() {
        try {
            Statement stmt = conn.createStatement();
            
            // Users 테이블
            String createUsers = "CREATE TABLE IF NOT EXISTS Users (" +
                    "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT NOT NULL UNIQUE, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)";
            stmt.execute(createUsers);
            
            // Movies 테이블
            String createMovies = "CREATE TABLE IF NOT EXISTS Movies (" +
                    "movie_cd TEXT PRIMARY KEY, " +
                    "movie_nm TEXT NOT NULL, " +
                    "rank INTEGER, " +
                    "open_dt TEXT, " +
                    "audi_acc INTEGER, " +
                    "sales_acc INTEGER, " +
                    "update_dt DATETIME DEFAULT CURRENT_TIMESTAMP)";
            stmt.execute(createMovies);
            
            // Reviews 테이블
            String createReviews = "CREATE TABLE IF NOT EXISTS Reviews (" +
                    "review_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL, " +
                    "movie_cd TEXT NOT NULL, " +
                    "rating INTEGER NOT NULL CHECK(rating >= 1 AND rating <= 5), " +
                    "content TEXT, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (user_id) REFERENCES Users(user_id), " +
                    "FOREIGN KEY (movie_cd) REFERENCES Movies(movie_cd))";
            stmt.execute(createReviews);
            
            // Bookmarks 테이블 (선택 기능)
            String createBookmarks = "CREATE TABLE IF NOT EXISTS Bookmarks (" +
                    "bookmark_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL, " +
                    "movie_cd TEXT NOT NULL, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (user_id) REFERENCES Users(user_id), " +
                    "FOREIGN KEY (movie_cd) REFERENCES Movies(movie_cd), " +
                    "UNIQUE(user_id, movie_cd))";
            stmt.execute(createBookmarks);
            
            stmt.close();
            System.out.println("DB> 테이블 생성 완료");
        } catch (SQLException e) {
            System.err.println("DB> 테이블 생성 실패: " + e.getMessage());
        }
    }
    
    // ========== Users 테이블 관련 메소드 ==========
    
    // 사용자 생성 (로그인)
    public int createUser(String username) {
        try {
            // 중복 체크
            if (isUsernameTaken(username)) {
                return -1; // 중복된 닉네임
            }
            
            String sql = "INSERT INTO Users (username) VALUES (?)";
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, username);
            pstmt.executeUpdate();
            
            // 생성된 user_id 가져오기
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int userId = rs.getInt(1);
                rs.close();
                pstmt.close();
                System.out.println("DB> 사용자 생성: " + username + " (ID: " + userId + ")");
                return userId;
            }
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("DB> 사용자 생성 실패: " + e.getMessage());
        }
        return -1;
    }
    
    // 닉네임 중복 체크
    public boolean isUsernameTaken(String username) {
        try {
            String sql = "SELECT COUNT(*) FROM Users WHERE username = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt(1);
                rs.close();
                pstmt.close();
                return count > 0;
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("DB> 닉네임 중복 체크 실패: " + e.getMessage());
        }
        return false;
    }
    
    // userId로 닉네임 가져오기
    public String getUsername(int userId) {
        try {
            String sql = "SELECT username FROM Users WHERE user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String username = rs.getString("username");
                rs.close();
                pstmt.close();
                return username;
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("DB> 닉네임 조회 실패: " + e.getMessage());
        }
        return null;
    }
    
    // ========== Movies 테이블 관련 메소드 ==========
    
    // 영화 저장 또는 갱신 (UPSERT)
    public boolean saveMovie(String movieCd, String movieNm, int rank, String openDt, 
                             long audiAcc, long salesAcc) {
        try {
            // SQLite는 REPLACE INTO 사용 (INSERT OR REPLACE)
            String sql = "REPLACE INTO Movies (movie_cd, movie_nm, rank, open_dt, audi_acc, sales_acc, update_dt) " +
                         "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, movieCd);
            pstmt.setString(2, movieNm);
            pstmt.setInt(3, rank);
            pstmt.setString(4, openDt);
            pstmt.setLong(5, audiAcc);
            pstmt.setLong(6, salesAcc);
            pstmt.executeUpdate();
            pstmt.close();
            System.out.println("DB> 영화 저장: " + movieNm);
            return true;
        } catch (SQLException e) {
            System.err.println("DB> 영화 저장 실패: " + e.getMessage());
            return false;
        }
    }
    
    // 박스오피스 Top 10 조회
    public ResultSet getTop10Movies() {
        try {
            String sql = "SELECT * FROM Movies ORDER BY rank LIMIT 10";
            Statement stmt = conn.createStatement();
            return stmt.executeQuery(sql);
        } catch (SQLException e) {
            System.err.println("DB> 영화 목록 조회 실패: " + e.getMessage());
            return null;
        }
    }
    
    // 영화 상세 정보 조회
    public ResultSet getMovieDetail(String movieCd) {
        try {
            String sql = "SELECT * FROM Movies WHERE movie_cd = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, movieCd);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("DB> 영화 상세 조회 실패: " + e.getMessage());
            return null;
        }
    }
    
    // 영화 검색
    public ResultSet searchMovies(String keyword) {
        try {
            String sql = "SELECT * FROM Movies WHERE movie_nm LIKE ? ORDER BY rank";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + keyword + "%");
            return pstmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("DB> 영화 검색 실패: " + e.getMessage());
            return null;
        }
    }
    
    // ========== Reviews 테이블 관련 메소드 ==========
    
    // 감상평 작성
    public int submitReview(int userId, String movieCd, int rating, String content) {
        try {
            String sql = "INSERT INTO Reviews (user_id, movie_cd, rating, content) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, userId);
            pstmt.setString(2, movieCd);
            pstmt.setInt(3, rating);
            pstmt.setString(4, content);
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int reviewId = rs.getInt(1);
                rs.close();
                pstmt.close();
                System.out.println("DB> 감상평 작성: reviewId=" + reviewId);
                return reviewId;
            }
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("DB> 감상평 작성 실패: " + e.getMessage());
        }
        return -1;
    }
    
    // 영화별 감상평 조회 (작성자명 포함 - JOIN)
    public ResultSet getReviews(String movieCd) {
        try {
            String sql = "SELECT r.review_id, u.username, r.rating, r.content, r.created_at " +
                         "FROM Reviews r JOIN Users u ON r.user_id = u.user_id " +
                         "WHERE r.movie_cd = ? ORDER BY r.created_at DESC";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, movieCd);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("DB> 감상평 조회 실패: " + e.getMessage());
            return null;
        }
    }
    
    // 평균 평점 계산
    public double getAverageRating(String movieCd) {
        try {
            String sql = "SELECT AVG(rating) as avg_rating FROM Reviews WHERE movie_cd = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, movieCd);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                double avgRating = rs.getDouble("avg_rating");
                rs.close();
                pstmt.close();
                return avgRating;
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("DB> 평균 평점 조회 실패: " + e.getMessage());
        }
        return 0.0;
    }
    
    // 감상평 개수 조회
    public int getReviewCount(String movieCd) {
        try {
            String sql = "SELECT COUNT(*) as count FROM Reviews WHERE movie_cd = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, movieCd);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt("count");
                rs.close();
                pstmt.close();
                return count;
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("DB> 감상평 개수 조회 실패: " + e.getMessage());
        }
        return 0;
    }
    
    // 감상평 삭제 (권한 확인 포함)
    public boolean deleteReview(int reviewId, int userId) {
        try {
            // 본인 작성 여부 확인
            String checkSql = "SELECT user_id FROM Reviews WHERE review_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, reviewId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                int authorId = rs.getInt("user_id");
                rs.close();
                checkStmt.close();
                
                if (authorId != userId) {
                    System.out.println("DB> 권한 없음: 다른 사용자의 감상평");
                    return false; // 권한 없음
                }
                
                // 삭제 실행
                String deleteSql = "DELETE FROM Reviews WHERE review_id = ?";
                PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
                deleteStmt.setInt(1, reviewId);
                deleteStmt.executeUpdate();
                deleteStmt.close();
                System.out.println("DB> 감상평 삭제: reviewId=" + reviewId);
                return true;
            }
            rs.close();
            checkStmt.close();
        } catch (SQLException e) {
            System.err.println("DB> 감상평 삭제 실패: " + e.getMessage());
        }
        return false;
    }
    
    // ========== Bookmarks 테이블 관련 메소드 (선택 기능) ==========
    
    // 북마크 추가
    public boolean addBookmark(int userId, String movieCd) {
        try {
            String sql = "INSERT INTO Bookmarks (user_id, movie_cd) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setString(2, movieCd);
            pstmt.executeUpdate();
            pstmt.close();
            System.out.println("DB> 북마크 추가: userId=" + userId + ", movieCd=" + movieCd);
            return true;
        } catch (SQLException e) {
            System.err.println("DB> 북마크 추가 실패 (중복 가능): " + e.getMessage());
            return false;
        }
    }
    
    // 북마크 삭제
    public boolean deleteBookmark(int userId, String movieCd) {
        try {
            String sql = "DELETE FROM Bookmarks WHERE user_id = ? AND movie_cd = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setString(2, movieCd);
            int rows = pstmt.executeUpdate();
            pstmt.close();
            System.out.println("DB> 북마크 삭제: " + rows + "개");
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("DB> 북마크 삭제 실패: " + e.getMessage());
            return false;
        }
    }
    
    // 사용자의 북마크 목록 조회
    public ResultSet getBookmarks(int userId) {
        try {
            String sql = "SELECT m.* FROM Bookmarks b JOIN Movies m ON b.movie_cd = m.movie_cd " +
                         "WHERE b.user_id = ? ORDER BY b.created_at DESC";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("DB> 북마크 조회 실패: " + e.getMessage());
            return null;
        }
    }
    
    // DB 연결 종료
    public void close() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("DB> 데이터베이스 연결 종료");
            }
        } catch (SQLException e) {
            System.err.println("DB> 연결 종료 실패: " + e.getMessage());
        }
    }
}