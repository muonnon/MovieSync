//프로젝트 이름 : MovieSync
//개발자 : 권미리
//개발 기간: 2025.12.01 ~ 2025.12.13

// 감상평 패널 (임시)

import javax.swing.*;
import java.awt.*;

public class ReviewPanel extends JPanel {
    private Client client;
    private String username;
    
    public ReviewPanel(Client client, String username) {
        this.client = client;
        this.username = username;
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        JLabel label = new JLabel("감상평 화면 (개발 중)", SwingConstants.CENTER);
        label.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        add(label, BorderLayout.CENTER);
    }
    
    public void loadReviews(String movieCd, String movieNm) {
        System.out.println("ReviewPanel> 감상평 로드: " + movieNm);
        // TODO: 구현 예정
    }
}