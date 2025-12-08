# Database Schema Design

## ERD Overview

```
[사용자 / 지도사 그룹]

user ──< instructor_group_member >── instructor_group


[인증]

user ──< token


[시즌 / 시즌 참여 / 티켓]

instructor_group ──< season
                         │
                         ├──< season_enrollment >── user
                         │         │
                         │         └──< season_enrollment_log
                         │
                         └──< season_ticket_account >── user
                                    │
                                    └──< ticket_log


[레슨 / 지도사 / 예약 / 출석]

season ──< lesson
           │
           ├──< lesson_instructor >── instructor_group_member
           │
           └──< reservation >── user
                        │
                        ├──> season_ticket_account
                        └──< lesson_attendance
```

> No FK constraints for MSA migration flexibility

---

## Enum Types

```sql
CREATE TYPE season_status AS ENUM ('ACTIVE', 'CLOSED');
CREATE TYPE enrollment_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'WITHDRAWN');
CREATE TYPE enrollment_log_type AS ENUM ('APPLIED', 'REAPPLIED', 'APPROVED', 'REJECTED', 'WITHDRAWN');
CREATE TYPE lesson_status AS ENUM ('SCHEDULED', 'CANCELLED');
CREATE TYPE reservation_status AS ENUM ('RESERVED', 'CANCELLED_BY_USER', 'CANCELLED_BY_INSTRUCTOR');
CREATE TYPE ticket_log_type AS ENUM ('GRANT', 'USE', 'REFUND', 'ADDITIONAL');
CREATE TYPE attendance_status AS ENUM ('ATTENDED', 'NO_SHOW');
```

---

## Tables

### 1. user
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PK | 내부용 |
| uuid | UUID | UNIQUE, NOT NULL, DEFAULT gen_random_uuid() | 외부 API용 |
| email | VARCHAR(255) | UNIQUE, NOT NULL | 로그인 이메일 |
| password | VARCHAR(255) | NOT NULL | BCrypt 해시 |
| name | VARCHAR(100) | NOT NULL | 이름 |
| phone | VARCHAR(20) | | 연락처 |
| is_system_admin | BOOLEAN | NOT NULL DEFAULT false | 시스템 관리자 여부 |
| created_at | TIMESTAMPTZ | NOT NULL | |
| updated_at | TIMESTAMPTZ | NOT NULL | |
| deleted_at | TIMESTAMPTZ | | soft delete |

> - 시스템 관리자: is_system_admin = true
> - 지도사: instructor_group_member 레코드 존재
> - 일반 회원: 둘 다 해당 없음

---

### 2. instructor_group
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PK | 내부용 |
| uuid | UUID | UNIQUE, NOT NULL, DEFAULT gen_random_uuid() | 외부 API용 |
| name | VARCHAR(200) | NOT NULL | 그룹명 (학원명) |
| description | TEXT | | 그룹 설명 |
| leader_user_id | BIGINT | NOT NULL | 그룹장 user.id 참조 |
| created_at | TIMESTAMPTZ | NOT NULL | |
| updated_at | TIMESTAMPTZ | NOT NULL | |
| deleted_at | TIMESTAMPTZ | | soft delete |

> - 그룹당 그룹장 1명
> - leader_user_id는 user.id를 직접 참조 (순환 참조 방지)
> - 그룹장은 반드시 해당 그룹의 instructor_group_member여야 함 (애플리케이션 레벨 검증)

---

### 3. instructor_group_member
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PK | 내부용 |
| uuid | UUID | UNIQUE, NOT NULL, DEFAULT gen_random_uuid() | 외부 API용 |
| instructor_group_id | BIGINT | NOT NULL | instructor_group.id 참조 |
| user_id | BIGINT | NOT NULL | user.id 참조 |
| created_at | TIMESTAMPTZ | NOT NULL | |
| updated_at | TIMESTAMPTZ | NOT NULL | |
| deleted_at | TIMESTAMPTZ | | soft delete |

> - UNIQUE(instructor_group_id, user_id) - 한 그룹 내 중복 가입 방지
> - 한 사용자가 여러 그룹에 소속 가능 (N:M)
> - MVP: 그룹 멤버 = 모든 지도사 권한 (세분화된 역할/권한은 Post-MVP)

