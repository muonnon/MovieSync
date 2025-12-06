//프로젝트 이름 : MovieSync
//개발자 : 권미리
//개발 기간: 2025.12.01 ~ 2025.12.13
// 서버가 클라이언트에게 보낼 메시지를 정의하는 클래스

public class MSGBuilder {
    
    // LOGIN_OK//userId//닉네임 확인 완료//END
    String loginOkMSG(int userId, String username) {
        return "LOGIN_OK//" + userId + "//" + username + " 확인 완료//END";
    }
    
    // LOGIN_FAIL//에러메시지//END
    String loginFailMSG(String reason) {
        return "LOGIN_FAIL//" + reason + "//END";
    }
    
    // WELCOME//환영 메시지//END
    String welcomeMSG() {
        return "WELCOME//MovieSync에 오신 것을 환영합니다!//END";
    }
    
    // MOVIES_COUNT//총개수//END
    String moviesCountMSG(int count) {
        return "MOVIES_COUNT//" + count + "//END";
    }
    
    // MOVIES_DATA//영화코드//영화제목//순위//개봉일//누적관객//누적매출//END
    String movieDataMSG(String movieCd, String movieNm, int rank, String openDt, 
                        long audiAcc, long salesAcc) {
        return "MOVIES_DATA//" + movieCd + "//" + movieNm + "//" + rank + "//" + 
               openDt + "//" + audiAcc + "//" + salesAcc + "//END";
    }
    
    // MOVIES_END//END
    String moviesEndMSG() {
        return "MOVIES_END//END";
    }
    
    // DETAIL//영화코드//영화제목//순위//개봉일//누적관객//누적매출//평균평점//END
    String movieDetailMSG(String movieCd, String movieNm, int rank, String openDt,
                          long audiAcc, long salesAcc, double avgRating) {
        return "DETAIL//" + movieCd + "//" + movieNm + "//" + rank + "//" + 
               openDt + "//" + audiAcc + "//" + salesAcc + "//" + avgRating + "//END";
    }
    
    // ROOM_OK//roomId//영화제목//END
    String roomOkMSG(String roomId, String movieNm) {
        return "ROOM_OK//" + roomId + "//" + movieNm + "//END";
    }
    
    // USER_JOIN//닉네임//현재인원//END
    String userJoinMSG(String username, int count) {
        return "USER_JOIN//" + username + "//" + count + "//END";
    }
    
    // USER_LEFT//닉네임//현재인원//END
    String userLeftMSG(String username, int count) {
        return "USER_LEFT//" + username + "//" + count + "//END";
    }
    
    // CHAT_ALL//발신자//메시지//END
    String chatAllMSG(String sender, String message) {
        return "CHAT_ALL//" + sender + "//" + message + "//END";
    }
    
    // REV_SUMMARY//영화코드//영화제목//평균평점//감상평개수//END
    String reviewSummaryMSG(String movieCd, String movieNm, double avgRating, int count) {
        return "REV_SUMMARY//" + movieCd + "//" + movieNm + "//" + avgRating + "//" + count + "//END";
    }
    
    // REV_COUNT//총개수//END
    String reviewCountMSG(int count) {
        return "REV_COUNT//" + count + "//END";
    }
    
    // REV_DATA//reviewId//작성자//별점//내용//작성일시//END
    String reviewDataMSG(int reviewId, String username, int rating, String content, String createdAt) {
        return "REV_DATA//" + reviewId + "//" + username + "//" + rating + "//" + 
               content + "//" + createdAt + "//END";
    }
    
    // REV_END//END
    String reviewEndMSG() {
        return "REV_END//END";
    }
    
    // REV_OK//reviewId//감상평이 저장되었습니다//END
    String reviewOkMSG(int reviewId) {
        return "REV_OK//" + reviewId + "//감상평이 저장되었습니다//END";
    }
    
    // REV_FAIL//에러메시지//END
    String reviewFailMSG(String reason) {
        return "REV_FAIL//" + reason + "//END";
    }
    
    // DEL_OK//감상평이 삭제되었습니다//END
    String deleteOkMSG() {
        return "DEL_OK//감상평이 삭제되었습니다//END";
    }
    
    // DEL_FAIL//에러메시지//END
    String deleteFailMSG(String reason) {
        return "DEL_FAIL//" + reason + "//END";
    }
    
    // BOOKMARK_OK//북마크가 추가되었습니다//END
    String bookmarkOkMSG() {
        return "BOOKMARK_OK//북마크가 추가되었습니다//END";
    }
    
    // BOOKMARK_DEL_OK//북마크가 삭제되었습니다//END
    String bookmarkDelOkMSG() {
        return "BOOKMARK_DEL_OK//북마크가 삭제되었습니다//END";
    }
    
    // DISCONNECT_OK//연결이 종료되었습니다//END
    String disconnectOkMSG() {
        return "DISCONNECT_OK//연결이 종료되었습니다//END";
    }
    
    // ERROR//에러메시지//END
    String errorMSG(String errorMsg) {
        return "ERROR//" + errorMsg + "//END";
    }
}
