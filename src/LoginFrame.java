//프로젝트 이름 : MovieSync
//개발자 : 권미리
//개발 기간: 2025.12.01 ~ 2025.12.13
// 로그인 화면 GUI - 사용자가 닉네임을 입력하고 서버에 연결하는 화면

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/*로그인 화면을 담당하는 클래스
 * 사용자가 닉네임을 입력하고 서버에 연결할 수 있다
 */
public class LoginFrame extends JFrame {
	// GUI 컴포넌트들
	private JTextField usernameField; // 닉네임 입력 필드
	private JButton loginButton; // 연결 버튼
	private Client client; // 서버와 통신하는 클라이언트 객체

	// 생성자 - 로그인 화면 초기화
	public LoginFrame() {
		// 기본 윈도우 설정
		setTitle("MovieSync - 로그인");
		setSize(400, 350);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // X버튼 클릭시 프로그램 종료
		setLocationRelativeTo(null); // 화면 중앙에 배치
		setResizable(false); // 크기 고정

		// 메인 패널 생성 - GridBagLayout으로 컴포넌트 배치
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10);

		// 타이틀
		JLabel titleLabel = new JLabel("MovieSync");
		titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
		titleLabel.setForeground(new Color(33, 150, 243)); // 파란색
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		mainPanel.add(titleLabel, gbc);

		// 부제목
		JLabel subtitleLabel = new JLabel("실시간 영화 감상평 공유 플랫폼");
		subtitleLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
		subtitleLabel.setForeground(Color.GRAY);
		gbc.gridy = 1;
		mainPanel.add(subtitleLabel, gbc);

		// 간격
		gbc.gridy = 2;
		mainPanel.add(Box.createVerticalStrut(30), gbc);

		// 닉네임 레이블
		JLabel usernameLabel = new JLabel("닉네임:");
		usernameLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
		gbc.gridwidth = 1;
		gbc.gridy = 3;
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.EAST;
		mainPanel.add(usernameLabel, gbc);

		// 닉네임 입력 필드 - 스타일 개선
		usernameField = new JTextField(15);
		usernameField.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
		usernameField.setPreferredSize(new Dimension(200, 35));
		usernameField.setMinimumSize(new Dimension(200, 35));
		usernameField.setBorder(
				BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
						BorderFactory.createEmptyBorder(5, 10, 5, 10)));
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		mainPanel.add(usernameField, gbc);

		// Enter 키로 로그인
		usernameField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				attemptLogin();
			}
		});

		// 로그인 버튼 - 스타일 개선
		loginButton = new JButton("연결하기");
		loginButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
		loginButton.setBackground(new Color(33, 150, 243));
		loginButton.setForeground(Color.WHITE);
		loginButton.setFocusPainted(false);
		loginButton.setPreferredSize(new Dimension(200, 40));
		loginButton.setOpaque(true);
		loginButton.setBorderPainted(false);
		loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(loginButton, gbc);

		// 로그인 버튼 액션
		loginButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				attemptLogin();
			}
		});

		// 서버 주소 표시
		JLabel serverLabel = new JLabel("서버: localhost:55555");
		serverLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
		serverLabel.setForeground(Color.GRAY);
		gbc.gridy = 5;
		mainPanel.add(serverLabel, gbc);

		add(mainPanel);
		setVisible(true);
	}

	// 로그인 시도
	private void attemptLogin() {
		String username = usernameField.getText().trim();

		// 유효성 검사
		if (username.isEmpty()) {
			JOptionPane.showMessageDialog(this, "닉네임을 입력해주세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (username.length() < 2 || username.length() > 20) {
			JOptionPane.showMessageDialog(this, "닉네임은 2-20자 사이여야 합니다.", "입력 오류", JOptionPane.WARNING_MESSAGE);
			return;
		}

		// 버튼 비활성화 (중복 클릭 방지)
		loginButton.setEnabled(false);
		loginButton.setText("연결 중...");

		// 서버 연결 시도
		client = new Client();

		// 메시지 콜백 설정
		client.setMessageCallback(new Client.MessageCallback() {
			@Override
			public void onMessageReceived(String message) {
				handleServerMessage(message);
			}
		});

		// 서버 연결
		if (client.connectToServer(username)) {
			System.out.println("LoginFrame> 서버 연결 성공");
		} else {
			// 연결 실패 시 UI 업데이트
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(LoginFrame.this, "서버에 연결할 수 없습니다.\n서버가 실행 중인지 확인해주세요.", "연결 실패",
							JOptionPane.ERROR_MESSAGE);
					loginButton.setEnabled(true);
					loginButton.setText("연결하기");
				}
			});
		}
	}

	// 서버 메시지 처리
	private void handleServerMessage(String message) {
		System.out.println("LoginFrame> 수신: " + message);

		// LOGIN_OK 메시지 확인
		if (message.startsWith("LOGIN_OK")) {
			// 로그인 성공 - 메인 화면으로 전환
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					String username = usernameField.getText().trim();
					new MainFrame(client, username);
					dispose(); // 로그인 창 닫기
				}
			});
		} else if (message.startsWith("LOGIN_FAIL")) {
			// 로그인 실패 - 에러 메시지 표시
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					String[] parts = message.split("\\|");
					String reason = parts.length > 1 ? parts[1] : "알 수 없는 오류";

					JOptionPane.showMessageDialog(LoginFrame.this, reason, "로그인 실패", JOptionPane.ERROR_MESSAGE);

					loginButton.setEnabled(true);
					loginButton.setText("연결하기");
					client.disconnect();
				}
			});
		}
	}

	/*
	 * main 메소드 LoginFrame을 단독으로 실행할 때 사용
	 */
	public static void main(String[] args) {
		// Swing 컴포넌트는 EDT(Event Dispatch Thread)에서 실행해야 함
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new LoginFrame();
			}
		});
	}
}