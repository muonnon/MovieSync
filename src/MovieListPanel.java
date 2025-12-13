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

//영화 목록 패널 - 박스오피스 Top 10을 테이블로 표시 (좌측 테이블 + 우측 상세정보)
public class MovieListPanel extends JPanel {
    // 서버 통신 관련
    private Client client;          //서버 통신 객체
    private MainFrame mainFrame;    //부모 프레임 참조 (화면 전환용)
    
    // 테이블 관련
    private JTable movieTable;              //영화 목록 테이블
    private DefaultTableModel tableModel;   //테이블 데이터 모델
    private ArrayList<MovieData> movies;    //영화 데이터 목록
    
    // 영화 상세 정보 표시 레이블
    private JLabel titleLabel;      //영화 제목
    private JLabel rankLabel;       //순위
    private JLabel openDtLabel;     //개봉일
    private JLabel audiAccLabel;    //누적 관객
    
    // 액션 버튼
    private JButton chatButton;      //채팅방 입장 버튼
    private JButton reviewButton;    //감상평 보기 버튼
    private JButton bookmarkButton;  //북마크 추가 버튼
    
    // 선택된 영화 정보
    private String selectedMovieCd = null;   //선택된 영화 코드
    private String selectedMovieNm = null;   //선택된 영화 제목
    
    // 북마크한 영화 목록 - 현재 세션에서 추가한 것만 저장 (중복 방지용)
    private HashSet<String> bookmarkedMovies = new HashSet<String>();
    
    //생성자 - 영화 목록 패널 초기화
    public MovieListPanel(Client client, MainFrame mainFrame) {
        this.client = client;
        this.mainFrame = mainFrame;
        this.movies = new ArrayList<MovieData>();
        
        // 기본 레이아웃 설정 - BorderLayout with 10픽셀 간격
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); //외곽 여백 10픽셀
        
        // ========== 상단 영역: 타이틀 + 새로고침 버튼 ==========
        JPanel topPanel = new JPanel(new BorderLayout());
        
        // 오늘 날짜 구하기 - yyyy-MM-dd 형식
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(new Date());
        
        // 헤더 레이블 - "실시간 박스오피스 Top 10 (날짜)"
        JLabel headerLabel = new JLabel("실시간 박스오피스 Top 10 (" + today + ")");
        headerLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        topPanel.add(headerLabel, BorderLayout.WEST);
        
