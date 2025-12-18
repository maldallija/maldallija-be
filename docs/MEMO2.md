# 역할별 기능 정의 (Naming Convention)

## 명명 규칙
- **Admin (관리자)**: 서비스 운영자
- **Representative (대표)**: 승마장 대표 (법적/비즈니스 소유자)
- **Staff (직원)**: 승마장 직원 (강사, 매니저 등)
- **Member (수강생)**: 시즌 참여자
- **User (사용자)**: 일반 가입자

## 시스템 관리자 (Admin)
- `user.is_system_admin = true` 플래그로 구분
- 승마장 생성
- 대표 사용자 지정
- 전체 사용자 조회
- 전체 승마장/시즌/레슨/예약 조회
- (기타 시스템 레벨 기능 TBD)

## 승마장 대표 (Representative)
- `equestrian_center.representative_user_id`가 자신의 `user.id`인 경우
- 승마장 정보 수정
- 직원 초대/추방
- 직원 목록 공개 여부 설정 (Post-MVP)
- MVP: 일반 직원과 동일한 권한 (역할/권한 구분 없음)
- Post-MVP: 대표는 법적 소유자, 기능 권한은 role로 분리

## 승마장 직원 (Staff)
- `equestrian_center_staff`에 레코드가 있는 사용자
- MVP: 승마장 내 모든 직원이 동일한 권한 (역할 구분 없음, instructor만)
- Post-MVP: INSTRUCTOR, MANAGER, ADMIN 역할 분리
- 주요 기능:
  - 시즌 생성/수정/종료 (시작일~종료일, 정원, 기본 티켓 수 설정) - 센터 단위
  - 시즌 참여 신청 승인/거절
  - 기본 티켓 부여, 추가 티켓 부여
  - 레슨 개설/수정/취소 (날짜, 시간, 정원, 승마장, 담당 직원 지정)
    - 레슨 시간: 1시간 단위 (2시간 레슨 = 2티켓)
    - 같은 시즌 같은 시간대에 여러 레슨 개설 가능
    - 한 레슨에 1명 이상의 직원(강사) 배정 가능
  - 레슨 취소 시 예약자 전원 티켓 환불
  - 출석 체크 (출석/노쇼 처리, lesson_attendance 기록)
- 본인이 속한 센터의 레슨 예약자 목록 조회
- 한 사용자가 여러 센터에 직원으로 소속 가능

## 시즌 참여자 (Member, 수강생)
- `season_enrollment`에 APPROVED 레코드가 있는 사용자
- 시즌 참여 신청
- 승인된 시즌의 레슨 목록 조회 (모든 직원 레슨)
- 레슨 예약 (시즌 티켓 계좌에서 차감, 레슨 시간에 따라 계산)
- 예약 취소
  - 레슨 3일 전까지: 티켓 환불
  - 레슨 2일 전부터: 환불 불가
- 본인 예약 내역 조회
- 본인 티켓 잔액/이력 조회 (시즌별, season_ticket_account 기준)

## 일반 사용자 (User)
- 가입만 하고 승마장 직원도 아니고 시즌 참여자도 아닌 상태
- 회원가입/로그인
- 개설된 시즌 목록 조회

---

# 개발 순서

## Phase 1: 인증/인가 ✅ COMPLETED
1. **User** - 회원가입 (`is_system_admin` 플래그 포함) ✅ COMPLETED
2. ~~**Token** - Opaque 토큰 발급/검증/삭제, 로그인/로그아웃~~ ✅ IMPLEMENTED
   - **AuthenticationAccessSession** (1시간) + **AuthenticationRefreshSession** (30일)
   - 로그인/로그아웃/세션갱신 구현
   - Rotating refresh token pattern (SESSION_REFRESH 시 기존 세션 무효화)
   - 단일 디바이스 정책 (NEW_SIGN_IN 시 모든 세션 무효화)
   - HttpOnly 쿠키 전송, AuthenticationFilter 구현
   - 세션 무효화 추적: revoked_at, revoked_reason (NEW_SIGN_IN/SIGN_OUT/SESSION_REFRESH)

## Phase 2: 승마장 (MVP)