---

### 4. token
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PK | |
| user_id | BIGINT | NOT NULL | user.id 참조 |
| token | VARCHAR(255) | UNIQUE, NOT NULL | Opaque token (1 day expiry) |
| expires_at | TIMESTAMPTZ | NOT NULL | 만료 시간 |
| created_at | TIMESTAMPTZ | NOT NULL | |

> 새 로그인 시 기존 토큰 삭제 → 단일 세션 유지

---

### 5. season
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PK | 내부용 |
| uuid | UUID | UNIQUE, NOT NULL, DEFAULT gen_random_uuid() | 외부 API용 |
| instructor_group_id | BIGINT | NOT NULL | instructor_group.id 참조 |
| title | VARCHAR(200) | NOT NULL | 시즌명 |
| description | TEXT | | 시즌 설명 |
| start_date | DATE | NOT NULL | 시작일 |
| end_date | DATE | NOT NULL | 종료일 |
| capacity | INTEGER | NOT NULL | 시즌 정원 |
| default_ticket_count | INTEGER | NOT NULL | 참여 승인 시 기본 부여 티켓 수 |
| status | season_status | NOT NULL DEFAULT 'ACTIVE' | ACTIVE / CLOSED |
| created_by | BIGINT | NOT NULL | 생성한 instructor_group_member.id 참조 |
| created_at | TIMESTAMPTZ | NOT NULL | |
| updated_at | TIMESTAMPTZ | NOT NULL | |
| deleted_at | TIMESTAMPTZ | | soft delete |

> 시즌은 그룹 단위로 생성

---

### 6. season_enrollment
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PK | 내부용 |
| uuid | UUID | UNIQUE, NOT NULL, DEFAULT gen_random_uuid() | 외부 API용 |
| season_id | BIGINT | NOT NULL | season.id 참조 |
| member_id | BIGINT | NOT NULL | user.id 참조 |
| status | enrollment_status | NOT NULL DEFAULT 'PENDING' | PENDING / APPROVED / REJECTED / WITHDRAWN |
| created_at | TIMESTAMPTZ | NOT NULL | 신청 시간 |
| updated_at | TIMESTAMPTZ | NOT NULL | |

> - 현재 상태 스냅샷 테이블
> - REJECTED/WITHDRAWN 상태에서 재신청 가능
> - PENDING/APPROVED 상태일 때 중복 신청 방지 (부분 UNIQUE 인덱스 사용)

---

### 7. season_enrollment_log
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PK | |
| season_enrollment_id | BIGINT | NOT NULL | season_enrollment.id 참조 |
| type | enrollment_log_type | NOT NULL | APPLIED / REAPPLIED / APPROVED / REJECTED / WITHDRAWN |
| actor_id | BIGINT | | 작업 수행자 user.id (지도사/본인) |
| note | TEXT | | 비고 (거절 사유 등) |
| created_at | TIMESTAMPTZ | NOT NULL | |

> - 시즌 참여 상태 변화 이력
> - 재신청, 승인/거절 반복 등 타임라인 추적

---

### 8. season_ticket_account
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PK | |
| season_id | BIGINT | NOT NULL | season.id 참조 |
| member_id | BIGINT | NOT NULL | user.id 참조 |
| balance | INTEGER | NOT NULL DEFAULT 0 | 현재 잔액 |
| created_at | TIMESTAMPTZ | NOT NULL | |
| updated_at | TIMESTAMPTZ | NOT NULL | |

> - UNIQUE(season_id, member_id) - 시즌별 회원당 1개 티켓 계좌
> - season_enrollment과 1:1 관계 (APPROVED 시 생성)

---

### 9. ticket_log
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PK | |
| season_ticket_account_id | BIGINT | NOT NULL | season_ticket_account.id 참조 |
| amount | INTEGER | NOT NULL | 변동량 (+/-) |
| type | ticket_log_type | NOT NULL | GRANT / USE / REFUND / ADDITIONAL |
| description | VARCHAR(500) | | 비고 |
| reservation_id | BIGINT | | 관련 reservation.id (USE/REFUND 타입만 해당) |
| granted_by | BIGINT | | 티켓 부여한 instructor_group_member.id (GRANT/ADDITIONAL만 해당) |
| created_at | TIMESTAMPTZ | NOT NULL | |

