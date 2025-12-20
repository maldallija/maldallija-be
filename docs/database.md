# Database Schema Design

## ERD Overview

```
[사용자 / 승마장 / 초대]

user ──< equestrian_center_invitation >── equestrian_center
                                          │
user ──< equestrian_center_staff >───────┘


[인증]

user ──< authentication_access_session
user ──< authentication_refresh_session


[시즌 / 시즌 참여 / 티켓]

equestrian_center ──< season
                         │
                         ├──< season_enrollment >── user
                         │         │
                         │         └──< season_enrollment_log
                         │
                         └──< season_ticket_account >── user
                                    │
                                    └──< ticket_log


[레슨 / 직원 / 예약 / 출석]

season ──< lesson
           │
           ├──< lesson_instructor >── equestrian_center_staff
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
CREATE TYPE authentication_session_revoked_reason AS ENUM ('NEW_SIGN_IN', 'SIGN_OUT', 'SESSION_REFRESH');
CREATE TYPE invitation_status AS ENUM ('INVITED', 'APPROVED', 'REJECTED', 'EXPIRED', 'WITHDRAWN');
CREATE TYPE member_left_reason AS ENUM ('LEFT_VOLUNTARILY', 'EXPELLED');
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

> - 시스템 관리자(Admin): is_system_admin = true
> - 승마장 직원(Staff): equestrian_center_staff 레코드 존재
> - 시즌 참여자(Member): season_enrollment 레코드 존재
> - 일반 사용자(User): 가입만 한 상태

---

### 2. equestrian_center
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PK | 내부용 |
| uuid | UUID | UNIQUE, NOT NULL, DEFAULT gen_random_uuid() | 외부 API용 |
| name | VARCHAR(200) | NOT NULL | 승마장명 |
| description | TEXT | | 승마장 설명 |
| representative_user_id | BIGINT | NOT NULL | 대표 사용자 user.id 참조 |
| created_by | BIGINT | NOT NULL | 생성자 user.id 참조 |
| created_at | TIMESTAMPTZ | NOT NULL | |
| updated_by | BIGINT | NOT NULL | 최종 수정자 user.id 참조 |
| updated_at | TIMESTAMPTZ | NOT NULL | |
| deleted_at | TIMESTAMPTZ | | soft delete |

> - 센터당 대표 사용자 1명
> - representative_user_id는 user.id를 직접 참조 (순환 참조 방지)
> - 대표는 반드시 해당 센터의 equestrian_center_staff여야 함 (애플리케이션 레벨 검증)
> - created_by, updated_by로 생성자와 최종 수정자 추적 (감사 추적)
> - Post-MVP: 대표는 법적/비즈니스 대표, 기능 권한은 role_id로 분리

---

### 3. equestrian_center_invitation
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PK | 내부용 |
| uuid | UUID | UNIQUE, NOT NULL, DEFAULT gen_random_uuid() | 외부 API용 |
| equestrian_center_id | BIGINT | NOT NULL | equestrian_center.id 참조 |
| user_id | BIGINT | NOT NULL | user.id 참조 (피초대자) |
| invited_by | BIGINT | NOT NULL | user.id 참조 (초대자) |
| status | invitation_status | NOT NULL DEFAULT 'INVITED' | INVITED / APPROVED / REJECTED / EXPIRED / WITHDRAWN |
| invited_at | TIMESTAMPTZ | NOT NULL | 초대 시각 |
| responded_at | TIMESTAMPTZ | | 응답 시각 (승인/거절 시) |
| expires_at | TIMESTAMPTZ | NOT NULL | 만료 시각 (invited_at + 7일) |
| created_at | TIMESTAMPTZ | NOT NULL | |
| updated_at | TIMESTAMPTZ | NOT NULL | |
| updated_by | BIGINT | NOT NULL | 최종 수정자 user.id 참조 |

> - 로그형 테이블 - 같은 사용자에게 여러 초대 레코드 생성 가능 (재초대 이력 보존)
> - 초대 만료: 7일 (배치 없이 조회 시점에 체크)
> - 초대 취소: INVITED → WITHDRAWN (대표만)
> - 재초대 정책:
>   - REJECTED/EXPIRED/WITHDRAWN 후 재초대 가능 (새 레코드 생성)
>   - INVITED 상태가 이미 있으면 재초대 불가 (중복 방지)
> - 승인 시: INVITED → APPROVED, equestrian_center_staff 레코드 생성
> - 거절 시: INVITED → REJECTED

---

### 4. equestrian_center_staff
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PK | 내부용 |
| uuid | UUID | UNIQUE, NOT NULL, DEFAULT gen_random_uuid() | 외부 API용 |
| equestrian_center_id | BIGINT | NOT NULL | equestrian_center.id 참조 |
| user_id | BIGINT | NOT NULL | user.id 참조 |
| joined_at | TIMESTAMPTZ | NOT NULL | 가입 시각 (초대 승인 시각) |
| left_at | TIMESTAMPTZ | | 탈퇴/추방 시각 |
| left_by | BIGINT | | user.id 참조 (추방한 사용자, NULL = 스스로 탈퇴) |
| left_reason | member_left_reason | | LEFT_VOLUNTARILY / EXPELLED |
| created_at | TIMESTAMPTZ | NOT NULL | |
| updated_at | TIMESTAMPTZ | NOT NULL | |
| updated_by | BIGINT | NOT NULL | 최종 수정자 user.id 참조 |
| deleted_at | TIMESTAMPTZ | | soft delete (실수 복구용) |

> - 승마장 직원 (staff) 관리: 강사, 매니저 등 모든 직원
> - 입퇴사 이력 보존: 탈퇴/추방 후 재가입 시 새 레코드 생성
> - 활성 직원: `WHERE left_at IS NULL AND deleted_at IS NULL`
> - UNIQUE INDEX (equestrian_center_id, user_id) WHERE left_at IS NULL AND deleted_at IS NULL
>   - 한 센터에 중복 활성 직원 방지
>   - 탈퇴/추방 후 재가입 가능 (새 레코드)
> - 한 사용자가 여러 센터에 소속 가능 (N:M)
> - MVP: 모든 직원이 동일한 권한 (세분화된 역할/권한은 Post-MVP)
> - Post-MVP: role 컬럼 추가 (INSTRUCTOR, MANAGER, ADMIN)
> - 대표 변경: representative_user_id 업데이트, 이전 대표는 일반 직원으로 유지

---

### 5. authentication_access_session
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PK | 내부용 |
| uuid | UUID | UNIQUE, NOT NULL, DEFAULT gen_random_uuid() | 외부 API용 |
| authentication_access_session | UUID | UNIQUE, NOT NULL, DEFAULT gen_random_uuid() | 액세스 세션 ID (쿠키에 저장) |
| user_id | BIGINT | NOT NULL | user.id 참조 |
| created_at | TIMESTAMPTZ | NOT NULL | 생성 시간 |
| expires_at | TIMESTAMPTZ | NOT NULL | 만료 시간 (1시간) |
| revoked_at | TIMESTAMPTZ | | 무효화 시간 |
| revoked_reason | authentication_session_revoked_reason | | 무효화 이유 (NEW_SIGN_IN, SIGN_OUT, SESSION_REFRESH) |

> - 짧은 만료 시간 (1시간) - 보안 강화
> - 새 로그인 시 기존 세션 무효화 (revoked_at 설정)
> - HttpOnly 쿠키로 전송

---

### 6. authentication_refresh_session
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PK | 내부용 |
| uuid | UUID | UNIQUE, NOT NULL, DEFAULT gen_random_uuid() | 외부 API용 |
| authentication_refresh_session | UUID | UNIQUE, NOT NULL, DEFAULT gen_random_uuid() | 리프레시 세션 ID (쿠키에 저장) |
| user_id | BIGINT | NOT NULL | user.id 참조 |
| created_at | TIMESTAMPTZ | NOT NULL | 생성 시간 |
| expires_at | TIMESTAMPTZ | NOT NULL | 만료 시간 (30일) |
| revoked_at | TIMESTAMPTZ | | 무효화 시간 |
| revoked_reason | authentication_session_revoked_reason | | 무효화 이유 (NEW_SIGN_IN, SIGN_OUT, SESSION_REFRESH) |

> - 긴 만료 시간 (30일) - UX 개선 (remember-me)
> - 세션 갱신 시 회전 (rotating refresh token) - 기존 세션 무효화
> - 단일 디바이스 정책 (새 로그인 시 모든 세션 무효화)
> - HttpOnly 쿠키로 전송

---

### 7. season
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PK | 내부용 |
| uuid | UUID | UNIQUE, NOT NULL, DEFAULT gen_random_uuid() | 외부 API용 |
| equestrian_center_id | BIGINT | NOT NULL | equestrian_center.id 참조 |
| title | VARCHAR(200) | NOT NULL | 시즌명 |
| description | TEXT | | 시즌 설명 |
| start_date | DATE | NOT NULL | 시작일 |
| end_date | DATE | NOT NULL | 종료일 |
| capacity | INTEGER | NOT NULL | 시즌 정원 |
| default_ticket_count | INTEGER | NOT NULL | 참여 승인 시 기본 부여 티켓 수 |
| status | season_status | NOT NULL DEFAULT 'ACTIVE' | ACTIVE / CLOSED |
| created_by | BIGINT | NOT NULL | 생성한 equestrian_center_staff.id 참조 |
| created_at | TIMESTAMPTZ | NOT NULL | |
| updated_at | TIMESTAMPTZ | NOT NULL | |
| deleted_at | TIMESTAMPTZ | | soft delete |

> 시즌은 센터 단위로 생성

---

### 8. season_enrollment
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

### 9. season_enrollment_log
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

### 10. season_ticket_account
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

### 11. ticket_log
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PK | |
| season_ticket_account_id | BIGINT | NOT NULL | season_ticket_account.id 참조 |
| amount | INTEGER | NOT NULL | 변동량 (+/-) |
| type | ticket_log_type | NOT NULL | GRANT / USE / REFUND / ADDITIONAL |
| description | VARCHAR(500) | | 비고 |
| reservation_id | BIGINT | | 관련 reservation.id (USE/REFUND 타입만 해당) |
| granted_by | BIGINT | | 티켓 부여한 equestrian_center_staff.id (GRANT/ADDITIONAL만 해당) |
| created_at | TIMESTAMPTZ | NOT NULL | |

> - GRANT: 참여 승인 시 기본 티켓 부여
> - USE: 레슨 예약 시 차감
> - REFUND: 예약 취소 시 환불 (레슨 날짜 기준 D-3까지 환불, D-2부터 환불 불가)
> - ADDITIONAL: 지도사가 추가 부여

---

### 12. lesson
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
| created_by | BIGINT | NOT NULL | 생성한 equestrian_center_staff.id 참조 |
| created_at | TIMESTAMPTZ | NOT NULL | |
| updated_at | TIMESTAMPTZ | NOT NULL | |
| deleted_at | TIMESTAMPTZ | | soft delete |

> - lesson_date + start_time/end_time은 Season의 start_date~end_date 범위 내
> - 레슨 시간은 1시간 단위 (예: 10:00~12:00 = 2시간 = 2티켓)
> - 같은 시즌 같은 시간대에 여러 레슨 개설 가능
> - current_count는 RESERVED 상태 예약만 카운트 (정원 판단용)
> - version 컬럼으로 동시 예약 시 race condition 방지

---

### 13. lesson_instructor
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PK | |
| lesson_id | BIGINT | NOT NULL | lesson.id 참조 |
| staff_id | BIGINT | NOT NULL | equestrian_center_staff.id 참조 |
| created_at | TIMESTAMPTZ | NOT NULL | |

> - UNIQUE(lesson_id, staff_id)
> - 한 레슨에 1명 이상의 직원(강사) 배정 가능

---

### 14. reservation
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

### 15. lesson_attendance
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PK | |
| reservation_id | BIGINT | UNIQUE, NOT NULL | reservation.id 참조 |
| status | attendance_status | NOT NULL | ATTENDED / NO_SHOW |
| checked_by | BIGINT | NOT NULL | 출석 체크한 equestrian_center_staff.id 참조 |
| checked_at | TIMESTAMPTZ | NOT NULL | 출석 체크 시간 |
| note | TEXT | | 비고 |
| created_at | TIMESTAMPTZ | NOT NULL | |
| updated_at | TIMESTAMPTZ | NOT NULL | |

> - reservation과 1:1 관계
> - 출석 체크 담당 직원 추적

---

## Indexes

```sql
-- user
CREATE INDEX idx_user_uuid ON "user"(uuid);
CREATE INDEX idx_user_email ON "user"(email);
CREATE INDEX idx_user_deleted_at ON "user"(deleted_at);
CREATE INDEX idx_user_is_system_admin ON "user"(is_system_admin);

