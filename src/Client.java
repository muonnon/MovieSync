//프로젝트 이름 : MovieSync
//개발자 : 권미리
//개발 기간: 2025.12.01 ~ 2025.12.13
// 클라이언트 메인 클래스 - 서버와 TCP 소켓으로 통신하는 클라이언트

import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * 서버와 통신하는 클라이언트 클래스
 * GUI와 콘솔 모드 모두 지원한다
 */
public class Client {
    // 소켓 통신 관련
    Socket mySocket = null;              // 서버 연결 소켓
    String myUsername = null;            // 내 닉네임
    int myUserId = -1;                   // 내 사용자 ID
    
    // 데이터 송수신 관련
    DataOutputStream dataOutStream = null;  // 데이터 출력 스트림
    MessageListener msgListener = null;     // 메시지 수신 스레드
    CMSGBuilder cmb = new CMSGBuilder();    // 클라이언트 메시지 빌더
    
    // GUI 연동용 콜백 (package-private으로 MessageListener에서 접근 가능)
    MessageCallback callback;
    
    /**
     * GUI에서 서버 메시지를 받기 위한 콜백 인터페이스
     */
    public interface MessageCallback {
        void onMessageReceived(String message);
    }
    
    /**
     * GUI 콜백 설정 - 서버로부터 메시지를 받으면 콜백 호출
     * @param callback 메시지 수신 시 호출되는 콜백
     */
    public void setMessageCallback(MessageCallback callback) {
        this.callback = callback;
        if (msgListener != null) {
            msgListener.setCallback(callback);
        }
    }
    
    // ========== GUI용 메소드들 ==========
    
    /**
     * 서버에 연결하고 로그인 메시지를 전송한다
     * @param username 사용할 닉네임
     * @return 연결 성공 여부
     */
    public boolean connectToServer(String username) {
        try {
            mySocket = new Socket("localhost", 55555);
            myUsername = username;
            
            OutputStream outStream = mySocket.getOutputStream();
            dataOutStream = new DataOutputStream(outStream);
            
            // 로그인 메시지 전송
            dataOutStream.writeUTF(cmb.loginMSG(username));
            
            // 메시지 수신 스레드 시작
            msgListener = new MessageListener(mySocket, this);
            msgListener.start();
            
            return true;
        } catch (IOException e) {
            System.err.println("Client> 서버 연결 실패: " + e.getMessage());
            return false;
        }
    }
    