> - GRANT: 참여 승인 시 기본 티켓 부여
> - USE: 레슨 예약 시 차감
> - REFUND: 예약 취소 시 환불 (레슨 날짜 기준 D-3까지 환불, D-2부터 환불 불가)
> - ADDITIONAL: 지도사가 추가 부여

---

### 10. lesson
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PK | 내부용 |
| uuid | UUID | UNIQUE, NOT NULL, DEFAULT gen_random_uuid() | 외부 API용 |
| season_id | BIGINT | NOT NULL | season.id 참조 |
| title | VARCHAR(200) | NOT NULL | 레슨명 |
| description | TEXT | | 레슨 설명 |
| lesson_date | DATE | NOT NULL | 레슨 날짜 |
| start_time | TIME | NOT NULL | 시작 시간 |
| end_time | TIME | NOT NULL | 종료 시간 |
| capacity | INTEGER | NOT NULL | 정원 |
| current_count | INTEGER | NOT NULL DEFAULT 0 | 현재 예약 인원 (RESERVED 상태만) |
| riding_center | VARCHAR(200) | | 승마장 (텍스트) |
| status | lesson_status | NOT NULL DEFAULT 'SCHEDULED' | SCHEDULED / CANCELLED |
| version | BIGINT | NOT NULL DEFAULT 0 | 낙관적 락 (동시성 제어) |
| created_by | BIGINT | NOT NULL | 생성한 instructor_group_member.id 참조 |
| created_at | TIMESTAMPTZ | NOT NULL | |
| updated_at | TIMESTAMPTZ | NOT NULL | |
| deleted_at | TIMESTAMPTZ | | soft delete |

> - lesson_date + start_time/end_time은 Season의 start_date~end_date 범위 내
> - 레슨 시간은 1시간 단위 (예: 10:00~12:00 = 2시간 = 2티켓)
> - 같은 시즌 같은 시간대에 여러 레슨 개설 가능
> - current_count는 RESERVED 상태 예약만 카운트 (정원 판단용)
> - version 컬럼으로 동시 예약 시 race condition 방지

---

### 11. lesson_instructor
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PK | |
| lesson_id | BIGINT | NOT NULL | lesson.id 참조 |
| instructor_group_member_id | BIGINT | NOT NULL | instructor_group_member.id 참조 |
| created_at | TIMESTAMPTZ | NOT NULL | |

> - UNIQUE(lesson_id, instructor_group_member_id)
> - 한 레슨에 1명 이상의 지도사 배정 가능

---

### 12. reservation
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PK | 내부용 |
| uuid | UUID | UNIQUE, NOT NULL, DEFAULT gen_random_uuid() | 외부 API용 |
| lesson_id | BIGINT | NOT NULL | lesson.id 참조 |
| member_id | BIGINT | NOT NULL | user.id 참조 |
| season_ticket_account_id | BIGINT | NOT NULL | season_ticket_account.id 참조 |
| ticket_used | INTEGER | NOT NULL | 사용 티켓 수 (레슨 시간에 따라 계산) |
| status | reservation_status | NOT NULL DEFAULT 'RESERVED' | RESERVED / CANCELLED_BY_USER / CANCELLED_BY_INSTRUCTOR |
| cancelled_at | TIMESTAMPTZ | | 취소 시간 |
| created_at | TIMESTAMPTZ | NOT NULL | |
| updated_at | TIMESTAMPTZ | NOT NULL | |
| deleted_at | TIMESTAMPTZ | | soft delete |

> - UNIQUE(lesson_id, member_id) - 동일 레슨 중복 예약 방지
> - 어느 시즌 티켓 계좌로 결제했는지 명시
> - 출석 체크는 lesson_attendance 테이블에서 별도 관리

---

