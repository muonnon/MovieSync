//프로젝트 이름 : MovieSync
//개발자 : 권미리
//개발 기간: 2025.12.01 ~ 2025.12.13
// 영화 목록 패널 - 박스오피스 Top 10 영화 목록을 테이블로 표시

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * 영화 목록을 테이블로 표시하는 패널
 * 왼쪽: 영화 목록 테이블, 오른쪽: 선택한 영화 상세 정보
 */
public class MovieListPanel extends JPanel {
    // 서버 통신 관련
    private Client client;          // 서버 통신 객체
    private MainFrame mainFrame;    // 부모 프레임 참조 (화면 전환용)
    
    // 테이블 관련
    private JTable movieTable;              // 영화 목록 테이블
    private DefaultTableModel tableModel;   // 테이블 데이터 모델
    private ArrayList<MovieData> movies;    // 영화 데이터 목록
    
    // 영화 상세 정보 표시 레이블
    private JLabel titleLabel;      // 영화 제목
    private JLabel rankLabel;       // 순위
    private JLabel openDtLabel;     // 개봉일
    private JLabel audiAccLabel;    // 누적 관객
    
    // 액션 버튼
    private JButton chatButton;      // 채팅방 입장 버튼
    private JButton reviewButton;    // 감상평 보기 버튼
    private JButton bookmarkButton;  // 북마크 추가 버튼
    
    // 선택된 영화 정보
    private String selectedMovieCd = null;   // 선택된 영화 코드
    private String selectedMovieNm = null;   // 선택된 영화 제목
    
