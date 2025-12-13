//프로젝트 이름 : MovieSync
//개발자 : 권미리
//개발 기간: 2025.12.01 ~ 2025.12.13

import java.io.*;
import java.net.*;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

//MovieSync 서버 메인 클래스 - TCP 소켓으로 클라이언트 연결 처리 및 메시지 중계
public class Server {
	ServerSocket ss = null; // 서버 소켓 (포트 55555)

	// 연결된 클라이언트 관리
	ArrayList<ConnectedClient> clients = new ArrayList<ConnectedClient>(); // 연결된 클라이언트 목록
	HashMap<String, ConnectedClient> clientMap = new HashMap<String, ConnectedClient>(); // 닉네임으로 클라이언트 검색용
	HashMap<String, ChatRoom> chatRooms = new HashMap<String, ChatRoom>(); // 채팅방 목록 (키: 영화코드)

	DatabaseManager dbManager; // 데이터베이스 관리자
	APIManager apiManager; // 영화진흥위원회 API 관리자

	// 서버 메인 메소드 - DB 초기화 -> API 시작 -> 클라이언트 연결 대기
	public static void main(String[] args) {
		Server server = new Server();
		try {
			// 1. DB 초기화
			server.dbManager = new DatabaseManager();
			System.out.println("Server> 데이터베이스 초기화 완료");

			// 2. API Manager 시작 - 영화 데이터 자동 갱신
			server.apiManager = new APIManager(server.dbManager);
			server.apiManager.start(); // 별도 스레드에서 실행
			System.out.println("Server> API Manager 시작");

			// 3. 서버 소켓 생성 - 포트 55555
			server.ss = new ServerSocket(55555);
			System.out.println("Server> 서버 소켓 생성 완료 (포트: 55555)");
			System.out.println("Server> 클라이언트 연결 대기 중...\n");

			// 4. 클라이언트 연결 수락 루프 - 무한 루프
			while (true) {
				Socket socket = server.ss.accept(); // 클라이언트 연결 대기 (블로킹)
				ConnectedClient c = new ConnectedClient(socket, server); // 클라이언트 스레드 생성
				server.clients.add(c); // 목록에 추가
				c.start(); // 스레드 시작
			}
		} catch (SocketException e) {
			System.out.println("Server> 서버 종료");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 서버 종료 시 정리 작업
			if (server.apiManager != null) {
				server.apiManager.stopAPI(); // API 스레드 종료
			}
			if (server.dbManager != null) {
				server.dbManager.close(); // DB 연결 종료
			}
		}
	}

	// 특정 채팅방의 모든 사용자에게 메시지 브로드캐스트
	public void broadcastToRoom(String roomId, String msg) {
		ChatRoom room = chatRooms.get(roomId); // 채팅방 가져오기
		if (room != null) {
			room.broadcast(msg); // 채팅방 내 모든 사용자에게 전송
		}
	}

	// 특정 사용자에게 메시지 전송 - 닉네임으로 검색
	public boolean sendToUser(String username, String msg) {
		ConnectedClient target = clientMap.get(username); // 닉네임으로 클라이언트 찾기
		if (target != null) {
			target.sendMessage(msg); // 메시지 전송
			return true;
		}
		return false; // 찾지 못함
	}

	// 클라이언트 제거 - 연결 종료 시 호출
	public void removeClient(ConnectedClient client) {
		clients.remove(client); // 목록에서 제거
		if (client.username != null) {
			clientMap.remove(client.username); // 닉네임 맵에서 제거

			// 모든 채팅방에서 제거
			for (ChatRoom room : chatRooms.values()) {
				room.removeUser(client);
			}
		}
	}

	// 채팅방 가져오기 (없으면 생성)
	public ChatRoom getOrCreateRoom(String roomId) {
		if (!chatRooms.containsKey(roomId)) { // 채팅방이 없으면
			chatRooms.put(roomId, new ChatRoom(roomId)); // 새로 생성
		}
		return chatRooms.get(roomId);
	}
}

