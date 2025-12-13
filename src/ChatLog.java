//프로젝트 이름 : MovieSync
//개발자 : 권미리
//개발 기간: 2025.12.01 ~ 2025.12.13
//채팅 로그를 채팅방별로 파일에 저장하는 클래스

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatLog {
    private static String CONNECTION_LOG_FILE = "connection_log.txt"; // 접속/종료 로그
    
    // 현재 시간을 문자열로 변환
    private static String getTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(new Date());
    }
    
    // 파일에 로그를 쓰는 메소드
    private static void writeLog(String filename, String log) {
        try {
            FileWriter fw = new FileWriter(filename, true); // append 모드
            fw.write(log + "\n");
            fw.close();
        } catch(IOException e) {
            System.err.println("ChatLog> 로그 저장 실패: " + e.getMessage());
        }
    }
    
    // 채팅방별 채팅 로그 저장
    // roomId: "room_20234567" 형식
    // senderId: 발신자 닉네임
    // message: 채팅 메시지
    public static void logChatRoom(String roomId, String senderId, String message) {
        String filename = "chatlog_" + roomId + ".txt";
        String log = "[" + getTime() + "] " + senderId + ": " + message;
        writeLog(filename, log);
    }
    
    // 사용자 접속 로그
    public static void logConnect(String userId) {
        String log = "[" + getTime() + "] [접속] " + userId + " 사용자 접속";
        writeLog(CONNECTION_LOG_FILE, log);
    }
    
    // 사용자 종료 로그
    public static void logDisconnect(String userId) {
        String log = "[" + getTime() + "] [종료] " + userId + " 사용자 접속 종료";
        writeLog(CONNECTION_LOG_FILE, log);
    }
    
    // 채팅방 입장 로그
    public static void logJoinRoom(String roomId, String userId, String movieName) {
        String filename = "chatlog_" + roomId + ".txt";
        String log = "[" + getTime() + "] [입장] " + userId + "님이 '" + movieName + "' 채팅방에 입장했습니다.";
        writeLog(filename, log);
    }
    
    // 채팅방 퇴장 로그
    public static void logLeaveRoom(String roomId, String userId, String movieName) {
        String filename = "chatlog_" + roomId + ".txt";
        String log;
        if (movieName != null && !movieName.isEmpty()) {
            log = "[" + getTime() + "] [퇴장] " + userId + "님이 '" + movieName + "' 채팅방에서 퇴장했습니다.";
        } else {
            log = "[" + getTime() + "] [퇴장] " + userId + "님이 채팅방에서 퇴장했습니다.";
        }
        writeLog(filename, log);
    }
}