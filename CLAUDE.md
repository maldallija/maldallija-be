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

- `src/main/kotlin/dev/maldallija/maldallijabe/` - Main application code
- `src/test/kotlin/dev/maldallija/maldallijabe/` - Test code
- `src/main/resources/` - Configuration files and static resources

## JPA Configuration

The project uses `allOpen` plugin for JPA entities - classes annotated with `@Entity`, `@MappedSuperclass`, or `@Embeddable` are automatically open for proxying.

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
- No waitlist, no horse assignment, no level system (see docs/MEMO.md for future ideas)

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
  - ✅ `RefreshAuthenticationSessionUseCase` ❌ `RefreshSessionUseCase`
  - ✅ `authenticationAccessSession` ❌ `session` or `accessSession`
  - ✅ `authenticationRefreshSessionId` ❌ `refreshId` or `sessionId`
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

## Related Documents

- `docs/database.md` - DB schema design (15 tables: user, equestrian_center, equestrian_center_invitation, equestrian_center_staff, authentication_access_session, authentication_refresh_session, season, season_enrollment, season_enrollment_log, season_ticket_account, ticket_log, lesson, lesson_instructor, reservation, lesson_attendance)
  - Note: Originally specified "instructor_group" table, renamed to "equestrian_center" for clarity
  - Note: Originally specified "leader_user_id", renamed to "representative_user_id" for clarity
  - Note: Originally specified "instructor_group_member", renamed to "equestrian_center_staff" (staff = 강사, 매니저 등 모든 직원)
  - Note: Originally specified "token" table (1 day expiry), implemented as dual-session system (access 1h + refresh 30d)
  - Note: Added equestrian_center_invitation table for invitation system (log-style)
- `docs/MEMO.md` - Future feature ideas (OAuth2, notifications, batch, role-based permissions, etc.)
- `docs/MEMO2.md` - Role-based feature definitions + Development order (Phase 1~7)

## Current Implementation Status

### Completed
- **User domain** (Phase 1 partial) - NEEDS UPDATE for new schema
  - Domain: `User.kt` (has old `role` field, needs `is_system_admin`)
  - Ports/Service/Persistence/Web implemented with old structure
  - **TODO**: Migrate to new structure (remove role, add is_system_admin)

- **Authentication System** (Phase 1) ✅ COMPLETED
  - Dual session system: authentication_access_session (1h) + authentication_refresh_session (30d)
  - Sign-in/Sign-out/Refresh endpoints with HttpOnly cookies
  - AuthenticationFilter with ValidateAuthenticationSessionUseCase
  - Session rotation on refresh (rotating refresh token pattern)
  - Single device policy (new sign-in invalidates existing sessions)
  - InvalidSessionException → 401 UNAUTHORIZED
  - Full hexagonal architecture compliance

- **EquestrianCenter CRUD** (Phase 2A) ✅ COMPLETED
  - CREATE: POST /api/v1/administration/equestrian-centers (System Admin only)
  - READ List: GET /api/v1/equestrian-centers (Public, paginated)
  - READ Detail: GET /api/v1/equestrian-centers/{uuid} (Public)
  - UPDATE: PATCH /api/v1/equestrian-centers/{uuid} (Representative only)
  - DELETE: Deferred (soft delete)
  - Renamed "leader" → "representative" throughout codebase
  - AuthenticationFilter allows only GET requests without auth

- **EquestrianCenter Invitation System** (Phase 2B) ❌ NOT IMPLEMENTED
  - EquestrianCenterInvitation domain/table designed (log-style)
  - Invitation API endpoints not implemented:
    - POST /api/v1/equestrian-centers/{centerUuid}/invitations (send invitation)
    - GET /api/v1/equestrian-centers/{centerUuid}/invitations (list sent invitations)
    - DELETE /api/v1/equestrian-centers/{centerUuid}/invitations/{invitationUuid} (withdraw)
    - GET /api/v1/my/equestrian-center-invitations (received invitations)
    - POST /api/v1/my/equestrian-center-invitations/{invitationUuid}/approve
    - POST /api/v1/my/equestrian-center-invitations/{invitationUuid}/reject
  - 7-day expiration logic (check at query time)
  - Re-invitation policy enforcement

