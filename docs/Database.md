# 🗄️ 데이터베이스 설계

## 개요

MovieSync는 **SQLite** 데이터베이스를 사용하여 사용자, 영화, 감상평, 북마크 정보를 저장합니다.

### 데이터베이스 정보

| 항목 | 값 |
|------|-----|
| **DBMS** | SQLite |
| **파일명** | `moviesync.db` |
| **JDBC URL** | `jdbc:sqlite:moviesync.db` |
| **드라이버** | `org.sqlite.JDBC` |

---

## ER 다이어그램

```
┌─────────────────┐          ┌─────────────────┐
│     Users       │          │     Movies      │
├─────────────────┤          ├─────────────────┤
│ PK user_id      │          │ PK movie_cd     │
│    username     │          │    movie_nm     │
│    created_at   │          │    rank         │
└────────┬────────┘          │    open_dt      │
         │                   │    audi_acc     │
         │                   │    sales_acc    │
         │                   │    update_dt    │
         │                   └────────┬────────┘
         │                            │
         │     ┌──────────────────────┼──────────────────────┐
         │     │                      │                      │
         ▼     ▼                      ▼                      │
┌─────────────────┐          ┌─────────────────┐             │
│    Reviews      │          │   Bookmarks     │             │
├─────────────────┤          ├─────────────────┤             │
│ PK review_id    │          │ PK bookmark_id  │             │
│ FK user_id      │──────────│ FK user_id      │◀────────────┘
│ FK movie_cd     │──────────│ FK movie_cd     │
│    rating       │          │    created_at   │
│    content      │          └─────────────────┘
│    created_at   │
└─────────────────┘
```

---

## 테이블 정의

### 1. Users (사용자)

사용자 정보를 저장하는 테이블입니다.