-- equestrian_center
CREATE INDEX idx_equestrian_center_uuid ON equestrian_center(uuid);
CREATE INDEX idx_equestrian_center_representative_user_id ON equestrian_center(representative_user_id);
CREATE INDEX idx_equestrian_center_created_by ON equestrian_center(created_by);
CREATE INDEX idx_equestrian_center_updated_by ON equestrian_center(updated_by);
CREATE INDEX idx_equestrian_center_deleted_at ON equestrian_center(deleted_at);

-- equestrian_center_invitation
CREATE INDEX idx_invitation_uuid ON equestrian_center_invitation(uuid);
CREATE INDEX idx_invitation_center_id ON equestrian_center_invitation(equestrian_center_id);
CREATE INDEX idx_invitation_user_id ON equestrian_center_invitation(user_id);
CREATE INDEX idx_invitation_invited_by ON equestrian_center_invitation(invited_by);
CREATE INDEX idx_invitation_status ON equestrian_center_invitation(status);
CREATE INDEX idx_invitation_user_center_status ON equestrian_center_invitation(user_id, equestrian_center_id, status);
CREATE INDEX idx_invitation_expires_at ON equestrian_center_invitation(expires_at) WHERE status = 'INVITED';
CREATE INDEX idx_invitation_updated_by ON equestrian_center_invitation(updated_by);

