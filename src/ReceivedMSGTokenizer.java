//프로젝트 이름 : MovieSync
//개발자 : 권미리
//개발 기간: 2025.12.01 ~ 2025.12.13

import java.util.StringTokenizer;

//클라이언트에게 받은 메시지를 파싱하는 클래스 - StringTokenizer 사용, 구분자: |
public class ReceivedMSGTokenizer {
	StringTokenizer st; // 메시지 분리용 토크나이저
	MSGTable mt = new MSGTable(); // 메시지 태그 테이블
	private static final String DELIMITER = "|"; // 구분자 - 파이프

	// 메시지 타입을 감지하여 번호로 반환 (0~13) - MSGTable의 인덱스 반환
	public int detection(String _msg) {
		int result = -1; // 찾지 못하면 -1
		String tag; // 메시지 태그
		st = new StringTokenizer(_msg, DELIMITER); // | 로 분리
		tag = st.nextToken(); // 첫 번째 토큰 (메시지 태그)
		for (int i = 0; i < mt.numberOfMSG; i++) { // 모든 메시지 태그 순회
			if (tag.equals(mt.MSGtags[i])) { // 일치하는 태그 찾으면
				result = i; // 인덱스 저장
				break;
			}
		}
		return result; // 메시지 타입 번호 반환
	}

	// LOGIN|닉네임|END : 닉네임 추출
	String findUsername(String _msg) {
		st = new StringTokenizer(_msg, DELIMITER);
		st.nextToken(); // 태그 부분 (LOGIN)
		return st.nextToken(); // 닉네임 반환
	}

	// GET_DETAIL|영화코드|END : 영화코드 추출
	String findMovieCode(String _msg) {
		st = new StringTokenizer(_msg, DELIMITER);
		st.nextToken(); // 태그 (GET_DETAIL)
		return st.nextToken(); // 영화코드 반환
	}

	// JOIN_ROOM|영화코드|영화제목|END : 영화코드 추출
	String findRoomMovieCode(String _msg) {
		st = new StringTokenizer(_msg, DELIMITER);
		st.nextToken(); // 태그 (JOIN_ROOM)
		return st.nextToken(); // 영화코드 반환
	}

	// JOIN_ROOM|영화코드|영화제목|END : 영화제목 추출
	String findRoomMovieName(String _msg) {
		st = new StringTokenizer(_msg, DELIMITER);
		st.nextToken(); // 태그 (JOIN_ROOM)
		st.nextToken(); // 영화코드 (건너뛰기)
		return st.nextToken(); // 영화제목 반환
	}

	// LEAVE_ROOM|영화코드|END : 영화코드 추출
	String findLeaveRoomCode(String _msg) {
		st = new StringTokenizer(_msg, DELIMITER);
		st.nextToken(); // 태그 (LEAVE_ROOM)
		return st.nextToken(); // 영화코드 반환
	}

	// CHAT|영화코드|메시지|END : 영화코드 추출
	String findChatRoomCode(String _msg) {
		st = new StringTokenizer(_msg, DELIMITER);
		st.nextToken(); // 태그 (CHAT)
		return st.nextToken(); // 영화코드 반환
	}

	// CHAT|영화코드|메시지|END : 메시지 추출
	String findChatMessage(String _msg) {
		st = new StringTokenizer(_msg, DELIMITER);
		st.nextToken(); // 태그 (CHAT)
		st.nextToken(); // 영화코드 (건너뛰기)
		return st.nextToken(); // 메시지 반환
	}

	// GET_REVIEWS|영화코드|END : 영화코드 추출
	String findReviewMovieCode(String _msg) {
		st = new StringTokenizer(_msg, DELIMITER);
		st.nextToken(); // 태그 (GET_REVIEWS)
		return st.nextToken(); // 영화코드 반환
	}

	// SUBMIT_REVIEW|영화코드|별점|감상평내용|END : 영화코드 추출
	String findSubmitMovieCode(String _msg) {
		st = new StringTokenizer(_msg, DELIMITER);
		st.nextToken(); // 태그 (SUBMIT_REVIEW)
		return st.nextToken(); // 영화코드 반환
	}

	// SUBMIT_REVIEW|영화코드|별점|감상평내용|END : 별점 추출
	int findSubmitRating(String _msg) {
		st = new StringTokenizer(_msg, DELIMITER);
		st.nextToken(); // 태그 (SUBMIT_REVIEW)
		st.nextToken(); // 영화코드 (건너뛰기)
		return Integer.parseInt(st.nextToken()); // 별점 반환 (int 타입)
	}

	// SUBMIT_REVIEW|영화코드|별점|감상평내용|END : 감상평내용 추출
	String findSubmitContent(String _msg) {
		st = new StringTokenizer(_msg, DELIMITER);
		st.nextToken(); // 태그 (SUBMIT_REVIEW)
		st.nextToken(); // 영화코드 (건너뛰기)
		st.nextToken(); // 별점 (건너뛰기)
		return st.nextToken(); // 감상평내용 반환
	}

	// DELETE_REVIEW|reviewId|END : reviewId 추출
	int findDeleteReviewId(String _msg) {
		st = new StringTokenizer(_msg, DELIMITER);
		st.nextToken(); // 태그 (DELETE_REVIEW)
		return Integer.parseInt(st.nextToken()); // reviewId 반환 (int 타입)
	}

	// SEARCH_MOVIE|검색어|END : 검색어 추출
	String findSearchKeyword(String _msg) {
		st = new StringTokenizer(_msg, DELIMITER);
		st.nextToken(); // 태그 (SEARCH_MOVIE)
		return st.nextToken(); // 검색어 반환
	}

	// ADD_BOOKMARK|영화코드|END : 영화코드 추출
	String findBookmarkMovieCode(String _msg) {
		st = new StringTokenizer(_msg, DELIMITER);
		st.nextToken(); // 태그 (ADD_BOOKMARK)
		return st.nextToken(); // 영화코드 반환
	}

	// DELETE_BOOKMARK|영화코드|END : 영화코드 추출
	String findDeleteBookmarkCode(String _msg) {
		st = new StringTokenizer(_msg, DELIMITER);
		st.nextToken(); // 태그 (DELETE_BOOKMARK)
		return st.nextToken(); // 영화코드 반환
	}
}