- **EquestrianCenterStaff Management** (Phase 2C) ❌ NOT IMPLEMENTED
  - EquestrianCenterStaff domain/table designed (join/leave history tracking)
  - Staff management API endpoints not implemented:
    - GET /api/v1/equestrian-centers/{centerUuid}/staff (list staff)
    - DELETE /api/v1/equestrian-centers/{centerUuid}/staff/{staffUuid} (expel)
    - DELETE /api/v1/equestrian-centers/{centerUuid}/staff/me (leave)
    - GET /api/v1/my/equestrian-center-staff-memberships (my centers as staff)
  - Leave/expulsion tracking (left_at, left_by, left_reason)
  - MVP: All staff have equal permissions (no role system, instructor only)
  - Post-MVP: role column (INSTRUCTOR, MANAGER, ADMIN)

### Not Implemented Yet
- **Season + Enrollment** (Phase 3)
  - Season - CRUD, status, capacity management
  - SeasonEnrollment - apply, approve/reject with status tracking
  - SeasonEnrollmentLog - enrollment history timeline
- **Ticket system** (Phase 4)
  - SeasonTicketAccount - balance per season-member
  - TicketLog - transaction history with account reference
- **Lesson + Assignment** (Phase 5)
  - Lesson - CRUD, status, time validation
  - LessonInstructor - instructor assignment (N:M with InstructorGroupMember)
- **Reservation + Attendance** (Phase 6)
  - Reservation - booking, cancellation, ticket account reference
  - LessonAttendance - attendance tracking with checker info
- **Admin features** (Phase 7) - TBD
- **Spring Security** - authentication/authorization filters
- **Tests** - unit/integration tests not written yet

## Next Steps

1. ~~Migrate User domain to new schema (remove role, add is_system_admin)~~ ✅ COMPLETED
2. ~~Implement Token domain (login, logout, token validation)~~ ✅ COMPLETED (as dual-session system)
3. ~~Add Spring Security configuration~~ ✅ COMPLETED (AuthenticationFilter + SecurityConfig)
4. ~~EquestrianCenter creation~~ ✅ COMPLETED (Phase 2 partial)
5. Complete EquestrianCenter CRUD (retrieval, update, delete) (Phase 2)
6. Implement InstructorGroupMember (Phase 2)
7. Implement Season + Enrollment (with enrollment log) (Phase 3)
8. Continue with Ticket → Lesson → Reservation → Attendance (Phase 4-6)

## Development Log

### 2024-11-24: Initial setup + User domain
- Project requirements gathered (see Domain Model section)
- DB schema designed (docs/database.md)
- Hexagonal architecture package structure established
- User domain implemented without authentication
- API: POST /api/users, GET /api/users, GET /api/users/{uuid}, PATCH /api/users/{uuid}/role
- Dependencies added: spring-security-crypto for BCrypt

### 2024-11-24: Domain model revision (Credit → Ticket)
- Changed from Credit (global) to Ticket (per Season)
- Added SeasonEnrollment: Member applies to Season, Instructor approves
- Ticket granted upon enrollment approval (default_ticket_count from Season)
- Instructor can grant additional tickets during Season
- Lesson time unit changed from 30min to 1hour
- Ticket cost = lesson duration in hours (e.g., 2h lesson = 2 tickets)
- Multiple lessons allowed at same time slot within a Season
- Admin role features deferred (TBD)
- Refund policy: D-3 (3 days before) refundable, D-2 onwards no refund

### 2024-11-24: Group structure added
- Added InstructorGroup (academy) concept
- Instructor belongs to exactly 1 group (instructor_group_id required for INSTRUCTOR role)
- Group has 1 leader (group leader)
- Season is created at group level (not individual instructor)
- Lesson can have multiple instructors (N:M via lesson_instructor table)
- Admin creates groups, assigns instructors, designates group leader
- Group creation process: create group → add instructors → designate leader
- MVP: All instructors have full permissions within group
- Post-MVP: Group leader can configure instructor permissions (see MEMO.md)