### Phase 2A: EquestrianCenter CRUD ✅ COMPLETED
3. **EquestrianCenter** - 승마장 CRUD, 대표 사용자 지정
   - ✅ CREATE: POST /api/v1/administration/equestrian-centers (System Admin 전용)
   - ✅ READ List: GET /api/v1/equestrian-centers (공개, 페이지네이션)
   - ✅ READ Detail: GET /api/v1/equestrian-centers/{uuid} (공개)
   - ✅ UPDATE: PATCH /api/v1/equestrian-centers/{uuid} (대표 사용자 전용, name/description만)
   - ❌ DELETE: 보류 (soft delete via deleted_at)
   - Renamed: leader → representative (11 files)

### Phase 2B: Invitation System ❌ NOT IMPLEMENTED
4. **EquestrianCenterInvitation** - 초대 시스템 (로그형 테이블)
   - POST /api/v1/equestrian-centers/{centerUuid}/invitations (초대 발송, 대표 전용)
   - GET /api/v1/equestrian-centers/{centerUuid}/invitations (발송한 초대 목록)
   - DELETE /api/v1/equestrian-centers/{centerUuid}/invitations/{invitationUuid} (초대 취소)
   - GET /api/v1/my/equestrian-center-invitations (받은 초대 목록)
   - POST /api/v1/my/equestrian-center-invitations/{invitationUuid}/approve (승인)
   - POST /api/v1/my/equestrian-center-invitations/{invitationUuid}/reject (거절)
   - Status: INVITED → APPROVED/REJECTED/EXPIRED/WITHDRAWN
   - 7일 만료 (조회 시점 체크, 배치 없음)
   - 재초대 정책: REJECTED/EXPIRED/WITHDRAWN 후 가능, INVITED 중복 불가

### Phase 2C: Staff Management ❌ NOT IMPLEMENTED
5. **EquestrianCenterStaff** - 직원 관리 (입퇴사 이력 추적)
   - GET /api/v1/equestrian-centers/{centerUuid}/staff (직원 목록, 공개 여부는 대표 설정)
   - DELETE /api/v1/equestrian-centers/{centerUuid}/staff/{staffUuid} (추방, 대표 전용)
   - DELETE /api/v1/equestrian-centers/{centerUuid}/staff/me (스스로 탈퇴)
   - GET /api/v1/my/equestrian-center-staff-memberships (직원으로 속한 승마장 목록)
   - joined_at, left_at, left_by, left_reason 추적
   - Leave reasons: LEFT_VOLUNTARILY, EXPELLED
   - 재가입 시 새 레코드 생성 (입퇴사 이력 보존)

6. MVP: 모든 센터 직원이 동일한 권한 (역할/권한 시스템은 Post-MVP)
   - Post-MVP: role 컬럼 추가 (INSTRUCTOR, MANAGER, ADMIN)

## Phase 3: 시즌 및 참여
7. **Season** - CRUD, 상태 관리, 정원 관리 (센터 단위, created_by는 equestrian_center_staff.id)
8. **SeasonEnrollment** - 참여 신청, 승인/거절, 탈퇴
9. **SeasonEnrollmentLog** - 참여 상태 변화 이력 (APPLIED/REAPPLIED/APPROVED/REJECTED/WITHDRAWN)

## Phase 4: 티켓
10. **SeasonTicketAccount** - 시즌별 회원 티켓 계좌 (APPROVED 시 생성)
11. **TicketLog** - 이력 관리 (GRANT/USE/REFUND/ADDITIONAL), granted_by는 equestrian_center_staff.id

## Phase 5: 레슨
12. **Lesson** - CRUD, 상태 관리, 시간 검증 (created_by는 equestrian_center_staff.id)
13. **LessonInstructor** - 레슨-지도사 배정 (N:M, equestrian_center_staff.id 참조)

## Phase 6: 예약 및 출석
14. **Reservation** - 예약/취소, 티켓 차감/환불 (season_ticket_account_id 참조)
15. **LessonAttendance** - 출석 체크 (checked_by는 equestrian_center_staff.id, checked_at 기록)

## Phase 7: 관리자 & Post-MVP
16. **Admin 기능** - 시스템 관리자 UI, 승마장 관리, 전체 조회 (TBD)
17. **권한 시스템** - 역할 기반 권한 관리 (InstructorGroupRole, InstructorGroupPermission, InstructorGroupRolePermission)