-- equestrian_center_staff
CREATE INDEX idx_staff_uuid ON equestrian_center_staff(uuid);
CREATE INDEX idx_staff_center_id ON equestrian_center_staff(equestrian_center_id);
CREATE INDEX idx_staff_user_id ON equestrian_center_staff(user_id);
CREATE INDEX idx_staff_left_at ON equestrian_center_staff(left_at);
CREATE INDEX idx_staff_left_by ON equestrian_center_staff(left_by);
CREATE INDEX idx_staff_updated_by ON equestrian_center_staff(updated_by);
CREATE INDEX idx_staff_deleted_at ON equestrian_center_staff(deleted_at);
-- 부분 UNIQUE 인덱스: 활성 멤버 중복 방지 (탈퇴 후 재가입 가능)
CREATE UNIQUE INDEX idx_staff_active_unique ON equestrian_center_staff(equestrian_center_id, user_id)
WHERE left_at IS NULL AND deleted_at IS NULL;

-- authentication_access_session
CREATE INDEX idx_authentication_access_session_uuid ON authentication_access_session(uuid);
CREATE INDEX idx_authentication_access_session_session ON authentication_access_session(authentication_access_session);
CREATE INDEX idx_authentication_access_session_user_id ON authentication_access_session(user_id);
CREATE INDEX idx_authentication_access_session_expires_at ON authentication_access_session(expires_at);
CREATE INDEX idx_authentication_access_session_revoked_at ON authentication_access_session(revoked_at);

-- authentication_refresh_session
CREATE INDEX idx_authentication_refresh_session_uuid ON authentication_refresh_session(uuid);
CREATE INDEX idx_authentication_refresh_session_session ON authentication_refresh_session(authentication_refresh_session);
CREATE INDEX idx_authentication_refresh_session_user_id ON authentication_refresh_session(user_id);
CREATE INDEX idx_authentication_refresh_session_expires_at ON authentication_refresh_session(expires_at);
CREATE INDEX idx_authentication_refresh_session_revoked_at ON authentication_refresh_session(revoked_at);

-- season
CREATE INDEX idx_season_uuid ON season(uuid);
CREATE INDEX idx_season_equestrian_center_id ON season(equestrian_center_id);
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
CREATE INDEX idx_lesson_instructor_staff_id ON lesson_instructor(staff_id);

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

### Center Leader Management
- `equestrian_center.leader_user_id`는 user.id 참조
- 센터장은 반드시 해당 센터의 active member여야 함 (애플리케이션 레벨 검증)
- 센터장 탈퇴 시: 새 센터장 지정 후 탈퇴 가능
