//프로젝트 이름 : MovieSync
//개발자 : 권미리
//개발 기간: 2025.12.01 ~ 2025.12.13

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class ReviewPanel extends JPanel {
    private Client client;
    private String username;
    private MainFrame mainFrame;
    
    // 현재 상태
    private boolean isMainView = true;     // true: 메인 화면(전체), false: 특정 영화
    private String currentMovieCd = null;
    private String currentMovieNm = null;
    
    // 감상평 데이터
    private ArrayList<ReviewData> reviews = new ArrayList<ReviewData>();
    
    // GUI 컴포넌트
    private JLabel titleLabel;
    private JLabel summaryLabel;
    private JButton writeButton;
    private JButton leaveButton;
    private JPanel reviewListPanel;
    private JScrollPane scrollPane;
    
    public ReviewPanel(Client client, String username, MainFrame mainFrame) {
        this.client = client;
        this.username = username;
        this.mainFrame = mainFrame;
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 상단 패널
        JPanel topPanel = new JPanel(new BorderLayout());
        
        // 타이틀
        JPanel titlePanel = new JPanel(new BorderLayout());
        titleLabel = new JLabel("감상평을 선택해주세요");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        
        summaryLabel = new JLabel(" ");
        summaryLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        summaryLabel.setForeground(new Color(100, 100, 100));
        titlePanel.add(summaryLabel, BorderLayout.SOUTH);
        
        topPanel.add(titlePanel, BorderLayout.WEST);
        
        // 나가기 버튼
        leaveButton = new JButton("나가기");
        leaveButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        leaveButton.setEnabled(true);  // 항상 활성화
        leaveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (isMainView) {
                    // 메인 화면 → 영화 목록
                    mainFrame.showMovieList();
                } else {
                    // 특정 영화 → 메인 화면 (전체 감상평)
                    loadAllReviews();
                }
            }
        });
        topPanel.add(leaveButton, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        
        // 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        writeButton = new JButton("감상평 작성하기");
        writeButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        writeButton.setEnabled(false);
        writeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showWriteDialog();
            }
        });
        buttonPanel.add(writeButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // 중앙: 감상평 목록
        reviewListPanel = new JPanel();
        reviewListPanel.setLayout(new BoxLayout(reviewListPanel, BoxLayout.Y_AXIS));
        reviewListPanel.setBackground(Color.WHITE);
        
        scrollPane = new JScrollPane(reviewListPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    // 특정 영화의 감상평 로드 (영화 목록에서 버튼 클릭)
    public void loadReviews(String movieCd, String movieNm) {
        isMainView = false;  // 특정 영화 화면
        currentMovieCd = movieCd;
        currentMovieNm = movieNm;
        
        titleLabel.setText("감상평: " + movieNm);
        summaryLabel.setText("로딩 중...");
        
        // UI 초기화
        reviews.clear();
        reviewListPanel.removeAll();
        reviewListPanel.revalidate();
        reviewListPanel.repaint();
        
        writeButton.setEnabled(true);
        leaveButton.setEnabled(true);
        
        // 서버에 감상평 요청
        client.requestReviews(movieCd);
    }
    
    // 전체 감상평 로드 (메뉴에서 진입 시)
    public void loadAllReviews() {
        isMainView = true;  // 메인 화면
        currentMovieCd = null;
        currentMovieNm = null;
        
        titleLabel.setText("전체 감상평");
        summaryLabel.setText("로딩 중...");
        
        // UI 초기화
        reviews.clear();
        reviewListPanel.removeAll();
        reviewListPanel.revalidate();
        reviewListPanel.repaint();
        
        writeButton.setEnabled(false);  // 전체 목록에서는 작성 불가
        leaveButton.setEnabled(true);   // 나가기는 항상 활성화
        
        // 서버에 전체 감상평 요청
        client.requestAllReviews();
    }
    
    // 감상평 작성 다이얼로그
    private void showWriteDialog() {
        if (currentMovieCd == null) {
            return;
        }
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "감상평 작성", true);
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // 영화 제목
        JLabel movieLabel = new JLabel("영화: " + currentMovieNm);
        movieLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        movieLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(movieLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        
        // 별점 선택
        JLabel ratingLabel = new JLabel("평점: ★☆☆☆☆ (1점)");
        ratingLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        ratingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(ratingLabel);
        
        JSlider ratingSlider = new JSlider(1, 5, 3);
        ratingSlider.setMajorTickSpacing(1);
        ratingSlider.setPaintTicks(true);
        ratingSlider.setPaintLabels(true);
        ratingSlider.setSnapToTicks(true);
        ratingSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        ratingSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                int rating = ratingSlider.getValue();
                String stars = "";
                for (int i = 0; i < rating; i++) {
                    stars += "★";
                }
                for (int i = rating; i < 5; i++) {
                    stars += "☆";
                }
                ratingLabel.setText("평점: " + stars + " (" + rating + "점)");
            }
        });
        contentPanel.add(ratingSlider);
        contentPanel.add(Box.createVerticalStrut(15));
        
        // 내용 입력
        JLabel contentLabel = new JLabel("내용:");
        contentLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        contentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(contentLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        
        JTextArea contentArea = new JTextArea(10, 40);
        contentArea.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        
        JScrollPane contentScroll = new JScrollPane(contentArea);
        contentScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(contentScroll);
        
        // 글자 수 카운터
        JLabel charCountLabel = new JLabel("0 / 1000자");
        charCountLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        charCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateCount();
            }
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateCount();
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateCount();
            }
            void updateCount() {
                int length = contentArea.getText().length();
                charCountLabel.setText(length + " / 1000자");
                if (length > 1000) {
                    charCountLabel.setForeground(Color.RED);
                } else {
                    charCountLabel.setForeground(Color.BLACK);
                }
            }
        });
        contentPanel.add(charCountLabel);
        
        dialog.add(contentPanel, BorderLayout.CENTER);
        
        // 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton cancelButton = new JButton("취소");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        buttonPanel.add(cancelButton);
        
        JButton submitButton = new JButton("작성 완료");
        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String content = contentArea.getText().trim();
                
                // 유효성 검사
                if (content.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "내용을 입력해주세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                if (content.length() > 1000) {
                    JOptionPane.showMessageDialog(dialog, "내용은 1000자 이하로 입력해주세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                int rating = ratingSlider.getValue();
                
                // 서버에 감상평 전송
                client.submitReview(currentMovieCd, rating, content);
                
                dialog.dispose();
            }
        });
        buttonPanel.add(submitButton);
        
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    // 별점을 별 문자로 변환
    private String getStarString(int rating) {
        String stars = "";
        for (int i = 0; i < rating; i++) {
            stars += "★";
        }
        for (int i = rating; i < 5; i++) {
            stars += "☆";
        }
        return stars;
    }
    
    // 감상평 카드 생성
    private JPanel createReviewCard(ReviewData review) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(10, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        
        // 상단: 별점 + 작성자 + 날짜
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        JLabel starLabel = new JLabel(getStarString(review.rating));
        starLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        starLabel.setForeground(new Color(255, 193, 7));
        
        JLabel infoLabel = new JLabel(review.username + " (" + review.createdAt + ")");
        infoLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(100, 100, 100));
        
        headerPanel.add(starLabel, BorderLayout.WEST);
        headerPanel.add(infoLabel, BorderLayout.EAST);
        
        card.add(headerPanel, BorderLayout.NORTH);
        
        // 중앙: 감상평 내용
        JTextArea contentArea = new JTextArea(review.content);
        contentArea.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setBackground(Color.WHITE);
        contentArea.setBorder(null);
        
        card.add(contentArea, BorderLayout.CENTER);
        
        // 하단: 삭제 버튼 (본인 것만)
        if (review.username.equals(username)) {
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottomPanel.setBackground(Color.WHITE);
            
            JButton deleteButton = new JButton("삭제");
            deleteButton.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
            deleteButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int result = JOptionPane.showConfirmDialog(
                        ReviewPanel.this,
                        "정말 삭제하시겠습니까?",
                        "감상평 삭제",
                        JOptionPane.YES_NO_OPTION
                    );
                    
                    if (result == JOptionPane.YES_OPTION) {
                        client.deleteReview(review.reviewId);
                    }
                }
            });
            bottomPanel.add(deleteButton);
            
            card.add(bottomPanel, BorderLayout.SOUTH);
        }
        
        return card;
    }
    
    // 전체 감상평 카드 생성 (영화 제목 포함)
    private JPanel createAllReviewCard(ReviewData review) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(10, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        
        // 상단: 영화 제목 + 별점 + 작성자 + 날짜
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        // 영화 제목
        JLabel movieLabel = new JLabel("[" + review.movieNm + "]");
        movieLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        movieLabel.setForeground(new Color(33, 150, 243));
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        leftPanel.setBackground(Color.WHITE);
        
        JLabel starLabel = new JLabel(getStarString(review.rating));
        starLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        starLabel.setForeground(new Color(255, 193, 7));
        
        leftPanel.add(movieLabel);
        leftPanel.add(starLabel);
        
        JLabel infoLabel = new JLabel(review.username + " (" + review.createdAt + ")");
        infoLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(100, 100, 100));
        
        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(infoLabel, BorderLayout.EAST);
        
        card.add(headerPanel, BorderLayout.NORTH);
        
        // 중앙: 감상평 내용
        JTextArea contentArea = new JTextArea(review.content);
        contentArea.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setBackground(Color.WHITE);
        contentArea.setBorder(null);
        
        card.add(contentArea, BorderLayout.CENTER);
        
        // 하단: 삭제 버튼 (본인 것만)
        if (review.username.equals(username)) {
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottomPanel.setBackground(Color.WHITE);
            
            JButton deleteButton = new JButton("삭제");
            deleteButton.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
            deleteButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int result = JOptionPane.showConfirmDialog(
                        ReviewPanel.this,
                        "정말 삭제하시겠습니까?",
                        "감상평 삭제",
                        JOptionPane.YES_NO_OPTION
                    );
                    
                    if (result == JOptionPane.YES_OPTION) {
                        client.deleteReview(review.reviewId);
                    }
                }
            });
            bottomPanel.add(deleteButton);
            
            card.add(bottomPanel, BorderLayout.SOUTH);
        }
        
        return card;
    }
    
    // 서버 메시지 처리
    public void handleMessage(String message) {
        if (message.startsWith("REV_SUMMARY")) {
            // 요약 정보: REV_SUMMARY|영화코드|영화제목|평균평점|감상평개수|END
            String[] parts = message.split("\\|");
            if (parts.length >= 5) {
                double avgRating = Double.parseDouble(parts[3]);
                int count = Integer.parseInt(parts[4]);
                
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        String avgStars = getStarString((int) Math.round(avgRating));
                        summaryLabel.setText("평균 평점: " + avgStars + " (" + String.format("%.1f", avgRating) + "/5.0) - 총 " + count + "개");
                    }
                });
            }
            
        } else if (message.startsWith("ALL_REV_DATA")) {
            // 전체 감상평 데이터: ALL_REV_DATA|reviewId|영화제목|작성자|별점|내용|작성일시|END
            String[] parts = message.split("\\|", 8);
            if (parts.length >= 7) {
                ReviewData review = new ReviewData();
                review.reviewId = Integer.parseInt(parts[1]);
                review.movieNm = parts[2];  // 영화 제목 포함
                review.username = parts[3];
                review.rating = Integer.parseInt(parts[4]);
                review.content = parts[5];
                review.createdAt = parts[6];
                
                reviews.add(review);
                
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        JPanel card = createAllReviewCard(review);
                        reviewListPanel.add(card);
                        reviewListPanel.add(Box.createVerticalStrut(10));
                        reviewListPanel.revalidate();
                        reviewListPanel.repaint();
                    }
                });
            }
            
        } else if (message.startsWith("ALL_REV_END")) {
            // 전체 감상평 로딩 완료
            System.out.println("ReviewPanel> 전체 감상평 로드 완료: " + reviews.size() + "개");
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    summaryLabel.setText("총 " + reviews.size() + "개의 감상평");
                }
            });
            
        } else if (message.startsWith("REV_COUNT")) {
            // 감상평 개수 (무시)
            
        } else if (message.startsWith("REV_DATA")) {
            // 감상평 데이터: REV_DATA|reviewId|작성자|별점|내용|작성일시|END
            String[] parts = message.split("\\|", 7);
            if (parts.length >= 6) {
                ReviewData review = new ReviewData();
                review.reviewId = Integer.parseInt(parts[1]);
                review.username = parts[2];
                review.rating = Integer.parseInt(parts[3]);
                review.content = parts[4];
                review.createdAt = parts[5];
                
                reviews.add(review);
                
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        JPanel card = createReviewCard(review);
                        reviewListPanel.add(card);
                        reviewListPanel.add(Box.createVerticalStrut(10));
                        reviewListPanel.revalidate();
                        reviewListPanel.repaint();
                    }
                });
            }
            
        } else if (message.startsWith("REV_END")) {
            // 감상평 로딩 완료
            System.out.println("ReviewPanel> 감상평 로드 완료: " + reviews.size() + "개");
            
        } else if (message.startsWith("REV_OK")) {
            // 감상평 작성 성공
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JOptionPane.showMessageDialog(ReviewPanel.this, "감상평이 작성되었습니다!");
                    // 목록 새로고침
                    loadReviews(currentMovieCd, currentMovieNm);
                }
            });
            
        } else if (message.startsWith("REV_FAIL")) {
            // 감상평 작성 실패
            String[] parts = message.split("\\|");
            String reason = parts.length > 1 ? parts[1] : "알 수 없는 오류";
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JOptionPane.showMessageDialog(ReviewPanel.this, reason, "작성 실패", JOptionPane.ERROR_MESSAGE);
                }
            });
            
        } else if (message.startsWith("DEL_OK")) {
            // 감상평 삭제 성공
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JOptionPane.showMessageDialog(ReviewPanel.this, "감상평이 삭제되었습니다!");
                    // 목록 새로고침
                    loadReviews(currentMovieCd, currentMovieNm);
                }
            });
            
        } else if (message.startsWith("DEL_FAIL")) {
            // 감상평 삭제 실패
            String[] parts = message.split("\\|");
            String reason = parts.length > 1 ? parts[1] : "알 수 없는 오류";
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JOptionPane.showMessageDialog(ReviewPanel.this, reason, "삭제 실패", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }
    
    // 감상평 데이터 클래스
    class ReviewData {
        int reviewId;
        String movieNm;    // 영화 제목 (전체 감상평용)
        String username;
        int rating;
        String content;
        String createdAt;
    }
}