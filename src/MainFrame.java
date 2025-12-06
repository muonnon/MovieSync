//프로젝트 이름 : MovieSync
//개발자 : 권미리
//개발 기간: 2025.12.01 ~ 2025.12.13
// 메인 프레임 GUI - JTree 메뉴와 CardLayout을 이용한 화면 전환

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;

/**
 * 로그인 후 보여지는 메인 화면
 * 왼쪽에 JTree 메뉴, 오른쪽에 CardLayout으로 화면 전환
 */
public class MainFrame extends JFrame {
    // 서버 통신 관련
    private Client client;      // 서버와 통신하는 클라이언트
    private String username;    // 로그인한 사용자 닉네임
    
    // GUI 컴포넌트
    private JTree menuTree;         // 왼쪽 메뉴 트리
    private JPanel contentPanel;    // 오른쪽 컨텐츠 영역
    private CardLayout cardLayout;  // 화면 전환을 위한 레이아웃
    
    // 각 화면 패널들 (메뉴에 따라 전환됨)
    private MovieListPanel movieListPanel;   // 영화 목록 화면
    private ChatPanel chatPanel;             // 채팅방 화면
    private ReviewPanel reviewPanel;         // 감상평 화면
    private BookmarkPanel bookmarkPanel;     // 북마크 화면
    
    /**
     * 생성자 - 메인 화면 초기화
     * @param client 서버 통신 객체
     * @param username 로그인한 사용자 닉네임
     */
    public MainFrame(Client client, String username) {
        this.client = client;
        this.username = username;
        
        setTitle("MovieSync - " + username);
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // 메인 레이아웃
        setLayout(new BorderLayout());
        
        // 상단 패널 (타이틀 + 로그아웃)
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(33, 150, 243));
        topPanel.setPreferredSize(new Dimension(0, 60));
        
        JLabel titleLabel = new JLabel("  🎬 MovieSync");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel, BorderLayout.WEST);
        
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setOpaque(false);
        JLabel userLabel = new JLabel(username + "님 환영합니다  ");
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        
        JButton logoutButton = new JButton("로그아웃");
        logoutButton.setBackground(Color.WHITE);
        logoutButton.setForeground(new Color(33, 150, 243));
        logoutButton.setFocusPainted(false);
        // 로그아웃 버튼 클릭 이벤트 설정
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });
        
        userPanel.add(userLabel);
        userPanel.add(logoutButton);
        topPanel.add(userPanel, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        
        // 왼쪽 메뉴 (JTree)
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(200, 0));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
        
        // JTree 생성
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("MovieSync");
        DefaultMutableTreeNode moviesNode = new DefaultMutableTreeNode("🎬 영화 목록");
        DefaultMutableTreeNode chatNode = new DefaultMutableTreeNode("💬 채팅방");
        DefaultMutableTreeNode reviewNode = new DefaultMutableTreeNode("⭐ 감상평");
        DefaultMutableTreeNode bookmarkNode = new DefaultMutableTreeNode("🔖 북마크");
        
        root.add(moviesNode);
        root.add(chatNode);
        root.add(reviewNode);
        root.add(bookmarkNode);
        
        menuTree = new JTree(root);
        menuTree.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        menuTree.setRowHeight(30);
        menuTree.setRootVisible(false);
        menuTree.setShowsRootHandles(true);
        
        // 첫 번째 노드 확장
        menuTree.expandRow(0);
        
        // 트리 선택 리스너 - 메뉴 클릭 시 화면 전환
        menuTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            @Override
            public void valueChanged(javax.swing.event.TreeSelectionEvent e) {
                DefaultMutableTreeNode selectedNode = 
                    (DefaultMutableTreeNode) menuTree.getLastSelectedPathComponent();
                
                if (selectedNode != null && selectedNode.isLeaf()) {
                    String nodeName = selectedNode.toString();
                    handleMenuSelection(nodeName);
                }
            }
        });
        
        JScrollPane treeScrollPane = new JScrollPane(menuTree);
        treeScrollPane.setBorder(null);
        leftPanel.add(treeScrollPane, BorderLayout.CENTER);
        
        add(leftPanel, BorderLayout.WEST);
        
        // 오른쪽 컨텐츠 영역 (CardLayout)
        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        
        // 각 패널 생성
        movieListPanel = new MovieListPanel(client, this);
        chatPanel = new ChatPanel(client, username);
        reviewPanel = new ReviewPanel(client, username);
        bookmarkPanel = new BookmarkPanel(client, this);
        
        contentPanel.add(movieListPanel, "MOVIES");
        contentPanel.add(chatPanel, "CHAT");
        contentPanel.add(reviewPanel, "REVIEW");
        contentPanel.add(bookmarkPanel, "BOOKMARK");
        
        add(contentPanel, BorderLayout.CENTER);
        
        // 초기 화면: 영화 목록
        cardLayout.show(contentPanel, "MOVIES");
        movieListPanel.loadMovies();
        
        setVisible(true);
    }
    
    // 메뉴 선택 처리
    private void handleMenuSelection(String menuName) {
        if (menuName.contains("영화 목록")) {
            cardLayout.show(contentPanel, "MOVIES");
            movieListPanel.loadMovies();
        } else if (menuName.contains("채팅방")) {
            cardLayout.show(contentPanel, "CHAT");
        } else if (menuName.contains("감상평")) {
            cardLayout.show(contentPanel, "REVIEW");
        } else if (menuName.contains("북마크")) {
            cardLayout.show(contentPanel, "BOOKMARK");
            bookmarkPanel.loadBookmarks();
        }
    }
    
    // 특정 영화의 채팅방으로 이동
    public void showChatRoom(String movieCd, String movieNm) {
        cardLayout.show(contentPanel, "CHAT");
        chatPanel.joinRoom(movieCd, movieNm);
    }
    
    // 특정 영화의 감상평으로 이동
    public void showReviews(String movieCd, String movieNm) {
        cardLayout.show(contentPanel, "REVIEW");
        reviewPanel.loadReviews(movieCd, movieNm);
    }
    
    // 로그아웃
    private void logout() {
        int result = JOptionPane.showConfirmDialog(this, 
            "로그아웃 하시겠습니까?", 
            "로그아웃", 
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            client.disconnect();
            dispose();
            new LoginFrame();
        }
    }
    
    // 종료 시 처리
    @Override
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            client.disconnect();
        }
        super.processWindowEvent(e);
    }
}
