//프로젝트 이름 : MovieSync
//개발자 : 권미리
//개발 기간: 2025.12.01 ~ 2025.12.13
// 클라이언트에게 받은 메시지를 파싱하는 클래스
// 메시지 구분자: | (파이프)

import java.util.StringTokenizer;

/**
 * 클라이언트로부터 받은 메시지를 파싱하는 클래스
 * StringTokenizer를 사용하여 | 구분자로 메시지를 분리한다
 */
public class ReceivedMSGTokenizer {
    StringTokenizer st;                          // 메시지 분리용
    MSGTable mt = new MSGTable();                // 메시지 태그 테이블
    private static final String DELIMITER = "|"; // 구분자
    
    /**
     * 메시지 타입을 감지하여 번호로 반환 (0~13)
     * @param _msg 수신된 메시지
     * @return 메시지 타입 번호 (MSGTable 참고)
     */
    public int detection(String _msg) {
        int result = -1;
        String tag;
        st = new StringTokenizer(_msg, DELIMITER);
        tag = st.nextToken();
        for (int i = 0; i < mt.numberOfMSG; i++) {
            if (tag.equals(mt.MSGtags[i])) {
                result = i;
                break;
            }
        }
        return result;
    }
    
    // LOGIN|닉네임|END : 닉네임 추출
    String findUsername(String _msg) {
        st = new StringTokenizer(_msg, DELIMITER);
        st.nextToken(); // 태그 부분
        return st.nextToken(); // 닉네임
    }
    
    // GET_DETAIL|영화코드|END : 영화코드 추출
    String findMovieCode(String _msg) {
        st = new StringTokenizer(_msg, DELIMITER);
        st.nextToken(); // 태그
        return st.nextToken(); // 영화코드
    }
    
    // JOIN_ROOM|영화코드|영화제목|END : 영화코드 추출
    String findRoomMovieCode(String _msg) {
        st = new StringTokenizer(_msg, DELIMITER);
        st.nextToken(); // 태그
        return st.nextToken(); // 영화코드
    }
    
    // JOIN_ROOM|영화코드|영화제목|END : 영화제목 추출
    String findRoomMovieName(String _msg) {
        st = new StringTokenizer(_msg, DELIMITER);
        st.nextToken(); // 태그
        st.nextToken(); // 영화코드
        return st.nextToken(); // 영화제목
    }
    
    // LEAVE_ROOM|영화코드|END : 영화코드 추출
    String findLeaveRoomCode(String _msg) {
        st = new StringTokenizer(_msg, DELIMITER);
        st.nextToken(); // 태그
        return st.nextToken(); // 영화코드
    }
    
    // CHAT|영화코드|메시지|END : 영화코드 추출
    String findChatRoomCode(String _msg) {
        st = new StringTokenizer(_msg, DELIMITER);
        st.nextToken(); // 태그
        return st.nextToken(); // 영화코드
    }
    
    // CHAT|영화코드|메시지|END : 메시지 추출
    String findChatMessage(String _msg) {
        st = new StringTokenizer(_msg, DELIMITER);
        st.nextToken(); // 태그
        st.nextToken(); // 영화코드
        return st.nextToken(); // 메시지
    }
    
    // GET_REVIEWS|영화코드|END : 영화코드 추출
    String findReviewMovieCode(String _msg) {
        st = new StringTokenizer(_msg, DELIMITER);
        st.nextToken(); // 태그
        return st.nextToken(); // 영화코드
    }
    
    // SUBMIT_REVIEW|영화코드|별점|감상평내용|END : 영화코드
    String findSubmitMovieCode(String _msg) {
        st = new StringTokenizer(_msg, DELIMITER);
        st.nextToken(); // 태그
        return st.nextToken(); // 영화코드
    }
    
    // SUBMIT_REVIEW|영화코드|별점|감상평내용|END : 별점
    int findSubmitRating(String _msg) {
        st = new StringTokenizer(_msg, DELIMITER);
        st.nextToken(); // 태그
        st.nextToken(); // 영화코드
        return Integer.parseInt(st.nextToken()); // 별점
    }
    
    // SUBMIT_REVIEW|영화코드|별점|감상평내용|END : 감상평내용
    String findSubmitContent(String _msg) {
        st = new StringTokenizer(_msg, DELIMITER);
        st.nextToken(); // 태그
        st.nextToken(); // 영화코드
        st.nextToken(); // 별점
        return st.nextToken(); // 감상평내용
    }
    
    // DELETE_REVIEW|reviewId|END : reviewId 추출
    int findDeleteReviewId(String _msg) {
        st = new StringTokenizer(_msg, DELIMITER);
        st.nextToken(); // 태그
        return Integer.parseInt(st.nextToken()); // reviewId
    }
    
    // SEARCH_MOVIE|검색어|END : 검색어 추출
    String findSearchKeyword(String _msg) {
        st = new StringTokenizer(_msg, DELIMITER);
        st.nextToken(); // 태그
        return st.nextToken(); // 검색어
    }
    
    // ADD_BOOKMARK|영화코드|END : 영화코드 추출
    String findBookmarkMovieCode(String _msg) {
        st = new StringTokenizer(_msg, DELIMITER);
        st.nextToken(); // 태그
        return st.nextToken(); // 영화코드
    }
    
    // DELETE_BOOKMARK|영화코드|END : 영화코드 추출
    String findDeleteBookmarkCode(String _msg) {
        st = new StringTokenizer(_msg, DELIMITER);
        st.nextToken(); // 태그
        return st.nextToken(); // 영화코드
    }
}