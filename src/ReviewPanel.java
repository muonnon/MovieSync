//프로젝트 이름 : MovieSync
//개발자 : 권미리
//개발 기간: 2025.12.01 ~ 2025.12.13
// 감상평 패널 - 영화별 감상평 조회/작성 기능 (개발 예정)

import javax.swing.*;
import java.awt.*;

/**
 * 감상평 화면을 담당하는 패널
 * 영화별 감상평 조회 및 작성 기능 제공 (현재 개발 중)
 */
public class ReviewPanel extends JPanel {
    private Client client;      // 서버 통신 객체
    private String username;    // 현재 사용자 닉네임
    
    /**
     * 생성자
     * @param client 서버 통신 객체
     * @param username 현재 로그인한 사용자 닉네임
     */
    public ReviewPanel(Client client, String username) {
        this.client = client;
        this.username = username;
        
        setLayout(new BorderLayout());
        
        // 임시 안내 메시지
        JLabel label = new JLabel("감상평 화면 (개발 중)", SwingConstants.CENTER);
        label.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        add(label, BorderLayout.CENTER);
    }
    
    /**
     * 특정 영화의 감상평 로드
     * @param movieCd 영화 코드
     * @param movieNm 영화 제목
     */
    public void loadReviews(String movieCd, String movieNm) {
        System.out.println("ReviewPanel> 감상평 로드: " + movieNm);
        // TODO: 감상평 조회 기능 구현 예정
    }
}