### 2024-11-24: Audit tracking added
- Added `created_by` to season table (tracks creator instructor)
- Added `created_by` to lesson table (tracks creator instructor)
- Added `granted_by` to ticket_log table (tracks who granted tickets for GRANT/ADDITIONAL types)
- Enables tracking of who created/modified key entities for accountability

### 2025-12-08: Major schema redesign - Permission system + N:M relationships
- **User role system redesigned**: Removed `user.role` enum, added `is_system_admin` boolean
- **Group membership changed to N:M**: Instructors can belong to multiple groups via `instructor_group_member`
- **Permission system introduced** (MVP scope):
  - `instructor_group_role`: Roles defined per group
  - `instructor_group_permission`: System-wide permission catalog
  - `instructor_group_role_permission`: Role-permission mapping (N:M)
- **Enrollment history tracking**: Added `season_enrollment_log` to track status changes with actor
- **Enrollment status expanded**: Added WITHDRAWN status
- **Ticket renamed to account**: `ticket` → `season_ticket_account` for clarity
- **Season capacity added**: Track enrollment limits per season
- **Attendance tracking enhanced**: Added `lesson_attendance` table with `checked_by` and `checked_at`
- **Reservation status granularity**: Split CANCELLED into CANCELLED_BY_USER / CANCELLED_BY_INSTRUCTOR
- **Reference integrity improved**: `lesson_instructor` now references `instructor_group_member.id` instead of `user.id`
- **Ticket payment tracking**: `reservation.season_ticket_account_id` explicitly tracks which account was charged
- **Total tables increased**: 10 → 16 tables
- **Database documentation updated**: docs/database.md completely rewritten with new ERD
- **CLAUDE.md updated**: Domain Model, Package Structure, Implementation Status reflect new design

### 2025-12-08: Schema refinements - Concurrency & business logic clarification
- **Fixed circular reference**: `instructor_group.leader_id` → `leader_user_id` (now references `user.id` directly)
- **Re-enrollment enabled**: Changed UNIQUE constraint to partial index on `season_enrollment(season_id, member_id) WHERE status IN ('PENDING', 'APPROVED')` - allows REJECTED/WITHDRAWN to reapply
- **Attendance separated from reservation**: Removed ATTENDED/NO_SHOW from `reservation_status` enum, attendance managed solely via `lesson_attendance`
- **Concurrency control added**: `lesson.version` column for optimistic locking on concurrent bookings
- **Season enrollment count**: Decided to calculate via COUNT query (no actual column) for MVP simplicity
- **Lesson capacity clarification**: `lesson.current_count` tracks RESERVED status only (for availability check)
- **Refund policy clarified**: Based on `lesson.lesson_date` (date only, time ignored) - D-3 refundable, D-2 no refund
- **Ticket log field renamed**: `reference_id` → `reservation_id` for clarity (USE/REFUND types only)
- **Additional indexes**: Added type/status indexes for `ticket_log`, `season_enrollment_log`, `lesson_attendance` performance
- **Business rules documented**: Added comprehensive Business Rules & Notes section to database.md
- **Lesson cancellation flow**: Documented automatic refund process when instructor cancels lesson
- **Soft delete handling**: Clarified that `created_by` can reference deleted members (audit trail preservation)

### 2025-12-08: MVP simplification - Permission system deferred to Post-MVP
- **Removed 3 permission tables** for MVP: instructor_group_role, instructor_group_permission, instructor_group_role_permission
- **Total tables reduced**: 16 → 13 tables
- **instructor_group_member simplified**: Removed role_id column
- **Permission model**: MVP - All group members have equal permissions, Post-MVP - Role-based permissions
- **Group leader**: Still designated via instructor_group.leader_user_id, but no permission differences in MVP
- **Rationale**: Reduce complexity for toy project while maintaining real service potential
- **Post-MVP path**: Add 3 permission tables back when needed (5-10 weeks saved in initial development)
- **Updated documents**: database.md (ERD, tables, indexes), CLAUDE.md (domain model, implementation status), MEMO2.md (role definitions, phase plan)

