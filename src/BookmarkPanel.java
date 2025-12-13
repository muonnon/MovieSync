//프로젝트 이름 : MovieSync
//개발자 : 권미리
//개발 기간: 2025.12.01 ~ 2025.12.13

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class BookmarkPanel extends JPanel {
    private Client client; //서버통신용 클라이언트 개체
    private MainFrame mainFrame; //화면전환용
    
    // 북마크 데이터 - 서버로부터 받은 영화 정보를 저장하는 리스트
    private ArrayList<MovieData> bookmarkedMovies = new ArrayList<MovieData>();
    
    // GUI 컴포넌트 생성
    private JLabel titleLabel; //제목
    private JLabel countLabel; //북마크개수표시
    private JPanel bookmarkListPanel; //영화 카드 추가용 컨테이너 패널
    private JScrollPane scrollPane; //스크롤
    private JButton leaveButton; //나가기
    
    public BookmarkPanel(Client client, MainFrame mainFrame) { //생성자
        this.client = client;
        this.mainFrame = mainFrame;
        
        //레이아웃 설정, 10픽셀 간격
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 상단 패널 - 타이틀(L), 버튼(R)
        JPanel topPanel = new JPanel(new BorderLayout());
        
        // 타이틀
        JPanel titlePanel = new JPanel(new BorderLayout());
        titleLabel = new JLabel("내 북마크");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18)); //폰트, 굵은 글씨
        titlePanel.add(titleLabel, BorderLayout.NORTH); //타이틀패널 상단
        
        //북마크 개수 표시
        countLabel = new JLabel("총 0개의 영화");
        countLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        countLabel.setForeground(new Color(100, 100, 100));
        titlePanel.add(countLabel, BorderLayout.SOUTH); //타이틀패널 하단
        
        topPanel.add(titlePanel, BorderLayout.WEST); //타이틀 패널을 상단 패널 왼쪽에 배치
        
        // 나가기 버튼 생성
        leaveButton = new JButton("나가기");
        leaveButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        leaveButton.addActionListener(new ActionListener() { //나가기 버튼 클릭시 영화 목록 화면(메인화면)으로 전환
            public void actionPerformed(ActionEvent e) {
                mainFrame.showMovieList(); //MainFrame의 CardLayout을 사용해 영화 목록 화면으로 전환
            }
        });
        
        topPanel.add(leaveButton, BorderLayout.EAST); //나가기 버튼을 상단 패널 우측에 배치
        
        add(topPanel, BorderLayout.NORTH); //상단 패널을 메인 패널의 북쪽에 추가
        
        // 중앙 - 북마크 목록
        bookmarkListPanel = new JPanel(); //영화 카드들을 세로로 나열
        bookmarkListPanel.setLayout(new BoxLayout(bookmarkListPanel, BoxLayout.Y_AXIS));
        bookmarkListPanel.setBackground(Color.WHITE); //흰 배경
        
        scrollPane = new JScrollPane(bookmarkListPanel); //스크롤 패널로 감싸기
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); //수직 스크롤바 항상 표시
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); //한번에 16픽셀식 이동
        
        add(scrollPane, BorderLayout.CENTER); //스크롤패널을 메인패널 중앙에 추가
    }
    
    // 북마크 목록 로드
    public void loadBookmarks() {
        bookmarkedMovies.clear();// UI 초기화, 기존 영화 데이터 리스트 비우기
        bookmarkListPanel.removeAll();//화면에 표시된 모든 영화 카드 제거
        bookmarkListPanel.revalidate();//레이아웃 다시 계산
        bookmarkListPanel.repaint();//화면 다시 그리기
        
        countLabel.setText("로딩 중..."); //로딩중 메세지 표시
        
        // 서버에 북마크 요청
        client.requestBookmarks();
    }
    
    // 영화 카드 생성
    private JPanel createMovieCard(MovieData movie) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(10, 10)); //좌측에 정보, 우측에 버튼들 배치
        card.setBorder(BorderFactory.createCompoundBorder( //테두리 설정
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(Color.WHITE); //카드 배경색
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));//최대크기. 가로: 무제한, 세로는 150픽셀
        
        // 좌측 - 영화 정보
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        
        JLabel nameLabel = new JLabel(movie.movieNm); //영화 제목 레이블
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT); //좌측정렬
        infoPanel.add(nameLabel);
        
        infoPanel.add(Box.createVerticalStrut(5)); //제목과 다음 정보 사이 간격
        
        //순위 및 개봉일
        JLabel rankLabel = new JLabel("순위: " + movie.rank + "위 | 개봉일: " + movie.openDt);
        rankLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        rankLabel.setForeground(new Color(100, 100, 100)); //회색
        rankLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(rankLabel);
        
        infoPanel.add(Box.createVerticalStrut(3)); //순위 ~ 누적관객 사이 간격
        
        //누적 관객 수
        JLabel audiLabel = new JLabel("누적관객: " + String.format("%,d", movie.audiAcc) + "명"); //1000단위 , 추가
        audiLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        audiLabel.setForeground(new Color(100, 100, 100));
        audiLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(audiLabel);
        
        card.add(infoPanel, BorderLayout.CENTER); //정보 패널을 카드 중앙(L)에 배치
        
        // 우측 - 버튼들
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 0)); //버튼들을 우측정렬로 가로나열
        buttonPanel.setBackground(Color.WHITE);
        
        //1.채팅방 입장
        JButton chatButton = new JButton("채팅방 입장");
        chatButton.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        chatButton.addActionListener(new ActionListener() { //클릭 시 해당 영화 채팅방으로 이동
            public void actionPerformed(ActionEvent e) {
                mainFrame.showChatRoom(movie.movieCd, movie.movieNm);
            }
        });
        buttonPanel.add(chatButton);
        
        //2. 감상평 보기
        JButton reviewButton = new JButton("감상평 보기");
        reviewButton.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        reviewButton.addActionListener(new ActionListener() { //클릭 시 해당 영화의 감상평 화면으로 이동
            public void actionPerformed(ActionEvent e) {
                mainFrame.showReviews(movie.movieCd, movie.movieNm);
            }
        });
        buttonPanel.add(reviewButton);

        //3. 삭제
        JButton deleteButton = new JButton("삭제");
        deleteButton.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        deleteButton.setForeground(new Color(244, 67, 54)); //빨간색 강조
        deleteButton.addActionListener(new ActionListener() {//북마크 삭제 확인 후 서버에 요청
            public void actionPerformed(ActionEvent e) {
                int result = JOptionPane.showConfirmDialog( //확인 다이얼로그
                    BookmarkPanel.this,//부모컴포넌트
                    "'" + movie.movieNm + "'을(를) 북마크에서 삭제하시겠습니까?",
                    "북마크 삭제", //다이얼로그 제목
                    JOptionPane.YES_NO_OPTION //버튼 타입
                );
                
                if (result == JOptionPane.YES_OPTION) { //'예'를 선택했다면
                    client.deleteBookmark(movie.movieCd); //DELETE_BOOKMARK|영화코드|END 전송
                }
            }
        });
        buttonPanel.add(deleteButton); //버튼패널을 카드 우측에 배치
        
        card.add(buttonPanel, BorderLayout.EAST);
        
        return card; //완성된 카드 반환
    }
    
    // 서버 메시지 처리
    public void handleMessage(String message) {
        // MOVIES_COUNT - 서버가 북마크를 MOVIES 형식으로 보냄
        if (message.startsWith("MOVIES_COUNT")) {
            String[] parts = message.split("\\|");
            if (parts.length >= 2) {
                int count = Integer.parseInt(parts[1]);
                
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        bookmarkedMovies.clear();
                        bookmarkListPanel.removeAll();
                        countLabel.setText("총 " + count + "개의 영화");
                    }
                });
            }
            
        } else if (message.startsWith("MOVIES_DATA")) {
            // 북마크 영화 데이터: MOVIES_DATA|영화코드|영화제목|순위|개봉일|누적관객|누적매출|END
            String[] parts = message.split("\\|");
            if (parts.length >= 7) {
                MovieData movie = new MovieData();
                movie.movieCd = parts[1];
                movie.movieNm = parts[2];
                movie.rank = Integer.parseInt(parts[3]);
                movie.openDt = parts[4];
                movie.audiAcc = Long.parseLong(parts[5]);
                movie.salesAcc = Long.parseLong(parts[6]);
                
                bookmarkedMovies.add(movie);
                
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        JPanel card = createMovieCard(movie);
                        bookmarkListPanel.add(card);
                        bookmarkListPanel.add(Box.createVerticalStrut(10));
                        bookmarkListPanel.revalidate();
                        bookmarkListPanel.repaint();
                    }
                });
            }
            
        } else if (message.startsWith("MOVIES_END")) {
            // 북마크 로딩 완료
            System.out.println("BookmarkPanel> 북마크 로드 완료: " + bookmarkedMovies.size() + "개");
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    // 영화가 하나도 없을 때만 빈 메시지 표시
                    if (bookmarkedMovies.isEmpty()) {
                        JLabel emptyLabel = new JLabel("북마크한 영화가 없습니다.");
                        emptyLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
                        emptyLabel.setForeground(new Color(150, 150, 150));
                        emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                        
                        bookmarkListPanel.add(Box.createVerticalStrut(50));
                        bookmarkListPanel.add(emptyLabel);
                        
                        JLabel guideLabel = new JLabel("영화 목록에서 북마크를 추가해보세요!");
                        guideLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
                        guideLabel.setForeground(new Color(150, 150, 150));
                        guideLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                        
                        bookmarkListPanel.add(Box.createVerticalStrut(10));
                        bookmarkListPanel.add(guideLabel);
                        
                        bookmarkListPanel.revalidate();
                        bookmarkListPanel.repaint();
                    }
                }
            });
            
        } else if (message.startsWith("BOOKMARK_DEL_OK")) {
            // 북마크 삭제 성공
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JOptionPane.showMessageDialog(BookmarkPanel.this, "북마크가 삭제되었습니다!");
                    loadBookmarks();
                }
            });
            
        } else if (message.startsWith("BOOKMARK_DEL_FAIL")) {
            // 북마크 삭제 실패
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JOptionPane.showMessageDialog(BookmarkPanel.this, "북마크 삭제에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                }
            });
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