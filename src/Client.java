//프로젝트 이름 : MovieSync
//개발자 : 권미리
//개발 기간: 2025.12.01 ~ 2025.12.13

import java.io.*;
import java.net.*;
import java.util.Scanner;

//서버와 통신하는 클라이언트 클래스 (GUI + 콘솔 모드 지원)
public class Client {
    // 소켓 통신 관련
    Socket mySocket = null;              //서버 연결 소켓 (localhost:55555)
    String myUsername = null;            //내 닉네임
    int myUserId = -1;                   //내 사용자 ID (서버에서 할당)
    
    // 데이터 송수신 관련
    DataOutputStream dataOutStream = null;  //데이터 출력 스트림 (서버로 메세지 전송)
    MessageListener msgListener = null;     //메시지 수신 스레드 (서버 메세지 수신)
    CMSGBuilder cmb = new CMSGBuilder();    //클라이언트 메시지 빌더 (메세지 생성)
    
    //GUI 연동용 콜백 - MessageListener에서 접근 가능
    MessageCallback callback;
    
    //GUI에서 서버 메시지를 받기 위한 콜백 인터페이스
    public interface MessageCallback {
        void onMessageReceived(String message); //메세지 수신 시 호출
    }
    
    //GUI 콜백 설정 - MainFrame에서 호출하여 콜백 설정
    public void setMessageCallback(MessageCallback callback) {
        this.callback = callback; //콜백 저장
        if (msgListener != null) { //메세지 수신 스레드가 이미 시작되었으면
            msgListener.setCallback(callback); //스레드에도 콜백 설정
        }
    }
    
    //GUI용 메소드
    //서버에 연결하고 로그인 메시지를 전송한다
    public boolean connectToServer(String username) {
        try {
            mySocket = new Socket("localhost", 55555); //서버 연결 (localhost:55555)
            myUsername = username; //닉네임 저장
            
            OutputStream outStream = mySocket.getOutputStream(); //출력 스트림 가져오기
            dataOutStream = new DataOutputStream(outStream); //DataOutputStream 생성
            
            //로그인 메시지 전송 - LOGIN|닉네임|END
            dataOutStream.writeUTF(cmb.loginMSG(username));
            
            //메시지 수신 스레드 시작 (별도 스레드에서 서버 메세지 계속 수신)
            msgListener = new MessageListener(mySocket, this);
            msgListener.start();
            
            return true; //연결 성공
        } catch (IOException e) {
            System.err.println("Client> 서버 연결 실패: " + e.getMessage());
            return false; //연결 실패
        }
    }
    
