//프로젝트 이름 : MovieSync
//개발자 : 권미리
//개발 기간: 2025.12.01 ~ 2025.12.13
// 북마크 패널 (임시)

import javax.swing.*;
import java.awt.*;

public class BookmarkPanel extends JPanel {
    private Client client;
    private MainFrame mainFrame;
    
    public BookmarkPanel(Client client, MainFrame mainFrame) {
        this.client = client;
        this.mainFrame = mainFrame;
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        JLabel label = new JLabel("북마크 화면 (개발 중)", SwingConstants.CENTER);
        label.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        add(label, BorderLayout.CENTER);
    }
    
    public void loadBookmarks() {
        System.out.println("BookmarkPanel> 북마크 로드");
        // TODO: 구현 예정
    }
}