    // 영화 목록 조회
    public void requestMovies() {
        try {
            dataOutStream.writeUTF(cmb.getMoviesMSG());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // 영화 상세 정보 조회
    public void requestMovieDetail(String movieCd) {
        try {
            dataOutStream.writeUTF(cmb.getDetailMSG(movieCd));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // 채팅방 입장
    public void joinRoom(String movieCd, String movieNm) {
        try {
            dataOutStream.writeUTF(cmb.joinRoomMSG(movieCd, movieNm));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // 채팅방 퇴장
    public void leaveRoom(String movieCd) {
        try {
            dataOutStream.writeUTF(cmb.leaveRoomMSG(movieCd));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // 채팅 메시지 전송
    public void sendChat(String movieCd, String message) {
        try {
            dataOutStream.writeUTF(cmb.chatMSG(movieCd, message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // 감상평 조회
    public void requestReviews(String movieCd) {
        try {
            dataOutStream.writeUTF(cmb.getReviewsMSG(movieCd));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // 감상평 작성
    public void submitReview(String movieCd, int rating, String content) {
        try {
            dataOutStream.writeUTF(cmb.submitReviewMSG(movieCd, rating, content));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // 감상평 삭제
    public void deleteReview(int reviewId) {
        try {
            dataOutStream.writeUTF(cmb.deleteReviewMSG(reviewId));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // 영화 검색
    public void searchMovie(String keyword) {
        try {
            dataOutStream.writeUTF(cmb.searchMovieMSG(keyword));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // 북마크 추가
    public void addBookmark(String movieCd) {
        try {
            dataOutStream.writeUTF(cmb.addBookmarkMSG(movieCd));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // 북마크 삭제
    public void deleteBookmark(String movieCd) {
        try {
            dataOutStream.writeUTF(cmb.deleteBookmarkMSG(movieCd));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // 북마크 목록 조회
    public void requestBookmarks() {
        try {
            dataOutStream.writeUTF(cmb.getBookmarksMSG());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // 연결 종료
    public void disconnect() {
        try {
            if (dataOutStream != null) {
                dataOutStream.writeUTF(cmb.disconnectMSG());
            }
            if (mySocket != null) {
                mySocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // ========== 콘솔 모드 메인 (테스트용) ==========
    
    public static void main(String[] args) {
        Client client = new Client();
        Scanner scn = new Scanner(System.in);
        
        try {
            // 서버 연결
            System.out.print("Client> 닉네임을 입력하세요: ");
            String username = scn.nextLine();
            
            if (!client.connectToServer(username)) {
                System.out.println("Client> 서버 연결 실패");
                return;
            }
            
            System.out.println("Client> 서버에 연결되었습니다.");
            Thread.sleep(500); // 서버 응답 대기
            
            // 메인 메뉴
            while (true) {
                showMenu();
                System.out.print("선택> ");
                String choice = scn.nextLine();
                System.out.println();
                
                switch (choice) {
                    case "1": // 영화 목록 조회
                        client.requestMovies();
                        break;
                        
                    case "2": // 영화 상세 정보
                        System.out.print("영화 코드 입력> ");
                        String movieCd = scn.nextLine();
                        client.requestMovieDetail(movieCd);
                        break;
                        
                    case "3": // 채팅방 입장
                        System.out.print("영화 코드 입력> ");
                        String joinMovieCd = scn.nextLine();
                        System.out.print("영화 제목 입력> ");
                        String movieNm = scn.nextLine();
                        client.joinRoom(joinMovieCd, movieNm);
                        break;
                        
                    case "4": // 채팅 메시지 전송
                        System.out.print("영화 코드 입력> ");
                        String chatMovieCd = scn.nextLine();
                        System.out.print("메시지 입력> ");
                        String chatMsg = scn.nextLine();
                        client.sendChat(chatMovieCd, chatMsg);
                        break;
                        
                    case "5": // 채팅방 퇴장
                        System.out.print("영화 코드 입력> ");
                        String leaveMovieCd = scn.nextLine();
                        client.leaveRoom(leaveMovieCd);
                        break;
                        
                    case "6": // 감상평 조회
                        System.out.print("영화 코드 입력> ");
                        String reviewMovieCd = scn.nextLine();
                        client.requestReviews(reviewMovieCd);
                        break;
                        
                    case "7": // 감상평 작성
                        System.out.print("영화 코드 입력> ");
                        String submitMovieCd = scn.nextLine();
                        System.out.print("별점 (1-5) 입력> ");
                        int rating = Integer.parseInt(scn.nextLine());
                        System.out.print("감상평 내용 입력> ");
                        String content = scn.nextLine();
                        client.submitReview(submitMovieCd, rating, content);
                        break;
                        
                    case "8": // 감상평 삭제
                        System.out.print("감상평 ID 입력> ");
                        int reviewId = Integer.parseInt(scn.nextLine());
                        client.deleteReview(reviewId);
                        break;
                        
                    case "9": // 영화 검색
                        System.out.print("검색어 입력> ");
                        String keyword = scn.nextLine();
                        client.searchMovie(keyword);
                        break;
                        
                    case "10": // 북마크 추가
                        System.out.print("영화 코드 입력> ");
                        String bookmarkCd = scn.nextLine();
                        client.addBookmark(bookmarkCd);
                        break;
                        
                    case "11": // 북마크 삭제
                        System.out.print("영화 코드 입력> ");
                        String delBookmarkCd = scn.nextLine();
                        client.deleteBookmark(delBookmarkCd);
                        break;
                        
                    case "12": // 북마크 목록
                        client.requestBookmarks();
                        break;
                        
                    case "0": // 종료
                        client.disconnect();
                        Thread.sleep(500);
                        System.out.println("Client> 연결을 종료합니다.");
                        System.exit(0);
                        break;
                        
                    default:
                        System.out.println("올바른 번호를 입력해주세요.");
                }
                
                Thread.sleep(200); // 출력 순서 보장
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void showMenu() {
        System.out.println("\n════════════════ MovieSync 메뉴 ════════════════");
        System.out.println("1. 영화 목록 조회");
        System.out.println("2. 영화 상세 정보");
        System.out.println("3. 채팅방 입장");
        System.out.println("4. 채팅 메시지 전송");
        System.out.println("5. 채팅방 퇴장");
        System.out.println("6. 감상평 조회");
        System.out.println("7. 감상평 작성");
        System.out.println("8. 감상평 삭제");
        System.out.println("9. 영화 검색");
        System.out.println("10. 북마크 추가");
        System.out.println("11. 북마크 삭제");
        System.out.println("12. 북마크 목록");
        System.out.println("0. 종료");
        System.out.println("════════════════════════════════════════════");
    }
}

// 서버 메시지를 수신하는 스레드
class MessageListener extends Thread {
    Socket socket;
    InputStream inStream;
    DataInputStream dataInStream;
    Client client;
    
    MessageListener(Socket _s, Client client) {
        this.socket = _s;
        this.client = client;
    }
    
    // 콜백 설정 메서드 (동적 업데이트용)
    public void setCallback(Client.MessageCallback callback) {
        // Client의 콜백을 직접 참조하므로 별도 저장 불필요
    }
    
    public void run() {
        try {
            inStream = socket.getInputStream();
            dataInStream = new DataInputStream(inStream);
            
            while (true) {
                String msg = dataInStream.readUTF();
                
                // Client의 현재 콜백으로 전달 (동적으로 변경된 콜백 참조)
                if (client.callback != null) {
                    client.callback.onMessageReceived(msg);
                } else {
                    // 콘솔 모드면 직접 출력
                    parseAndDisplay(msg);
                }
            }
            
        } catch (IOException e) {
            if (client.callback != null) {
                client.callback.onMessageReceived("ERROR|서버와의 연결이 해제됨|END");
            } else {
                System.out.println("\nClient> 서버와의 연결이 해제됨");
            }
        }
    }
    
    // 콘솔 모드용 메시지 파싱 및 출력
    private void parseAndDisplay(String msg) {
        System.out.println("[수신] " + msg);
        System.out.println("────────────────────────────────");
    }
}