//프로젝트 이름 : MovieSync
//개발자 : 권미리
//개발 기간: 2025.12.01 ~ 2025.12.13

// 채팅 패널 (임시)

import javax.swing.*;
import java.awt.*;

public class ChatPanel extends JPanel {
    private Client client;
    private String username;
    
    public ChatPanel(Client client, String username) {
        this.client = client;
        this.username = username;
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        JLabel label = new JLabel("채팅방 화면 (개발 중)", SwingConstants.CENTER);
        label.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        add(label, BorderLayout.CENTER);
    }
    
    public void joinRoom(String movieCd, String movieNm) {
        System.out.println("ChatPanel> 채팅방 입장: " + movieNm);
        // TODO: 구현 예정
    }
}