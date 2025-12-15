# 역할별 기능 정의

## 시스템 관리자 (System Admin)
- `user.is_system_admin = true` 플래그로 구분
- 그룹(학원) 생성
- 지도사를 그룹에 초대
- 그룹장 지정
- 전체 사용자 조회
- 전체 그룹/시즌/레슨/예약 조회
- (기타 시스템 레벨 기능 TBD)

## 그룹장 (Group Leader)
- `instructor_group.leader_user_id`가 자신의 `user.id`인 경우
- 그룹 내 지도사 초대/추방
- 그룹 정보 수정
- MVP: 일반 지도사와 동일한 권한 (역할/권한 구분 없음)

## 지도사 (Instructor)
- `instructor_group_member`에 레코드가 있는 사용자
- MVP: 그룹 내 모든 지도사가 동일한 권한 (역할 구분 없음)
- 주요 기능:
  - 시즌 생성/수정/종료 (시작일~종료일, 정원, 기본 티켓 수 설정) - 그룹 단위
  - 시즌 참여 신청 승인/거절
  - 기본 티켓 부여, 추가 티켓 부여
  - 레슨 개설/수정/취소 (날짜, 시간, 정원, 승마장, 담당 지도사 지정)
    - 레슨 시간: 1시간 단위 (2시간 레슨 = 2티켓)
    - 같은 시즌 같은 시간대에 여러 레슨 개설 가능
    - 한 레슨에 1명 이상의 지도사 배정 가능
  - 레슨 취소 시 예약자 전원 티켓 환불
  - 출석 체크 (출석/노쇼 처리, lesson_attendance 기록)
- 본인이 속한 그룹의 레슨 예약자 목록 조회
- 한 사용자가 여러 그룹에 소속 가능

## 일반 회원 (Member)
- 그룹 멤버십이 없는 일반 사용자
- 회원가입/로그인
- 개설된 시즌 목록 조회
- 시즌 참여 신청
- 승인된 시즌의 레슨 목록 조회 (모든 지도사 레슨)
- 레슨 예약 (시즌 티켓 계좌에서 차감, 레슨 시간에 따라 계산)
- 예약 취소
  - 레슨 3일 전까지: 티켓 환불
  - 레슨 2일 전부터: 환불 불가
- 본인 예약 내역 조회
- 본인 티켓 잔액/이력 조회 (시즌별, season_ticket_account 기준)

---

# 개발 순서

## Phase 1: 인증/인가 ✅ COMPLETED
1. **User** - 회원가입 (`is_system_admin` 플래그 포함) ⚠️ NEEDS UPDATE (still has old `role` field)
2. ~~**Token** - Opaque 토큰 발급/검증/삭제, 로그인/로그아웃~~ ✅ IMPLEMENTED
   - **AuthenticationAccessSession** (1시간) + **AuthenticationRefreshSession** (30일)
   - 로그인/로그아웃/세션갱신 구현
   - Rotating refresh token pattern (SESSION_REFRESH 시 기존 세션 무효화)
   - 단일 디바이스 정책 (NEW_SIGN_IN 시 모든 세션 무효화)
   - HttpOnly 쿠키 전송, AuthenticationFilter 구현
   - 세션 무효화 추적: revoked_at, revoked_reason (NEW_SIGN_IN/SIGN_OUT/SESSION_REFRESH)

## Phase 2: 그룹 (MVP)
3. **InstructorGroup** - 그룹 CRUD, 그룹장 지정
4. **InstructorGroupMember** - 사용자-그룹 N:M 관계
5. MVP: 모든 그룹 멤버가 동일한 권한 (역할/권한 시스템은 Post-MVP)

## Phase 3: 시즌 및 참여
6. **Season** - CRUD, 상태 관리, 정원 관리 (그룹 단위, created_by는 instructor_group_member.id)
7. **SeasonEnrollment** - 참여 신청, 승인/거절, 탈퇴
8. **SeasonEnrollmentLog** - 참여 상태 변화 이력 (APPLIED/REAPPLIED/APPROVED/REJECTED/WITHDRAWN)

## Phase 4: 티켓
9. **SeasonTicketAccount** - 시즌별 회원 티켓 계좌 (APPROVED 시 생성)
10. **TicketLog** - 이력 관리 (GRANT/USE/REFUND/ADDITIONAL), granted_by는 instructor_group_member.id

## Phase 5: 레슨
11. **Lesson** - CRUD, 상태 관리, 시간 검증 (created_by는 instructor_group_member.id)
12. **LessonInstructor** - 레슨-지도사 배정 (N:M, instructor_group_member.id 참조)

## Phase 6: 예약 및 출석
13. **Reservation** - 예약/취소, 티켓 차감/환불 (season_ticket_account_id 참조)
14. **LessonAttendance** - 출석 체크 (checked_by는 instructor_group_member.id, checked_at 기록)

## Phase 7: 관리자 & Post-MVP
15. **Admin 기능** - 시스템 관리자 UI, 그룹 관리, 전체 조회 (TBD)
16. **권한 시스템** - 역할 기반 권한 관리 (InstructorGroupRole, InstructorGroupPermission, InstructorGroupRolePermission)
