//프로젝트 이름 : MovieSync
//개발자 : 권미리
//개발 기간: 2025.12.01 ~ 2025.12.13

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;

//메인 프레임 GUI - 좌측 JTree 메뉴 + 우측 CardLayout 화면 전환
public class MainFrame extends JFrame {
    private Client client;      //서버 통신 객체
    private String username;    //로그인한 사용자 닉네임
    private JTree menuTree;         //좌측 메뉴 트리
    private JPanel contentPanel;    //우측 컨텐츠 영역
    private CardLayout cardLayout;  //화면 전환용 레이아웃
    private MovieListPanel movieListPanel;   //영화 목록 화면
    private ChatPanel chatPanel;             //채팅방 화면
    private ReviewPanel reviewPanel;         //감상평 화면
    private BookmarkPanel bookmarkPanel;     //북마크 화면
    
    //생성자 - 메인 화면 초기화
    public MainFrame(Client client, String username) {
        this.client = client;
        this.username = username;
        
        setTitle("MovieSync - " + username); //제목에 사용자명 표시
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); //화면 중앙 배치
        setLayout(new BorderLayout());
        
        // ========== 상단 패널 (타이틀 + 사용자 정보 + 로그아웃) ==========
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(33, 150, 243)); //파란색 배경
        topPanel.setPreferredSize(new Dimension(0, 60)); //높이 60픽셀 고정
        
        // 좌측 타이틀 - "MovieSync"
        JLabel titleLabel = new JLabel("  MovieSync");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE); //흰 글씨
        topPanel.add(titleLabel, BorderLayout.WEST);
        
        // 우측 사용자 패널 - 닉네임 + 로그아웃 버튼
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setOpaque(false); //투명 배경
        JLabel userLabel = new JLabel(username + "님 환영합니다  ");
        userLabel.setForeground(Color.WHITE); //흰 글씨
        userLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        
        JButton logoutButton = new JButton("로그아웃");
        logoutButton.setBackground(Color.WHITE); //흰 배경
        logoutButton.setForeground(new Color(33, 150, 243)); //파란 글씨
        logoutButton.setFocusPainted(false); //포커스 테두리 제거
        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logout(); //로그아웃 처리
            }
        });
        
        userPanel.add(userLabel);
        userPanel.add(logoutButton);
        topPanel.add(userPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
        
        // ========== 좌측 메뉴 (JTree) ==========
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(200, 0)); //너비 200픽셀 고정
        leftPanel.setBackground(Color.WHITE); //흰 배경
        leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY)); //우측 테두리만
        
        // JTree 노드 구조 생성 - 루트 > 4개 메뉴
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("MovieSync");
        DefaultMutableTreeNode moviesNode = new DefaultMutableTreeNode("영화 목록");
        DefaultMutableTreeNode chatNode = new DefaultMutableTreeNode("채팅방");
        DefaultMutableTreeNode reviewNode = new DefaultMutableTreeNode("감상평");
        DefaultMutableTreeNode bookmarkNode = new DefaultMutableTreeNode("북마크");
        
        root.add(moviesNode); //루트에 추가
        root.add(chatNode);
        root.add(reviewNode);
        root.add(bookmarkNode);
        
        menuTree = new JTree(root); //JTree 생성
        menuTree.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        menuTree.setRowHeight(30); //행 높이 30픽셀
        menuTree.setRootVisible(false); //루트 노드 숨기기
        menuTree.setShowsRootHandles(true); //루트 핸들 표시
        menuTree.expandRow(0); //첫 번째 노드 확장
        
        // 트리 선택 리스너 - 메뉴 클릭 시 화면 전환
        menuTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode selectedNode = 
                    (DefaultMutableTreeNode) menuTree.getLastSelectedPathComponent();
                
                if (selectedNode != null && selectedNode.isLeaf()) { //리프 노드(메뉴)만 처리
                    String nodeName = selectedNode.toString(); //노드 이름
                    handleMenuSelection(nodeName); //메뉴 선택 처리
                }
            }
        });
        
        JScrollPane treeScrollPane = new JScrollPane(menuTree);
        treeScrollPane.setBorder(null); //스크롤 테두리 제거
        leftPanel.add(treeScrollPane, BorderLayout.CENTER);
        add(leftPanel, BorderLayout.WEST);
        
        // ========== 우측 컨텐츠 영역 (CardLayout) ==========
        contentPanel = new JPanel();
        cardLayout = new CardLayout(); //CardLayout으로 화면 전환
        contentPanel.setLayout(cardLayout);
        
        // 각 패널 생성 - MainFrame 참조 전달 (화면 전환 메소드 호출용)
        movieListPanel = new MovieListPanel(client, this);
        chatPanel = new ChatPanel(client, username, this);
        reviewPanel = new ReviewPanel(client, username, this);
        bookmarkPanel = new BookmarkPanel(client, this);
        
        // CardLayout에 패널 추가 - 카드 이름으로 전환
        contentPanel.add(movieListPanel, "MOVIES"); //영화 목록
        contentPanel.add(chatPanel, "CHAT"); //채팅방
        contentPanel.add(reviewPanel, "REVIEW"); //감상평
        contentPanel.add(bookmarkPanel, "BOOKMARK"); //북마크
        
        add(contentPanel, BorderLayout.CENTER);
        
        // 메시지 핸들러 설정 - 모든 패널에 메시지 전달
        setupMessageHandler();
        
        cardLayout.show(contentPanel, "MOVIES"); //초기 화면: 영화 목록
        setVisible(true);
        
        // 초기 데이터 로드 - EDT에서 실행
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                movieListPanel.initialize(); //영화 목록 초기화
            }
        });
    }
    
    //메시지 핸들러 설정 - 모든 패널에 서버 메시지 전달
    private void setupMessageHandler() {
        client.setMessageCallback(new Client.MessageCallback() {
            public void onMessageReceived(String message) {
                movieListPanel.handleMessage(message); //영화 목록 패널 처리
                chatPanel.handleMessage(message); //채팅 패널 처리
                reviewPanel.handleMessage(message); //감상평 패널 처리
                bookmarkPanel.handleMessage(message); //북마크 패널 처리
            }
        });
    }
    
    //메뉴 선택 처리 - 노드 이름으로 화면 전환
    private void handleMenuSelection(String menuName) {
        if (menuName.contains("영화 목록")) {
            cardLayout.show(contentPanel, "MOVIES"); //영화 목록 화면
            movieListPanel.loadMovies(); //영화 목록 새로고침
        } else if (menuName.contains("채팅방")) {
            cardLayout.show(contentPanel, "CHAT"); //채팅방 화면
            chatPanel.showMainView();  //채팅방 목록 표시
        } else if (menuName.contains("감상평")) {
            cardLayout.show(contentPanel, "REVIEW"); //감상평 화면
            reviewPanel.loadAllReviews();  //전체 감상평 로드
        } else if (menuName.contains("북마크")) {
            cardLayout.show(contentPanel, "BOOKMARK"); //북마크 화면
            bookmarkPanel.loadBookmarks(); //북마크 목록 새로고침
        }
    }
    
    //영화 목록 화면으로 이동 - 다른 패널에서 호출
    public void showMovieList() {
        menuTree.clearSelection(); //JTree 선택 해제
        cardLayout.show(contentPanel, "MOVIES"); //영화 목록 화면으로 전환
        movieListPanel.loadMovies(); //영화 목록 새로고침
    }
    
    //특정 영화의 채팅방으로 이동 - MovieListPanel, BookmarkPanel에서 호출
    public void showChatRoom(String movieCd, String movieNm) {
        menuTree.clearSelection(); //JTree 선택 해제
        cardLayout.show(contentPanel, "CHAT"); //채팅방 화면으로 전환
        chatPanel.joinRoom(movieCd, movieNm); //채팅방 입장
    }
    
    //특정 영화의 감상평으로 이동 - MovieListPanel, BookmarkPanel에서 호출
    public void showReviews(String movieCd, String movieNm) {
        menuTree.clearSelection(); //JTree 선택 해제
        cardLayout.show(contentPanel, "REVIEW"); //감상평 화면으로 전환
        reviewPanel.loadReviews(movieCd, movieNm); //감상평 로드
    }
    
    //로그아웃 - 확인 후 서버 연결 종료
    private void logout() {
        int result = JOptionPane.showConfirmDialog(this, 
            "로그아웃 하시겠습니까?", 
            "로그아웃", 
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) { //예 선택 시
            client.disconnect(); //서버 연결 종료
            dispose(); //메인 창 닫기
            new LoginFrame(); //로그인 창 다시 열기
        }
    }
    
    //창 종료 시 처리 - X버튼 클릭 시
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            client.disconnect(); //서버 연결 종료
        }
        super.processWindowEvent(e);
    }
}