```sql
CREATE TABLE IF NOT EXISTS Users (
    user_id    INTEGER PRIMARY KEY AUTOINCREMENT,
    username   TEXT NOT NULL UNIQUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| `user_id` | INTEGER | PK, AUTO_INCREMENT | 사용자 고유 ID |
| `username` | TEXT | NOT NULL, UNIQUE | 닉네임 (중복 불가) |
| `created_at` | DATETIME | DEFAULT CURRENT_TIMESTAMP | 가입 일시 |

#### 예시 데이터

| user_id | username | created_at |
|---------|----------|------------|
| 1 | 영화광 | 2025-12-01 10:00:00 |
| 2 | 씨네필 | 2025-12-01 11:30:00 |
| 3 | 무비러버 | 2025-12-02 09:15:00 |

---

### 2. Movies (영화)

박스오피스 영화 정보를 저장하는 테이블입니다.

```sql
CREATE TABLE IF NOT EXISTS Movies (
    movie_cd   TEXT PRIMARY KEY,
    movie_nm   TEXT NOT NULL,
    rank       INTEGER,
    open_dt    TEXT,
    audi_acc   INTEGER,
    sales_acc  INTEGER,
    update_dt  DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| `movie_cd` | TEXT | PK | 영화 코드 (KOFIC 제공) |
| `movie_nm` | TEXT | NOT NULL | 영화 제목 |
| `rank` | INTEGER | | 박스오피스 순위 (1~10) |
| `open_dt` | TEXT | | 개봉일 (YYYY-MM-DD) |
| `audi_acc` | INTEGER | | 누적 관객 수 |
| `sales_acc` | INTEGER | | 누적 매출액 (원) |
| `update_dt` | DATETIME | DEFAULT CURRENT_TIMESTAMP | 데이터 갱신 일시 |

#### 예시 데이터

| movie_cd | movie_nm | rank | open_dt | audi_acc | sales_acc |
|----------|----------|------|---------|----------|-----------|
| 20234567 | 위키드 | 1 | 2024-11-20 | 5000000 | 50000000000 |
| 20234568 | 글래디에이터 2 | 2 | 2024-11-06 | 3500000 | 35000000000 |

---

### 3. Reviews (감상평)

사용자 감상평을 저장하는 테이블입니다.

```sql
CREATE TABLE IF NOT EXISTS Reviews (
    review_id  INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id    INTEGER NOT NULL,
    movie_cd   TEXT NOT NULL,
    rating     INTEGER NOT NULL CHECK(rating >= 1 AND rating <= 5),
    content    TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (movie_cd) REFERENCES Movies(movie_cd)
);
```

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| `review_id` | INTEGER | PK, AUTO_INCREMENT | 감상평 고유 ID |
| `user_id` | INTEGER | FK → Users, NOT NULL | 작성자 ID |
| `movie_cd` | TEXT | FK → Movies, NOT NULL | 영화 코드 |
| `rating` | INTEGER | NOT NULL, CHECK(1~5) | 별점 (1~5) |
| `content` | TEXT | | 감상평 내용 |
| `created_at` | DATETIME | DEFAULT CURRENT_TIMESTAMP | 작성 일시 |

#### 예시 데이터

| review_id | user_id | movie_cd | rating | content | created_at |
|-----------|---------|----------|--------|---------|------------|
| 1 | 1 | 20234567 | 5 | 정말 감동적인 영화였어요! | 2025-12-01 15:00:00 |
| 2 | 2 | 20234567 | 4 | OST가 너무 좋아요 | 2025-12-01 16:30:00 |
| 3 | 1 | 20234568 | 3 | 기대만큼은 아니었어요 | 2025-12-02 10:00:00 |

---

### 4. Bookmarks (북마크)

사용자 북마크를 저장하는 테이블입니다.

```sql
CREATE TABLE IF NOT EXISTS Bookmarks (
    bookmark_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id     INTEGER NOT NULL,
    movie_cd    TEXT NOT NULL,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (movie_cd) REFERENCES Movies(movie_cd),
    UNIQUE(user_id, movie_cd)
);
```

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| `bookmark_id` | INTEGER | PK, AUTO_INCREMENT | 북마크 고유 ID |
| `user_id` | INTEGER | FK → Users, NOT NULL | 사용자 ID |
| `movie_cd` | TEXT | FK → Movies, NOT NULL | 영화 코드 |
| `created_at` | DATETIME | DEFAULT CURRENT_TIMESTAMP | 북마크 일시 |
| | | UNIQUE(user_id, movie_cd) | 중복 북마크 방지 |

#### 예시 데이터

| bookmark_id | user_id | movie_cd | created_at |
|-------------|---------|----------|------------|
| 1 | 1 | 20234567 | 2025-12-01 12:00:00 |
| 2 | 1 | 20234568 | 2025-12-01 12:05:00 |
| 3 | 2 | 20234567 | 2025-12-02 09:00:00 |

---

## 주요 쿼리

### Users 관련

```sql
-- 사용자 생성
INSERT INTO Users (username) VALUES (?);

-- 닉네임 중복 체크
SELECT COUNT(*) FROM Users WHERE username = ?;

-- userId로 닉네임 조회
SELECT username FROM Users WHERE user_id = ?;
```

### Movies 관련

```sql
-- 영화 저장/갱신 (UPSERT)
REPLACE INTO Movies (movie_cd, movie_nm, rank, open_dt, audi_acc, sales_acc, update_dt) 
VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP);

-- Top 10 조회
SELECT * FROM Movies ORDER BY rank LIMIT 10;

-- 영화 상세 조회
SELECT * FROM Movies WHERE movie_cd = ?;

-- 영화 검색
SELECT * FROM Movies WHERE movie_nm LIKE ? ORDER BY rank;
```

### Reviews 관련

```sql
-- 감상평 작성
INSERT INTO Reviews (user_id, movie_cd, rating, content) VALUES (?, ?, ?, ?);

-- 영화별 감상평 조회 (작성자명 JOIN)
SELECT r.review_id, u.username, r.rating, r.content, r.created_at 
FROM Reviews r JOIN Users u ON r.user_id = u.user_id 
WHERE r.movie_cd = ? ORDER BY r.created_at DESC;

-- 평균 평점 계산
SELECT AVG(rating) as avg_rating FROM Reviews WHERE movie_cd = ?;

-- 감상평 개수 조회
SELECT COUNT(*) as count FROM Reviews WHERE movie_cd = ?;

-- 감상평 삭제 (본인 확인)
-- 1. 권한 확인
SELECT user_id FROM Reviews WHERE review_id = ?;
-- 2. 삭제 실행
DELETE FROM Reviews WHERE review_id = ?;
```

### Bookmarks 관련

```sql
-- 북마크 추가
INSERT INTO Bookmarks (user_id, movie_cd) VALUES (?, ?);

-- 북마크 삭제
DELETE FROM Bookmarks WHERE user_id = ? AND movie_cd = ?;

-- 북마크 목록 조회 (영화 정보 JOIN)
SELECT m.* FROM Bookmarks b JOIN Movies m ON b.movie_cd = m.movie_cd 
WHERE b.user_id = ? ORDER BY b.created_at DESC;
```

---

## 인덱스 (권장)

성능 향상을 위해 다음 인덱스 추가를 권장합니다:

```sql
-- Reviews: 영화별 조회 최적화
CREATE INDEX idx_reviews_movie ON Reviews(movie_cd);

-- Reviews: 사용자별 조회 최적화
CREATE INDEX idx_reviews_user ON Reviews(user_id);

-- Bookmarks: 사용자별 조회 최적화
CREATE INDEX idx_bookmarks_user ON Bookmarks(user_id);
```

---

## 데이터 무결성

### 외래 키 제약

SQLite에서 외래 키를 활성화하려면:

```sql
PRAGMA foreign_keys = ON;
```

### CHECK 제약

- `Reviews.rating`: 1~5 범위만 허용

### UNIQUE 제약

- `Users.username`: 닉네임 중복 방지
- `Bookmarks(user_id, movie_cd)`: 동일 영화 중복 북마크 방지

---

[← 돌아가기](./README.md)
