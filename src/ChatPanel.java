//프로젝트 이름 : MovieSync
//개발자 : 권미리
//개발 기간: 2025.12.01 ~ 2025.12.13

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatPanel extends JPanel {
    private Client client;
    private String username;
    private MainFrame mainFrame;
    
    // 현재 상태
    private boolean isMainView = true;     // true: 메인 화면(목록), false: 특정 채팅방
    private boolean isInRoom = false;
    private String currentMovieCd = null;
    private String currentMovieNm = null;
    
    // GUI 컴포넌트
    private JLabel titleLabel;
    private JLabel userCountLabel;
    private JTextArea chatArea;
    private JScrollPane chatScrollPane;
    private JTextField messageField;
    private JButton sendButton;
    private JButton leaveButton;
    private DefaultListModel<String> userListModel;
    private JList<String> userList;
    
    // 메인 화면용 (채팅방 목록)
    private JPanel mainViewPanel;
    private JLabel mainTitleLabel;
    private JTextArea roomListArea;
    
    // 채팅방 화면용
    private JPanel chatRoomPanel;
    
    public ChatPanel(Client client, String username, MainFrame mainFrame) {
        this.client = client;
        this.username = username;
        this.mainFrame = mainFrame;
        
        setLayout(new CardLayout());
        
        // 메인 화면 (채팅방 목록)
        createMainView();
        
        // 채팅방 화면
        createChatRoomView();
        
        add(mainViewPanel, "MAIN");
        add(chatRoomPanel, "CHAT_ROOM");
        
        // 초기 화면: 메인 화면
        showMainView();
    }
    
    // 메인 화면 생성 (채팅방 목록)
    private void createMainView() {
        mainViewPanel = new JPanel(new BorderLayout(10, 10));
        mainViewPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 상단
        JPanel topPanel = new JPanel(new BorderLayout());
        
        mainTitleLabel = new JLabel("채팅방 목록");
        mainTitleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        topPanel.add(mainTitleLabel, BorderLayout.WEST);
        
        JButton refreshButton = new JButton("새로고침");
        refreshButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateRoomList();
            }
        });
        topPanel.add(refreshButton, BorderLayout.EAST);
        
        mainViewPanel.add(topPanel, BorderLayout.NORTH);
        
        // 중앙: 채팅방 목록
        roomListArea = new JTextArea();
        roomListArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        roomListArea.setEditable(false);
        roomListArea.setText("현재 활성화된 채팅방이 없습니다.\n\n영화 목록에서 '채팅방 입장' 버튼을 눌러\n채팅방을 만들어보세요!");
        
        JScrollPane scrollPane = new JScrollPane(roomListArea);
        mainViewPanel.add(scrollPane, BorderLayout.CENTER);
        
        // 하단: 나가기 버튼
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton mainLeaveButton = new JButton("나가기");
        mainLeaveButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        mainLeaveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainFrame.showMovieList();
            }
        });
        bottomPanel.add(mainLeaveButton);
        
        mainViewPanel.add(bottomPanel, BorderLayout.SOUTH);
    }
    
    // 채팅방 화면 생성
    private void createChatRoomView() {
        chatRoomPanel = new JPanel(new BorderLayout(10, 10));
        chatRoomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 상단: 타이틀 + 나가기
        JPanel topPanel = new JPanel(new BorderLayout());
        
        titleLabel = new JLabel("채팅방을 선택해주세요");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        topPanel.add(titleLabel, BorderLayout.WEST);
        
        leaveButton = new JButton("나가기");
        leaveButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        leaveButton.setEnabled(true);
        leaveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (isInRoom) {
                    leaveRoom();
                }
                showMainView();
            }
        });
        topPanel.add(leaveButton, BorderLayout.EAST);
        
        chatRoomPanel.add(topPanel, BorderLayout.NORTH);
        
        // 중앙: 채팅 영역 + 접속자 목록
        JPanel centerPanel = new JPanel(new BorderLayout(10, 0));
        
        // 채팅 영역
        chatArea = new JTextArea();
        chatArea.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        
        chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        centerPanel.add(chatScrollPane, BorderLayout.CENTER);
        
        // 접속자 목록
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setPreferredSize(new Dimension(150, 0));
        
        userCountLabel = new JLabel("접속자 (0명)");
        userCountLabel.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        userPanel.add(userCountLabel, BorderLayout.NORTH);
        
        userListModel = new DefaultListModel<String>();
        userList = new JList<String>(userListModel);
        userList.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        
        JScrollPane userScrollPane = new JScrollPane(userList);
        userPanel.add(userScrollPane, BorderLayout.CENTER);
        
        centerPanel.add(userPanel, BorderLayout.EAST);
        
        chatRoomPanel.add(centerPanel, BorderLayout.CENTER);
        
        // 하단: 메시지 입력
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 0));
        
        messageField = new JTextField();
        messageField.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        messageField.setEnabled(false);
        messageField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        
        sendButton = new JButton("전송");
        sendButton.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        sendButton.setEnabled(false);
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        
        chatRoomPanel.add(bottomPanel, BorderLayout.SOUTH);
    }
    
    // 메인 화면 표시 (채팅방 목록) - 메뉴에서 진입
    public void showMainView() {
        isMainView = true;
        CardLayout cl = (CardLayout) getLayout();
        cl.show(this, "MAIN");
        updateRoomList();
    }
    
    // 채팅방 목록 업데이트
    private void updateRoomList() {
        roomListArea.setText("현재 활성화된 채팅방이 없습니다.\n\n영화 목록에서 '채팅방 입장' 버튼을 눌러\n채팅방을 만들어보세요!");
    }
    
    // 특정 영화의 채팅방 입장 (영화 목록에서 버튼 클릭)
    public void joinRoom(String movieCd, String movieNm) {
        // 이전 채팅방에 있었다면 퇴장
        if (isInRoom) {
            leaveRoom();
        }
        
        currentMovieCd = movieCd;
        currentMovieNm = movieNm;
        isMainView = false;
        
        // 채팅방 화면으로 전환
        CardLayout cl = (CardLayout) getLayout();
        cl.show(this, "CHAT_ROOM");
        
        // UI 초기화
        chatArea.setText("");
        userListModel.clear();
        titleLabel.setText("채팅방: " + movieNm);
        userCountLabel.setText("접속자 (0명)");
        
        messageField.setEnabled(false);
        sendButton.setEnabled(false);
        
        appendSystemMessage("'" + movieNm + "' 채팅방에 입장합니다...");
        
        // 서버에 입장 요청
        client.joinRoom(movieCd, movieNm);
    }
    
    // 채팅방 퇴장
    public void leaveRoom() {
        if (!isInRoom || currentMovieCd == null) {
            return;
        }
        
        client.leaveRoom(currentMovieCd);
        
        isInRoom = false;
        messageField.setEnabled(false);
        sendButton.setEnabled(false);
        
        appendSystemMessage("채팅방에서 나갔습니다.");
    }
    
    // 메시지 전송
    private void sendMessage() {
        if (!isInRoom || currentMovieCd == null) {
            return;
        }
        
        String message = messageField.getText().trim();
        
        if (message.isEmpty()) {
            return;
        }
        
        if (message.length() > 500) {
            JOptionPane.showMessageDialog(this, "메시지는 500자 이하로 입력해주세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        client.sendChat(currentMovieCd, message);
        messageField.setText("");
    }
    
    // 채팅 메시지 추가
    private void appendChatMessage(String sender, String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String timestamp = sdf.format(new Date());
        
        chatArea.append("[" + timestamp + "] " + sender + ": " + message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
    
    // 시스템 메시지 추가
    private void appendSystemMessage(String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String timestamp = sdf.format(new Date());
        
        chatArea.append("[" + timestamp + "] [시스템] " + message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
    
    // 접속자 수 업데이트
    private void updateUserCount(int count) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                userCountLabel.setText("접속자 (" + count + "명)");
            }
        });
    }
    
    // 서버 메시지 처리
    public void handleMessage(String message) {
        if (message.startsWith("ROOM_OK")) {
            // 입장 성공: ROOM_OK|roomId|영화제목|END
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    isInRoom = true;
                    messageField.setEnabled(true);
                    sendButton.setEnabled(true);
                    appendSystemMessage("채팅방에 입장했습니다.");
                }
            });
            
        } else if (message.startsWith("USER_JOIN")) {
            // 사용자 입장: USER_JOIN|닉네임|현재인원|END
            String[] parts = message.split("\\|");
            if (parts.length >= 3) {
                String joinedUser = parts[1];
                int userCount = Integer.parseInt(parts[2]);
                
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (!userListModel.contains(joinedUser)) {
                            userListModel.addElement(joinedUser);
                        }
                        updateUserCount(userCount);
                        
                        if (!joinedUser.equals(username)) {
                            appendSystemMessage(joinedUser + "님이 입장했습니다.");
                        }
                    }
                });
            }
            
        } else if (message.startsWith("USER_LEFT")) {
            // 사용자 퇴장: USER_LEFT|닉네임|현재인원|END
            String[] parts = message.split("\\|");
            if (parts.length >= 3) {
                String leftUser = parts[1];
                int userCount = Integer.parseInt(parts[2]);
                
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        userListModel.removeElement(leftUser);
                        updateUserCount(userCount);
                        appendSystemMessage(leftUser + "님이 퇴장했습니다.");
                    }
                });
            }
            
        } else if (message.startsWith("CHAT_ALL")) {
            // 채팅 메시지: CHAT_ALL|발신자|메시지|END
            String[] parts = message.split("\\|", 4);
            if (parts.length >= 3) {
                String sender = parts[1];
                String chatMessage = parts[2];
                
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        appendChatMessage(sender, chatMessage);
                    }
                });
            }
        }
    }
}