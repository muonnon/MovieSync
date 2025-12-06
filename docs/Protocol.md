# 📡 통신 프로토콜

## 개요

MovieSync는 **TCP 소켓** 기반의 텍스트 프로토콜을 사용합니다. 모든 메시지는 `//`를 구분자로 사용하며, `END`로 종료됩니다.

### 기본 정보

| 항목 | 값 |
|------|-----|
| **전송 방식** | TCP Socket |
| **포트** | 55555 |
| **인코딩** | UTF-8 (DataInputStream/DataOutputStream) |
| **메시지 구분자** | `//` |
| **메시지 종료 마커** | `END` |

---

## 메시지 형식

### 기본 구조

```
[태그]//[데이터1]//[데이터2]//...//END
```

### 예시

```
LOGIN//영화광//END
MOVIES_DATA//20234567//위키드//1//2024-11-20//5000000//50000000000//END
```

---

## 클라이언트 → 서버 메시지

클라이언트가 서버로 전송하는 14종류의 메시지입니다.

| 코드 | 태그 | 설명 |
|------|------|------|
| 0 | LOGIN | 로그인 |
| 1 | GET_MOVIES | 영화 목록 조회 |
| 2 | GET_DETAIL | 영화 상세 조회 |
| 3 | JOIN_ROOM | 채팅방 입장 |
| 4 | LEAVE_ROOM | 채팅방 퇴장 |
| 5 | CHAT | 채팅 메시지 |
| 6 | GET_REVIEWS | 감상평 조회 |
| 7 | SUBMIT_REVIEW | 감상평 작성 |
| 8 | DELETE_REVIEW | 감상평 삭제 |
| 9 | SEARCH_MOVIE | 영화 검색 |
| 10 | ADD_BOOKMARK | 북마크 추가 |
| 11 | DELETE_BOOKMARK | 북마크 삭제 |
| 12 | GET_BOOKMARKS | 북마크 목록 조회 |
| 13 | DISCONNECT | 연결 종료 |

---

### 0. LOGIN - 로그인

```
LOGIN//닉네임//END
```

| 필드 | 타입 | 설명 |
|------|------|------|
| 닉네임 | String | 사용할 닉네임 (2~20자) |

**예시:**
```
LOGIN//영화광//END
```

---

### 1. GET_MOVIES - 영화 목록 조회

```
GET_MOVIES//END
```

추가 필드 없음. 박스오피스 Top 10을 요청합니다.

---

### 2. GET_DETAIL - 영화 상세 조회

```
GET_DETAIL//영화코드//END
```

| 필드 | 타입 | 설명 |
|------|------|------|
| 영화코드 | String | KOFIC 영화 코드 |

**예시:**
```
GET_DETAIL//20234567//END
```

---

### 3. JOIN_ROOM - 채팅방 입장

```
JOIN_ROOM//영화코드//영화제목//END
```

| 필드 | 타입 | 설명 |
|------|------|------|
| 영화코드 | String | KOFIC 영화 코드 |
| 영화제목 | String | 영화 이름 |

**예시:**
```
JOIN_ROOM//20234567//위키드//END
```

---

### 4. LEAVE_ROOM - 채팅방 퇴장

```
LEAVE_ROOM//영화코드//END
```

| 필드 | 타입 | 설명 |
|------|------|------|
| 영화코드 | String | 퇴장할 채팅방의 영화 코드 |

---

### 5. CHAT - 채팅 메시지 전송

```
CHAT//영화코드//메시지//END
```

| 필드 | 타입 | 설명 |
|------|------|------|
| 영화코드 | String | 채팅방 영화 코드 |
| 메시지 | String | 전송할 메시지 |

**예시:**
```
CHAT//20234567//이 영화 정말 재밌어요!//END
```

---

### 6. GET_REVIEWS - 감상평 조회

```
GET_REVIEWS//영화코드//END
```

| 필드 | 타입 | 설명 |
|------|------|------|
| 영화코드 | String | 조회할 영화 코드 |

---

### 7. SUBMIT_REVIEW - 감상평 작성

```
SUBMIT_REVIEW//영화코드//별점//감상평내용//END
```

