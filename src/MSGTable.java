//프로젝트 이름 : MovieSync
//개발자 : 권미리
//개발 기간: 2025.12.01 ~ 2025.12.13

/* 메세지 포맷 중 태그부분을 정의하는 클래스
 * Client > Server 메세지 포맷
LOGIN//닉네임//END
GET_MOVIES//END
GET_DETAIL//영화코드//END
JOIN_ROOM//영화코드//영화제목//END
LEAVE_ROOM//영화코드//END
CHAT//영화코드//메시지//END
GET_REVIEWS//영화코드//END
SUBMIT_REVIEW//영화코드//별점//감상평내용//END
DELETE_REVIEW//reviewId//END
SEARCH_MOVIE//검색어//END
ADD_BOOKMARK//영화코드//END
DELETE_BOOKMARK//영화코드//END
GET_BOOKMARKS//END
DISCONNECT//END
*/

public class MSGTable {
    // Client > Server 메시지
    int numberOfMSG = 14;
    String[] MSGtags = {
        "LOGIN",           // 0. 로그인 (닉네임)
        "GET_MOVIES",      // 1. 영화 목록 조회
        "GET_DETAIL",      // 2. 영화 상세 정보 조회
        "JOIN_ROOM",       // 3. 채팅방 입장
        "LEAVE_ROOM",      // 4. 채팅방 퇴장
        "CHAT",            // 5. 채팅 메시지 전송
        "GET_REVIEWS",     // 6. 감상평 조회
        "SUBMIT_REVIEW",   // 7. 감상평 작성
        "DELETE_REVIEW",   // 8. 감상평 삭제
        "SEARCH_MOVIE",    // 9. 영화 검색
        "ADD_BOOKMARK",    // 10. 북마크 추가
        "DELETE_BOOKMARK", // 11. 북마크 삭제
        "GET_BOOKMARKS",   // 12. 북마크 목록 조회
        "DISCONNECT"       // 13. 연결 종료
    };
    
    // Server > Client 응답 메시지 (참고용)
    // MOVIES_COUNT, MOVIES_DATA, MOVIES_END
    // REV_COUNT, REV_DATA, REV_END
}