//각 클라이언트를 처리하는 스레드 - 별도 스레드에서 클라이언트 메시지 수신
class ConnectedClient extends Thread {
	Socket socket; // 클라이언트 소켓
	Server server; // 서버 참조
	String username = null; // 사용자 닉네임
	int userId = -1; // 사용자 ID (DB)

	OutputStream outStream; // 출력 스트림
	DataOutputStream dataOutStream; // 데이터 출력 스트림
	InputStream inStream; // 입력 스트림
	DataInputStream dataInStream; // 데이터 입력 스트림

	MSGBuilder mb = new MSGBuilder(); // 서버 메시지 빌더
	ReceivedMSGTokenizer tk = new ReceivedMSGTokenizer(); // 메시지 파싱용

	// 생성자
	ConnectedClient(Socket _s, Server _server) {
		this.socket = _s;
		this.server = _server;
	}

	// 스레드 실행 - 클라이언트 메시지 수신 루프
	public void run() {
		try {
			System.out.println("Server> " + socket.getRemoteSocketAddress() + " 클라이언트 연결됨");

			outStream = socket.getOutputStream(); // 출력 스트림 가져오기
			dataOutStream = new DataOutputStream(outStream);
			inStream = socket.getInputStream(); // 입력 스트림 가져오기
			dataInStream = new DataInputStream(inStream);

			while (true) { // 메시지 수신 루프 - 무한 루프
				String msg = dataInStream.readUTF(); // 메시지 수신 (블로킹)
				System.out.println("Server> 수신: " + msg);

				int msgType = tk.detection(msg); // 메시지 타입 감지 (0~13)

				switch (msgType) { // 메시지 타입별 처리
				case 0: // LOGIN|닉네임|END
					handleLogin(msg);
					break;

				case 1: // GET_MOVIES|END
					handleGetMovies();
					break;

				case 2: // GET_DETAIL|영화코드|END
					handleGetDetail(msg);
					break;

				case 3: // JOIN_ROOM|영화코드|영화제목|END
					handleJoinRoom(msg);
					break;

				case 4: // LEAVE_ROOM|영화코드|END
					handleLeaveRoom(msg);
					break;

				case 5: // CHAT|영화코드|메시지|END
					handleChat(msg);
					break;

				case 6: // GET_REVIEWS|영화코드|END
					handleGetReviews(msg);
					break;

				case 7: // GET_ALL_REVIEWS|END
					handleGetAllReviews();
					break;

				case 8: // SUBMIT_REVIEW|영화코드|별점|감상평내용|END
					handleSubmitReview(msg);
					break;

				case 9: // DELETE_REVIEW|reviewId|END
					handleDeleteReview(msg);
					break;

				case 10: // SEARCH_MOVIE|검색어|END
					handleSearchMovie(msg);
					break;

				case 11: // ADD_BOOKMARK|영화코드|END
					handleAddBookmark(msg);
					break;

				case 12: // DELETE_BOOKMARK|영화코드|END
					handleDeleteBookmark(msg);
					break;

				case 13: // GET_BOOKMARKS|END
					handleGetBookmarks();
					break;

				case 14: // DISCONNECT|END
					handleDisconnect();
					return; // 스레드 종료

				default:
					System.out.println("Server> 알 수 없는 메시지 타입: " + msgType);
				}
			}
		} catch (IOException e) {
			System.out.println("Server> " + socket.getRemoteSocketAddress() + " 연결 종료");
		} finally {
			server.removeClient(this); // 서버에서 클라이언트 제거
			try {
				socket.close(); // 소켓 닫기
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// LOGIN|닉네임|END - 로그인 처리
	private void handleLogin(String msg) {
		String username = tk.findUsername(msg); // 닉네임 추출

		// 현재 접속 중인 사용자인지 확인 - 동시 접속 방지
		if (server.clientMap.containsKey(username)) {
			sendMessage(mb.loginFailMSG("이미 접속 중인 닉네임입니다"));
			return;
		}

		// 사용자 생성 또는 기존 사용자 조회
		int userId = server.dbManager.createUser(username);
		if (userId > 0) { // 로그인 성공
			this.username = username; // 닉네임 저장
			this.userId = userId; // 사용자 ID 저장
			server.clientMap.put(username, this); // 맵에 추가

			sendMessage(mb.loginOkMSG(userId, username)); // 로그인 성공 메시지
			sendMessage(mb.welcomeMSG()); // 환영 메시지
			System.out.println("Server> 로그인 성공: " + username + " (ID: " + userId + ")");
		} else { // 로그인 실패
			sendMessage(mb.loginFailMSG("로그인 처리 중 오류가 발생했습니다"));
		}
	}

	// GET_MOVIES|END - 영화 목록 조회
	private void handleGetMovies() {
		try {
			ResultSet rs = server.dbManager.getTop10Movies(); // Top 10 조회

			// 영화 개수 세기
			int count = 0;
			while (rs.next()) {
				count++;
			}

			// ResultSet 다시 처음으로
			rs.close();
			rs = server.dbManager.getTop10Movies();

			// 개수 먼저 전송 - MOVIES_COUNT|개수|END
			sendMessage(mb.moviesCountMSG(count));

			// 영화 데이터 전송 - MOVIES_DATA|영화코드|영화제목|순위|개봉일|누적관객|누적매출|END
			while (rs.next()) {
				String movieCd = rs.getString("movie_cd");
				String movieNm = rs.getString("movie_nm");
				int rank = rs.getInt("rank");
				String openDt = rs.getString("open_dt");
				long audiAcc = rs.getLong("audi_acc");
				long salesAcc = rs.getLong("sales_acc");

				sendMessage(mb.movieDataMSG(movieCd, movieNm, rank, openDt, audiAcc, salesAcc));
			}

			sendMessage(mb.moviesEndMSG()); // 전송 완료 - MOVIES_END|END
			rs.close();
		} catch (Exception e) {
			System.err.println("Server> 영화 목록 조회 실패: " + e.getMessage());
			sendMessage(mb.errorMSG("영화 목록 조회 실패"));
		}
	}

	// GET_DETAIL|영화코드|END - 영화 상세 정보 조회
	private void handleGetDetail(String msg) {
		String movieCd = tk.findMovieCode(msg); // 영화코드 추출

		try {
			ResultSet rs = server.dbManager.getMovieDetail(movieCd); // 영화 상세 조회
			if (rs.next()) {
				String movieNm = rs.getString("movie_nm");
				int rank = rs.getInt("rank");
				String openDt = rs.getString("open_dt");
				long audiAcc = rs.getLong("audi_acc");
				long salesAcc = rs.getLong("sales_acc");

				// 평균 평점 계산
				double avgRating = server.dbManager.getAverageRating(movieCd);

				sendMessage(mb.movieDetailMSG(movieCd, movieNm, rank, openDt, audiAcc, salesAcc, avgRating));
			} else {
				sendMessage(mb.errorMSG("영화를 찾을 수 없습니다"));
			}
			rs.close();
		} catch (Exception e) {
			System.err.println("Server> 영화 상세 조회 실패: " + e.getMessage());
			sendMessage(mb.errorMSG("영화 상세 조회 실패"));
		}
	}

	// JOIN_ROOM|영화코드|영화제목|END - 채팅방 입장
	private void handleJoinRoom(String msg) {
		String movieCd = tk.findRoomMovieCode(msg); // 영화코드 추출
		String movieNm = tk.findRoomMovieName(msg); // 영화제목 추출

		String roomId = "room_" + movieCd; // 채팅방 ID 생성
		ChatRoom room = server.getOrCreateRoom(roomId); // 채팅방 가져오기 (없으면 생성)
		room.addUser(this); // 채팅방에 사용자 추가

		sendMessage(mb.roomOkMSG(roomId, movieNm)); // 입장 성공 메시지

		// 다른 사용자들에게 입장 알림 - USER_JOIN|닉네임|현재인원|END
		String joinMsg = mb.userJoinMSG(username, room.getUserCount());
		room.broadcast(joinMsg); // 채팅방 전체에 브로드캐스트

		System.out.println("Server> " + username + " 채팅방 입장: " + roomId);
	}

	// LEAVE_ROOM|영화코드|END - 채팅방 퇴장
	private void handleLeaveRoom(String msg) {
		String movieCd = tk.findLeaveRoomCode(msg); // 영화코드 추출
		String roomId = "room_" + movieCd; // 채팅방 ID

		ChatRoom room = server.chatRooms.get(roomId); // 채팅방 가져오기
		if (room != null) {
			room.removeUser(this); // 채팅방에서 사용자 제거

			// 다른 사용자들에게 퇴장 알림 - USER_LEFT|닉네임|현재인원|END
			String leaveMsg = mb.userLeftMSG(username, room.getUserCount());
			room.broadcast(leaveMsg);

			System.out.println("Server> " + username + " 채팅방 퇴장: " + roomId);
		}
	}

	// CHAT|영화코드|메시지|END - 채팅 메시지 처리
	private void handleChat(String msg) {
		String movieCd = tk.findChatRoomCode(msg); // 영화코드 추출
		String message = tk.findChatMessage(msg); // 메시지 추출

		String roomId = "room_" + movieCd; // 채팅방 ID
		String chatMsg = mb.chatAllMSG(username, message); // CHAT_ALL|발신자|메시지|END
		server.broadcastToRoom(roomId, chatMsg); // 채팅방 전체에 브로드캐스트

		System.out.println("Server> [" + roomId + "] " + username + ": " + message);
	}

	// GET_REVIEWS|영화코드|END - 감상평 조회
	private void handleGetReviews(String msg) {
		String movieCd = tk.findReviewMovieCode(msg); // 영화코드 추출

		try {
			// 요약 정보 전송 - REVIEW_SUMMARY|영화코드|영화제목|평균평점|개수|END
			String movieNm = getMovieName(movieCd); // 영화 이름 가져오기
			double avgRating = server.dbManager.getAverageRating(movieCd); // 평균 평점
			int count = server.dbManager.getReviewCount(movieCd); // 감상평 개수

			sendMessage(mb.reviewSummaryMSG(movieCd, movieNm, avgRating, count));

			// 개수 전송 - REVIEW_COUNT|개수|END
			sendMessage(mb.reviewCountMSG(count));

			// 감상평 데이터 전송 - REVIEW_DATA|reviewId|작성자|별점|내용|작성일|END
			ResultSet rs = server.dbManager.getReviews(movieCd); // 감상평 조회
			while (rs.next()) {
				int reviewId = rs.getInt("review_id");
				String reviewUsername = rs.getString("username");
				int rating = rs.getInt("rating");
				String content = rs.getString("content");
				String createdAt = rs.getString("created_at");

				sendMessage(mb.reviewDataMSG(reviewId, reviewUsername, rating, content, createdAt));
			}

			sendMessage(mb.reviewEndMSG()); // 전송 완료 - REVIEW_END|END
			rs.close();
		} catch (Exception e) {
			System.err.println("Server> 감상평 조회 실패: " + e.getMessage());
			sendMessage(mb.errorMSG("감상평 조회 실패"));
		}
	}

	// GET_ALL_REVIEWS|END - 전체 감상평 조회
	private void handleGetAllReviews() {
		try {
			ResultSet rs = server.dbManager.getAllReviews(); // 전체 감상평 조회 (영화명 포함 JOIN)

			while (rs.next()) {
				int reviewId = rs.getInt("review_id");
				String movieNm = rs.getString("movie_nm"); // 영화 제목
				String reviewUsername = rs.getString("username"); // 작성자
				int rating = rs.getInt("rating");
				String content = rs.getString("content");
				String createdAt = rs.getString("created_at");

				sendMessage(mb.allReviewDataMSG(reviewId, movieNm, reviewUsername, rating, content, createdAt));
			}

			sendMessage(mb.allReviewEndMSG()); // 전송 완료 - ALL_REV_END|END
			rs.close();
		} catch (Exception e) {
			System.err.println("Server> 전체 감상평 조회 실패: " + e.getMessage());
			sendMessage(mb.errorMSG("전체 감상평 조회 실패"));
		}
	}

	// SUBMIT_REVIEW|영화코드|별점|감상평내용|END - 감상평 작성
	private void handleSubmitReview(String msg) {
		String movieCd = tk.findSubmitMovieCode(msg); // 영화코드 추출
		int rating = tk.findSubmitRating(msg); // 별점 추출
		String content = tk.findSubmitContent(msg); // 감상평 내용 추출

		int reviewId = server.dbManager.submitReview(userId, movieCd, rating, content); // DB 저장
		if (reviewId > 0) { // 작성 성공
			sendMessage(mb.reviewOkMSG(reviewId)); // REVIEW_OK|reviewId|END
			System.out.println("Server> 감상평 작성: " + username + " -> " + movieCd);
		} else { // 작성 실패
			sendMessage(mb.reviewFailMSG("감상평 작성 실패"));
		}
	}

	// DELETE_REVIEW|reviewId|END - 감상평 삭제
	private void handleDeleteReview(String msg) {
		int reviewId = tk.findDeleteReviewId(msg); // reviewId 추출

		boolean success = server.dbManager.deleteReview(reviewId, userId); // DB 삭제 (권한 검증 포함)
		if (success) { // 삭제 성공
			sendMessage(mb.deleteOkMSG()); // DELETE_OK|END
			System.out.println("Server> 감상평 삭제: reviewId=" + reviewId);
		} else { // 삭제 실패
			sendMessage(mb.deleteFailMSG("삭제 권한이 없거나 존재하지 않는 감상평입니다"));
		}
	}

	// SEARCH_MOVIE|검색어|END - 영화 검색
	private void handleSearchMovie(String msg) {
		String keyword = tk.findSearchKeyword(msg); // 검색어 추출

		try {
			ResultSet rs = server.dbManager.searchMovies(keyword); // 영화 검색 (LIKE)

			// 개수 세기
			int count = 0;
			while (rs.next()) {
				count++;
			}

			rs.close();
			rs = server.dbManager.searchMovies(keyword);

			sendMessage(mb.moviesCountMSG(count)); // MOVIES_COUNT|개수|END

			// 검색 결과 전송 - MOVIES_DATA 형식
			while (rs.next()) {
				String movieCd = rs.getString("movie_cd");
				String movieNm = rs.getString("movie_nm");
				int rank = rs.getInt("rank");
				String openDt = rs.getString("open_dt");
				long audiAcc = rs.getLong("audi_acc");
				long salesAcc = rs.getLong("sales_acc");

				sendMessage(mb.movieDataMSG(movieCd, movieNm, rank, openDt, audiAcc, salesAcc));
			}

			sendMessage(mb.moviesEndMSG()); // MOVIES_END|END
			rs.close();
		} catch (Exception e) {
			System.err.println("Server> 영화 검색 실패: " + e.getMessage());
			sendMessage(mb.errorMSG("영화 검색 실패"));
		}
	}

	// ADD_BOOKMARK|영화코드|END - 북마크 추가
	private void handleAddBookmark(String msg) {
		String movieCd = tk.findBookmarkMovieCode(msg); // 영화코드 추출

		boolean success = server.dbManager.addBookmark(userId, movieCd); // DB 저장
		if (success) { // 추가 성공
			sendMessage(mb.bookmarkOkMSG()); // BOOKMARK_OK|END
		} else { // 추가 실패 (중복)
			sendMessage(mb.errorMSG("북마크 추가 실패 (중복 가능)"));
		}
	}

	// DELETE_BOOKMARK|영화코드|END - 북마크 삭제
	private void handleDeleteBookmark(String msg) {
		String movieCd = tk.findDeleteBookmarkCode(msg); // 영화코드 추출

		boolean success = server.dbManager.deleteBookmark(userId, movieCd); // DB 삭제
		if (success) { // 삭제 성공
			sendMessage(mb.bookmarkDelOkMSG()); // BOOKMARK_DEL_OK|END
		} else { // 삭제 실패
			sendMessage(mb.errorMSG("북마크 삭제 실패"));
		}
	}

	// GET_BOOKMARKS|END - 북마크 목록 조회
	private void handleGetBookmarks() {
		try {
			ResultSet rs = server.dbManager.getBookmarks(userId); // 사용자 북마크 조회

			// 개수 세기
			int count = 0;
			while (rs.next()) {
				count++;
			}

			rs.close();
			rs = server.dbManager.getBookmarks(userId);

			sendMessage(mb.moviesCountMSG(count)); // MOVIES_COUNT|개수|END

			// 북마크 데이터 전송 - MOVIES_DATA 형식
			while (rs.next()) {
				String movieCd = rs.getString("movie_cd");
				String movieNm = rs.getString("movie_nm");
				int rank = rs.getInt("rank");
				String openDt = rs.getString("open_dt");
				long audiAcc = rs.getLong("audi_acc");
				long salesAcc = rs.getLong("sales_acc");

				sendMessage(mb.movieDataMSG(movieCd, movieNm, rank, openDt, audiAcc, salesAcc));
			}

			sendMessage(mb.moviesEndMSG()); // MOVIES_END|END
			rs.close();
		} catch (Exception e) {
			System.err.println("Server> 북마크 조회 실패: " + e.getMessage());
			sendMessage(mb.errorMSG("북마크 조회 실패"));
		}
	}

	// DISCONNECT|END - 연결 종료 요청
	private void handleDisconnect() {
		sendMessage(mb.disconnectOkMSG()); // DISCONNECT_OK|END
		System.out.println("Server> " + username + " 연결 종료 요청");
	}

	// 메시지 전송
	public void sendMessage(String msg) {
		try {
			dataOutStream.writeUTF(msg); // UTF 형식으로 전송
		} catch (IOException e) {
			System.err.println("Server> 메시지 전송 실패");
		}
	}

	// 영화 이름 가져오기 - 헬퍼 메소드
	private String getMovieName(String movieCd) {
		try {
			ResultSet rs = server.dbManager.getMovieDetail(movieCd);
			if (rs.next()) {
				String name = rs.getString("movie_nm");
				rs.close();
				return name;
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "영화"; // 기본값
	}
}

//채팅방 클래스 - 특정 영화의 채팅방 관리
class ChatRoom {
	String roomId; // 채팅방 ID (room_영화코드)
	ArrayList<ConnectedClient> users = new ArrayList<ConnectedClient>(); // 채팅방 사용자 목록

	// 생성자
	ChatRoom(String roomId) {
		this.roomId = roomId;
	}

	// 사용자 추가 - 중복 체크
	public void addUser(ConnectedClient user) {
		if (!users.contains(user)) { // 중복 방지
			users.add(user);
		}
	}

	// 사용자 제거
	public void removeUser(ConnectedClient user) {
		users.remove(user);
	}

	// 채팅방 전체에 메시지 브로드캐스트
	public void broadcast(String msg) {
		for (ConnectedClient user : users) { // 모든 사용자에게
			user.sendMessage(msg); // 메시지 전송
		}
	}

	// 현재 채팅방 인원 수
	public int getUserCount() {
		return users.size();
	}
}