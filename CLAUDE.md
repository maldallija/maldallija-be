# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Dageudak (다그닥)** - Horse riding reservation server.

Spring Boot 3.5.8 application written in Kotlin, using Gradle (Kotlin DSL) as the build tool. Uses JPA with PostgreSQL.

## Build Commands

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "dev.maldallija.maldallijabe.MaldallijaBeApplicationTests"

# Run a single test method
./gradlew test --tests "dev.maldallija.maldallijabe.MaldallijaBeApplicationTests.contextLoads"

# Clean build
./gradlew clean build
```

## Tech Stack

- **Language**: Kotlin 1.9.25, Java 21
- **Framework**: Spring Boot 3.5.8
- **Build**: Gradle with Kotlin DSL
- **Database**: PostgreSQL with Spring Data JPA
- **Testing**: JUnit 5

## Project Structure

**Modular Monolith Architecture** - Separated into independent modules for MSA migration readiness.

### Modules
- `user/` - User domain module (complete independence, no dependencies)
  - Domain: User, UserException hierarchy
  - Repository: UserRepository interface and implementation
  - Soft delete policy applied to all queries
- `src/` - Main application module (depends on user)
  - All other domains: auth, equestriancenter, season, etc.
  - Will be further separated in future phases

### Directory Structure
- `user/src/main/kotlin/dev/maldallija/maldallijabe/user/` - User module code
- `src/main/kotlin/dev/maldallija/maldallijabe/` - Main application code
- `src/test/kotlin/dev/maldallija/maldallijabe/` - Test code
- `src/main/resources/` - Configuration files and static resources

## JPA Configuration

The project uses `allOpen` plugin for JPA entities - classes annotated with `@Entity`, `@MappedSuperclass`, or `@Embeddable` are automatically open for proxying.

### Entity Immutability Policy

**All Entity fields must be immutable (`val`)**:
- NO JPA dirty checking - explicit save required for updates
- Update pattern: Create new Entity instance with modified fields, then save
- Benefits:
  - Prevents accidental `updatedAt`/`updatedBy` omission
  - Clearer update semantics
  - Functional programming style
- Example:
  ```kotlin
  @Entity
  class UserEntity(
      @Id val id: Long,
      val username: String,
      val updatedAt: Instant,  // All fields are val
  )

  // Update: create new instance
  val updated = entity.copy(
      username = newUsername,
      updatedAt = Instant.now(),
  )
  repository.save(updated)
  ```

## Domain Model

### Naming Convention (명명 체계)
- **Admin**: 서비스 운영자 (System Admin, `is_system_admin = true`)
- **Representative**: 승마장 대표 (`equestrian_center.representative_user_id`)
- **Staff**: 승마장 직원 (`equestrian_center_staff` - 강사, 매니저 등)
- **Member**: 시즌 참여자 (`season_enrollment` - 수강생)
- **User**: 일반 사용자 (가입만 한 상태)

### User Roles
- **System Admin (관리자)**: Has `is_system_admin = true`, manages system-level operations (equestrian center creation, etc.)
- **Center Staff (직원)**: User belongs to EquestrianCenter(s) via equestrian_center_staff
  - **Center Representative (대표)**: Designated representative (equestrian_center.representative_user_id), legal/business owner
    - Invites users to center, manages staff expulsion
    - Can configure staff list visibility (public vs staff-only) - Post-MVP
    - Post-MVP: Representative is separate from functional permissions (role-based)
  - **Staff (직원)**: Creates seasons/lessons, manages enrollments, checks attendance
    - MVP: Instructor role only
    - Post-MVP: INSTRUCTOR, MANAGER, ADMIN roles
  - MVP: All center staff have equal permissions (no role-based restrictions)
- **Season Member (수강생)**: User who applied and approved to season, books lessons
- **General User**: Registered user who hasn't joined any center or season

### Core Entities
- **User**: System account
  - `is_system_admin`: boolean flag for system administrators
  - Can belong to multiple EquestrianCenters
- **EquestrianCenter**: Equestrian center (riding academy/club)
  - Created by System Admin
  - Has 1 representative user (center representative)
  - Tracks creator and last updater: `created_by`, `updated_by`
  - MVP: All center members have equal instructor permissions
- **EquestrianCenterInvitation**: Invitation from representative to user (log-style table)
  - Status: INVITED → APPROVED / REJECTED / EXPIRED / WITHDRAWN
  - Invited by representative, responded by invitee
  - Expires in 7 days (checked at query time, no batch)
  - Multiple invitation records possible per user (history preserved)
  - Re-invitation allowed after REJECTED/EXPIRED/WITHDRAWN
  - Cannot re-invite if INVITED status already exists
- **EquestrianCenterStaff**: N:M relationship between User and EquestrianCenter (승마장 직원)
  - Links user to center (active staff membership)
  - Created when invitation APPROVED
  - Tracks join/leave history: `joined_at`, `left_at`, `left_by`, `left_reason`
  - Leave reasons: LEFT_VOLUNTARILY (self), EXPELLED (by representative)
  - New record created on re-join after leave (preserves employment history)
  - One user can belong to multiple centers as staff
  - MVP: All staff have equal permissions (instructor only)
  - Post-MVP: role column (INSTRUCTOR, MANAGER, ADMIN)
- **Season**: Period (start~end date) created at center level
  - `capacity`: season enrollment limit
  - `default_ticket_count`: tickets granted upon enrollment approval
  - `created_by`: tracks which center staff created the season
- **SeasonEnrollment**: Member (수강생) applies to Season, Staff approves/rejects
  - Status: PENDING → APPROVED / REJECTED / WITHDRAWN
  - Upon approval, Member receives default tickets for that Season
- **SeasonEnrollmentLog**: History of enrollment status changes
  - Tracks APPLIED, REAPPLIED, APPROVED, REJECTED, WITHDRAWN events
  - Records actor (who performed the action) and notes
- **SeasonTicketAccount**: Virtual currency account per (Season, Member)
  - Balance tracked separately for each season
  - Created when enrollment is APPROVED
- **TicketLog**: Transaction history (GRANT/USE/REFUND/ADDITIONAL)
  - `granted_by`: tracks which center staff granted tickets
  - Links to season_ticket_account instead of season+member
- **Lesson**: Class within Season
  - Staff sets: date, time (1-hour unit), capacity, riding location (text)
  - Duration determines ticket cost (e.g., 2-hour lesson = 2 tickets)
  - Multiple Lessons allowed at same time slot within a Season
  - Lesson datetime must be within Season period
  - `created_by`: tracks which center staff created the lesson
- **LessonInstructor**: N:M relationship between Lesson and EquestrianCenterStaff
  - 1+ staff (instructors) can be assigned to a lesson
  - References EquestrianCenterStaff (not User directly)
- **Reservation**: Approved Member books Lesson using Tickets
  - Cancel before D-3: Ticket refunded
  - Cancel from D-2: No refund
  - Links to season_ticket_account for payment tracking
- **LessonAttendance**: Attendance tracking
  - Status: ATTENDED / NO_SHOW
  - `checked_by`: tracks which center staff checked attendance
  - `checked_at`: timestamp of attendance check

### Business Rules
- Member (수강생) must have APPROVED enrollment to book Lessons in that Season
- Member can book multiple Lessons simultaneously
- Staff can only manage seasons/lessons within their center(s)
- Approved member can book any lesson in the season (regardless of which staff created it)
- MVP: All center staff have equal permissions (no role-based restrictions)
- **Center creation & staff management**:
  - System Admin creates center with representative designation
  - Representative auto-added to equestrian_center_staff on center creation
  - Representative invites users → Users approve/reject within 7 days
  - Re-invitation allowed after rejection/expiration/withdrawal
  - Cannot re-invite while INVITED status exists (prevent spam)
  - Leave/expulsion supported, re-join creates new staff record
  - Representative change: updates representative_user_id, previous representative remains as staff
- No waitlist, no horse assignment, no level system (see docs/ROADMAP.md for future ideas)

### Capacity & Concurrency
- **Season enrollment count**: Calculated via COUNT query (no actual column)
- **Lesson booking**: Uses optimistic locking (version column) to prevent race conditions
- **Concurrent reservations**: Version mismatch triggers retry or error

### Refund Policy
- **Cancellation deadline**: Based on lesson_date (date only, time ignored)
- **D-3 or earlier**: Full refund
- **D-2 or later**: No refund (cancellation allowed but tickets lost)
- **Example**: Lesson on Jan 10 → Cancel by Jan 7 for refund

### Lesson Cancellation by Staff
- When staff cancels lesson (SCHEDULED → CANCELLED):
  1. All RESERVED reservations → CANCELLED_BY_INSTRUCTOR (status name kept for compatibility)
  2. All affected members receive full ticket refund
  3. lesson.current_count reset to 0

### Re-enrollment Rules
- **REJECTED**: Can reapply immediately
- **WITHDRAWN**: Can reapply immediately
- **PENDING/APPROVED**: Cannot apply again (partial UNIQUE index enforces)

### Authentication
- Opaque Token (DB-stored, supports duplicate login prevention)
- New login invalidates existing session (single device only)
- Email/Password registration
- System Admin flag set manually (TBD: Admin UI)

### Status Management
- **Lesson**: SCHEDULED / CANCELLED
- **Reservation**: RESERVED / CANCELLED_BY_USER / CANCELLED_BY_INSTRUCTOR
- **Season**: ACTIVE / CLOSED
- **SeasonEnrollment**: PENDING / APPROVED / REJECTED / WITHDRAWN
- **LessonAttendance**: ATTENDED / NO_SHOW
- Instructor marks attendance via LessonAttendance entity (separate from Reservation)
- No penalty for NO_SHOW

## Database Conventions

- Table names: singular (user, season, lesson, etc.)
- ID strategy: `id` (BIGSERIAL) for internal use, `uuid` (UUID) for external API exposure
- Enum types: Use PostgreSQL native ENUM (CREATE TYPE)
- Soft delete: Use `deleted_at` column instead of actual deletion
- No FK constraints: For future MSA migration flexibility
- Timestamp: Use TIMESTAMPTZ (UTC storage) for international expansion

## Architecture

- Hexagonal Architecture (Ports & Adapters)
- Reference: "Clean Architecture" by Robert C. Martin

### Package Structure
```
dev.maldallija.maldallijabe
├── user
│   ├── adapter
│   │   ├── in/web             # REST 컨트롤러
│   │   └── out/persistence    # JPA Repository 구현
│   ├── application
│   │   ├── port/in            # 입력 포트 (유스케이스 인터페이스)
│   │   ├── port/out           # 출력 포트 (영속성 인터페이스)
│   │   └── service            # 유스케이스 구현
│   └── domain                 # 도메인 모델
├── auth                       # Authentication domain
├── administration             # Administration endpoints (system admin only)
├── equestriancenter           # Equestrian center domain
│   ├── invitation             # EquestrianCenterInvitation subdomain
│   └── staff                  # EquestrianCenterStaff subdomain
├── season
│   ├── enrollment             # SeasonEnrollment subdomain
│   ├── enrollmentlog          # SeasonEnrollmentLog subdomain
│   └── ticketaccount          # SeasonTicketAccount subdomain
├── ticketlog
├── lesson
│   ├── instructor             # LessonInstructor subdomain (staff assignment)
│   └── attendance             # LessonAttendance subdomain
└── reservation
```

## Testing

- Unit test + Integration test
- Given-When-Then pattern
- API docs: Swagger (springdoc-openapi)

## Working Guidelines

- Act as expert DBA and Kotlin Spring backend developer
- Be concise - no unnecessary words or phrases
- Ask clarifying questions when requirements are ambiguous before proceeding
- Log all significant actions to this file in English

### Coding Standards

#### Naming Conventions
- **ALWAYS use full names** - NO abbreviations or shortened forms
  - `RefreshAuthenticationSessionUseCase` `RefreshSessionUseCase`
  - `authenticationAccessSession` `session` or `accessSession`
  - `authenticationRefreshSessionId` `refreshId` or `sessionId`
- Variables, methods, classes, parameters - all must use complete descriptive names
- Only exception: Standard loop counters (i, j) in rare cases where context is obvious

#### Constants
- Use enums instead of string literals for type safety
- Place constants in appropriate layer (adapter for HTTP details, domain for business concepts)

#### Formatting
- Consistent multi-line parameter calls for readability
- Example:
  ```kotlin
  repository.revokeAllByUserId(
      userId = userId,
      reason = AuthenticationSessionRevokedReason.SIGN_OUT,
  )
  ```

#### Exception Hierarchy
- **2-tier structure**: `RuntimeException` → Domain Exception (sealed)
- Each module defines its own exception hierarchy (complete independence)
- Domain Exceptions: Sealed classes for exhaustive type checking
  - `UserException`, `AuthException`, `EquestrianCenterException`, `EquestrianCenterInvitationException`
- Benefits:
  - Compile-time exhaustive checking with sealed classes
  - Complete module independence (no shared base class)
  - Easy MSA migration (no common dependency)
  - Type-safe error handling in when expressions
- Example:
  ```kotlin
  // Each module defines its own exception independently
  sealed class UserException(
      val errorCode: String,
      message: String,
  ) : RuntimeException(message)

  sealed class AuthException(
      val errorCode: String,
      message: String,
  ) : RuntimeException(message)
  ```
- Note: 3-line duplication < module independence (MSA-ready)

## Related Documents

- `docs/database.md` - DB schema design (15 tables: user, equestrian_center, equestrian_center_invitation, equestrian_center_staff, authentication_access_session, authentication_refresh_session, season, season_enrollment, season_enrollment_log, season_ticket_account, ticket_log, lesson, lesson_instructor, reservation, lesson_attendance)
  - Note: Originally specified "instructor_group" table, renamed to "equestrian_center" for clarity
  - Note: Originally specified "leader_user_id", renamed to "representative_user_id" for clarity
  - Note: Originally specified "instructor_group_member", renamed to "equestrian_center_staff" (staff = 강사, 매니저 등 모든 직원)
  - Note: Originally specified "token" table (1 day expiry), implemented as dual-session system (access 1h + refresh 30d)
  - Note: Added equestrian_center_invitation table for invitation system (log-style)
- `docs/ROADMAP.md` - Role-based feature definitions, development phases (Phase 1~7), future feature ideas, architecture improvements

## Current Implementation Status

### Completed
- **Authentication System** (Phase 1)
  - Dual session system: authentication_access_session (1h) + authentication_refresh_session (30d)
  - Sign-in/Sign-out/Refresh endpoints with HttpOnly cookies
  - AuthenticationFilter with ValidateAuthenticationSessionUseCase
  - Session rotation on refresh (rotating refresh token pattern)
  - Single device policy (new sign-in invalidates existing sessions)
  - InvalidSessionException → 401 UNAUTHORIZED
  - Full hexagonal architecture compliance

- **EquestrianCenter CRUD** (Phase 2A)
  - CREATE: POST /api/v1/administration/equestrian-centers (System Admin only)
  - READ List: GET /api/v1/equestrian-centers (Public, paginated)
  - READ Detail: GET /api/v1/equestrian-centers/{uuid} (Public)
  - UPDATE: PATCH /api/v1/equestrian-centers/{uuid} (Representative only)
  - DELETE: Deferred (soft delete)
  - Renamed "leader" → "representative" throughout codebase
  - AuthenticationFilter allows only GET requests without auth

- **EquestrianCenter Invitation System** (Phase 2B) - 6/6 endpoints
  - EquestrianCenterInvitation domain/table implemented (log-style)
  - Invitation API endpoints:
    - POST /api/v1/equestrian-centers/{centerUuid}/invitations (send invitation)
    - GET /api/v1/equestrian-centers/{centerUuid}/invitations (list sent invitations)
    - DELETE /api/v1/equestrian-centers/{centerUuid}/invitations/{invitationUuid} (withdraw)
    - GET /api/v1/users/{userUuid}/equestrian-center-invitations (received invitations)
    - POST /api/v1/users/{userUuid}/equestrian-center-invitations/{invitationUuid}/approve
    - POST /api/v1/users/{userUuid}/equestrian-center-invitations/{invitationUuid}/reject
  - 7-day expiration logic (check at query time)
  - Re-invitation policy enforcement
  - N+1 prevention with batch fetching
  - Authorization: Representative for center endpoints, self-only for user endpoints
  - Invitation approval creates EquestrianCenterStaff record with joinedAt

- **EquestrianCenterStaff Management** (Phase 2C) - 4/4 endpoints
  - EquestrianCenterStaff domain/table implemented (join/leave history tracking)
  - Staff management API endpoints:
    - GET /api/v1/equestrian-centers/{centerUuid}/staff (list staff)
    - DELETE /api/v1/equestrian-centers/{centerUuid}/staff/{staffUuid}/expel (representative only)
    - DELETE /api/v1/equestrian-centers/{centerUuid}/staff/{staffUuid}/leave (self only)
    - GET /api/v1/users/{userUuid}/equestrian-center-staff-affiliations (my centers as staff)
  - Leave/expulsion tracking: left_at, left_by, left_reason (LEFT_VOLUNTARILY/EXPELLED)
  - N+1 prevention with batch fetching
  - Employment history preserved on re-join
  - MVP: All staff have equal permissions (no role system, instructor only)
  - Post-MVP: role column (INSTRUCTOR, MANAGER, ADMIN)

- **Season CRUD** (Phase 3) - 5/5 endpoints
  - API #1: POST /api/v1/equestrian-centers/{centerUuid}/seasons (시즌 생성)
  - API #2: GET /api/v1/equestrian-centers/{centerUuid}/seasons (시즌 목록 조회 - status/date filtering)
  - API #3: GET /api/v1/equestrian-centers/{centerUuid}/seasons/{seasonUuid} (시즌 상세 조회)
  - API #4: PATCH /api/v1/equestrian-centers/{centerUuid}/seasons/{seasonUuid} (시즌 수정)
  - API #5: PATCH /api/v1/equestrian-centers/{centerUuid}/seasons/{seasonUuid}/close (시즌 종료 ACTIVE→CLOSED)
  - Audit tracking: created_by, updated_by (감사 추적)
  - 10 Exception classes (SeasonException hierarchy)
  - Business policies documented with NOTE comments:
    - Past date creation allowed (data migration, historical records)
    - defaultTicketCount behavior (snapshot at enrollment approval time)
    - CLOSED seasons cannot be reopened (create new season instead)
  - Public API: Season list/detail accessible without authentication
  - Phase 5 concern: Lesson date range validation (documented in ROADMAP.md)

- **SeasonEnrollment** (Phase 3) - 4/4 endpoints
  - POST /api/v1/equestrian-centers/{centerUuid}/seasons/{seasonUuid}/enrollments (참여 신청)
  - GET /api/v1/equestrian-centers/{centerUuid}/seasons/{seasonUuid}/enrollments (신청 목록 조회, 직원용)
  - POST /{centerUuid}/seasons/{seasonUuid}/enrollments/{enrollmentUuid}/approve (승인, 직원용)
  - POST /{centerUuid}/seasons/{seasonUuid}/enrollments/{enrollmentUuid}/reject (거절, 직원용)
  - Status tracking: PENDING → APPROVED / REJECTED
  - SeasonEnrollmentLog: Automatic log creation on approve/reject (APPROVED/REJECTED types)
  - Authorization: Staff permission required for list/approve/reject
  - Approval logic (12 steps):
    - Season capacity check (countBySeasonIdAndStatus)
    - SeasonTicketAccount creation (balance = season.defaultTicketCount)
    - TicketLog creation (GRANT type, grantedBy = staff.id)
  - Rejection logic (9 steps):
    - Status change + SeasonEnrollmentLog creation (note field for rejection reason)
  - N+1 prevention with batch fetching for member details

- **SeasonTicketAccount & TicketLog** (Phase 4) ✅ COMPLETED
  - SeasonTicketAccount domain + persistence layer
    - Domain: SeasonTicketAccount (id, seasonId, memberId, balance, timestamps)
    - Repository: save, findBySeasonIdAndMemberId, existsBySeasonIdAndMemberId
    - Created on enrollment approval with default ticket count
  - TicketLog domain + persistence layer
    - Domain: TicketLog (id, accountId, amount, type, description, reservationId, grantedBy, createdAt)
    - TicketLogType enum: GRANT, USE, REFUND, ADDITIONAL
    - GRANT log created on enrollment approval
  - Ticket APIs (3 endpoints):
    - POST /api/v1/equestrian-centers/{centerUuid}/seasons/{seasonUuid}/enrollments/{enrollmentUuid}/tickets (추가 티켓 부여)
    - GET /api/v1/users/{userUuid}/seasons/{seasonUuid}/ticket-account (티켓 잔액 조회)
    - GET /api/v1/users/{userUuid}/seasons/{seasonUuid}/ticket-logs (티켓 로그 조회)
  - Exception handling: SeasonTicketAccountException, TicketLogException
  - GlobalExceptionHandler: DataIntegrityViolationException for duplicate ticket account

- **User Module Separation** (Phase 1 Step 1) - Modular Monolith
  - Created independent `user/` module with own build.gradle.kts
  - 10 files moved: Domain, Exception (4), Port, Adapter (4)
  - Exception hierarchy refactored: RuntimeException → UserException (2-tier, no BaseException)
  - Soft delete policy: All UserRepository methods check deletedAt
  - Plugins: kotlin-jvm, kotlin-spring, kotlin-jpa, ktlint
  - Complete module independence (MSA-ready)
  - Dependency: app → user (unidirectional)

- **Lesson + LessonInstructor** (Phase 5) - 5/5 endpoints
  - Lesson CRUD (5 APIs):
    - POST /api/v1/equestrian-centers/{centerUuid}/seasons/{seasonUuid}/lessons (레슨 생성)
    - GET /api/v1/equestrian-centers/{centerUuid}/seasons/{seasonUuid}/lessons (레슨 목록)
    - GET /api/v1/equestrian-centers/{centerUuid}/seasons/{seasonUuid}/lessons/{lessonUuid} (레슨 상세)
    - PATCH /api/v1/equestrian-centers/{centerUuid}/seasons/{seasonUuid}/lessons/{lessonUuid} (레슨 수정)
    - PATCH .../lessons/{lessonUuid}/cancel (레슨 취소)
  - LessonInstructor: N:M relationship with EquestrianCenterStaff (assigned during create/update)
  - UpdateSeasonService enhancement: SCHEDULED lesson date range validation on season date change
  - 12 Exception classes (LessonException hierarchy)
  - Validations: Season ACTIVE, lesson date within season period, capacity, time range
  - Authorization: Staff only for create/update/cancel

- **Architecture Refactoring** (Phase 5.5) - Tech Debt resolved
  - Season → Lesson domain dependency separation
  - Created CheckScheduledLessonsExistOutsideDateRangeUseCase in Lesson domain
  - UpdateSeasonService now depends on UseCase instead of LessonRepository
  - Benefit: Clear domain boundary, easier MSA migration

### Not Implemented Yet
- **Reservation + Attendance** (Phase 6)
  - Reservation - booking, cancellation, ticket account reference
  - LessonAttendance - attendance tracking with checker info
- **Admin features** (Phase 7) - TBD
- **Auth Module Separation** (Post-MVP)
  - AuthException refactoring (remove BaseException, extend RuntimeException)
  - Create auth/ module + build.gradle.kts
- **Tests** - unit/integration tests not written yet

## Next Steps

1. ~~Migrate User domain to new schema (remove role, add is_system_admin)~~ COMPLETED
2. ~~Implement Token domain (login, logout, token validation)~~ COMPLETED (as dual-session system)
3. ~~Add Spring Security configuration~~ COMPLETED (AuthenticationFilter + SecurityConfig)
4. ~~EquestrianCenter CRUD~~ COMPLETED (Phase 2A)
5. ~~EquestrianCenter Invitation System~~ COMPLETED (Phase 2B)
6. ~~EquestrianCenterStaff Management~~ COMPLETED (Phase 2C)
7. ~~Season CRUD~~ COMPLETED (Phase 3 - API #1-5)
8. ~~Implement SeasonEnrollment (apply, approve/reject, enrollment log)~~ COMPLETED (Phase 3 - 4/4 endpoints)
9. ~~Implement SeasonTicketAccount & TicketLog APIs~~ COMPLETED (Phase 4)
10. ~~Implement Lesson + LessonInstructor~~ COMPLETED (Phase 5)
11. ~~Architecture Refactoring - Season/Lesson domain separation~~ COMPLETED (Phase 5.5)
12. Implement Reservation + LessonAttendance (Phase 6)

## Development Log

**Note**: Historical development logs (2024-11-24 ~ 2026-01-05) have been archived to [`docs/backup-archive-eng.md`](./docs/backup-archive-eng.md) for token optimization.

### 2026-01-06: Modular Monolith - user module separation

**Architecture refactoring:**
- Implemented **Modular Monolith** architecture for MSA migration readiness
- Separated user domain into independent module with complete isolation
- Dependency structure: app → user (unidirectional, no circular dependencies)

**user module creation:**
- Created `user/` module with independent build.gradle.kts
- Plugins: kotlin-jvm, kotlin-spring, kotlin-jpa, ktlint
- Dependencies: spring-data-jpa, jackson, kotlin-reflect, postgresql
- 10 files moved from app to user module:
  - Domain: User.kt
  - Exceptions: UserException.kt, UserNotFoundException.kt, DuplicateUsernameException.kt, UnauthorizedUserOperationException.kt
  - Port: UserRepository.kt (interface)
  - Adapter: UserRepositoryAdapter.kt, UserEntity.kt, UserMapper.kt, UserJpaRepository.kt

**Exception hierarchy refactoring:**
- Changed from 3-tier to 2-tier structure for complete module independence
- Before: RuntimeException → BaseException → UserException (shared dependency)
- After: RuntimeException → UserException (module independent)
- UserException now directly extends RuntimeException with errorCode field
- Benefits: MSA-ready, no common library dependency, compile-time exhaustiveness

**Soft delete policy consistency:**
- Applied `AndDeletedAtIsNull` to all UserRepository methods
- Before: Only findById checked deletedAt (inconsistent)
- After: All methods check deletedAt (consistent with EquestrianCenter, Season)
- Methods updated:
  - existsByUsernameAndDeletedAtIsNull
  - findByUuidAndDeletedAtIsNull
  - findByUsernameAndDeletedAtIsNull
  - findAllByIdInAndDeletedAtIsNull

**Configuration fixes:**
- Added kotlin-spring plugin to user module (CGLIB proxy generation)
- Added ktlint plugin to user module (code style consistency)
- Root project now depends on user module: implementation(project(":user"))
- settings.gradle.kts updated: include("user")

**Verification:**
- Build successful (both user module and entire project)
- Application starts successfully (Tomcat on port 8080)
- All imports work correctly (same package structure preserved)
- Code style checks pass (ktlintCheck, ktlintFormat)

**Technical decisions:**
- user module has NO UseCase (port/in) - it's a shared data access layer
- Other modules (auth, equestriancenter, etc.) use UserRepository as output dependency
- Hexagonal architecture maintained within each module
- Entity immutability preserved (all fields `val`)

**Documentation updated:**
- CLAUDE.md Project Structure: Added Modular Monolith section
- CLAUDE.md Development Log: Added this entry (2026-01-06)
- Phase 1 (user module separation) completed, Phase 2 (auth module) ready to start


### 2026-01-11: Lesson + LessonInstructor (Phase 5) completed

**Lesson CRUD implementation:**
- 5 API endpoints implemented:
  - POST /seasons/{seasonUuid}/lessons (create with instructor assignment)
  - GET /seasons/{seasonUuid}/lessons (list with date/status filtering)
  - GET /seasons/{seasonUuid}/lessons/{lessonUuid} (detail)
  - PATCH /seasons/{seasonUuid}/lessons/{lessonUuid} (update)
  - PATCH /seasons/{seasonUuid}/lessons/{lessonUuid}/cancel (cancel)
- LessonInstructor: N:M relationship, full replacement on update (MVP)
- 12 Exception classes in LessonException hierarchy

**UpdateSeasonService enhancement:**
- Added SCHEDULED lesson date range validation
- When season startDate/endDate changes, validates no SCHEDULED lessons exist outside new range
- CANCELLED lessons excluded from validation (already completed, no impact)
- New exception: LessonsExistOutsideDateRangeException

**Technical decisions:**
- Season → Lesson dependency: ~~Currently allowed (documented as tech debt for future refactoring)~~ Resolved in Phase 5.5
- ~~Post-MVP: Consider separating into CheckLessonsExistOutsideDateRangeUseCase~~ Done

**Files changed:**
- SeasonException.kt: Added LessonsExistOutsideDateRangeException
- LessonRepository.kt: Added existsBySeasonIdAndScheduledLessonDateOutsideRange
- LessonJpaRepository.kt: Added JPQL query with SCHEDULED status filter
- LessonRepositoryAdapter.kt: Implemented new method
- UpdateSeasonService.kt: Added date change validation logic (step 7)


### 2026-01-11: Architecture Refactoring (Phase 5.5) completed

**Domain dependency separation:**
- Resolved Season → Lesson domain boundary violation
- Before: UpdateSeasonService directly referenced LessonRepository (output port)
- After: UpdateSeasonService depends on CheckScheduledLessonsExistOutsideDateRangeUseCase (input port)

**Files created:**
- CheckScheduledLessonsExistOutsideDateRangeUseCase.kt (lesson/application/port/in)
- CheckScheduledLessonsExistOutsideDateRangeService.kt (lesson/application/service)

**Files modified:**
- UpdateSeasonService.kt: LessonRepository → CheckScheduledLessonsExistOutsideDateRangeUseCase

**Benefits:**
- Clear domain boundary between Season and Lesson
- Lesson domain encapsulates its own logic
- Easier MSA migration (UseCase can become API call)