### 2025-12-08: Package name correction
- **Package name updated**: Changed from `dev.ehyeon.dageudaktest` to `dev.maldallija.maldallijabe` throughout CLAUDE.md
- **Test class name updated**: `DageudakTestApplicationTests` → `MaldallijaBeApplicationTests`
- **Project structure paths corrected**: Updated all file path references to match actual codebase structure
- **Rationale**: CLAUDE.md contained outdated package references that would cause build commands to fail

### 2025-12-15: Authentication/Authorization System Implementation

**Implemented dual-session authentication system replacing planned single "token" table:**
- Created authentication_access_session (1 hour expiry) and authentication_refresh_session (30 days expiry) tables
- Replaced database.md spec's single "token" table with dual-session system for better security and UX
- HttpOnly cookies (SameSite=Strict, Secure) for web security - prevents XSS/CSRF attacks
- Industry standard expiry times based on Auth0/AWS Cognito/Firebase defaults

**Authentication Features:**
- **SignIn** (POST /api/v1/auth/sign-in): Issues both access and refresh sessions, invalidates all existing sessions (single device policy)
- **SignOut** (POST /api/v1/auth/sign-out): Revokes all user sessions, deletes both cookies, uses @AuthenticationPrincipal annotation
- **Refresh** (POST /api/v1/auth/sessions/refresh): Validates refresh session, issues new access+refresh pair, invalidates old sessions (rotating refresh token pattern)

**Architecture Improvements:**
- Achieved 100% hexagonal architecture compliance
- AuthenticationFilter uses ValidateAuthenticationSessionUseCase (not Repository directly) - proper layering
- SignInResult DTO moved from domain to application/port/in - domain models hidden from Controller
- Cookie constants moved from domain to adapter/in/web/constant - HTTP details separated from business logic
- Filter → UseCase → Service → Repository dependency flow maintained throughout

**Code Quality Standards Established:**
- **CRITICAL RULE**: Always use full naming - NO abbreviations (RefreshAuthenticationSessionUseCase not RefreshSessionUseCase)
- Applied to all: classes (RefreshAuthenticationSessionService), methods (refreshAuthenticationSession), variables (authenticationAccessSession)
- Enums for constants: AuthenticationSessionRevokedReason (NEW_SIGN_IN, SIGN_OUT, SESSION_REFRESH) instead of string literals
- Consistent formatting: Multi-line parameter calls for readability throughout codebase

