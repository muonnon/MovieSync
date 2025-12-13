//프로젝트 이름 : MovieSync
//개발자 : 권미리
//개발 기간: 2025.12.01 ~ 2025.12.13

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatPanel extends JPanel {
    private Client client; //서버통신용 클라이언트 개체
    private String username; //현재 로그인한 사용자 닉네임
    private MainFrame mainFrame; //화면전환용
    
    // 관리자 모드 - username이 "admin"이면 true
    private boolean isAdmin = false;
    
    // 현재 상태
    private boolean isMainView = true; // true: 메인 화면(채팅방 목록), false: 특정 채팅방
    private boolean isInRoom = false; //채팅방 입장 여부
    private String currentRoomId = null; // 현재 채팅방 ID (room_영화코드)
    private String currentMovieCd = null; //현재 영화 코드
    private String currentMovieNm = null; //현재 영화 제목
    private String currentMovieName = null; // 로그 다운로드용 영화명
    
    // GUI 컴포넌트 생성
    private JLabel titleLabel; //채팅방 제목
    private JLabel userCountLabel; //접속자 수 표시
    private JTextArea chatArea; //채팅 메세지 영역
    private JScrollPane chatScrollPane; //채팅 영역 스크롤
    private JTextField messageField; //메세지 입력 필드
    private JButton sendButton; //전송 버튼
    private JButton leaveButton; //나가기 버튼
    private DefaultListModel<String> userListModel; //접속자 리스트 모델
    private JList<String> userList; //접속자 리스트
    
    // 메인 화면용 (채팅방 목록)
    private JPanel mainViewPanel; //채팅방 목록 메인 패널
    private JLabel mainTitleLabel; //메인 화면 제목
    private JPanel roomListPanel;  // 채팅방 카드들이 추가되는 패널
    
    // 채팅방 화면용
    private JPanel chatRoomPanel; //채팅방 화면 패널
    
    public ChatPanel(Client client, String username, MainFrame mainFrame) { //생성자
        this.client = client;
        this.username = username;
        this.mainFrame = mainFrame;
        
        // 관리자 확인 - 닉네임이 "admin"이면 관리자 모드
        this.isAdmin = username.equals("admin");
        
        setLayout(new CardLayout()); //CardLayout으로 메인화면과 채팅방화면 전환
        
        // 메인 화면 (채팅방 목록) 생성
        createMainView();
        
        // 채팅방 화면 생성
        createChatRoomView();
        
        add(mainViewPanel, "MAIN"); //메인 화면 추가
        add(chatRoomPanel, "CHAT_ROOM"); //채팅방 화면 추가
        
        // 초기 화면: 메인 화면(채팅방 목록)
        showMainView();
    }
    
    // 메인 화면 생성 (채팅방 목록)
    private void createMainView() {
        mainViewPanel = new JPanel(new BorderLayout(10, 10)); //상하좌우 10픽셀 간격
        mainViewPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); //여백
        
        // 상단 - 타이틀(L), 새로고침(R)
        JPanel topPanel = new JPanel(new BorderLayout());
        
        mainTitleLabel = new JLabel("활성 채팅방");
        mainTitleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18)); //굵은 글씨
        topPanel.add(mainTitleLabel, BorderLayout.WEST); //상단 패널 왼쪽
        
        JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0)); //우측 정렬
        
        JButton refreshButton = new JButton("새로고침");
        refreshButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        refreshButton.addActionListener(new ActionListener() { //새로고침 클릭 시 채팅방 목록 갱신
            public void actionPerformed(ActionEvent e) {
                updateRoomList(); //서버에 영화 목록 요청
            }
        });
        topRightPanel.add(refreshButton);
        
        topPanel.add(topRightPanel, BorderLayout.EAST); //상단 패널 오른쪽
        
        mainViewPanel.add(topPanel, BorderLayout.NORTH); //메인 패널 상단
        
        // 중앙 - 채팅방 목록 (영화 카드들)
        roomListPanel = new JPanel();
        roomListPanel.setLayout(new BoxLayout(roomListPanel, BoxLayout.Y_AXIS)); //세로 나열
        roomListPanel.setBackground(Color.WHITE); //흰 배경
        
        JScrollPane scrollPane = new JScrollPane(roomListPanel); //스크롤 가능
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); //수직 스크롤바 항상 표시
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); //한번에 16픽셀씩 이동
        
        mainViewPanel.add(scrollPane, BorderLayout.CENTER); //메인 패널 중앙
        
        // 하단 - 나가기 버튼
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); //우측 정렬
        
        JButton mainLeaveButton = new JButton("나가기");
        mainLeaveButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        mainLeaveButton.addActionListener(new ActionListener() { //나가기 클릭 시 영화 목록 화면으로 이동
            public void actionPerformed(ActionEvent e) {
                mainFrame.showMovieList(); //영화 목록 화면으로 전환
            }
        });
        bottomPanel.add(mainLeaveButton);
        
        mainViewPanel.add(bottomPanel, BorderLayout.SOUTH); //메인 패널 하단
    }
    
    // 채팅방 화면 생성
    private void createChatRoomView() {
        chatRoomPanel = new JPanel(new BorderLayout(10, 10)); //상하좌우 10픽셀 간격
        chatRoomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); //여백
        
        // 상단 - 타이틀(L), 버튼들(R)
        JPanel topPanel = new JPanel(new BorderLayout());
        
        titleLabel = new JLabel("채팅방을 선택해주세요");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18)); //굵은 글씨
        topPanel.add(titleLabel, BorderLayout.WEST); //상단 패널 왼쪽
        
        // 우측 - 버튼들 (로그 다운로드 + 나가기)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0)); //우측 정렬
        
        // 관리자 전용: 로그 다운로드 버튼 (닉네임이 "admin"일 때만 표시)
        if (isAdmin) {
            JButton downloadLogButton = new JButton("로그 다운로드");
            downloadLogButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
            downloadLogButton.setForeground(new Color(76, 175, 80)); //초록색
            downloadLogButton.addActionListener(new ActionListener() { //클릭 시 현재 채팅방 로그 다운로드
                public void actionPerformed(ActionEvent e) {
                    downloadCurrentRoomLog(); //로그 파일 생성 및 저장
                }
            });
            buttonPanel.add(downloadLogButton);
        }
        
        leaveButton = new JButton("나가기");
        leaveButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        leaveButton.setEnabled(true); //활성화
        leaveButton.addActionListener(new ActionListener() { //나가기 클릭 시
            public void actionPerformed(ActionEvent e) {
                if (isInRoom) { //채팅방에 있으면
                    leaveRoom(); //채팅방 퇴장 (서버에 LEAVE_ROOM 전송)
                }
                showMainView(); //메인 화면(채팅방 목록)으로 전환
            }
        });
        buttonPanel.add(leaveButton);
        
        topPanel.add(buttonPanel, BorderLayout.EAST); //상단 패널 오른쪽
        
        chatRoomPanel.add(topPanel, BorderLayout.NORTH); //채팅방 패널 상단
        
        // 중앙 - 채팅 영역(L) + 접속자 목록(R)
        JPanel centerPanel = new JPanel(new BorderLayout(10, 0)); //좌우 10픽셀 간격
        
        // 채팅 영역 - 메세지 표시
        chatArea = new JTextArea();
        chatArea.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        chatArea.setEditable(false); //읽기 전용
        chatArea.setLineWrap(true); //자동 줄바꿈
        chatArea.setWrapStyleWord(true); //단어 단위 줄바꿈
        
        chatScrollPane = new JScrollPane(chatArea); //스크롤 가능
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); //수직 스크롤바 항상 표시
        
        centerPanel.add(chatScrollPane, BorderLayout.CENTER); //중앙 패널 중앙
        
        // 접속자 목록 - 우측에 표시
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setPreferredSize(new Dimension(150, 0)); //너비 150픽셀 고정
        
        userCountLabel = new JLabel("접속자 (0명)");
        userCountLabel.setFont(new Font("맑은 고딕", Font.BOLD, 12)); //굵은 글씨
        userPanel.add(userCountLabel, BorderLayout.NORTH); //접속자 패널 상단
        
        userListModel = new DefaultListModel<String>(); //접속자 리스트 모델 생성
        userList = new JList<String>(userListModel); //접속자 리스트 생성
        userList.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        
        JScrollPane userScrollPane = new JScrollPane(userList); //스크롤 가능
        userPanel.add(userScrollPane, BorderLayout.CENTER); //접속자 패널 중앙
        
        centerPanel.add(userPanel, BorderLayout.EAST); //중앙 패널 오른쪽
        
        chatRoomPanel.add(centerPanel, BorderLayout.CENTER); //채팅방 패널 중앙
        
        // 하단 - 메시지 입력 필드 + 전송 버튼
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 0)); //좌우 5픽셀 간격
        
        messageField = new JTextField();
        messageField.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        messageField.setEnabled(false); //초기에는 비활성화 (채팅방 입장 전)
        messageField.addActionListener(new ActionListener() { //Enter키 누르면 전송
            public void actionPerformed(ActionEvent e) {
                sendMessage(); //메세지 전송
            }
        });
        
        sendButton = new JButton("전송");
        sendButton.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        sendButton.setEnabled(false); //초기에는 비활성화
        sendButton.addActionListener(new ActionListener() { //전송 버튼 클릭 시
            public void actionPerformed(ActionEvent e) {
                sendMessage(); //메세지 전송
            }
        });
        
        bottomPanel.add(messageField, BorderLayout.CENTER); //하단 패널 중앙
        bottomPanel.add(sendButton, BorderLayout.EAST); //하단 패널 오른쪽
        
        chatRoomPanel.add(bottomPanel, BorderLayout.SOUTH); //채팅방 패널 하단
    }
    
    // 메인 화면 표시 (채팅방 목록) - 메뉴에서 진입
    public void showMainView() {
        isMainView = true; //메인 화면 상태로 변경
        CardLayout cl = (CardLayout) getLayout(); //CardLayout 가져오기
        cl.show(this, "MAIN"); //메인 화면 표시
        updateRoomList(); //채팅방 목록 갱신 (서버에 영화 목록 요청)
    }
    
    // 채팅방 목록 업데이트 (영화 목록 표시)
    private void updateRoomList() {
        roomListPanel.removeAll(); //기존 카드 모두 제거
        roomListPanel.revalidate(); //레이아웃 재계산
        roomListPanel.repaint(); //화면 다시 그리기
        
        // 서버에 영화 목록 요청 - GET_MOVIES|END
        client.requestMovies();
    }
    
    // 영화 목록 데이터 저장용 - 서버에서 받은 영화들을 임시 저장
    private java.util.ArrayList<MovieRoomData> tempMovieList = new java.util.ArrayList<MovieRoomData>();
    
    // 영화 카드 생성 (채팅방 목록용)
    private JPanel createRoomCard(MovieRoomData movie) {
        JPanel card = new JPanel(new BorderLayout(10, 10)); //좌측: 정보, 우측: 버튼
        card.setBorder(BorderFactory.createCompoundBorder( //테두리 설정
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1), //회색 실선 1px
            BorderFactory.createEmptyBorder(15, 15, 15, 15) //내부 여백 15px
        ));
        card.setBackground(Color.WHITE); //흰 배경
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100)); //최대 크기: 가로 무제한, 세로 100px
        
        // 좌측 - 영화 정보
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS)); //세로 나열
        infoPanel.setBackground(Color.WHITE);
        
        //영화 제목 (순위 포함)
        JLabel nameLabel = new JLabel(movie.rank + "위. " + movie.movieNm);
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16)); //굵은 글씨
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT); //좌측 정렬
        infoPanel.add(nameLabel);
        
        infoPanel.add(Box.createVerticalStrut(5)); //제목과 다음 정보 사이 5px 간격
        
        //개봉일 + 누적관객 - 한 줄로 표시
        JLabel detailLabel = new JLabel("개봉일: " + movie.openDt + " | 누적관객: " + String.format("%,d", movie.audiAcc) + "명");
        detailLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        detailLabel.setForeground(new Color(100, 100, 100)); //회색
        detailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(detailLabel);
        
        card.add(infoPanel, BorderLayout.CENTER); //정보 패널을 카드 중앙(L)에 배치
        
        // 우측 - 입장 버튼
        JButton enterButton = new JButton("입장");
        enterButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        enterButton.addActionListener(new ActionListener() { //입장 버튼 클릭 시
            public void actionPerformed(ActionEvent e) {
                joinRoom(movie.movieCd, movie.movieNm); //해당 영화 채팅방으로 입장
            }
        });
        card.add(enterButton, BorderLayout.EAST); //버튼을 카드 오른쪽에 배치
        
        return card; //완성된 카드 반환
    }
    
    // 특정 영화의 채팅방 입장 (영화 목록에서 버튼 클릭)
    public void joinRoom(String movieCd, String movieNm) {
        // 이전 채팅방에 있었다면 퇴장
        if (isInRoom) {
            leaveRoom(); //기존 채팅방 퇴장
        }
        
        currentMovieCd = movieCd; //영화 코드 저장
        currentMovieNm = movieNm; //영화 제목 저장
        currentMovieName = movieNm; // 로그 다운로드용
        currentRoomId = "room_" + movieCd; // 채팅방 ID 생성 (room_영화코드)
        isMainView = false; //채팅방 화면 상태로 변경
        
        // 채팅방 화면으로 전환
        CardLayout cl = (CardLayout) getLayout(); //CardLayout 가져오기
        cl.show(this, "CHAT_ROOM"); //채팅방 화면 표시
        
        // UI 초기화
        chatArea.setText(""); //채팅 영역 초기화
        userListModel.clear(); //접속자 목록 초기화
        titleLabel.setText("채팅방: " + movieNm); //타이틀 변경
        userCountLabel.setText("접속자 (0명)"); //접속자 수 초기화
        
        messageField.setEnabled(false); //메세지 입력 비활성화 (입장 완료 전)
        sendButton.setEnabled(false); //전송 버튼 비활성화
        
        appendSystemMessage("'" + movieNm + "' 채팅방에 입장합니다..."); //시스템 메세지
        
        // 서버에 입장 요청 - JOIN_ROOM|영화코드|영화제목|END
        client.joinRoom(movieCd, movieNm);
    }
    
    // 채팅방 퇴장
    public void leaveRoom() {
        if (!isInRoom || currentMovieCd == null) { //채팅방에 없으면 무시
            return;
        }
        
        client.leaveRoom(currentMovieCd); //서버에 LEAVE_ROOM|영화코드|END 전송
        
        isInRoom = false; //입장 상태 false로 변경
        currentRoomId = null; //현재 채팅방 ID 초기화
        currentMovieName = null; //영화명 초기화
        messageField.setEnabled(false); //메세지 입력 비활성화
        sendButton.setEnabled(false); //전송 버튼 비활성화
        
        appendSystemMessage("채팅방에서 나갔습니다."); //시스템 메세지
    }
    
    // 메시지 전송
    private void sendMessage() {
        if (!isInRoom || currentMovieCd == null) { //채팅방에 없으면 무시
            return;
        }
        
        String message = messageField.getText().trim(); //입력 필드 텍스트 가져오기 (양쪽 공백 제거)
        
        if (message.isEmpty()) { //빈 메세지면 무시
            return;
        }
        
        if (message.length() > 500) { //500자 초과 검증
            JOptionPane.showMessageDialog(this, "메시지는 500자 이하로 입력해주세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        client.sendChat(currentMovieCd, message); //서버에 CHAT|영화코드|메세지|END 전송
        messageField.setText(""); //입력 필드 초기화
    }
    
    // 채팅 메시지 추가 - 화면에 표시
    private void appendChatMessage(String sender, String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss"); //시간 포맷 (시:분:초)
        String timestamp = sdf.format(new Date()); //현재 시간
        
        chatArea.append("[" + timestamp + "] " + sender + ": " + message + "\n"); //메세지 추가
        chatArea.setCaretPosition(chatArea.getDocument().getLength()); //스크롤 맨 아래로
    }
    
    // 시스템 메시지 추가 - 입장/퇴장 알림
    private void appendSystemMessage(String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss"); //시간 포맷
        String timestamp = sdf.format(new Date()); //현재 시간
        
        chatArea.append("[" + timestamp + "] [시스템] " + message + "\n"); //시스템 메세지 추가
        chatArea.setCaretPosition(chatArea.getDocument().getLength()); //스크롤 맨 아래로
    }
    
    // 접속자 수 업데이트
    private void updateUserCount(int count) {
        SwingUtilities.invokeLater(new Runnable() { //UI 업데이트는 EDT에서
            public void run() {
                userCountLabel.setText("접속자 (" + count + "명)"); //접속자 수 표시
            }
        });
    }
    
    // 관리자 전용: 현재 채팅방 로그 다운로드
    private void downloadCurrentRoomLog() {
        if (currentRoomId == null || currentRoomId.isEmpty()) { //채팅방에 입장하지 않았으면
            JOptionPane.showMessageDialog(this, "현재 채팅방에 입장하지 않았습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            // 현재 채팅방 로그 파일명 - chatlog_room_영화코드.txt
            String logFileName = "chatlog_" + currentRoomId + ".txt";
            java.io.File logFile = new java.io.File(logFileName);
            
            if (!logFile.exists()) { //로그 파일이 없으면
                JOptionPane.showMessageDialog(this, "채팅 로그가 없습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // 다운로드 파일명 생성 - chatlog_영화명_타임스탬프.txt
            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            String downloadFileName = "chatlog_" + currentMovieName + "_" + timestamp + ".txt";
            
            // 원본 파일 내용 읽기
            java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(logFile));
            java.io.FileWriter fw = new java.io.FileWriter(downloadFileName); //새 파일 생성
            
            // 헤더 작성 - 채팅 로그 정보
            fw.write("========================================\n");
            fw.write("MovieSync 채팅 로그\n");
            fw.write("영화: " + currentMovieName + "\n");
            fw.write("방 ID: " + currentRoomId + "\n");
            fw.write("다운로드 시간: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()) + "\n");
            fw.write("========================================\n\n");
            
            // 로그 내용 복사 - 원본 파일의 모든 줄을 새 파일에 복사
            String line;
            while ((line = br.readLine()) != null) { //한 줄씩 읽기
                fw.write(line + "\n"); //새 파일에 쓰기
            }
            
            br.close(); //파일 닫기
            fw.close();
            
            //성공 메세지
            JOptionPane.showMessageDialog(this, 
                "채팅 로그가 저장되었습니다!\n파일명: " + downloadFileName + "\n저장 위치: 프로그램 실행 폴더", 
                "다운로드 완료", 
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (java.io.IOException ex) { //파일 입출력 오류
            JOptionPane.showMessageDialog(this, 
                "로그 다운로드 실패: " + ex.getMessage(), 
                "오류", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // 서버 메시지 처리 - Client의 메세지 수신 스레드에서 호출됨
    public void handleMessage(String message) {
        if (message.startsWith("MOVIES_COUNT")) {
            // 영화 개수: MOVIES_COUNT|개수|END - tempMovieList 초기화
            tempMovieList.clear(); //기존 목록 비우기
            
        } else if (message.startsWith("MOVIES_DATA")) {
            // 영화 데이터: MOVIES_DATA|영화코드|영화제목|순위|개봉일|누적관객|누적매출|END
            String[] parts = message.split("\\|"); //| 구분자로 분리 (이스케이프 필요)
            if (parts.length >= 7 && isMainView) { //메인 화면일 때만 처리
                MovieRoomData movie = new MovieRoomData( //영화 데이터 객체 생성
                    parts[1], // movieCd - 영화 코드
                    parts[2], // movieNm - 영화 제목
                    Integer.parseInt(parts[3]), // rank - 순위
                    parts[4], // openDt - 개봉일
                    Long.parseLong(parts[5])  // audiAcc - 누적 관객수
                );
                tempMovieList.add(movie); //임시 목록에 추가
            }
            
        } else if (message.startsWith("MOVIES_END")) {
            // 영화 목록 로딩 완료: MOVIES_END|END - 순위 재정렬 후 화면에 표시
            if (isMainView) { //메인 화면일 때만 처리
                // 순위 재정렬 (중복 순위 제거) - 1위부터 순서대로
                for (int i = 0; i < tempMovieList.size(); i++) {
                    tempMovieList.get(i).rank = i + 1; //1, 2, 3, ...
                }
                
                SwingUtilities.invokeLater(new Runnable() { //UI 업데이트는 EDT에서
                    public void run() {
                        roomListPanel.removeAll(); //기존 카드 모두 제거
                        
                        if (tempMovieList.isEmpty()) { //영화가 없으면
                            JLabel emptyLabel = new JLabel("현재 개설된 채팅방이 없습니다.");
                            emptyLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
                            emptyLabel.setForeground(new Color(150, 150, 150)); //연한 회색
                            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT); //중앙 정렬
                            
                            roomListPanel.add(Box.createVerticalStrut(50)); //상단 여백
                            roomListPanel.add(emptyLabel);
                        } else { //영화가 있으면
                            for (MovieRoomData movie : tempMovieList) { //모든 영화에 대해
                                JPanel card = createRoomCard(movie); //카드 생성
                                roomListPanel.add(card); //패널에 추가
                                roomListPanel.add(Box.createVerticalStrut(10)); //카드 사이 10px 간격
                            }
                        }
                        
                        roomListPanel.revalidate(); //레이아웃 재계산
                        roomListPanel.repaint(); //화면 다시 그리기
                    }
                });
            }
            
        } else if (message.startsWith("ROOM_OK")) {
            // 입장 성공: ROOM_OK|roomId|영화제목|END
            SwingUtilities.invokeLater(new Runnable() { //UI 업데이트는 EDT에서
                public void run() {
                    isInRoom = true; //입장 상태로 변경
                    messageField.setEnabled(true); //메세지 입력 활성화
                    sendButton.setEnabled(true); //전송 버튼 활성화
                    appendSystemMessage("채팅방에 입장했습니다."); //시스템 메세지 추가
                }
            });
            
        } else if (message.startsWith("USER_JOIN")) {
            // 사용자 입장: USER_JOIN|닉네임|현재인원|END
            String[] parts = message.split("\\|"); //| 구분자로 분리
            if (parts.length >= 3) {
                String joinedUser = parts[1]; //입장한 사용자 닉네임
                int userCount = Integer.parseInt(parts[2]); //현재 인원
                
                SwingUtilities.invokeLater(new Runnable() { //UI 업데이트는 EDT에서
                    public void run() {
                        if (!userListModel.contains(joinedUser)) { //목록에 없으면
                            userListModel.addElement(joinedUser); //접속자 목록에 추가
                        }
                        updateUserCount(userCount); //접속자 수 업데이트
                        
                        if (!joinedUser.equals(username)) { //본인이 아니면
                            appendSystemMessage(joinedUser + "님이 입장했습니다."); //입장 알림
                        }
                    }
                });
            }
            
        } else if (message.startsWith("USER_LEFT")) {
            // 사용자 퇴장: USER_LEFT|닉네임|현재인원|END
            String[] parts = message.split("\\|"); //| 구분자로 분리
            if (parts.length >= 3) {
                String leftUser = parts[1]; //퇴장한 사용자 닉네임
                int userCount = Integer.parseInt(parts[2]); //현재 인원
                
                SwingUtilities.invokeLater(new Runnable() { //UI 업데이트는 EDT에서
                    public void run() {
                        userListModel.removeElement(leftUser); //접속자 목록에서 제거
                        updateUserCount(userCount); //접속자 수 업데이트
                        appendSystemMessage(leftUser + "님이 퇴장했습니다."); //퇴장 알림
                    }
                });
            }
            
        } else if (message.startsWith("CHAT_ALL")) {
            // 채팅 메시지: CHAT_ALL|발신자|메시지|END
            String[] parts = message.split("\\|", 4); //최대 4개로 분리 (메세지에 |가 있을 수 있음)
            if (parts.length >= 3) {
                String sender = parts[1]; //발신자 닉네임
                String chatMessage = parts[2]; //메세지 내용
                
                SwingUtilities.invokeLater(new Runnable() { //UI 업데이트는 EDT에서
                    public void run() {
                        appendChatMessage(sender, chatMessage); //채팅 메세지 추가
                    }
                });
            }
        }
    }
    
    // 영화 데이터 클래스 (채팅방 목록용)
    static class MovieRoomData {
        String movieCd; //영화 코드
        String movieNm; //영화 제목
        int rank; //순위
        String openDt; //개봉일
        long audiAcc; //누적 관객수
        
        MovieRoomData(String movieCd, String movieNm, int rank, String openDt, long audiAcc) { //생성자
            this.movieCd = movieCd;
            this.movieNm = movieNm;
            this.rank = rank;
            this.openDt = openDt;
            this.audiAcc = audiAcc;
        }
    }
}