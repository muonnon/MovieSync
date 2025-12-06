# 📊 클래스 다이어그램

## 전체 클래스 구조

```
┌──────────────────────────────────────────────────────────────────────────────────────┐
│                                    MovieSync Classes                                  │
├──────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                      │
│   ┌─────────────────────────────────────────────────────────────────────────────┐   │
│   │                            Server Side                                       │   │
│   │                                                                              │   │
│   │   ┌─────────────┐    has-a    ┌─────────────────┐                           │   │
│   │   │   Server    │◆───────────▶│ DatabaseManager │                           │   │
│   │   └──────┬──────┘             └─────────────────┘                           │   │
│   │          │ has-a                                                             │   │
│   │          ▼                                                                   │   │
│   │   ┌─────────────┐    has-a    ┌─────────────────┐                           │   │
│   │   │ APIManager  │◆───────────▶│ DatabaseManager │                           │   │
│   │   └─────────────┘             └─────────────────┘                           │   │
│   │          │                                                                   │   │
│   │   ┌─────────────────┐         ┌─────────────────┐                           │   │
│   │   │ ConnectedClient │────────▶│    ChatRoom     │                           │   │
│   │   └────────┬────────┘         └─────────────────┘                           │   │
│   │            │ uses                                                            │   │
│   │   ┌────────▼────────┐  ┌────────────────────────┐                           │   │
│   │   │   MSGBuilder    │  │ ReceivedMSGTokenizer   │                           │   │
│   │   └─────────────────┘  └───────────┬────────────┘                           │   │
│   │                                    │ uses                                    │   │
│   │                        ┌───────────▼────────────┐                           │   │
│   │                        │      MSGTable          │                           │   │
│   │                        └────────────────────────┘                           │   │
│   └─────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                      │
│   ┌─────────────────────────────────────────────────────────────────────────────┐   │
│   │                            Client Side                                       │   │
│   │                                                                              │   │
│   │   ┌─────────────┐  creates   ┌─────────────┐                                │   │
│   │   │ LoginFrame  │───────────▶│   Client    │                                │   │
│   │   └──────┬──────┘            └──────┬──────┘                                │   │
│   │          │ creates                  │ uses                                   │   │
│   │          ▼                          ▼                                        │   │
│   │   ┌─────────────┐            ┌─────────────┐                                │   │
│   │   │  MainFrame  │            │ CMSGBuilder │                                │   │
│   │   └──────┬──────┘            └─────────────┘                                │   │
│   │          │ contains                                                          │   │
│   │   ┌──────┴──────────────────────────────────────────┐                       │   │
│   │   │              │              │                   │                        │   │
│   │   ▼              ▼              ▼                   ▼                        │   │
│   │ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────┐                     │   │
│   │ │MovieList │ │ ChatPanel│ │ReviewPanel│ │BookmarkPanel│                     │   │
│   │ │  Panel   │ │          │ │          │ │              │                     │   │
│   │ └──────────┘ └──────────┘ └──────────┘ └──────────────┘                     │   │
│   │                                                                              │   │
│   │   ┌─────────────────┐                                                       │   │
│   │   │ MessageListener │ ◀── Client 내부 스레드                                │   │
│   │   └─────────────────┘                                                       │   │
│   └─────────────────────────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 서버 클래스 상세

### Server

```java
public class Server {
    // 필드
    ServerSocket ss                              // 서버 소켓
    ArrayList<ConnectedClient> clients           // 연결된 클라이언트 목록
    HashMap<String, ConnectedClient> clientMap   // 닉네임 기반 클라이언트 맵
    HashMap<String, ChatRoom> chatRooms          // 채팅방 맵 (영화코드가 키)
    DatabaseManager dbManager                    // DB 관리자
    APIManager apiManager                        // API 관리자

    // 메서드
    + main(String[] args)                        // 서버 시작점
    + broadcastToRoom(String roomId, String msg) // 채팅방 브로드캐스트
    + sendToUser(String username, String msg)    // 특정 사용자에게 메시지
    + removeClient(ConnectedClient client)       // 클라이언트 제거
    + getOrCreateRoom(String roomId)             // 채팅방 생성/조회
}
```

### ConnectedClient (extends Thread)

```java
class ConnectedClient extends Thread {
    // 필드
    Socket socket                    // 클라이언트 소켓
    Server server                    // 서버 참조
    String username                  // 사용자 닉네임
    int userId                       // 사용자 ID
    DataOutputStream dataOutStream   // 출력 스트림
    DataInputStream dataInStream     // 입력 스트림
    MSGBuilder mb                    // 메시지 빌더
    ReceivedMSGTokenizer tk          // 메시지 파서

