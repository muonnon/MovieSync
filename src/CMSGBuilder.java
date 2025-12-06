//프로젝트 이름 : MovieSync
//개발자 : 권미리
//개발 기간: 2025.12.01 ~ 2025.12.13
// 클라이언트가 서버에게 보낼 메시지를 정의하는 클래스
// 메시지 구분자: | (파이프)

public class CMSGBuilder {
    
    // LOGIN|닉네임|END
    public String loginMSG(String username) {
        return "LOGIN|" + username + "|END";
    }
    
    // GET_MOVIES|END
    public String getMoviesMSG() {
        return "GET_MOVIES|END";
    }
    
    // GET_DETAIL|영화코드|END
    public String getDetailMSG(String movieCd) {
        return "GET_DETAIL|" + movieCd + "|END";
    }
    
    // JOIN_ROOM|영화코드|영화제목|END
    public String joinRoomMSG(String movieCd, String movieNm) {
        return "JOIN_ROOM|" + movieCd + "|" + movieNm + "|END";
    }
    
    // LEAVE_ROOM|영화코드|END
    public String leaveRoomMSG(String movieCd) {
        return "LEAVE_ROOM|" + movieCd + "|END";
    }
    
    // CHAT|영화코드|메시지|END
    public String chatMSG(String movieCd, String message) {
        return "CHAT|" + movieCd + "|" + message + "|END";
    }
    
    // GET_REVIEWS|영화코드|END
    public String getReviewsMSG(String movieCd) {
        return "GET_REVIEWS|" + movieCd + "|END";
    }
    
    // SUBMIT_REVIEW|영화코드|별점|감상평내용|END
    public String submitReviewMSG(String movieCd, int rating, String content) {
        return "SUBMIT_REVIEW|" + movieCd + "|" + rating + "|" + content + "|END";
    }
    
    // DELETE_REVIEW|reviewId|END
    public String deleteReviewMSG(int reviewId) {
        return "DELETE_REVIEW|" + reviewId + "|END";
    }
    
    // SEARCH_MOVIE|검색어|END
    public String searchMovieMSG(String keyword) {
        return "SEARCH_MOVIE|" + keyword + "|END";
    }
    
    // ADD_BOOKMARK|영화코드|END
    public String addBookmarkMSG(String movieCd) {
        return "ADD_BOOKMARK|" + movieCd + "|END";
    }
    
    // DELETE_BOOKMARK|영화코드|END
    public String deleteBookmarkMSG(String movieCd) {
        return "DELETE_BOOKMARK|" + movieCd + "|END";
    }
    
    // GET_BOOKMARKS|END
    public String getBookmarksMSG() {
        return "GET_BOOKMARKS|END";
    }
    
    // DISCONNECT|END
    public String disconnectMSG() {
        return "DISCONNECT|END";
    }
}