### 13. lesson_attendance
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PK | |
| reservation_id | BIGINT | UNIQUE, NOT NULL | reservation.id 참조 |
| status | attendance_status | NOT NULL | ATTENDED / NO_SHOW |
| checked_by | BIGINT | NOT NULL | 출석 체크한 instructor_group_member.id 참조 |
| checked_at | TIMESTAMPTZ | NOT NULL | 출석 체크 시간 |
| note | TEXT | | 비고 |
| created_at | TIMESTAMPTZ | NOT NULL | |
| updated_at | TIMESTAMPTZ | NOT NULL | |

> - reservation과 1:1 관계
> - 출석 체크 담당 지도사 추적

---

## Indexes

```sql
-- user
CREATE INDEX idx_user_uuid ON "user"(uuid);
CREATE INDEX idx_user_email ON "user"(email);
CREATE INDEX idx_user_deleted_at ON "user"(deleted_at);
CREATE INDEX idx_user_is_system_admin ON "user"(is_system_admin);

-- instructor_group
CREATE INDEX idx_instructor_group_uuid ON instructor_group(uuid);
CREATE INDEX idx_instructor_group_leader_user_id ON instructor_group(leader_user_id);
CREATE INDEX idx_instructor_group_deleted_at ON instructor_group(deleted_at);

-- instructor_group_member
CREATE INDEX idx_instructor_group_member_uuid ON instructor_group_member(uuid);
CREATE INDEX idx_instructor_group_member_group_id ON instructor_group_member(instructor_group_id);
CREATE INDEX idx_instructor_group_member_user_id ON instructor_group_member(user_id);
CREATE INDEX idx_instructor_group_member_deleted_at ON instructor_group_member(deleted_at);

-- token
CREATE INDEX idx_token_token ON token(token);
CREATE INDEX idx_token_user_id ON token(user_id);
CREATE INDEX idx_token_expires_at ON token(expires_at);

-- season
CREATE INDEX idx_season_uuid ON season(uuid);
CREATE INDEX idx_season_instructor_group_id ON season(instructor_group_id);
CREATE INDEX idx_season_created_by ON season(created_by);
CREATE INDEX idx_season_status ON season(status);
CREATE INDEX idx_season_deleted_at ON season(deleted_at);

-- season_enrollment
CREATE INDEX idx_season_enrollment_uuid ON season_enrollment(uuid);
CREATE INDEX idx_season_enrollment_season_id ON season_enrollment(season_id);
CREATE INDEX idx_season_enrollment_member_id ON season_enrollment(member_id);
CREATE INDEX idx_season_enrollment_status ON season_enrollment(status);
-- 부분 UNIQUE 인덱스: PENDING/APPROVED 상태일 때만 중복 신청 방지 (REJECTED/WITHDRAWN은 재신청 가능)
CREATE UNIQUE INDEX idx_season_enrollment_active_unique ON season_enrollment(season_id, member_id)
WHERE status IN ('PENDING', 'APPROVED');

-- season_enrollment_log
CREATE INDEX idx_season_enrollment_log_enrollment_id ON season_enrollment_log(season_enrollment_id);
CREATE INDEX idx_season_enrollment_log_actor_id ON season_enrollment_log(actor_id);
CREATE INDEX idx_season_enrollment_log_type ON season_enrollment_log(type);
CREATE INDEX idx_season_enrollment_log_created_at ON season_enrollment_log(created_at);

-- season_ticket_account
CREATE INDEX idx_season_ticket_account_season_id ON season_ticket_account(season_id);
CREATE INDEX idx_season_ticket_account_member_id ON season_ticket_account(member_id);

-- ticket_log
CREATE INDEX idx_ticket_log_account_id ON ticket_log(season_ticket_account_id);
CREATE INDEX idx_ticket_log_type ON ticket_log(type);
CREATE INDEX idx_ticket_log_reservation_id ON ticket_log(reservation_id);
CREATE INDEX idx_ticket_log_granted_by ON ticket_log(granted_by);
CREATE INDEX idx_ticket_log_created_at ON ticket_log(created_at);

-- lesson
CREATE INDEX idx_lesson_uuid ON lesson(uuid);
CREATE INDEX idx_lesson_season_id ON lesson(season_id);
CREATE INDEX idx_lesson_created_by ON lesson(created_by);
CREATE INDEX idx_lesson_lesson_date ON lesson(lesson_date);
CREATE INDEX idx_lesson_status ON lesson(status);
CREATE INDEX idx_lesson_deleted_at ON lesson(deleted_at);

-- lesson_instructor
CREATE INDEX idx_lesson_instructor_lesson_id ON lesson_instructor(lesson_id);
CREATE INDEX idx_lesson_instructor_instructor_group_member_id ON lesson_instructor(instructor_group_member_id);

-- reservation
CREATE INDEX idx_reservation_uuid ON reservation(uuid);
CREATE INDEX idx_reservation_lesson_id ON reservation(lesson_id);
CREATE INDEX idx_reservation_member_id ON reservation(member_id);
CREATE INDEX idx_reservation_season_ticket_account_id ON reservation(season_ticket_account_id);
CREATE INDEX idx_reservation_status ON reservation(status);
CREATE INDEX idx_reservation_deleted_at ON reservation(deleted_at);

-- lesson_attendance
CREATE INDEX idx_lesson_attendance_reservation_id ON lesson_attendance(reservation_id);
CREATE INDEX idx_lesson_attendance_checked_by ON lesson_attendance(checked_by);
CREATE INDEX idx_lesson_attendance_status ON lesson_attendance(status);
CREATE INDEX idx_lesson_attendance_checked_at ON lesson_attendance(checked_at);
```