| 필드 | 타입 | 설명 |
|------|------|------|
| 영화코드 | String | 대상 영화 코드 |
| 별점 | int | 1~5 사이 정수 |
| 감상평내용 | String | 감상평 텍스트 |

**예시:**
```
SUBMIT_REVIEW//20234567//5//정말 감동적인 영화였습니다!//END
```

---

### 8. DELETE_REVIEW - 감상평 삭제

```
DELETE_REVIEW//reviewId//END
```

| 필드 | 타입 | 설명 |
|------|------|------|
| reviewId | int | 삭제할 감상평 ID |

---

### 9. SEARCH_MOVIE - 영화 검색

```
SEARCH_MOVIE//검색어//END
```

| 필드 | 타입 | 설명 |
|------|------|------|
| 검색어 | String | 영화 제목 검색어 |

---

### 10. ADD_BOOKMARK - 북마크 추가

```
ADD_BOOKMARK//영화코드//END
```

| 필드 | 타입 | 설명 |
|------|------|------|
| 영화코드 | String | 북마크할 영화 코드 |

---

### 11. DELETE_BOOKMARK - 북마크 삭제

```
DELETE_BOOKMARK//영화코드//END
```

| 필드 | 타입 | 설명 |
|------|------|------|
| 영화코드 | String | 북마크 해제할 영화 코드 |

---

### 12. GET_BOOKMARKS - 북마크 목록 조회

```
GET_BOOKMARKS//END
```

추가 필드 없음. 로그인한 사용자의 북마크 목록을 조회합니다.

---

### 13. DISCONNECT - 연결 종료

```
DISCONNECT//END
```

추가 필드 없음. 서버와의 연결을 정상 종료합니다.

---

## 서버 → 클라이언트 메시지

서버가 클라이언트로 전송하는 응답 메시지입니다.

### 인증 관련

#### LOGIN_OK - 로그인 성공

```
LOGIN_OK//userId//닉네임 확인 완료//END
```

| 필드 | 타입 | 설명 |
|------|------|------|
| userId | int | 부여된 사용자 ID |

#### LOGIN_FAIL - 로그인 실패

```
LOGIN_FAIL//에러메시지//END
```

**예시:**
```
LOGIN_FAIL//이미 사용 중인 닉네임입니다//END
```

#### WELCOME - 환영 메시지

```
WELCOME//MovieSync에 오신 것을 환영합니다!//END
```

---

### 영화 목록 관련

#### MOVIES_COUNT - 영화 개수

```
MOVIES_COUNT//총개수//END
```

#### MOVIES_DATA - 영화 데이터

```
MOVIES_DATA//영화코드//영화제목//순위//개봉일//누적관객//누적매출//END
```

| 필드 | 타입 | 설명 |
|------|------|------|
| 영화코드 | String | KOFIC 영화 코드 |
| 영화제목 | String | 영화 이름 |
| 순위 | int | 박스오피스 순위 |
| 개봉일 | String | YYYY-MM-DD 형식 |
| 누적관객 | long | 누적 관객 수 |
| 누적매출 | long | 누적 매출액 (원) |

**예시:**
```
MOVIES_DATA//20234567//위키드//1//2024-11-20//5000000//50000000000//END
```

#### MOVIES_END - 영화 목록 전송 완료

```
MOVIES_END//END
```

---

### 영화 상세 관련

#### DETAIL - 영화 상세 정보

```
DETAIL//영화코드//영화제목//순위//개봉일//누적관객//누적매출//평균평점//END
```

| 필드 | 타입 | 설명 |
|------|------|------|
| 평균평점 | double | 감상평 평균 별점 (0.0~5.0) |

---

### 채팅 관련

#### ROOM_OK - 채팅방 입장 성공

```
ROOM_OK//roomId//영화제목//END
```

| 필드 | 타입 | 설명 |
|------|------|------|
| roomId | String | 채팅방 ID (room_영화코드) |

#### USER_JOIN - 사용자 입장 알림

```
USER_JOIN//닉네임//현재인원//END
```

#### USER_LEFT - 사용자 퇴장 알림

```
USER_LEFT//닉네임//현재인원//END
```

#### CHAT_ALL - 채팅 메시지 브로드캐스트

