//프로젝트 이름 : MovieSync
//개발자 : 권미리
//개발 기간: 2025.12.01 ~ 2025.12.13

// 영화 목록 패널

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class MovieListPanel extends JPanel {
    private Client client;
    private MainFrame mainFrame;
    
    private JTable movieTable;
    private DefaultTableModel tableModel;
    private ArrayList<MovieData> movies;
    
    private JLabel titleLabel;
    private JLabel rankLabel;
    private JLabel openDtLabel;
    private JLabel audiAccLabel;
    private JButton chatButton;
    private JButton reviewButton;
    private JButton bookmarkButton;
    
    private String selectedMovieCd = null;
    private String selectedMovieNm = null;
    
    public MovieListPanel(Client client, MainFrame mainFrame) {
        this.client = client;
        this.mainFrame = mainFrame;
        this.movies = new ArrayList<>();
        
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 상단 타이틀
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        
        JLabel headerLabel = new JLabel("📊 실시간 박스오피스 Top 10");
        headerLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        topPanel.add(headerLabel, BorderLayout.WEST);
        
        JButton refreshButton = new JButton("🔄 새로고침");
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> loadMovies());
        topPanel.add(refreshButton, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        
        // 중앙 영역 (영화 목록 + 상세 정보)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(500);
        
        // 왼쪽: 영화 목록 테이블
        String[] columnNames = {"순위", "영화명", "누적관객", "개봉일"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 편집 불가
            }
        };
        
        movieTable = new JTable(tableModel);
        movieTable.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        movieTable.setRowHeight(30);
        movieTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        movieTable.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 13));
        movieTable.getTableHeader().setReorderingAllowed(false);
        
        // 열 너비 설정
        movieTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        movieTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        movieTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        movieTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        
        // 테이블 선택 리스너
        movieTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = movieTable.getSelectedRow();
                if (selectedRow >= 0 && selectedRow < movies.size()) {
                    showMovieDetail(movies.get(selectedRow));
                }
            }
        });
        
        JScrollPane tableScrollPane = new JScrollPane(movieTable);
        splitPane.setLeftComponent(tableScrollPane);
        
        // 오른쪽: 선택한 영화 상세 정보
        JPanel detailPanel = new JPanel();
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
        detailPanel.setBackground(Color.WHITE);
        detailPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
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
        
        // 액션 버튼들
        chatButton = new JButton("💬 채팅방 입장");
        chatButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        chatButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        chatButton.setMaximumSize(new Dimension(200, 40));
        chatButton.setEnabled(false);
        chatButton.addActionListener(e -> {
            if (selectedMovieCd != null) {
                mainFrame.showChatRoom(selectedMovieCd, selectedMovieNm);
            }
        });
        detailPanel.add(chatButton);
        
        detailPanel.add(Box.createVerticalStrut(10));
        
        reviewButton = new JButton("⭐ 감상평 보기");
        reviewButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        reviewButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        reviewButton.setMaximumSize(new Dimension(200, 40));
        reviewButton.setEnabled(false);
        reviewButton.addActionListener(e -> {
            if (selectedMovieCd != null) {
                mainFrame.showReviews(selectedMovieCd, selectedMovieNm);
            }
        });
        detailPanel.add(reviewButton);
        
        detailPanel.add(Box.createVerticalStrut(10));
        
        bookmarkButton = new JButton("🔖 북마크 추가");
        bookmarkButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        bookmarkButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        bookmarkButton.setMaximumSize(new Dimension(200, 40));
        bookmarkButton.setEnabled(false);
        bookmarkButton.addActionListener(e -> {
            if (selectedMovieCd != null) {
                client.addBookmark(selectedMovieCd);
                JOptionPane.showMessageDialog(this, "북마크에 추가되었습니다!");
            }
        });
        detailPanel.add(bookmarkButton);
        
        splitPane.setRightComponent(new JScrollPane(detailPanel));
        
        add(splitPane, BorderLayout.CENTER);
        
        // 클라이언트 메시지 콜백 설정
        setupMessageHandler();
    }
    
    // 영화 목록 로드
    public void loadMovies() {
        movies.clear();
        tableModel.setRowCount(0);
        client.requestMovies();
    }
    
    // 영화 상세 정보 표시
    private void showMovieDetail(MovieData movie) {
        selectedMovieCd = movie.movieCd;
        selectedMovieNm = movie.movieNm;
        
        titleLabel.setText(movie.movieNm);
        rankLabel.setText("📊 순위: " + movie.rank + "위");
        openDtLabel.setText("📅 개봉일: " + movie.openDt);
        audiAccLabel.setText("👥 누적관객: " + String.format("%,d", movie.audiAcc) + "명");
        
        chatButton.setEnabled(true);
        reviewButton.setEnabled(true);
        bookmarkButton.setEnabled(true);
    }
    
    // 서버 메시지 처리
    private void setupMessageHandler() {
        client.setMessageCallback(new Client.MessageCallback() {
            @Override
            public void onMessageReceived(String message) {
                handleMessage(message);
            }
        });
    }
    
    private void handleMessage(String message) {
        if (message.startsWith("MOVIES_COUNT")) {
            // 영화 개수 수신 - 준비
            movies.clear();
            tableModel.setRowCount(0);
            
        } else if (message.startsWith("MOVIES_DATA")) {
            // 영화 데이터 수신
            String[] parts = message.split("\\|");
            if (parts.length >= 7) {
                MovieData movie = new MovieData();
                movie.movieCd = parts[1];
                movie.movieNm = parts[2];
                movie.rank = Integer.parseInt(parts[3]);
                movie.openDt = parts[4];
                movie.audiAcc = Long.parseLong(parts[5]);
                movie.salesAcc = Long.parseLong(parts[6]);
                
                movies.add(movie);
                
                SwingUtilities.invokeLater(() -> {
                    Object[] row = {
                        movie.rank,
                        movie.movieNm,
                        String.format("%,d", movie.audiAcc),
                        movie.openDt
                    };
                    tableModel.addRow(row);
                });
            }
            
        } else if (message.startsWith("MOVIES_END")) {
            // 영화 목록 수신 완료
            System.out.println("MovieListPanel> 영화 목록 로드 완료: " + movies.size() + "개");
        }
    }
    
    // 영화 데이터 클래스
    class MovieData {
        String movieCd;
        String movieNm;
        int rank;
        String openDt;
        long audiAcc;
        long salesAcc;
    }
}