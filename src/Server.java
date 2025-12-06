//프로젝트 이름 : MovieSync
//개발자 : 권미리
//개발 기간: 2025.12.01 ~ 2025.12.13

//서버 메인 클래스

import java.io.*;
import java.net.*;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {
    ServerSocket ss = null;
    ArrayList<ConnectedClient> clients = new ArrayList<ConnectedClient>();
    HashMap<String, ConnectedClient> clientMap = new HashMap<String, ConnectedClient>(); // username을 키로 사용
    HashMap<String, ChatRoom> chatRooms = new HashMap<String, ChatRoom>(); // 채팅방 관리 (영화코드가 키)
    
    DatabaseManager dbManager; // DB 관리자
    APIManager apiManager; // API 관리자
    
    public static void main(String[] args) {
        Server server = new Server();
        try {
            // 1. DB 초기화
            server.dbManager = new DatabaseManager();
            System.out.println("Server> 데이터베이스 초기화 완료");
            
            // 2. API Manager 시작 (영화 데이터 자동 갱신)
            server.apiManager = new APIManager(server.dbManager);
            server.apiManager.start();
            System.out.println("Server> API Manager 시작");
            
            // 3. 서버 소켓 생성
            server.ss = new ServerSocket(55555);
            System.out.println("Server> 서버 소켓 생성 완료 (포트: 55555)");
            System.out.println("Server> 클라이언트 연결 대기 중...\n");
            
            // 4. 클라이언트 연결 수락 루프
            while (true) {
                Socket socket = server.ss.accept();
                ConnectedClient c = new ConnectedClient(socket, server);
                server.clients.add(c);
                c.start();
            }
        } catch (SocketException e) {
            System.out.println("Server> 서버 종료");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 서버 종료 시 정리
            if (server.apiManager != null) {
                server.apiManager.stopAPI();
            }
            if (server.dbManager != null) {
                server.dbManager.close();
            }
        }
    }
    
    // 특정 채팅방의 모든 사용자에게 메시지 브로드캐스트
    public void broadcastToRoom(String roomId, String msg) {
        ChatRoom room = chatRooms.get(roomId);
        if (room != null) {
            room.broadcast(msg);
        }
    }
    
    // 특정 사용자에게 메시지 전송
    public boolean sendToUser(String username, String msg) {
        ConnectedClient target = clientMap.get(username);
        if (target != null) {
            target.sendMessage(msg);
            return true;
        }
        return false;
    }
    
    // 클라이언트 제거
    public void removeClient(ConnectedClient client) {
        clients.remove(client);
        if (client.username != null) {
            clientMap.remove(client.username);
            
            // 모든 채팅방에서 제거
            for (ChatRoom room : chatRooms.values()) {
                room.removeUser(client);
            }
        }
    }
    
    // 채팅방 가져오기 (없으면 생성)
    public ChatRoom getOrCreateRoom(String roomId) {
        if (!chatRooms.containsKey(roomId)) {
            chatRooms.put(roomId, new ChatRoom(roomId));
        }
        return chatRooms.get(roomId);
    }
}

// 각 클라이언트를 처리하는 스레드
class ConnectedClient extends Thread {
    Socket socket;
    Server server;
    String username = null;
    int userId = -1;
    
    OutputStream outStream;
    DataOutputStream dataOutStream;
    InputStream inStream;
    DataInputStream dataInStream;
    
    MSGBuilder mb = new MSGBuilder();
    ReceivedMSGTokenizer tk = new ReceivedMSGTokenizer();
    
    ConnectedClient(Socket _s, Server _server) {
        this.socket = _s;
        this.server = _server;
    }
    
