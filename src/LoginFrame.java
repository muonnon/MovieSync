//프로젝트 이름 : MovieSync
//개발자 : 권미리
//개발 기간: 2025.12.01 ~ 2025.12.13

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

//로그인 화면 GUI - 닉네임 입력하고 서버 연결
public class LoginFrame extends JFrame {
    // GUI 컴포넌트
    private JTextField usernameField;  //닉네임 입력 필드
    private JButton loginButton;       //연결 버튼
    private Client client;             //서버 통신 객체
    
    //생성자 - 로그인 화면 초기화
    public LoginFrame() {
        // 기본 윈도우 설정
        setTitle("MovieSync - 로그인");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  //X버튼 클릭 시 프로그램 종료
        setLocationRelativeTo(null);  //화면 중앙 배치
        setResizable(false);  //크기 고정
        
        // 메인 패널 생성 - GridBagLayout으로 컴포넌트 배치
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); //여백 10픽셀
        
        // 타이틀 - "MovieSync" (파란색, 굵게, 28pt)
        JLabel titleLabel = new JLabel("MovieSync");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        titleLabel.setForeground(new Color(33, 150, 243)); //파란색 (#2196F3)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; //2칸 차지
        mainPanel.add(titleLabel, gbc);
        
        // 부제목 - "실시간 영화 감상평 공유 플랫폼"
        JLabel subtitleLabel = new JLabel("실시간 영화 감상평 공유 플랫폼");
        subtitleLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);
        gbc.gridy = 1;
        mainPanel.add(subtitleLabel, gbc);
        
        // 간격 추가 - 30픽셀
        gbc.gridy = 2;
        mainPanel.add(Box.createVerticalStrut(30), gbc);
        
        // 닉네임 레이블 - 오른쪽 정렬
        JLabel usernameLabel = new JLabel("닉네임:");
        usernameLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        gbc.gridwidth = 1;
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST; //오른쪽 정렬
        mainPanel.add(usernameLabel, gbc);
        
        // 닉네임 입력 필드 - 200x35 크기, 테두리 스타일
        usernameField = new JTextField(15);
        usernameField.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        usernameField.setPreferredSize(new Dimension(200, 35));
        usernameField.setMinimumSize(new Dimension(200, 35));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1), //회색 테두리
            BorderFactory.createEmptyBorder(5, 10, 5, 10) //내부 여백
        ));
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST; //왼쪽 정렬
        gbc.fill = GridBagConstraints.HORIZONTAL; //가로로 채우기
        mainPanel.add(usernameField, gbc);
        
        // Enter 키로 로그인 - ActionListener 등록
        usernameField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                attemptLogin(); //로그인 시도
            }
        });
        
        // 로그인 버튼 - 파란 배경, 흰 글씨, 200x40 크기
        loginButton = new JButton("연결하기");
        loginButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        loginButton.setBackground(new Color(33, 150, 243)); //파란색
        loginButton.setForeground(Color.WHITE); //흰 글씨
        loginButton.setFocusPainted(false); //포커스 테두리 제거
        loginButton.setPreferredSize(new Dimension(200, 40));
        loginButton.setOpaque(true); //불투명
        loginButton.setBorderPainted(false); //테두리 제거
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); //손 모양 커서
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER; //중앙 정렬
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(loginButton, gbc);
        
        // 로그인 버튼 클릭 이벤트
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                attemptLogin(); //로그인 시도
            }
        });
        
        // 서버 주소 표시 - "서버: localhost:55555" (회색, 작게)
        JLabel serverLabel = new JLabel("서버: localhost:55555");
        serverLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        serverLabel.setForeground(Color.GRAY);
        gbc.gridy = 5;
        mainPanel.add(serverLabel, gbc);
        
        add(mainPanel);
        setVisible(true);
    }
    
    //로그인 시도 - 유효성 검사 후 서버 연결
    private void attemptLogin() {
        String username = usernameField.getText().trim(); //앞뒤 공백 제거
        
        // 유효성 검사 - 빈 문자열
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "닉네임을 입력해주세요.", 
                "입력 오류", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 유효성 검사 - 길이 제한 (2-20자)
        if (username.length() < 2 || username.length() > 20) {
            JOptionPane.showMessageDialog(this, 
                "닉네임은 2-20자 사이여야 합니다.", 
                "입력 오류", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 버튼 비활성화 - 중복 클릭 방지
        loginButton.setEnabled(false);
        loginButton.setText("연결 중...");
        
        // 서버 연결 시도
        client = new Client();
        
        // 메시지 콜백 설정 - 서버 응답 받기
        client.setMessageCallback(new Client.MessageCallback() {
            public void onMessageReceived(String message) {
                handleServerMessage(message); //서버 메시지 처리
            }
        });
        
        // 서버 연결 - localhost:55555
        if (client.connectToServer(username)) {
            System.out.println("LoginFrame> 서버 연결 성공");
        } else {
            // 연결 실패 시 UI 업데이트 - EDT에서 실행
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JOptionPane.showMessageDialog(LoginFrame.this, 
                        "서버에 연결할 수 없습니다.\n서버가 실행 중인지 확인해주세요.", 
                        "연결 실패", 
                        JOptionPane.ERROR_MESSAGE);
                    loginButton.setEnabled(true); //버튼 활성화
                    loginButton.setText("연결하기"); //텍스트 복원
                }
            });
        }
    }
    
    //서버 메시지 처리 - LOGIN_OK 또는 LOGIN_FAIL
    private void handleServerMessage(String message) {
        System.out.println("LoginFrame> 수신: " + message);
        
        // LOGIN_OK - 로그인 성공
        if (message.startsWith("LOGIN_OK")) {
            // 메인 화면으로 전환 - EDT에서 실행
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    String username = usernameField.getText().trim();
                    new MainFrame(client, username); //메인 화면 생성
                    dispose();  //로그인 창 닫기
                }
            });
        } else if (message.startsWith("LOGIN_FAIL")) {
            // LOGIN_FAIL - 로그인 실패
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    String[] parts = message.split("\\|"); //| 로 분리
                    String reason = parts.length > 1 ? parts[1] : "알 수 없는 오류"; //실패 사유
                    
                    JOptionPane.showMessageDialog(LoginFrame.this, 
                        reason, 
                        "로그인 실패", 
                        JOptionPane.ERROR_MESSAGE);
                    
                    loginButton.setEnabled(true); //버튼 활성화
                    loginButton.setText("연결하기"); //텍스트 복원
                    client.disconnect(); //연결 종료
                }
            });
        }
    }
    
    //테스트용 main 메소드 - LoginFrame 단독 실행
    public static void main(String[] args) {
        // EDT(Event Dispatch Thread)에서 실행
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new LoginFrame(); //로그인 화면 생성
            }
        });
    }
}