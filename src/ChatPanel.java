//프로젝트 이름 : MovieSync
//개발자 : 권미리
//개발 기간: 2025.12.01 ~ 2025.12.13
// 채팅 패널 - 영화별 실시간 채팅 기능 (개발 예정)

import javax.swing.*;
import java.awt.*;

/**
 * 채팅방 화면을 담당하는 패널
 * 영화별로 실시간 채팅 기능 제공 (현재 개발 중)
 */
public class ChatPanel extends JPanel {
    private Client client;      // 서버 통신 객체
    private String username;    // 현재 사용자 닉네임
    
    /**
     * 생성자
     * @param client 서버 통신 객체
     * @param username 현재 로그인한 사용자 닉네임
     */
    public ChatPanel(Client client, String username) {
        this.client = client;
        this.username = username;
        
        setLayout(new BorderLayout());
        
        // 임시 안내 메시지
        JLabel label = new JLabel("채팅방 화면 (개발 중)", SwingConstants.CENTER);
        label.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        add(label, BorderLayout.CENTER);
    }
    
    /**
     * 특정 영화의 채팅방에 입장
     * @param movieCd 영화 코드
     * @param movieNm 영화 제목
     */
    public void joinRoom(String movieCd, String movieNm) {
        System.out.println("ChatPanel> 채팅방 입장: " + movieNm);
        // TODO: 채팅방 입장 기능 구현 예정
    }
}