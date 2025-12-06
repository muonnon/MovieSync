//프로젝트 이름 : MovieSync
//개발자 : 권미리
//개발 기간: 2025.12.01 ~ 2025.12.13
// 북마크 패널 - 사용자의 북마크한 영화 목록 (개발 예정)

import javax.swing.*;
import java.awt.*;

/**
 * 북마크 화면을 담당하는 패널
 * 사용자가 북마크한 영화 목록 표시 (현재 개발 중)
 */
public class BookmarkPanel extends JPanel {
    private Client client;          // 서버 통신 객체
    private MainFrame mainFrame;    // 부모 프레임 참조
    
    /**
     * 생성자
     * @param client 서버 통신 객체
     * @param mainFrame 부모 프레임
     */
    public BookmarkPanel(Client client, MainFrame mainFrame) {
        this.client = client;
        this.mainFrame = mainFrame;
        
        setLayout(new BorderLayout());
        
        // 임시 안내 메시지
        JLabel label = new JLabel("북마크 화면 (개발 중)", SwingConstants.CENTER);
        label.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        add(label, BorderLayout.CENTER);
    }
    
    /**
     * 북마크 목록 로드
     */
    public void loadBookmarks() {
        System.out.println("BookmarkPanel> 북마크 로드");
        // TODO: 북마크 목록 조회 기능 구현 예정
    }
}