        // 새로고침 버튼 - 클릭 시 영화 목록 다시 로드
        JButton refreshButton = new JButton("새로고침");
        refreshButton.setFocusPainted(false); //포커스 테두리 제거
        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadMovies(); //영화 목록 로드
            }
        });
        topPanel.add(refreshButton, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        
        // ========== 중앙 영역: 좌우 분할 (영화 목록 + 상세 정보) ==========
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(500);  //분할 위치 500픽셀
        
        // ----- 좌측: 영화 목록 테이블 -----
        // 테이블 컬럼 정의 - 순위, 영화명, 누적관객, 개봉일
        String[] columnNames = {"순위", "영화명", "누적관객", "개봉일"};
        
        // 테이블 모델 생성 - 셀 편집 불가 설정
        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;  //모든 셀 편집 불가
            }
        };
        
        // JTable 생성 및 기본 설정
        movieTable = new JTable(tableModel);
        movieTable.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        movieTable.setRowHeight(30);                                    //행 높이 30픽셀
        movieTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); //단일 선택만 허용
        movieTable.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 13));
        movieTable.getTableHeader().setReorderingAllowed(false);  //컬럼 순서 변경 불가
        
        // 각 컬럼 너비 설정
        movieTable.getColumnModel().getColumn(0).setPreferredWidth(50);   //순위 50픽셀
        movieTable.getColumnModel().getColumn(1).setPreferredWidth(200);  //영화명 200픽셀
        movieTable.getColumnModel().getColumn(2).setPreferredWidth(100);  //누적관객 100픽셀
        movieTable.getColumnModel().getColumn(3).setPreferredWidth(100);  //개봉일 100픽셀
        
        // 테이블 선택 리스너 - 영화 클릭 시 상세 정보 표시
        movieTable.getSelectionModel().addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                // 선택 변경 중인 경우 무시 (isAdjusting 체크)
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = movieTable.getSelectedRow();
                    // 유효한 행이 선택되었을 때만 처리
                    if (selectedRow >= 0 && selectedRow < movies.size()) {
                        showMovieDetail(movies.get(selectedRow)); //상세 정보 표시
                    }
                }
            }
        });
        
        JScrollPane tableScrollPane = new JScrollPane(movieTable);
        splitPane.setLeftComponent(tableScrollPane);
        
        // ----- 우측: 선택한 영화 상세 정보 -----
        JPanel detailPanel = new JPanel();
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS)); //세로 배치
        detailPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); //여백 10픽셀
        
        // 상세 정보 헤더 - "선택한 영화 정보"
        JLabel detailHeaderLabel = new JLabel("선택한 영화 정보");
        detailHeaderLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        detailHeaderLabel.setAlignmentX(Component.LEFT_ALIGNMENT); //왼쪽 정렬
        detailPanel.add(detailHeaderLabel);
        
        detailPanel.add(Box.createVerticalStrut(20)); //간격 20픽셀
        
        // 영화 제목 레이블 - 초기값 "영화를 선택해주세요"
        titleLabel = new JLabel("영화를 선택해주세요");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailPanel.add(titleLabel);
        
        detailPanel.add(Box.createVerticalStrut(10)); //간격 10픽셀
        
        // 순위 레이블
        rankLabel = new JLabel("");
        rankLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        rankLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailPanel.add(rankLabel);
        
        // 개봉일 레이블
        openDtLabel = new JLabel("");
        openDtLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        openDtLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailPanel.add(openDtLabel);
        
        // 누적 관객 레이블
        audiAccLabel = new JLabel("");
        audiAccLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        audiAccLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailPanel.add(audiAccLabel);
        
        detailPanel.add(Box.createVerticalStrut(30)); //간격 30픽셀
        
        // ----- 액션 버튼들 (채팅방, 감상평, 북마크) -----
        // 채팅방 입장 버튼 - 200x40 크기, 초기 비활성화
        chatButton = new JButton("채팅방 입장");
        chatButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        chatButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        chatButton.setMaximumSize(new Dimension(200, 40));
        chatButton.setEnabled(false);  //초기에는 비활성화
        chatButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectedMovieCd != null) {
                    mainFrame.showChatRoom(selectedMovieCd, selectedMovieNm); //채팅방 화면으로 전환
                }
            }
        });
        detailPanel.add(chatButton);
        
        detailPanel.add(Box.createVerticalStrut(10)); //간격 10픽셀
        
        // 감상평 보기 버튼 - 200x40 크기, 초기 비활성화
        reviewButton = new JButton("감상평 보기");
        reviewButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        reviewButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        reviewButton.setMaximumSize(new Dimension(200, 40));
        reviewButton.setEnabled(false);  //초기에는 비활성화
        reviewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectedMovieCd != null) {
                    mainFrame.showReviews(selectedMovieCd, selectedMovieNm); //감상평 화면으로 전환
                }
            }
        });
        detailPanel.add(reviewButton);
        
        detailPanel.add(Box.createVerticalStrut(10)); //간격 10픽셀
        
        // 북마크 추가 버튼 - 200x40 크기, 초기 비활성화
        bookmarkButton = new JButton("북마크 추가");
        bookmarkButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        bookmarkButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        bookmarkButton.setMaximumSize(new Dimension(200, 40));
        bookmarkButton.setEnabled(false);  //초기에는 비활성화
        bookmarkButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectedMovieCd != null) {
                    client.addBookmark(selectedMovieCd); //서버에 북마크 추가 요청
                    bookmarkedMovies.add(selectedMovieCd); //북마크 목록에 추가
                    // 버튼 비활성화 - 중복 추가 방지
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
    
    //초기화 메소드 - MainFrame에서 호출 (영화 목록 로드)
    public void initialize() {
        loadMovies(); //영화 목록 로드
    }
    
    //영화 목록을 서버에서 불러온다 - GET_MOVIES|END
    public void loadMovies() {
        movies.clear();             //기존 데이터 초기화
        tableModel.setRowCount(0); //테이블 초기화
        client.requestMovies();     //서버에 영화 목록 요청
    }
    
    //선택된 영화의 상세 정보를 우측 패널에 표시
    private void showMovieDetail(MovieData movie) {
        // 선택된 영화 정보 저장
        selectedMovieCd = movie.movieCd;
        selectedMovieNm = movie.movieNm;
        
        // 상세 정보 레이블 업데이트
        titleLabel.setText(movie.movieNm);
        rankLabel.setText("순위: " + movie.rank + "위");
        openDtLabel.setText("개봉일: " + movie.openDt);
        audiAccLabel.setText("누적관객: " + String.format("%,d", movie.audiAcc) + "명"); //천 단위 콤마
        
        // 버튼 활성화
        chatButton.setEnabled(true);
        reviewButton.setEnabled(true);
        
        // 북마크 버튼 상태 설정 - 이미 북마크한 영화인지 확인
        if (bookmarkedMovies.contains(movie.movieCd)) {
            // 이미 북마크한 영화 - 비활성화
            bookmarkButton.setEnabled(false);
            bookmarkButton.setText("북마크 추가됨");
        } else {
            // 북마크하지 않은 영화 - 활성화
            bookmarkButton.setEnabled(true);
            bookmarkButton.setText("북마크 추가");
        }
    }
    
    //서버로부터 받은 메시지를 파싱하여 처리
    public void handleMessage(String message) {
        // MOVIES_COUNT - 영화 개수 수신
        if (message.startsWith("MOVIES_COUNT")) {
            movies.clear(); //데이터 초기화
            tableModel.setRowCount(0); //테이블 초기화
            
        // MOVIES_DATA - 영화 데이터 수신 (MOVIES_DATA|영화코드|영화제목|순위|개봉일|누적관객|누적매출|END)
        } else if (message.startsWith("MOVIES_DATA")) {
            // 메시지 파싱 - | 로 분리
            String[] parts = message.split("\\|");
            if (parts.length >= 7) {
                // 영화 데이터 객체 생성
                MovieData movie = new MovieData();
                movie.movieCd = parts[1];                    //영화 코드
                movie.movieNm = parts[2];                    //영화 제목
                movie.rank = Integer.parseInt(parts[3]);     //순위
                movie.openDt = parts[4];                     //개봉일
                movie.audiAcc = Long.parseLong(parts[5]);    //누적 관객 (long 타입)
                movie.salesAcc = Long.parseLong(parts[6]);   //누적 매출 (long 타입)
                
                movies.add(movie); //목록에 추가
                
                // UI 업데이트는 EDT에서 실행 - 스레드 안전성
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        Object[] row = {
                            movie.rank,
                            movie.movieNm,
                            String.format("%,d", movie.audiAcc), //천 단위 콤마
                            movie.openDt
                        };
                        tableModel.addRow(row); //테이블에 행 추가
                    }
                });
            }
            
        // MOVIES_END - 영화 목록 수신 완료
        } else if (message.startsWith("MOVIES_END")) {
            System.out.println("MovieListPanel> 영화 목록 로드 완료: " + movies.size() + "개");
        }
    }
    
    //영화 데이터를 저장하는 내부 클래스 - 서버로부터 받은 영화 정보 저장
    class MovieData {
        String movieCd;    //영화 코드 (KOFIC 코드)
        String movieNm;    //영화 제목
        int rank;          //박스오피스 순위
        String openDt;     //개봉일
        long audiAcc;      //누적 관객 수
        long salesAcc;     //누적 매출액
    }
}