**Security Features:**
- BCrypt password hashing via Spring Security Crypto
- Session validation in AuthenticationFilter for all protected endpoints
- InvalidSessionException → 401 UNAUTHORIZED via GlobalExceptionHandler
- OSIV disabled (spring.jpa.open-in-view: false) for performance - hexagonal architecture loads all data in Service layer
- Excluded paths: /sign-in, /sign-up, /sessions/refresh, /swagger-ui/*, /v3/api-docs/*

**Database Schema:**
- authentication_access_session: id, uuid, authentication_access_session, user_id, created_at, expires_at, revoked_at, revoked_reason
- authentication_refresh_session: id, uuid, authentication_refresh_session, user_id, created_at, expires_at, revoked_at, revoked_reason
- Both use UUID for external API exposure, BIGSERIAL id for internal operations
- Soft delete support via revoked_at timestamp

**Configuration:**
- AuthProperties: access-session.expiry-hours (1), refresh-session.expiry-days (30)
- MaldallijaBeApplication: Excludes UserDetailsServiceAutoConfiguration to remove Spring Security warning
- SecurityConfig: Custom filter chain with AuthenticationFilter

**API Endpoints:**
- POST /api/v1/auth/sign-in - Login with email/password, returns access+refresh cookies
- POST /api/v1/auth/sign-out - Logout (requires authentication), deletes cookies
- POST /api/v1/auth/sessions/refresh - Refresh sessions (uses refresh cookie, no authentication required)

**Files Created:**
- Domain: AuthenticationAccessSession, AuthenticationRefreshSession, AuthenticationSessionRevokedReason, InvalidSessionException
- UseCases: SignInUseCase, SignOutUseCase, RefreshAuthenticationSessionUseCase, ValidateAuthenticationSessionUseCase
- Services: SignInService, SignOutService, RefreshAuthenticationSessionService, ValidateAuthenticationSessionService
- Repositories: AuthenticationAccessSessionRepository, AuthenticationRefreshSessionRepository (port/out interfaces)
- Adapters: AuthenticationAccessSessionRepositoryAdapter, AuthenticationRefreshSessionRepositoryAdapter
- Entities: AuthenticationAccessSessionEntity, AuthenticationRefreshSessionEntity
- Mappers: AuthenticationAccessSessionMapper, AuthenticationRefreshSessionMapper
- JPA Repositories: AuthenticationAccessSessionJpaRepository, AuthenticationRefreshSessionJpaRepository
- Web: AuthController (sign-in/sign-out/refresh endpoints), AuthenticationSessionCookieName (cookie constants)
- Filter: AuthenticationFilter (session validation)

**Key Technical Decisions:**
- Rotating refresh tokens: Refresh invalidates all old sessions for security
- Single device policy: New sign-in invalidates all existing sessions
- Session rotation reason tracking: NEW_SIGN_IN, SIGN_OUT, SESSION_REFRESH enum values
- Cookie-based for web (most secure), designed to support headers for future mobile expansion
- No JWT: Database-stored sessions for revocation support and simpler infrastructure

**Differences from Initial Specification:**
- **Initial spec** (docs/database.md line 115-125): Single "token" table with 1 day expiry
- **Actual implementation**: Dual-session system (access 1h + refresh 30d) for better security/UX balance
- **Rationale**: Industry standard approach provides better security (short-lived access tokens) while maintaining good UX (long-lived refresh tokens)

### 2025-12-17: InstructorGroup renamed to EquestrianCenter + Phase 2 partial implementation

**Naming refactoring for domain clarity:**
- **InstructorGroup → EquestrianCenter**: Renamed for better business domain representation
  - Package: `instructorgroup` → `equestriancenter`
  - Table: `instructor_group` → `equestrian_center`
  - All related classes, files, and documentation updated
  - Rationale: "Equestrian Center" is more intuitive and commonly used in actual riding facilities

**EquestrianCenter creation feature implemented (Phase 2 partial):**
- Domain: EquestrianCenter with audit tracking (createdBy, updatedBy)
- UseCase: CreateEquestrianCenterUseCase (System Admin only)
- API: POST /api/v1/equestrian-centers - Returns 201 Created with no body
- Request DTO: Uses leaderUserUuid (UUID) instead of internal ID for external API
- Added User.findByUuid() for UUID-based user lookup
- Exception handling: UnauthorizedEquestrianCenterOperationException (403), EquestrianCenterNotFoundException (404)
- Full hexagonal architecture compliance maintained

**Files created:**
- Domain: EquestrianCenter, EquestrianCenterException, UnauthorizedEquestrianCenterOperationException, EquestrianCenterNotFoundException
- UseCase: CreateEquestrianCenterUseCase
- Service: CreateEquestrianCenterService
- Repository: EquestrianCenterRepository (port), EquestrianCenterRepositoryAdapter
- Entity: EquestrianCenterEntity (with createdBy, updatedBy fields)
- Mapper: EquestrianCenterMapper
- JPA: EquestrianCenterJpaRepository
- Web: EquestrianCenterController, CreateEquestrianCenterRequest, EquestrianCenterResponse (not used)

**Audit tracking:**
- Added createdBy, updatedBy fields to track who created/modified equestrian centers
- Creator tracked on creation, updater tracked on future updates
- Supports accountability and audit trail requirements

### 2025-12-18: Phase 2A completion + Invitation system design (Phase 2B/2C)

**EquestrianCenter CRUD completion (Phase 2A):**
- **Architecture reorganization**:
  - Moved administration endpoints from `equestriancenter/adapter/in/web` to `administration/adapter/in/web/equestriancenter`
  - Separated access levels: `/api/v1/administration/*` (admin-only) vs `/api/v1/equestrian-centers` (public/authenticated)
  - Renamed AdministrationAuthorizationFilter → AdministratorAuthorizationFilter for clarity
  - Reorganized auth structure to match: `auth/adapter/in/web/auth/` with subdirectories (dto, constant)
- **Naming changes**:
  - "leader/센터장" → "representative/대표 사용자" throughout codebase (11 files)
  - `leaderUserId` → `representativeUserId` in domain, `leader_user_id` → `representative_user_id` in DB
  - Updated entity with `@Column(name = "representative_user_id")` for backward compatibility
- **CRUD operations implemented**:
  - CREATE: POST /api/v1/administration/equestrian-centers (System Admin only) ✅
  - READ List: GET /api/v1/equestrian-centers (Public, paginated, deleted excluded) ✅
  - READ Detail: GET /api/v1/equestrian-centers/{uuid} (Public, returns representativeUserUuid) ✅
  - UPDATE: PATCH /api/v1/equestrian-centers/{uuid} (Representative only, name/description) ✅
  - DELETE: Deferred (soft delete via deleted_at)
- **AuthenticationFilter refinement**:
  - Changed from allowing all `/equestrian-centers` to only GET requests without auth
  - PATCH/POST/DELETE require authentication
  - Uses `HttpMethod.GET.name()` for type-safe method checking
- **API responses**:
  - List: uuid, name, description (paginated)
  - Detail: +representativeUserUuid, createdAt, updatedAt
  - Update: 204 No Content
- **Exception handling**:
  - 404: EquestrianCenterNotFoundException (center not found or deleted)
  - 403: UnauthorizedEquestrianCenterOperationException (not representative)

**Invitation system design (Phase 2B - NOT IMPLEMENTED):**
- **Table structure finalized**: Log-style equestrian_center_invitation table
  - invitation_status enum: INVITED, APPROVED, REJECTED, EXPIRED, WITHDRAWN
  - Columns: id, uuid, equestrian_center_id, user_id, invited_by, status, invited_at, responded_at, expires_at
  - Separate from instructor_group_member (concerns separated: invitation process vs active membership)
  - Multiple invitation records per user allowed (complete history preservation)
- **Business rules defined**:
  - Expiration: 7 days from invited_at, checked at query time (no batch job)
  - Re-invitation: allowed after REJECTED/EXPIRED/WITHDRAWN, forbidden if INVITED exists
  - Cancellation: representative can WITHDRAW invitation (INVITED → WITHDRAWN)
  - Approval: INVITED → APPROVED, creates instructor_group_member record
  - Rejection: INVITED → REJECTED
- **API endpoints designed** (15 endpoints total):
  - Send invitation: POST /api/v1/equestrian-centers/{centerUuid}/invitations
  - List sent: GET /api/v1/equestrian-centers/{centerUuid}/invitations
  - Withdraw: DELETE /api/v1/equestrian-centers/{centerUuid}/invitations/{invitationUuid}
  - Received list: GET /api/v1/my/equestrian-center-invitations
  - Approve: POST /api/v1/my/equestrian-center-invitations/{invitationUuid}/approve
  - Reject: POST /api/v1/my/equestrian-center-invitations/{invitationUuid}/reject

**Member management design (Phase 2C - NOT IMPLEMENTED):**
- **Table structure finalized**: instructor_group_member with join/leave tracking
  - member_left_reason enum: LEFT_VOLUNTARILY, EXPELLED
  - Added columns: joined_at, left_at, left_by, left_reason
  - Employment history preserved: new record created on re-join after leave
  - UNIQUE INDEX on (equestrian_center_id, user_id) WHERE left_at IS NULL AND deleted_at IS NULL
- **Business rules defined**:
  - Representative auto-added to instructor_group_member on center creation
  - Leave: member sets left_at (left_by = NULL, left_reason = LEFT_VOLUNTARILY)
  - Expel: representative sets left_at, left_by, left_reason = EXPELLED
  - Re-join after leave: creates new membership record (preserves history)
  - Representative change: updates representative_user_id, previous representative stays as member
- **API endpoints designed**:
  - List members: GET /api/v1/equestrian-centers/{centerUuid}/members (visibility configurable by representative)
  - Expel member: DELETE /api/v1/equestrian-centers/{centerUuid}/members/{memberUuid}
  - Leave center: DELETE /api/v1/equestrian-centers/{centerUuid}/members/me
  - My memberships: GET /api/v1/my/equestrian-center-memberships

**Architecture decisions:**
- **representative_user_id retained** (vs is_representative flag):
  - Representative = legal/business owner (permanent designation)
  - Post-MVP: Functional permissions separated via role_id
  - Performance: no join needed for representative checks
  - Simplicity: direct ID comparison for authorization
- **Invitation log-style vs status update**:
  - Chosen: Log-style (new record per invitation)
  - Rationale: Complete audit trail, no separate invitation_log needed
  - Trade-off: More records vs better history tracking
- **Member table vs combined invitation+member**:
  - Chosen: Separate tables (invitation vs active membership)
  - Rationale: Concerns separation, cleaner queries, employment history tracking

**Documentation updated:**
- **database.md**:
  - Added invitation_status, member_left_reason enums
  - Added equestrian_center_invitation table (table #3)
  - Updated instructor_group_member with join/leave fields (table #4)
  - Updated indexes (representative_user_id, invitation indexes, member partial unique)
  - Renumbered all tables (14 → 15 tables total)
  - Updated ERD with invitation relationship
- **CLAUDE.md**:
  - Updated Domain Model (EquestrianCenterInvitation, InstructorGroupMember details)
  - Updated Business Rules (center creation & membership flow)
  - Updated Implementation Status (Phase 2A ✅, 2B/2C ❌)
  - Added this development log entry
- **Related Documents section**: Updated table count to 15

**Files modified:**
- EquestrianCenter domain/entity/controller (representative naming)
- AuthenticationFilter (HttpMethod enum for GET-only bypass)
- AdministratorAuthorizationFilter (renamed)
- Auth controllers/DTOs (reorganized to auth/adapter/in/web/auth/)
- All documentation files

### 2025-12-18: Naming convention refinement (instructor_group_member → equestrian_center_staff)

**Rationale: Future extensibility and clarity**
- "Instructor" limits scope to teaching role only
- "Staff" accommodates all employee roles (instructor, manager, admin, etc.)
- Aligns with real-world equestrian center operations
- No code changes yet (Phase 2B/2C not implemented) - documentation only

**Naming convention established:**
```
Admin         - 서비스 운영자 (System Admin, is_system_admin=true)
Representative - 승마장 대표 (equestrian_center.representative_user_id)
Staff         - 승마장 직원 (equestrian_center_staff: 강사, 매니저 등)
Member        - 시즌 참여자 (season_enrollment: 수강생)
User          - 일반 사용자 (가입만 한 상태)
```

**Database changes:**
- Table: `instructor_group_member` → `equestrian_center_staff`
- Column: `lesson_instructor.instructor_group_member_id` → `staff_id`
- Indexes: `idx_instructor_group_member_*` → `idx_staff_*`
- All references in created_by, granted_by, checked_by updated

**Documentation updated:**
- **database.md**: All table/column names, ERD, indexes, descriptions
- **CLAUDE.md**: Domain Model, Package Structure, Implementation Status, this log
- **MEMO2.md**: Phase 2 descriptions and API endpoint names

**API endpoint changes (designed, not yet implemented):**
```
Before: /api/v1/equestrian-centers/{uuid}/members
After:  /api/v1/equestrian-centers/{uuid}/staff

Before: /api/v1/my/equestrian-center-memberships
After:  /api/v1/my/equestrian-center-staff-memberships
```

**Domain model changes:**
```kotlin
Before: InstructorGroupMember
After:  EquestrianCenterStaff

// MVP: All staff = instructor role
// Post-MVP: role column (INSTRUCTOR, MANAGER, ADMIN)
```

**Complexity avoided:**
- staff is collective noun (no plural form needed)
- Staff vs Employee: staff chosen (more general, includes non-employees)
- lesson_instructor table kept (role-specific: who teaches this lesson)
- Distinction maintained: staff (employment) vs instructor (lesson role)