    // 메서드
    + run()                          // 스레드 실행 (메시지 수신 루프)
    - handleLogin(String msg)        // 로그인 처리
    - handleGetMovies()              // 영화 목록 조회
    - handleGetDetail(String msg)    // 영화 상세 조회
    - handleJoinRoom(String msg)     // 채팅방 입장
    - handleLeaveRoom(String msg)    // 채팅방 퇴장
    - handleChat(String msg)         // 채팅 메시지 처리
    - handleGetReviews(String msg)   // 감상평 조회
    - handleSubmitReview(String msg) // 감상평 작성
    - handleDeleteReview(String msg) // 감상평 삭제
    - handleSearchMovie(String msg)  // 영화 검색
    - handleAddBookmark(String msg)  // 북마크 추가
    - handleDeleteBookmark(String msg)// 북마크 삭제
    - handleGetBookmarks()           // 북마크 목록 조회
    - handleDisconnect()             // 연결 종료
    + sendMessage(String msg)        // 메시지 전송
    - getMovieName(String movieCd)   // 영화명 조회 (헬퍼)
}
```

### ChatRoom

```java
class ChatRoom {
    // 필드
    String roomId                              // 채팅방 ID (room_영화코드)
    ArrayList<ConnectedClient> users           // 참여 사용자 목록

    // 메서드
    + ChatRoom(String roomId)                  // 생성자
    + addUser(ConnectedClient user)            // 사용자 추가
    + removeUser(ConnectedClient user)         // 사용자 제거
    + broadcast(String msg)                    // 전체 브로드캐스트
    + getUserCount()                           // 현재 인원 수
}
```

### DatabaseManager

```java
public class DatabaseManager {
    // 상수
    - static final String DB_URL = "jdbc:sqlite:moviesync.db"

    // 필드
    - Connection conn                          // DB 연결

    // 생성자
    + DatabaseManager()                        // DB 연결 및 테이블 생성

    // Users 관련 메서드
    + createUser(String username): int         // 사용자 생성
    + isUsernameTaken(String username): boolean// 닉네임 중복 확인
    + getUsername(int userId): String          // 닉네임 조회

    // Movies 관련 메서드
    + saveMovie(...)                           // 영화 저장/갱신
    + getTop10Movies(): ResultSet              // Top 10 조회
    + getMovieDetail(String movieCd): ResultSet// 영화 상세 조회
    + searchMovies(String keyword): ResultSet  // 영화 검색

    // Reviews 관련 메서드
    + submitReview(...): int                   // 감상평 작성
    + getReviews(String movieCd): ResultSet    // 감상평 목록 조회
    + getAverageRating(String movieCd): double // 평균 평점 조회
    + getReviewCount(String movieCd): int      // 감상평 개수 조회
    + deleteReview(int reviewId, int userId): boolean // 감상평 삭제

    // Bookmarks 관련 메서드
    + addBookmark(int userId, String movieCd): boolean    // 북마크 추가
    + deleteBookmark(int userId, String movieCd): boolean // 북마크 삭제
    + getBookmarks(int userId): ResultSet                 // 북마크 목록

    // 기타
    + close()                                  // DB 연결 종료
}
```

### APIManager (extends Thread)

```java
public class APIManager extends Thread {
    // 상수
    - static final String API_KEY              // API 키
    - static final String API_URL              // API URL

    // 필드
    - DatabaseManager dbManager                // DB 관리자
    - boolean running                          // 실행 상태

    // 메서드
    + APIManager(DatabaseManager dbManager)    // 생성자
    + run()                                    // 스레드 실행
    + fetchAndSaveMovies()                     // API 호출 및 DB 저장
    - getTagValue(String tag, Element element) // XML 태그값 추출
    - getYesterdayDate(): String               // 어제 날짜 반환
    - getNextMondayMidnight(): long            // 다음 월요일 자정 계산
    + stopAPI()                                // 스레드 종료
}
```

---

## 클라이언트 클래스 상세

### Client

```java
public class Client {
    // 필드
    Socket mySocket                            // 서버 소켓
    String myUsername                          // 닉네임
    int myUserId                               // 사용자 ID
    DataOutputStream dataOutStream             // 출력 스트림
    MessageListener msgListener                // 메시지 수신 스레드
    CMSGBuilder cmb                            // 메시지 빌더
    - MessageCallback callback                 // 콜백 인터페이스

