//프로젝트 이름 : MovieSync
//개발자 : 권미리
//개발 기간: 2025.12.01 ~ 2025.12.13

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    
    // 북마크한 영화 목록
    private HashSet<String> bookmarkedMovies = new HashSet<String>();
    
    public MovieListPanel(Client client, MainFrame mainFrame) {
        this.client = client;
        this.mainFrame = mainFrame;
        this.movies = new ArrayList<MovieData>();
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 상단 영역
        JPanel topPanel = new JPanel(new BorderLayout());
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(new Date());
        
        JLabel headerLabel = new JLabel("실시간 박스오피스 Top 10 (" + today + ")");
        headerLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        topPanel.add(headerLabel, BorderLayout.WEST);
        
        JButton refreshButton = new JButton("새로고침");
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadMovies();
            }
        });
        topPanel.add(refreshButton, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        
        // 중앙 영역
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(500);
        
        // 왼쪽: 영화 목록 테이블
        String[] columnNames = {"순위", "영화명", "누적관객", "개봉일"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        movieTable = new JTable(tableModel);
        movieTable.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        movieTable.setRowHeight(30);
        movieTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        movieTable.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 13));
        movieTable.getTableHeader().setReorderingAllowed(false);
        
        movieTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        movieTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        movieTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        movieTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        
        movieTable.getSelectionModel().addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = movieTable.getSelectedRow();
                    if (selectedRow >= 0 && selectedRow < movies.size()) {
                        showMovieDetail(movies.get(selectedRow));
                    }
                }
            }
        });
        
        JScrollPane tableScrollPane = new JScrollPane(movieTable);
        splitPane.setLeftComponent(tableScrollPane);
        
        // 오른쪽: 상세 정보
        JPanel detailPanel = new JPanel();
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
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
        
        // 버튼들
        chatButton = new JButton("채팅방 입장");
        chatButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        chatButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        chatButton.setMaximumSize(new Dimension(200, 40));
        chatButton.setEnabled(false);
        chatButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectedMovieCd != null) {
                    mainFrame.showChatRoom(selectedMovieCd, selectedMovieNm);
                }
            }
        });
        detailPanel.add(chatButton);
        
        detailPanel.add(Box.createVerticalStrut(10));
        
        reviewButton = new JButton("감상평 보기");
        reviewButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        reviewButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        reviewButton.setMaximumSize(new Dimension(200, 40));
        reviewButton.setEnabled(false);
        reviewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectedMovieCd != null) {
                    mainFrame.showReviews(selectedMovieCd, selectedMovieNm);
                }
            }
        });
        detailPanel.add(reviewButton);
        
        detailPanel.add(Box.createVerticalStrut(10));
        
        bookmarkButton = new JButton("북마크 추가");
        bookmarkButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        bookmarkButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        bookmarkButton.setMaximumSize(new Dimension(200, 40));
        bookmarkButton.setEnabled(false);
        bookmarkButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectedMovieCd != null) {
                    client.addBookmark(selectedMovieCd);
                    bookmarkedMovies.add(selectedMovieCd);
                    bookmarkButton.setEnabled(false);
                    bookmarkButton.setText("북마크 추가됨");
                    JOptionPane.showMessageDialog(MovieListPanel.this, "북마크에 추가되었습니다!");
                }
            }
        });
        detailPanel.add(bookmarkButton);
        
        splitPane.setRightComponent(new JScrollPane(detailPanel));
        
        add(splitPane, BorderLayout.CENTER);
    }
    
    // 초기화: 북마크 로드 후 영화 목록 로드
    public void initialize() {
        client.requestBookmarks();
        // 약간의 딜레이 후 영화 목록도 요청 (안전장치)
        javax.swing.Timer timer = new javax.swing.Timer(300, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (tableModel.getRowCount() == 0) {
                    loadMovies();
                }
            }
        });
        timer.setRepeats(false);
        timer.start();
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
        rankLabel.setText("순위: " + movie.rank + "위");
        openDtLabel.setText("개봉일: " + movie.openDt);
        audiAccLabel.setText("누적관객: " + String.format("%,d", movie.audiAcc) + "명");
        
        chatButton.setEnabled(true);
        reviewButton.setEnabled(true);
        
        // 북마크 버튼 상태 설정
        if (bookmarkedMovies.contains(movie.movieCd)) {
            bookmarkButton.setEnabled(false);
            bookmarkButton.setText("북마크 추가됨");
        } else {
            bookmarkButton.setEnabled(true);
            bookmarkButton.setText("북마크 추가");
        }
    }
    
    // 서버 메시지 처리
    public void handleMessage(String message) {
        // 북마크 관련 메시지
        if (message.startsWith("BOOKMARK_COUNT")) {
            // 북마크 개수 - 시작 신호
            bookmarkedMovies.clear();
            
        } else if (message.startsWith("BOOKMARK_DATA")) {
            // 북마크 데이터: BOOKMARK_DATA|영화코드|END
            String[] parts = message.split("\\|");
            if (parts.length >= 2) {
                String movieCd = parts[1];
                bookmarkedMovies.add(movieCd);
            }
            
        } else if (message.startsWith("BOOKMARK_END")) {
            // 북마크 로딩 완료 - 이제 영화 목록 로드
            System.out.println("MovieListPanel> 북마크 로드 완료: " + bookmarkedMovies.size() + "개");
            loadMovies();
            
        // 영화 목록 관련 메시지
        } else if (message.startsWith("MOVIES_COUNT")) {
            movies.clear();
            tableModel.setRowCount(0);
            
        } else if (message.startsWith("MOVIES_DATA")) {
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
                
                SwingUtilities.invokeLater(new Runnable() {
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
            
        } else if (message.startsWith("MOVIES_END")) {
            System.out.println("MovieListPanel> 영화 목록 로드 완료: " + movies.size() + "개");
            
        } else if (message.startsWith("BOOKMARK_OK")) {
            // 북마크 추가 성공 (아무것도 안 함 - 이미 로컬에 추가했음)
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