    /**
     * 생성자 - 영화 목록 패널 초기화
     * @param client 서버 통신 객체
     * @param mainFrame 부모 프레임
     */
    public MovieListPanel(Client client, MainFrame mainFrame) {
        this.client = client;
        this.mainFrame = mainFrame;
        this.movies = new ArrayList<MovieData>();
        
        // 기본 레이아웃 설정
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // ========== 상단 영역: 타이틀 + 새로고침 버튼 ==========
        JPanel topPanel = new JPanel(new BorderLayout());
        
        // 헤더 레이블
        JLabel headerLabel = new JLabel("📊 실시간 박스오피스 Top 10");
        headerLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        topPanel.add(headerLabel, BorderLayout.WEST);
        
        // 새로고침 버튼 - 클릭시 영화 목록 다시 로드
        JButton refreshButton = new JButton("🔄 새로고침");
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadMovies();
            }
        });
        topPanel.add(refreshButton, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        
        // ========== 중앙 영역: 영화 목록 + 상세 정보 ==========
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(500);  // 분할 위치
        
        // ----- 왼쪽: 영화 목록 테이블 -----
        // 테이블 컨럼 정의
        String[] columnNames = {"순위", "영화명", "누적관객", "개봉일"};
        
        // 테이블 모델 생성 - 셀 편집 불가로 설정
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // 모든 셀 편집 불가
            }
        };
        
        // JTable 생성 및 기본 설정
        movieTable = new JTable(tableModel);
        movieTable.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        movieTable.setRowHeight(30);                                    // 행 높이
        movieTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // 단일 선택만 허용
        movieTable.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 13));
        movieTable.getTableHeader().setReorderingAllowed(false);  // 컨럼 순서 변경 불가
        
        // 각 컨럼 너비 설정
        movieTable.getColumnModel().getColumn(0).setPreferredWidth(50);   // 순위
        movieTable.getColumnModel().getColumn(1).setPreferredWidth(200);  // 영화명
        movieTable.getColumnModel().getColumn(2).setPreferredWidth(100);  // 누적관객
        movieTable.getColumnModel().getColumn(3).setPreferredWidth(100);  // 개봉일
        
        // 테이블 선택 리스너 - 영화 선택 시 상세 정보 표시
        movieTable.getSelectionModel().addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            @Override
            public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                // 선택 변경 중인 경우 무시 (isAdjusting)
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = movieTable.getSelectedRow();
                    // 유효한 행이 선택되었을 때만 처리
                    if (selectedRow >= 0 && selectedRow < movies.size()) {
                        showMovieDetail(movies.get(selectedRow));
                    }
                }
            }
        });
        
        JScrollPane tableScrollPane = new JScrollPane(movieTable);
        splitPane.setLeftComponent(tableScrollPane);
        
        // ----- 오른쪽: 선택한 영화 상세 정보 -----
        JPanel detailPanel = new JPanel();
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
        detailPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 상세 정보 헤더
        JLabel detailHeaderLabel = new JLabel("선택한 영화 정보");
        detailHeaderLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        detailHeaderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailPanel.add(detailHeaderLabel);
        
        detailPanel.add(Box.createVerticalStrut(20));
        
        titleLabel = new JLabel("영화를 선택해주세요");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailPanel.add(titleLabel);
        
        detailPanel.add(Box.createVerticalStrut(10));
        
        rankLabel = new JLabel("");
        rankLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        rankLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailPanel.add(rankLabel);
        
        openDtLabel = new JLabel("");
        openDtLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        openDtLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailPanel.add(openDtLabel);
        
        audiAccLabel = new JLabel("");
        audiAccLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        audiAccLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailPanel.add(audiAccLabel);
        
        detailPanel.add(Box.createVerticalStrut(30));
        
        // ----- 액션 버튼들 -----
        // 채팅방 입장 버튼
        chatButton = new JButton("💬 채팅방 입장");
        chatButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        chatButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        chatButton.setMaximumSize(new Dimension(200, 40));
        chatButton.setEnabled(false);  // 초기에는 비활성화
        chatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedMovieCd != null) {
                    mainFrame.showChatRoom(selectedMovieCd, selectedMovieNm);
                }
            }
        });
        detailPanel.add(chatButton);
        
        detailPanel.add(Box.createVerticalStrut(10));
        
        // 감상평 보기 버튼
        reviewButton = new JButton("⭐ 감상평 보기");
        reviewButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        reviewButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        reviewButton.setMaximumSize(new Dimension(200, 40));
        reviewButton.setEnabled(false);  // 초기에는 비활성화
        reviewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedMovieCd != null) {
                    mainFrame.showReviews(selectedMovieCd, selectedMovieNm);
                }
            }
        });
        detailPanel.add(reviewButton);
        
        detailPanel.add(Box.createVerticalStrut(10));
        
        // 북마크 추가 버튼
        bookmarkButton = new JButton("🔖 북마크 추가");
        bookmarkButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        bookmarkButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        bookmarkButton.setMaximumSize(new Dimension(200, 40));
        bookmarkButton.setEnabled(false);  // 초기에는 비활성화
        bookmarkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedMovieCd != null) {
                    client.addBookmark(selectedMovieCd);
                    JOptionPane.showMessageDialog(MovieListPanel.this, "북마크에 추가되었습니다!");
                }
            }
        });
        detailPanel.add(bookmarkButton);
        
        splitPane.setRightComponent(new JScrollPane(detailPanel));
        
        add(splitPane, BorderLayout.CENTER);
        
        // 클라이언트 메시지 콜백 설정
        setupMessageHandler();
    }
    
    /**
     * 영화 목록을 서버에서 불러온다
     */
    public void loadMovies() {
        movies.clear();             // 기존 데이터 초기화
        tableModel.setRowCount(0); // 테이블 초기화
        client.requestMovies();     // 서버에 영화 목록 요청
    }
    
    /**
     * 선택된 영화의 상세 정보를 오른쪽 패널에 표시
     * @param movie 선택된 영화 데이터
     */
    private void showMovieDetail(MovieData movie) {
        // 선택된 영화 정보 저장
        selectedMovieCd = movie.movieCd;
        selectedMovieNm = movie.movieNm;
        
        // 상세 정보 레이블 업데이트
        titleLabel.setText(movie.movieNm);
        rankLabel.setText("📊 순위: " + movie.rank + "위");
        openDtLabel.setText("📅 개봉일: " + movie.openDt);
        audiAccLabel.setText("👥 누적관객: " + String.format("%,d", movie.audiAcc) + "명");
        
        // 버튼 활성화
        chatButton.setEnabled(true);
        reviewButton.setEnabled(true);
        bookmarkButton.setEnabled(true);
    }
    
    /**
     * 서버로부터 메시지를 받아 처리하는 콜백 설정
     */
    private void setupMessageHandler() {
        client.setMessageCallback(new Client.MessageCallback() {
            @Override
            public void onMessageReceived(String message) {
                handleMessage(message);
            }
        });
    }
    
    /**
     * 서버로부터 받은 메시지를 파싱하여 처리
     * @param message 서버로부터 받은 메시지
     */
    private void handleMessage(String message) {
        // MOVIES_COUNT 메시지: 영화 개수 수신
        if (message.startsWith("MOVIES_COUNT")) {
            movies.clear();
            tableModel.setRowCount(0);
            
        // MOVIES_DATA 메시지: 영화 데이터 수신
        } else if (message.startsWith("MOVIES_DATA")) {
            // 메시지 파싱 (구분자: |)
            String[] parts = message.split("\\|");
            if (parts.length >= 7) {
                // 영화 데이터 객체 생성
                MovieData movie = new MovieData();
                movie.movieCd = parts[1];                    // 영화 코드
                movie.movieNm = parts[2];                    // 영화 제목
                movie.rank = Integer.parseInt(parts[3]);     // 순위
                movie.openDt = parts[4];                     // 개봉일
                movie.audiAcc = Long.parseLong(parts[5]);    // 누적 관객
                movie.salesAcc = Long.parseLong(parts[6]);   // 누적 매출
                
                movies.add(movie);
                
                // UI 업데이트는 EDT에서 실행
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        Object[] row = {
                            movie.rank,
                            movie.movieNm,
                            String.format("%,d", movie.audiAcc),
                            movie.openDt
                        };
                        tableModel.addRow(row);
                    }
                });
            }
            
        // MOVIES_END 메시지: 영화 목록 수신 완료
        } else if (message.startsWith("MOVIES_END")) {
            System.out.println("MovieListPanel> 영화 목록 로드 완료: " + movies.size() + "개");
        }
    }
    
    /**
     * 영화 데이터를 저장하는 내부 클래스
     * 서버로부터 받은 영화 정보를 저장
     */
    class MovieData {
        String movieCd;    // 영화 코드 (KOFIC 코드)
        String movieNm;    // 영화 제목
        int rank;          // 박스오피스 순위
        String openDt;     // 개봉일
        long audiAcc;      // 누적 관객 수
        long salesAcc;     // 누적 매출액
    }
}