```
CHAT_ALL//발신자//메시지//END
```

---

### 감상평 관련

#### REV_SUMMARY - 감상평 요약

```
REV_SUMMARY//영화코드//영화제목//평균평점//감상평개수//END
```

#### REV_COUNT - 감상평 개수

```
REV_COUNT//총개수//END
```

#### REV_DATA - 감상평 데이터

```
REV_DATA//reviewId//작성자//별점//내용//작성일시//END
```

| 필드 | 타입 | 설명 |
|------|------|------|
| reviewId | int | 감상평 ID |
| 작성자 | String | 작성자 닉네임 |
| 별점 | int | 1~5 |
| 내용 | String | 감상평 텍스트 |
| 작성일시 | String | 작성 시간 |

#### REV_END - 감상평 전송 완료

```
REV_END//END
```

#### REV_OK - 감상평 작성 성공

```
REV_OK//reviewId//감상평이 저장되었습니다//END
```

#### REV_FAIL - 감상평 작성 실패

```
REV_FAIL//에러메시지//END
```

---

### 삭제 관련

#### DEL_OK - 삭제 성공

```
DEL_OK//감상평이 삭제되었습니다//END
```

#### DEL_FAIL - 삭제 실패

```
DEL_FAIL//에러메시지//END
```

---

### 북마크 관련

#### BOOKMARK_OK - 북마크 추가 성공

```
BOOKMARK_OK//북마크가 추가되었습니다//END
```

#### BOOKMARK_DEL_OK - 북마크 삭제 성공

```
BOOKMARK_DEL_OK//북마크가 삭제되었습니다//END
```

---

### 연결 종료

#### DISCONNECT_OK - 연결 종료 확인

```
DISCONNECT_OK//연결이 종료되었습니다//END
```

---

### 에러

#### ERROR - 일반 에러

```
ERROR//에러메시지//END
```

---

## 통신 시퀀스 예시

### 1. 로그인 시퀀스

```
[Client]                         [Server]
    │                               │
    │──── LOGIN//영화광//END ──────▶│
    │                               │ DB: 닉네임 중복 체크
    │                               │ DB: 사용자 생성
    │◀─ LOGIN_OK//1//영화광 확인 ───│
    │◀─ WELCOME//환영합니다!//END ──│
    │                               │
```

### 2. 영화 목록 조회 시퀀스

```
[Client]                         [Server]
    │                               │
    │──── GET_MOVIES//END ─────────▶│
    │                               │ DB: Top 10 조회
    │◀─ MOVIES_COUNT//10//END ─────│
    │◀─ MOVIES_DATA//...//END ─────│ (10회 반복)
    │◀─ MOVIES_DATA//...//END ─────│
    │         ...                   │
    │◀─ MOVIES_END//END ───────────│
    │                               │
```

### 3. 채팅 시퀀스

```
[Client A]              [Server]              [Client B]
    │                      │                      │
    │── JOIN_ROOM//... ───▶│                      │
    │◀─ ROOM_OK//... ──────│                      │
    │◀─ USER_JOIN//A//1 ───│──▶ USER_JOIN//A//1 ─▶│
    │                      │                      │
    │── CHAT//...//Hi! ───▶│                      │
    │◀─ CHAT_ALL//A//Hi! ──│──▶ CHAT_ALL//A//Hi! ─▶│
    │                      │                      │
```

---

## 메시지 파싱

### ReceivedMSGTokenizer 사용

```java
ReceivedMSGTokenizer tk = new ReceivedMSGTokenizer();

// 메시지 타입 감지 (0~13)
int msgType = tk.detection(msg);

// 필드 추출
switch(msgType) {
    case 0: // LOGIN
        String username = tk.findUsername(msg);
        break;
    case 5: // CHAT
        String movieCd = tk.findChatRoomCode(msg);
        String message = tk.findChatMessage(msg);
        break;
    // ...
}
```

### MSGTable 코드 참조

```java
MSGTable mt = new MSGTable();
// mt.MSGtags[0] = "LOGIN"
// mt.MSGtags[5] = "CHAT"
// ...
```

---

[← 돌아가기](./README.md)