---

## Constraints Summary

```sql
-- season
ALTER TABLE season ADD CONSTRAINT chk_season_dates CHECK (end_date >= start_date);
ALTER TABLE season ADD CONSTRAINT chk_season_capacity CHECK (capacity > 0);
ALTER TABLE season ADD CONSTRAINT chk_season_tickets CHECK (default_ticket_count >= 0);

-- lesson
ALTER TABLE lesson ADD CONSTRAINT chk_lesson_times CHECK (end_time > start_time);
ALTER TABLE lesson ADD CONSTRAINT chk_lesson_capacity CHECK (capacity > 0);
ALTER TABLE lesson ADD CONSTRAINT chk_lesson_count CHECK (current_count >= 0 AND current_count <= capacity);

-- season_ticket_account
ALTER TABLE season_ticket_account ADD CONSTRAINT chk_ticket_balance CHECK (balance >= 0);

-- reservation
ALTER TABLE reservation ADD CONSTRAINT chk_reservation_tickets CHECK (ticket_used > 0);
```

---

## Business Rules & Notes

### Season Enrollment Management
- **current_enrollment_count**: 계산으로 처리 (COUNT 쿼리), 실제 컬럼 없음
- 조회 시: `SELECT COUNT(*) FROM season_enrollment WHERE season_id = ? AND status = 'APPROVED'`
- 정원 체크: `current_count < capacity` 확인 후 승인

### Lesson Capacity & Concurrent Booking
- **동시 예약 제어**: `lesson.version` 컬럼으로 낙관적 락 사용
- 예약 생성 시: `UPDATE lesson SET current_count = current_count + 1, version = version + 1 WHERE id = ? AND version = ?`
- version 불일치 시 재시도 또는 에러 응답

### Refund Policy
- **환불 기준**: `lesson.lesson_date` (날짜만, 시간 무시)
- **D-3까지 환불**: `lesson_date - INTERVAL '3 days' >= CURRENT_DATE`
- **D-2부터 환불 불가**: 취소는 가능하나 티켓 환불 안됨

### Lesson Cancellation
- 지도사가 lesson을 CANCELLED로 변경 시:
  1. 해당 레슨의 모든 RESERVED 예약 → CANCELLED_BY_INSTRUCTOR
  2. 모든 예약자에게 티켓 환불 (REFUND 로그 생성)
  3. lesson.current_count → 0

### Soft Delete & Audit Trail
- `created_by`는 삭제된 instructor_group_member를 참조 가능
- 조회 시 deleted_at 확인하여 "탈퇴한 지도사" 표시
- 이력 추적을 위해 참조 무결성 유지

### Group Leader Management
- `instructor_group.leader_user_id`는 user.id 참조
- 그룹장은 반드시 해당 그룹의 active member여야 함 (애플리케이션 레벨 검증)
- 그룹장 탈퇴 시: 새 그룹장 지정 후 탈퇴 가능