    public void run() {
        try {
            System.out.println("Server> " + socket.getRemoteSocketAddress() + " 클라이언트 연결됨");
            
            outStream = socket.getOutputStream();
            dataOutStream = new DataOutputStream(outStream);
            inStream = socket.getInputStream();
            dataInStream = new DataInputStream(inStream);
            
            while (true) {
                String msg = dataInStream.readUTF();
                System.out.println("Server> 수신: " + msg);
                
                int msgType = tk.detection(msg);
                
                switch (msgType) {
                    case 0: // LOGIN
                        handleLogin(msg);
                        break;
                        
                    case 1: // GET_MOVIES
                        handleGetMovies();
                        break;
                        
                    case 2: // GET_DETAIL
                        handleGetDetail(msg);
                        break;
                        
                    case 3: // JOIN_ROOM
                        handleJoinRoom(msg);
                        break;
                        
                    case 4: // LEAVE_ROOM
                        handleLeaveRoom(msg);
                        break;
                        
                    case 5: // CHAT
                        handleChat(msg);
                        break;
                        
                    case 6: // GET_REVIEWS
                        handleGetReviews(msg);
                        break;
                        
                    case 7: // SUBMIT_REVIEW
                        handleSubmitReview(msg);
                        break;
                        
                    case 8: // DELETE_REVIEW
                        handleDeleteReview(msg);
                        break;
                        
                    case 9: // SEARCH_MOVIE
                        handleSearchMovie(msg);
                        break;
                        
                    case 10: // ADD_BOOKMARK
                        handleAddBookmark(msg);
                        break;
                        
                    case 11: // DELETE_BOOKMARK
                        handleDeleteBookmark(msg);
                        break;
                        
                    case 12: // GET_BOOKMARKS
                        handleGetBookmarks();
                        break;
                        
                    case 13: // DISCONNECT
                        handleDisconnect();
                        return;
                        
                    default:
                        System.out.println("Server> 알 수 없는 메시지 타입: " + msgType);
                }
            }
        } catch (IOException e) {
            System.out.println("Server> " + socket.getRemoteSocketAddress() + " 연결 종료");
        } finally {
            server.removeClient(this);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    // LOGIN//닉네임//END
    private void handleLogin(String msg) {
        String username = tk.findUsername(msg);
        
        // 중복 체크
        if (server.dbManager.isUsernameTaken(username)) {
            sendMessage(mb.loginFailMSG("이미 사용 중인 닉네임입니다"));
            return;
        }
        
        // 사용자 생성
        int userId = server.dbManager.createUser(username);
        if (userId > 0) {
            this.username = username;
            this.userId = userId;
            server.clientMap.put(username, this);
            
            sendMessage(mb.loginOkMSG(userId, username));
            sendMessage(mb.welcomeMSG());
            System.out.println("Server> 로그인 성공: " + username + " (ID: " + userId + ")");
        } else {
            sendMessage(mb.loginFailMSG("로그인 처리 중 오류가 발생했습니다"));
        }
    }
    
    // GET_MOVIES//END
    private void handleGetMovies() {
        try {
            ResultSet rs = server.dbManager.getTop10Movies();
            
            // 영화 개수 세기
            int count = 0;
            while (rs.next()) {
                count++;
            }
            
            // ResultSet 다시 처음으로
            rs.close();
            rs = server.dbManager.getTop10Movies();
            
            // 개수 먼저 전송
            sendMessage(mb.moviesCountMSG(count));
            
            // 영화 데이터 전송
            while (rs.next()) {
                String movieCd = rs.getString("movie_cd");
                String movieNm = rs.getString("movie_nm");
                int rank = rs.getInt("rank");
                String openDt = rs.getString("open_dt");
                long audiAcc = rs.getLong("audi_acc");
                long salesAcc = rs.getLong("sales_acc");
                
                sendMessage(mb.movieDataMSG(movieCd, movieNm, rank, openDt, audiAcc, salesAcc));
            }
            
            sendMessage(mb.moviesEndMSG());
            rs.close();
        } catch (Exception e) {
            System.err.println("Server> 영화 목록 조회 실패: " + e.getMessage());
            sendMessage(mb.errorMSG("영화 목록 조회 실패"));
        }
    }
    
    // GET_DETAIL//영화코드//END
    private void handleGetDetail(String msg) {
        String movieCd = tk.findMovieCode(msg);
        
        try {
            ResultSet rs = server.dbManager.getMovieDetail(movieCd);
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
    
    // JOIN_ROOM//영화코드//영화제목//END
    private void handleJoinRoom(String msg) {
        String movieCd = tk.findRoomMovieCode(msg);
        String movieNm = tk.findRoomMovieName(msg);
        
        String roomId = "room_" + movieCd;
        ChatRoom room = server.getOrCreateRoom(roomId);
        room.addUser(this);
        
        sendMessage(mb.roomOkMSG(roomId, movieNm));
        
        // 다른 사용자들에게 입장 알림
        String joinMsg = mb.userJoinMSG(username, room.getUserCount());
        room.broadcast(joinMsg);
        
        System.out.println("Server> " + username + " 채팅방 입장: " + roomId);
    }
    
    // LEAVE_ROOM//영화코드//END
    private void handleLeaveRoom(String msg) {
        String movieCd = tk.findLeaveRoomCode(msg);
        String roomId = "room_" + movieCd;
        
        ChatRoom room = server.chatRooms.get(roomId);
        if (room != null) {
            room.removeUser(this);
            
            // 다른 사용자들에게 퇴장 알림
            String leaveMsg = mb.userLeftMSG(username, room.getUserCount());
            room.broadcast(leaveMsg);
            
            System.out.println("Server> " + username + " 채팅방 퇴장: " + roomId);
        }
    }
    
    // CHAT//영화코드//메시지//END
    private void handleChat(String msg) {
        String movieCd = tk.findChatRoomCode(msg);
        String message = tk.findChatMessage(msg);
        
        String roomId = "room_" + movieCd;
        String chatMsg = mb.chatAllMSG(username, message);
        server.broadcastToRoom(roomId, chatMsg);
        
        System.out.println("Server> [" + roomId + "] " + username + ": " + message);
    }
    
    // GET_REVIEWS//영화코드//END
    private void handleGetReviews(String msg) {
        String movieCd = tk.findReviewMovieCode(msg);
        
        try {
            // 요약 정보 전송
            String movieNm = getMovieName(movieCd);
            double avgRating = server.dbManager.getAverageRating(movieCd);
            int count = server.dbManager.getReviewCount(movieCd);
            
            sendMessage(mb.reviewSummaryMSG(movieCd, movieNm, avgRating, count));
            
            // 개수 전송
            sendMessage(mb.reviewCountMSG(count));
            
            // 감상평 데이터 전송
            ResultSet rs = server.dbManager.getReviews(movieCd);
            while (rs.next()) {
                int reviewId = rs.getInt("review_id");
                String reviewUsername = rs.getString("username");
                int rating = rs.getInt("rating");
                String content = rs.getString("content");
                String createdAt = rs.getString("created_at");
                
                sendMessage(mb.reviewDataMSG(reviewId, reviewUsername, rating, content, createdAt));
            }
            
            sendMessage(mb.reviewEndMSG());
            rs.close();
        } catch (Exception e) {
            System.err.println("Server> 감상평 조회 실패: " + e.getMessage());
            sendMessage(mb.errorMSG("감상평 조회 실패"));
        }
    }
    
    // SUBMIT_REVIEW//영화코드//별점//감상평내용//END
    private void handleSubmitReview(String msg) {
        String movieCd = tk.findSubmitMovieCode(msg);
        int rating = tk.findSubmitRating(msg);
        String content = tk.findSubmitContent(msg);
        
        int reviewId = server.dbManager.submitReview(userId, movieCd, rating, content);
        if (reviewId > 0) {
            sendMessage(mb.reviewOkMSG(reviewId));
            System.out.println("Server> 감상평 작성: " + username + " -> " + movieCd);
        } else {
            sendMessage(mb.reviewFailMSG("감상평 작성 실패"));
        }
    }
    
    // DELETE_REVIEW//reviewId//END
    private void handleDeleteReview(String msg) {
        int reviewId = tk.findDeleteReviewId(msg);
        
        boolean success = server.dbManager.deleteReview(reviewId, userId);
        if (success) {
            sendMessage(mb.deleteOkMSG());
            System.out.println("Server> 감상평 삭제: reviewId=" + reviewId);
        } else {
            sendMessage(mb.deleteFailMSG("삭제 권한이 없거나 존재하지 않는 감상평입니다"));
        }
    }
    
    // SEARCH_MOVIE//검색어//END
    private void handleSearchMovie(String msg) {
        String keyword = tk.findSearchKeyword(msg);
        
        try {
            ResultSet rs = server.dbManager.searchMovies(keyword);
            
            // 개수 세기
            int count = 0;
            while (rs.next()) {
                count++;
            }
            
            rs.close();
            rs = server.dbManager.searchMovies(keyword);
            
            sendMessage(mb.moviesCountMSG(count));
            
            while (rs.next()) {
                String movieCd = rs.getString("movie_cd");
                String movieNm = rs.getString("movie_nm");
                int rank = rs.getInt("rank");
                String openDt = rs.getString("open_dt");
                long audiAcc = rs.getLong("audi_acc");
                long salesAcc = rs.getLong("sales_acc");
                
                sendMessage(mb.movieDataMSG(movieCd, movieNm, rank, openDt, audiAcc, salesAcc));
            }
            
            sendMessage(mb.moviesEndMSG());
            rs.close();
        } catch (Exception e) {
            System.err.println("Server> 영화 검색 실패: " + e.getMessage());
            sendMessage(mb.errorMSG("영화 검색 실패"));
        }
    }
    
    // ADD_BOOKMARK//영화코드//END
    private void handleAddBookmark(String msg) {
        String movieCd = tk.findBookmarkMovieCode(msg);
        
        boolean success = server.dbManager.addBookmark(userId, movieCd);
        if (success) {
            sendMessage(mb.bookmarkOkMSG());
        } else {
            sendMessage(mb.errorMSG("북마크 추가 실패 (중복 가능)"));
        }
    }
    
    // DELETE_BOOKMARK//영화코드//END
    private void handleDeleteBookmark(String msg) {
        String movieCd = tk.findDeleteBookmarkCode(msg);
        
        boolean success = server.dbManager.deleteBookmark(userId, movieCd);
        if (success) {
            sendMessage(mb.bookmarkDelOkMSG());
        } else {
            sendMessage(mb.errorMSG("북마크 삭제 실패"));
        }
    }
    
    // GET_BOOKMARKS//END
    private void handleGetBookmarks() {
        try {
            ResultSet rs = server.dbManager.getBookmarks(userId);
            
            int count = 0;
            while (rs.next()) {
                count++;
            }
            
            rs.close();
            rs = server.dbManager.getBookmarks(userId);
            
            sendMessage(mb.moviesCountMSG(count));
            
            while (rs.next()) {
                String movieCd = rs.getString("movie_cd");
                String movieNm = rs.getString("movie_nm");
                int rank = rs.getInt("rank");
                String openDt = rs.getString("open_dt");
                long audiAcc = rs.getLong("audi_acc");
                long salesAcc = rs.getLong("sales_acc");
                
                sendMessage(mb.movieDataMSG(movieCd, movieNm, rank, openDt, audiAcc, salesAcc));
            }
            
            sendMessage(mb.moviesEndMSG());
            rs.close();
        } catch (Exception e) {
            System.err.println("Server> 북마크 조회 실패: " + e.getMessage());
            sendMessage(mb.errorMSG("북마크 조회 실패"));
        }
    }
    
    // DISCONNECT//END
    private void handleDisconnect() {
        sendMessage(mb.disconnectOkMSG());
        System.out.println("Server> " + username + " 연결 종료 요청");
    }
    
    // 메시지 전송
    public void sendMessage(String msg) {
        try {
            dataOutStream.writeUTF(msg);
        } catch (IOException e) {
            System.err.println("Server> 메시지 전송 실패");
        }
    }
    
    // 영화 이름 가져오기 (헬퍼 메소드)
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
        return "영화";
    }
}

// 채팅방 클래스
class ChatRoom {
    String roomId;
    ArrayList<ConnectedClient> users = new ArrayList<ConnectedClient>();
    
    ChatRoom(String roomId) {
        this.roomId = roomId;
    }
    
    public void addUser(ConnectedClient user) {
        if (!users.contains(user)) {
            users.add(user);
        }
    }
    
    public void removeUser(ConnectedClient user) {
        users.remove(user);
    }
    
    public void broadcast(String msg) {
        for (ConnectedClient user : users) {
            user.sendMessage(msg);
        }
    }
    
    public int getUserCount() {
        return users.size();
    }
}