    //영화 목록 조회 - GET_MOVIES|END
    public void requestMovies() {
        try {
            dataOutStream.writeUTF(cmb.getMoviesMSG()); //서버에 영화 목록 요청
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //영화 상세 정보 조회 - GET_DETAIL|영화코드|END
    public void requestMovieDetail(String movieCd) {
        try {
            dataOutStream.writeUTF(cmb.getDetailMSG(movieCd)); //서버에 상세 정보 요청
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //채팅방 입장 - JOIN_ROOM|영화코드|영화제목|END
    public void joinRoom(String movieCd, String movieNm) {
        try {
            dataOutStream.writeUTF(cmb.joinRoomMSG(movieCd, movieNm)); //서버에 입장 요청
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //채팅방 퇴장 - LEAVE_ROOM|영화코드|END
    public void leaveRoom(String movieCd) {
        try {
            dataOutStream.writeUTF(cmb.leaveRoomMSG(movieCd)); //서버에 퇴장 요청
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //채팅 메시지 전송 - CHAT|영화코드|메세지|END
    public void sendChat(String movieCd, String message) {
        try {
            dataOutStream.writeUTF(cmb.chatMSG(movieCd, message)); //서버에 채팅 메세지 전송
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //감상평 조회 - GET_REVIEWS|영화코드|END
    public void requestReviews(String movieCd) {
        try {
            dataOutStream.writeUTF(cmb.getReviewsMSG(movieCd)); //서버에 감상평 조회 요청
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //전체 감상평 조회 - GET_ALL_REVIEWS|END
    public void requestAllReviews() {
        try {
            dataOutStream.writeUTF(cmb.getAllReviewsMSG()); //서버에 전체 감상평 조회 요청
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //감상평 작성 - SUBMIT_REVIEW|영화코드|별점|내용|END
    public void submitReview(String movieCd, int rating, String content) {
        try {
            dataOutStream.writeUTF(cmb.submitReviewMSG(movieCd, rating, content)); //서버에 감상평 작성 요청
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //감상평 삭제 - DELETE_REVIEW|감상평ID|END
    public void deleteReview(int reviewId) {
        try {
            dataOutStream.writeUTF(cmb.deleteReviewMSG(reviewId)); //서버에 감상평 삭제 요청
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //영화 검색 - SEARCH_MOVIE|검색어|END
    public void searchMovie(String keyword) {
        try {
            dataOutStream.writeUTF(cmb.searchMovieMSG(keyword)); //서버에 영화 검색 요청
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //북마크 추가 - ADD_BOOKMARK|영화코드|END
    public void addBookmark(String movieCd) {
        try {
            dataOutStream.writeUTF(cmb.addBookmarkMSG(movieCd)); //서버에 북마크 추가 요청
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //북마크 삭제 - DELETE_BOOKMARK|영화코드|END
    public void deleteBookmark(String movieCd) {
        try {
            dataOutStream.writeUTF(cmb.deleteBookmarkMSG(movieCd)); //서버에 북마크 삭제 요청
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //북마크 목록 조회 - GET_BOOKMARKS|END
    public void requestBookmarks() {
        try {
            dataOutStream.writeUTF(cmb.getBookmarksMSG()); //서버에 북마크 목록 조회 요청
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //연결 종료 - DISCONNECT|END
    public void disconnect() {
        try {
            if (dataOutStream != null) { //출력 스트림이 있으면
                dataOutStream.writeUTF(cmb.disconnectMSG()); //서버에 종료 메세지 전송
            }
            if (mySocket != null) { //소켓이 있으면
                mySocket.close(); //소켓 닫기
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // ========== 콘솔 모드 메인 (테스트용) ==========
    
    public static void main(String[] args) {
        Client client = new Client(); //클라이언트 객체 생성
        Scanner scn = new Scanner(System.in); //사용자 입력용
        
        try {
            //서버 연결
            System.out.print("Client> 닉네임을 입력하세요: ");
            String username = scn.nextLine();
            
            if (!client.connectToServer(username)) { //연결 실패하면
                System.out.println("Client> 서버 연결 실패");
                return;
            }
            
            System.out.println("Client> 서버에 연결되었습니다.");
            Thread.sleep(500); //서버 응답 대기 (로그인 응답)
            
            //메인 메뉴 - 무한 루프
            while (true) {
                showMenu(); //메뉴 출력
                System.out.print("선택> ");
                String choice = scn.nextLine();
                System.out.println();
                
                switch (choice) {
                    case "1": //영화 목록 조회
                        client.requestMovies();
                        break;
                        
                    case "2": //영화 상세 정보
                        System.out.print("영화 코드 입력> ");
                        String movieCd = scn.nextLine();
                        client.requestMovieDetail(movieCd);
                        break;
                        
                    case "3": //채팅방 입장
                        System.out.print("영화 코드 입력> ");
                        String joinMovieCd = scn.nextLine();
                        System.out.print("영화 제목 입력> ");
                        String movieNm = scn.nextLine();
                        client.joinRoom(joinMovieCd, movieNm);
                        break;
                        
                    case "4": //채팅 메시지 전송
                        System.out.print("영화 코드 입력> ");
                        String chatMovieCd = scn.nextLine();
                        System.out.print("메시지 입력> ");
                        String chatMsg = scn.nextLine();
                        client.sendChat(chatMovieCd, chatMsg);
                        break;
                        
                    case "5": //채팅방 퇴장
                        System.out.print("영화 코드 입력> ");
                        String leaveMovieCd = scn.nextLine();
                        client.leaveRoom(leaveMovieCd);
                        break;
                        
                    case "6": //감상평 조회
                        System.out.print("영화 코드 입력> ");
                        String reviewMovieCd = scn.nextLine();
                        client.requestReviews(reviewMovieCd);
                        break;
                        
                    case "7": //감상평 작성
                        System.out.print("영화 코드 입력> ");
                        String submitMovieCd = scn.nextLine();
                        System.out.print("별점 (1-5) 입력> ");
                        int rating = Integer.parseInt(scn.nextLine());
                        System.out.print("감상평 내용 입력> ");
                        String content = scn.nextLine();
                        client.submitReview(submitMovieCd, rating, content);
                        break;
                        
                    case "8": //감상평 삭제
                        System.out.print("감상평 ID 입력> ");
                        int reviewId = Integer.parseInt(scn.nextLine());
                        client.deleteReview(reviewId);
                        break;
                        
                    case "9": //영화 검색
                        System.out.print("검색어 입력> ");
                        String keyword = scn.nextLine();
                        client.searchMovie(keyword);
                        break;
                        
                    case "10": //북마크 추가
                        System.out.print("영화 코드 입력> ");
                        String bookmarkCd = scn.nextLine();
                        client.addBookmark(bookmarkCd);
                        break;
                        
                    case "11": //북마크 삭제
                        System.out.print("영화 코드 입력> ");
                        String delBookmarkCd = scn.nextLine();
                        client.deleteBookmark(delBookmarkCd);
                        break;
                        
                    case "12": //북마크 목록
                        client.requestBookmarks();
                        break;
                        
                    case "0": //종료
                        client.disconnect(); //서버에 종료 메세지 전송 및 소켓 닫기
                        Thread.sleep(500); //서버 처리 대기
                        System.out.println("Client> 연결을 종료합니다.");
                        System.exit(0); //프로그램 종료
                        break;
                        
                    default:
                        System.out.println("올바른 번호를 입력해주세요.");
                }
                
                Thread.sleep(200); //출력 순서 보장 (서버 응답 대기)
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    //메뉴 출력
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

//서버 메시지를 수신하는 스레드 - 별도 스레드에서 계속 실행됨
class MessageListener extends Thread {
    Socket socket; //서버 소켓
    InputStream inStream; //입력 스트림
    DataInputStream dataInStream; //데이터 입력 스트림
    Client client; //클라이언트 객체 참조
    
    MessageListener(Socket _s, Client client) { //생성자
        this.socket = _s;
        this.client = client;
    }
    
    //콜백 설정 메서드 (동적 업데이트용)
    public void setCallback(Client.MessageCallback callback) {
        //Client의 콜백을 직접 참조하므로 별도 저장 불필요
    }
    
    public void run() { //스레드 실행
        try {
            inStream = socket.getInputStream(); //입력 스트림 가져오기
            dataInStream = new DataInputStream(inStream); //DataInputStream 생성
            
            while (true) { //무한 루프 (서버 메세지 계속 수신)
                String msg = dataInStream.readUTF(); //서버로부터 메세지 수신
                
                //Client의 현재 콜백으로 전달 (GUI 모드)
                if (client.callback != null) { //콜백이 설정되어 있으면
                    client.callback.onMessageReceived(msg); //콜백 호출 (GUI에서 처리)
                } else { //콜백이 없으면 (콘솔 모드)
                    parseAndDisplay(msg); //콘솔에 직접 출력
                }
            }
            
        } catch (IOException e) { //연결이 끊기면
            if (client.callback != null) { //GUI 모드면
                client.callback.onMessageReceived("ERROR|서버와의 연결이 해제됨|END"); //에러 메세지 전달
            } else { //콘솔 모드면
                System.out.println("\nClient> 서버와의 연결이 해제됨"); //콘솔에 출력
            }
        }
    }
    
    //콘솔 모드용 메시지 파싱 및 출력
    private void parseAndDisplay(String msg) {
        System.out.println("[수신] " + msg); //수신한 메세지 그대로 출력
        System.out.println("────────────────────────────────");
    }
}