    // 인터페이스
    interface MessageCallback {
        void onMessageReceived(String message)
    }

    // 메서드
    + setMessageCallback(MessageCallback callback)
    + connectToServer(String username): boolean
    + requestMovies()                          // 영화 목록 요청
    + requestMovieDetail(String movieCd)       // 영화 상세 요청
    + joinRoom(String movieCd, String movieNm) // 채팅방 입장
    + leaveRoom(String movieCd)                // 채팅방 퇴장
    + sendChat(String movieCd, String message) // 채팅 메시지 전송
    + requestReviews(String movieCd)           // 감상평 요청
    + submitReview(...)                        // 감상평 작성
    + deleteReview(int reviewId)               // 감상평 삭제
    + searchMovie(String keyword)              // 영화 검색
    + addBookmark(String movieCd)              // 북마크 추가
    + deleteBookmark(String movieCd)           // 북마크 삭제
    + requestBookmarks()                       // 북마크 목록 요청
    + disconnect()                             // 연결 종료
    + main(String[] args)                      // 콘솔 모드 진입점
}
```

### MessageListener (extends Thread)

```java
class MessageListener extends Thread {
    // 필드
    Socket socket
    DataInputStream dataInStream
    Client.MessageCallback callback

    // 메서드
    + MessageListener(Socket s, MessageCallback callback)
    + run()                                    // 메시지 수신 루프
    - parseAndDisplay(String msg)              // 콘솔 출력용
}
```

### LoginFrame (extends JFrame)

```java
public class LoginFrame extends JFrame {
    // 필드
    - JTextField usernameField                 // 닉네임 입력
    - JButton loginButton                      // 로그인 버튼
    - Client client                            // 클라이언트 객체

    // 메서드
    + LoginFrame()                             // 생성자 (GUI 초기화)
    - attemptLogin()                           // 로그인 시도
    - handleServerMessage(String message)      // 서버 응답 처리
    + main(String[] args)                      // GUI 진입점
}
```

### MainFrame (extends JFrame)

```java
public class MainFrame extends JFrame {
    // 필드
    - Client client                            // 클라이언트 객체
    - String username                          // 닉네임
    - JTree menuTree                           // 메뉴 트리
    - JPanel contentPanel                      // 콘텐츠 패널
    - CardLayout cardLayout                    // 화면 전환 레이아웃
    - MovieListPanel movieListPanel            // 영화 목록 패널
    - ChatPanel chatPanel                      // 채팅 패널
    - ReviewPanel reviewPanel                  // 감상평 패널
    - BookmarkPanel bookmarkPanel              // 북마크 패널

    // 메서드
    + MainFrame(Client client, String username)
    - handleMenuSelection(String menuName)     // 메뉴 선택 처리
    + showChatRoom(String movieCd, String movieNm)
    + showReviews(String movieCd, String movieNm)
    - logout()                                 // 로그아웃
    # processWindowEvent(WindowEvent e)        // 종료 처리
}
```

### MovieListPanel (extends JPanel)

```java
public class MovieListPanel extends JPanel {
    // 내부 클래스
    class MovieData {
        String movieCd, movieNm, openDt
        int rank
        long audiAcc, salesAcc
    }

    // 필드
    - Client client
    - MainFrame mainFrame
    - JTable movieTable
    - DefaultTableModel tableModel
    - ArrayList<MovieData> movies
    - String selectedMovieCd, selectedMovieNm

