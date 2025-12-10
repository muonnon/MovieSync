//프로젝트 이름 : MovieSync
//개발자 : 권미리
//개발 기간: 2025.12.01 ~ 2025.12.13
// 메인 프레임 GUI - JTree 메뉴와 CardLayout을 이용한 화면 전환

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;

/**
 * 로그인 후 보여지는 메인 화면
 * 왼쪽에 JTree 메뉴, 오른쪽에 CardLayout으로 화면 전환
 */
public class MainFrame extends JFrame {
    private Client client;
    private String username;
    private JTree menuTree;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private MovieListPanel movieListPanel;
    private ChatPanel chatPanel;
    private ReviewPanel reviewPanel;
    private BookmarkPanel bookmarkPanel;
    
    public MainFrame(Client client, String username) {
        this.client = client;
        this.username = username;
        
        setTitle("MovieSync - " + username);
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // 상단 패널
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(33, 150, 243));
        topPanel.setPreferredSize(new Dimension(0, 60));
        
        JLabel titleLabel = new JLabel("  MovieSync");
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
        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });
        
        userPanel.add(userLabel);
        userPanel.add(logoutButton);
        topPanel.add(userPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
        
        // 왼쪽 메뉴
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(200, 0));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
        
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("MovieSync");
        DefaultMutableTreeNode moviesNode = new DefaultMutableTreeNode("영화 목록");
        DefaultMutableTreeNode chatNode = new DefaultMutableTreeNode("채팅방");
        DefaultMutableTreeNode reviewNode = new DefaultMutableTreeNode("감상평");
        DefaultMutableTreeNode bookmarkNode = new DefaultMutableTreeNode("북마크");
        
        root.add(moviesNode);
        root.add(chatNode);
        root.add(reviewNode);
        root.add(bookmarkNode);
        
        menuTree = new JTree(root);
        menuTree.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        menuTree.setRowHeight(30);
        menuTree.setRootVisible(false);
        menuTree.setShowsRootHandles(true);
        menuTree.expandRow(0);
        
        menuTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
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
        
        // 컨텐츠 영역
        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        
        movieListPanel = new MovieListPanel(client, this);
        chatPanel = new ChatPanel(client, username, this);
        reviewPanel = new ReviewPanel(client, username, this);
        bookmarkPanel = new BookmarkPanel(client, this);
        
        contentPanel.add(movieListPanel, "MOVIES");
        contentPanel.add(chatPanel, "CHAT");
        contentPanel.add(reviewPanel, "REVIEW");
        contentPanel.add(bookmarkPanel, "BOOKMARK");
        
        add(contentPanel, BorderLayout.CENTER);
        
        // 메시지 핸들러 설정
        setupMessageHandler();
        
        cardLayout.show(contentPanel, "MOVIES");
        setVisible(true);
        
        // 초기 데이터 로드 (북마크 -> 영화 목록 순서)
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                movieListPanel.initialize();
            }
        });
    }
    
    private void setupMessageHandler() {
        client.setMessageCallback(new Client.MessageCallback() {
            public void onMessageReceived(String message) {
                movieListPanel.handleMessage(message);
                chatPanel.handleMessage(message);
                reviewPanel.handleMessage(message);
                bookmarkPanel.handleMessage(message);
            }
        });
    }
    
    private void handleMenuSelection(String menuName) {
        if (menuName.contains("영화 목록")) {
            cardLayout.show(contentPanel, "MOVIES");
            movieListPanel.loadMovies();
        } else if (menuName.contains("채팅방")) {
            cardLayout.show(contentPanel, "CHAT");
            chatPanel.showMainView();  // 채팅방 목록 표시
        } else if (menuName.contains("감상평")) {
            cardLayout.show(contentPanel, "REVIEW");
            reviewPanel.loadAllReviews();  // 전체 감상평 로드
        } else if (menuName.contains("북마크")) {
            cardLayout.show(contentPanel, "BOOKMARK");
            bookmarkPanel.loadBookmarks();
        }
    }
    
    public void showMovieList() {
        menuTree.clearSelection();
        cardLayout.show(contentPanel, "MOVIES");
        movieListPanel.loadMovies();
    }
    
    public void showChatRoom(String movieCd, String movieNm) {
        menuTree.clearSelection();
        cardLayout.show(contentPanel, "CHAT");
        chatPanel.joinRoom(movieCd, movieNm);
    }
    
    public void showReviews(String movieCd, String movieNm) {
        menuTree.clearSelection();
        cardLayout.show(contentPanel, "REVIEW");
        reviewPanel.loadReviews(movieCd, movieNm);
    }
    
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
    
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            client.disconnect();
        }
        super.processWindowEvent(e);
    }
}