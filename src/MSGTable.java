//프로젝트 이름 : MovieSync
//개발자 : 권미리
//개발 기간: 2025.12.01 ~ 2025.12.13

public class MSGTable {
    // Client > Server 메시지
    int numberOfMSG = 15;
    String[] MSGtags = {
        "LOGIN",           // 0. 로그인 (닉네임)
        "GET_MOVIES",      // 1. 영화 목록 조회
        "GET_DETAIL",      // 2. 영화 상세 정보 조회
        "JOIN_ROOM",       // 3. 채팅방 입장
        "LEAVE_ROOM",      // 4. 채팅방 퇴장
        "CHAT",            // 5. 채팅 메시지 전송
        "GET_REVIEWS",     // 6. 감상평 조회
        "GET_ALL_REVIEWS", // 7. 전체 감상평 조회
        "SUBMIT_REVIEW",   // 8. 감상평 작성
        "DELETE_REVIEW",   // 9. 감상평 삭제
        "SEARCH_MOVIE",    // 10. 영화 검색
        "ADD_BOOKMARK",    // 11. 북마크 추가
        "DELETE_BOOKMARK", // 12. 북마크 삭제
        "GET_BOOKMARKS",   // 13. 북마크 목록 조회
        "DISCONNECT"       // 14. 연결 종료
    };
}