    // 메서드
    + MovieListPanel(Client client, MainFrame mainFrame)
    + loadMovies()                             // 영화 목록 로드
    - showMovieDetail(MovieData movie)         // 상세 정보 표시
    - setupMessageHandler()                    // 콜백 설정
    - handleMessage(String message)            // 메시지 처리
}
```

---

## 메시지 관련 클래스

### MSGBuilder (서버 → 클라이언트)

```java
public class MSGBuilder {
    + loginOkMSG(int userId, String username)  // LOGIN_OK
    + loginFailMSG(String reason)              // LOGIN_FAIL
    + welcomeMSG()                             // WELCOME
    + moviesCountMSG(int count)                // MOVIES_COUNT
    + movieDataMSG(...)                        // MOVIES_DATA
    + moviesEndMSG()                           // MOVIES_END
    + movieDetailMSG(...)                      // DETAIL
    + roomOkMSG(...)                           // ROOM_OK
    + userJoinMSG(...)                         // USER_JOIN
    + userLeftMSG(...)                         // USER_LEFT
    + chatAllMSG(...)                          // CHAT_ALL
    + reviewSummaryMSG(...)                    // REV_SUMMARY
    + reviewCountMSG(int count)                // REV_COUNT
    + reviewDataMSG(...)                       // REV_DATA
    + reviewEndMSG()                           // REV_END
    + reviewOkMSG(int reviewId)                // REV_OK
    + reviewFailMSG(String reason)             // REV_FAIL
    + deleteOkMSG()                            // DEL_OK
    + deleteFailMSG(String reason)             // DEL_FAIL
    + bookmarkOkMSG()                          // BOOKMARK_OK
    + bookmarkDelOkMSG()                       // BOOKMARK_DEL_OK
    + disconnectOkMSG()                        // DISCONNECT_OK
    + errorMSG(String errorMsg)                // ERROR
}
```

### CMSGBuilder (클라이언트 → 서버)

```java
public class CMSGBuilder {
    + loginMSG(String username)                // LOGIN
    + getMoviesMSG()                           // GET_MOVIES
    + getDetailMSG(String movieCd)             // GET_DETAIL
    + joinRoomMSG(String movieCd, String movieNm) // JOIN_ROOM
    + leaveRoomMSG(String movieCd)             // LEAVE_ROOM
    + chatMSG(String movieCd, String message)  // CHAT
    + getReviewsMSG(String movieCd)            // GET_REVIEWS
    + submitReviewMSG(...)                     // SUBMIT_REVIEW
    + deleteReviewMSG(int reviewId)            // DELETE_REVIEW
    + searchMovieMSG(String keyword)           // SEARCH_MOVIE
    + addBookmarkMSG(String movieCd)           // ADD_BOOKMARK
    + deleteBookmarkMSG(String movieCd)        // DELETE_BOOKMARK
    + getBookmarksMSG()                        // GET_BOOKMARKS
    + disconnectMSG()                          // DISCONNECT
}
```

### ReceivedMSGTokenizer

```java
public class ReceivedMSGTokenizer {
    // 필드
    StringTokenizer st
    MSGTable mt

    // 메서드
    + detection(String msg): int               // 메시지 타입 감지 (0~13)
    + findUsername(String msg): String
    + findMovieCode(String msg): String
    + findRoomMovieCode(String msg): String
    + findRoomMovieName(String msg): String
    + findLeaveRoomCode(String msg): String
    + findChatRoomCode(String msg): String
    + findChatMessage(String msg): String
    + findReviewMovieCode(String msg): String
    + findSubmitMovieCode(String msg): String
    + findSubmitRating(String msg): int
    + findSubmitContent(String msg): String
    + findDeleteReviewId(String msg): int
    + findSearchKeyword(String msg): String
    + findBookmarkMovieCode(String msg): String
    + findDeleteBookmarkCode(String msg): String
}
```

### MSGTable

```java
public class MSGTable {
    // 상수
    int numberOfMSG = 14

    // 메시지 태그 배열
    String[] MSGtags = {
        "LOGIN",           // 0
        "GET_MOVIES",      // 1
        "GET_DETAIL",      // 2
        "JOIN_ROOM",       // 3
        "LEAVE_ROOM",      // 4
        "CHAT",            // 5
        "GET_REVIEWS",     // 6
        "SUBMIT_REVIEW",   // 7
        "DELETE_REVIEW",   // 8
        "SEARCH_MOVIE",    // 9
        "ADD_BOOKMARK",    // 10
        "DELETE_BOOKMARK", // 11
        "GET_BOOKMARKS",   // 12
        "DISCONNECT"       // 13
    }
}
```

---

## 클래스 관계 요약

| 관계 | 설명 |
|------|------|
| `Server ◇─▶ DatabaseManager` | Server가 DatabaseManager를 소유 (has-a) |
| `Server ◇─▶ APIManager` | Server가 APIManager를 소유 |
| `Server ◇─▶ ConnectedClient` | Server가 여러 ConnectedClient를 관리 (1:N) |
| `Server ◇─▶ ChatRoom` | Server가 여러 ChatRoom을 관리 (1:N) |
| `ConnectedClient ──▶ ChatRoom` | ConnectedClient가 ChatRoom에 참여 (N:M) |
| `LoginFrame ──▶ Client` | LoginFrame이 Client를 생성 |
| `LoginFrame ──▶ MainFrame` | 로그인 성공 시 MainFrame 생성 |
| `MainFrame ◇─▶ Panels` | MainFrame이 각 Panel을 포함 |
| `Client ◇─▶ MessageListener` | Client가 MessageListener 스레드 관리 |

---

[← 돌아가기](./README.md)
