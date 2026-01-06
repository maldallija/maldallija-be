# Archive - Historical Documentation (2024-11-24 ~ 2026-01-05)

**Archived for token optimization on 2026-01-06**

This file contains historical documentation that has been moved from CLAUDE.md and MODULAR_MONOLITH_RBAC_DESIGN.md.

---

# Table of Contents

1. [Development Log Archive (2024-11-24 ~ 2026-01-05)](#development-log-archive)
2. [RBAC Design (Phase 2 - Not Yet Implemented)](#rbac-design)
3. [Migration Plan (Detailed Implementation Steps)](#migration-plan)

---

<a id="development-log-archive"></a>

# Development Log Archive (2024-11-24 ~ 2026-01-05)

**Archived from CLAUDE.md for token optimization on 2026-01-06**

---

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
  - CREATE: POST /api/v1/administration/equestrian-centers (System Admin only)
  - READ List: GET /api/v1/equestrian-centers (Public, paginated, deleted excluded)
  - READ Detail: GET /api/v1/equestrian-centers/{uuid} (Public, returns representativeUserUuid)
  - UPDATE: PATCH /api/v1/equestrian-centers/{uuid} (Representative only, name/description)
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
  - Updated Implementation Status (Phase 2A, 2B/2C)
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

### 2025-12-20: Entity immutability policy + Phase 2B invitation implementation

**Entity immutability refactoring:**
- **All Entity fields changed to `val` (immutable)**
- **Removed all `updatable = false` annotations** (redundant with val)
- **NO JPA dirty checking** - explicit save() required for all updates
- **Update pattern**: Create new Entity instance → save()

**Rationale:**
- Prevents accidental `updatedAt`/`updatedBy` omission
- Explicit update semantics (functional programming style)
- Safer, more predictable code
- Clearer audit trail management

**Entities refactored:**
- `UserEntity` - all fields now val
- `EquestrianCenterEntity` - all fields now val
- `EquestrianCenterInvitationEntity` - all fields now val
- `EquestrianCenterStaffEntity` - all fields now val
- `AuthenticationAccessSessionEntity` - all fields now val
- `AuthenticationRefreshSessionEntity` - all fields now val

**Database schema additions:**
- Added `updated_by` column to `equestrian_center_invitation`
- Added `updated_by` column to `equestrian_center_staff`
- Added indexes: `idx_invitation_updated_by`, `idx_staff_updated_by`

**Phase 2B: Invitation creation (POST) implemented:**
- **API**: POST /api/v1/equestrian-centers/{equestrianCenterUuid}/invitations
- **Request**: `{ userUuid: UUID }`
- **Response**: 201 Created (no body)
- **Authorization**: Representative only
- **Validations**:
  1. Center exists & not deleted
  2. Requester is representative
  3. Invited user exists & not deleted
  4. Self-invitation prevented
  5. Already active staff check
  6. Duplicate INVITED status check
  7. Invitation created with 7-day expiration

**Exception hierarchy:**
- `EquestrianCenterInvitationException` (abstract parent with errorCode)
- `DuplicateInvitationException` - errorCode: "DUPLICATE_INVITATION"
- `AlreadyStaffMemberException` - errorCode: "ALREADY_STAFF_MEMBER"
- `SelfInvitationException` - errorCode: "SELF_INVITATION"

**Files created:**
- Domain: `EquestrianCenterInvitation`, `InvitationStatus` enum
- Exceptions: 3 concrete + 1 abstract parent
- UseCase: `CreateEquestrianCenterInvitationUseCase`
- Service: `CreateEquestrianCenterInvitationService`
- Repository: `EquestrianCenterInvitationRepository`, `EquestrianCenterStaffRepository` (minimal)
- Entity: `EquestrianCenterInvitationEntity`, `EquestrianCenterStaffEntity` (partial)
- Mapper: `EquestrianCenterInvitationMapper`
- JPA: `EquestrianCenterInvitationJpaRepository`, `EquestrianCenterStaffJpaRepository`
- Adapter: Repository adapters for both
- Web: `EquestrianCenterInvitationController`, `CreateEquestrianCenterInvitationRequest`
- GlobalExceptionHandler: Added `EquestrianCenterInvitationException` handler (400)

**Documentation updated:**
- **CLAUDE.md**: Added "Entity Immutability Policy" section under JPA Configuration
- **database.md**: Added `updated_by` to invitation/staff tables + indexes

### 2025-12-21: Exception hierarchy refactoring + Phase 2B continuation (4/6 completed)

**Exception hierarchy refactoring:**
- **Created `BaseException` abstract class**:
  - Central exception with `errorCode` and `message` fields
  - Extends `RuntimeException`
  - Provides foundation for all domain exceptions
- **Migrated all domain exceptions to sealed classes**:
  - Changed from: `abstract class DomainException(...) : RuntimeException(message)`
  - Changed to: `sealed class DomainException(...) : BaseException(errorCode, message)`
  - Affected: `UserException`, `AuthException`, `EquestrianCenterException`, `EquestrianCenterInvitationException`
- **Benefits**:
  - Exhaustive type checking at compile-time (sealed classes)
  - Centralized errorCode management via BaseException
  - Type-safe error handling in when expressions
  - Zero runtime overhead (compile-time only)
- **Files modified**:
  - `common/domain/exception/BaseException.kt` (created)
  - `user/domain/exception/UserException.kt` (sealed + BaseException)
  - `auth/domain/exception/AuthException.kt` (sealed + BaseException)
  - `equestriancenter/domain/exception/EquestrianCenterException.kt` (sealed + BaseException)
  - `equestriancenter/invitation/domain/exception/EquestrianCenterInvitationException.kt` (sealed + BaseException)

**Phase 2B continuation - 4/6 invitation endpoints completed:**

**Implemented endpoints:**
1. GET /api/v1/equestrian-centers/{centerUuid}/invitations (sent invitations list)
2. DELETE /api/v1/equestrian-centers/{centerUuid}/invitations/{invitationUuid} (withdraw invitation)
3. GET /api/v1/users/{userUuid}/equestrian-center-invitations (received invitations list)

**Remaining endpoints:**
- POST /api/v1/users/{userUuid}/equestrian-center-invitations/{invitationUuid}/approve
- POST /api/v1/users/{userUuid}/equestrian-center-invitations/{invitationUuid}/reject

**Key implementation details:**

**GET sent invitations (center perspective):**
- UseCase: `GetEquestrianCenterInvitationsUseCase`
- Service: `GetEquestrianCenterInvitationsService`
- DTOs: `EquestrianCenterInvitationDetail`, `EquestrianCenterInvitationListResponse`, `InvitedUserResponse`
- Authorization: Representative only
- Filtering: Optional status parameter (INVITED, APPROVED, REJECTED, EXPIRED, WITHDRAWN)
- Pagination: Default 20 per page, sorted by invitedAt DESC
- N+1 Prevention: Batch fetch users with `findAllByIdIn()` + `associateBy()`
- Response: invitationUuid, invitedUser{uuid, nickname}, invitationStatus, invitedAt, expiresAt, respondedAt

**DELETE withdraw invitation:**
- UseCase: `WithdrawEquestrianCenterInvitationUseCase`
- Service: `WithdrawEquestrianCenterInvitationService`
- Authorization: Representative only
- Validation:
  1. Center exists & not deleted
  2. Requester is representative
  3. Invitation exists & not deleted
  4. Invitation belongs to this center
  5. Only INVITED status can be withdrawn
- Business logic: INVITED → WITHDRAWN with respondedAt timestamp
- Response: 204 No Content
- Exception: `InvalidInvitationStatusException` for non-INVITED withdrawals

**GET received invitations (user perspective):**
- UseCase: `GetUserEquestrianCenterInvitationsUseCase`
- Service: `GetUserEquestrianCenterInvitationsService`
- DTOs: `UserEquestrianCenterInvitationDetail`, `UserEquestrianCenterInvitationListResponse`, `InvitingEquestrianCenterResponse`
- Authorization: 본인만 조회 가능 (self-only access)
- API Path Pattern: `/api/v1/users/{userUuid}/*` instead of `/api/v1/my/*`
  - Rationale: Consistency with future GET /api/v1/users/{userUuid} endpoint
- Validation:
  1. User lookup by UUID
  2. Verify requestingUserId matches user.id
  3. Throw `UnauthorizedUserOperationException` (403) if mismatch
- Filtering: Optional status parameter
- N+1 Prevention: Batch fetch equestrian centers with empty list check
- Response: invitationUuid, equestrianCenter{uuid, name}, invitationStatus, invitedAt, expiresAt, respondedAt

**Naming convention enforcement:**
- **CRITICAL**: Changed all `status` fields to `invitationStatus` for full naming compliance
- Applied to all DTOs: EquestrianCenterInvitationDetail, UserEquestrianCenterInvitationDetail, response DTOs
- Swagger descriptions updated with proper Korean labels

**Code quality improvements:**
- Empty list check optimization: `if (ids.isEmpty()) emptyMap() else repository.findAllByIdIn(ids)`
- Prevents unnecessary database queries when page is empty
- Applied to both user and center batch fetching

**Files created:**
- UseCases: `GetEquestrianCenterInvitationsUseCase`, `WithdrawEquestrianCenterInvitationUseCase`, `GetUserEquestrianCenterInvitationsUseCase`
- Services: `GetEquestrianCenterInvitationsService`, `WithdrawEquestrianCenterInvitationService`, `GetUserEquestrianCenterInvitationsService`
- DTOs (application/port/in/dto):
  - `EquestrianCenterInvitationDetail` (moved from root to dto/)
  - `InvitedUserResponse`
  - `UserEquestrianCenterInvitationDetail`
  - `InvitingEquestrianCenterResponse`
- Response DTOs (adapter/in/web):
  - `EquestrianCenterInvitationListResponse`
  - `UserEquestrianCenterInvitationListResponse`
- Controllers:
  - `EquestrianCenterInvitationController` (center endpoints)
  - `UserEquestrianCenterInvitationController` (user endpoints, new file)
- Exceptions:
  - `InvalidInvitationStatusException` (cannot withdraw non-INVITED)
  - `UnauthorizedUserOperationException` (user self-access check)
- Repository methods:
  - `EquestrianCenterInvitationRepository.findByEquestrianCenterIdAndOptionalStatus()`
  - `EquestrianCenterInvitationRepository.findByUserIdAndOptionalStatus()`
  - `EquestrianCenterRepository.findAllByIdIn()` (batch fetch)
  - `UserRepository.findAllByIdIn()` (batch fetch)
- GlobalExceptionHandler: Added `UnauthorizedUserOperationException` → 403 FORBIDDEN

**Architecture compliance:**
- Full hexagonal architecture maintained
- DTOs properly organized in dto/ subdirectory
- Batch fetching prevents N+1 queries
- Authorization at service layer
- Stateless immutable entities with .copy() updates

**Documentation updated:**
- **CLAUDE.md**:
  - Current Implementation Status: Phase 2B progress (4/6 endpoints)
  - Coding Standards: Added Exception Hierarchy section
  - Development Log: This entry (2025-12-21)

### 2025-12-21: Filter exception handling refactoring - Spring Security standard pattern

**Problem identified:**
- Each filter had duplicate try-catch blocks for exception handling
- AuthenticationFilter and AdministratorAuthorizationFilter manually called EntryPoint/Handler
- Code duplication and maintenance burden
- Violation of Single Responsibility Principle

**Solution - Centralized exception handling filter:**
- **Created `AuthExceptionHandlerFilter`**:
  - Wraps entire filter chain in try-catch
  - Catches `AuthenticationException` → delegates to `CustomAuthenticationEntryPoint` (401)
  - Catches `AccessDeniedException` → delegates to `CustomAccessDeniedHandler` (403)
  - Positioned before all auth filters in the chain
- **Created `CustomAuthenticationEntryPoint`**:
  - Implements Spring Security's `AuthenticationEntryPoint` interface
  - Returns 401 UNAUTHORIZED with JSON error response
  - Error code: "AUTHENTICATION_FAILED"
- **Created `CustomAccessDeniedHandler`**:
  - Implements Spring Security's `AccessDeniedHandler` interface
  - Returns 403 FORBIDDEN with JSON error response
  - Error code: "INSUFFICIENT_PERMISSIONS"

**Filter refactoring:**
- **AuthenticationFilter**:
  - Removed try-catch blocks and EntryPoint injection
  - Now only throws `SimpleAuthenticationException` (private inner class extending `AuthenticationException`)
  - Added cause propagation to `SimpleAuthenticationException` for debugging
  - Clean separation: authentication logic only, no exception handling
- **AdministratorAuthorizationFilter**:
  - Removed try-catch blocks and Handler injection
  - Now only throws Spring Security's `AccessDeniedException`
  - Clean separation: authorization logic only, no exception handling

**SecurityConfig updates:**
- Added `AuthExceptionHandlerFilter` to filter chain
- Filter registration order fixed:
  1. `authenticationFilter` (first registered to establish order)
  2. `authExceptionHandlerFilter` (positioned before AuthenticationFilter)
  3. `administratorAuthorizationFilter` (after AuthenticationFilter)
- Final execution order: AuthExceptionHandlerFilter → AuthenticationFilter → AdministratorAuthorizationFilter
- `.exceptionHandling()` configuration retained for Spring Security's built-in filters (defensive coding)

**Code quality improvements:**
- Removed 40+ lines of duplicated exception handling code
- Single Responsibility: Each filter handles only its business logic
- DRY principle: Exception handling centralized in one place
- Extensibility: New auth filters automatically get exception handling
- Debugging: Exception causes preserved via `SimpleAuthenticationException(message, cause)`

**Technical details:**
- `SimpleAuthenticationException` as private inner class (not exposed outside AuthenticationFilter)
- Cause parameter added: `SimpleAuthenticationException(message: String, cause: Throwable? = null)`
- Applied to UUID parsing and session validation failures
- Filter chain DSL formatting improved (line break after `.exceptionHandling()`)

**Files created:**
- `AuthExceptionHandlerFilter.kt` - Central exception handling filter
- `CustomAuthenticationEntryPoint.kt` - 401 response handler
- `CustomAccessDeniedHandler.kt` - 403 response handler

**Files modified:**
- `AuthenticationFilter.kt` - Removed try-catch, added cause to SimpleAuthenticationException
- `AdministratorAuthorizationFilter.kt` - Removed try-catch and Handler injection
- `SecurityConfig.kt` - Added AuthExceptionHandlerFilter, fixed filter registration order

**Benefits:**
- **Maintainability**: Single place to modify exception handling behavior
- **Consistency**: All auth filters follow same exception handling pattern
- **Testability**: Exception handling logic isolated and testable
- **Performance**: No overhead (exception handling happens only on errors)
- **Standards compliance**: Follows Spring Security's ExceptionTranslationFilter pattern

**Documentation updated:**
- **CLAUDE.md**: This Development Log entry

### 2026-01-01: Phase 2 completion verification and documentation update

**Phase 2B: Invitation System completion (6/6 endpoints):**
- All invitation endpoints implemented and verified:
  - Create, list, withdraw invitations (center perspective)
  - List received invitations, approve, reject (user perspective)
- Key features:
  - ApproveEquestrianCenterInvitationService creates EquestrianCenterStaff record on approval
  - RejectEquestrianCenterInvitationService handles rejection with respondedAt tracking
  - Expiration validation (7 days), duplicate response prevention
  - Authorization:본인만 승인/거절 가능 (self-only access)

**Phase 2C: Staff Management completion (4/4 endpoints):**
- All staff management endpoints implemented and verified:
  - GET /api/v1/equestrian-centers/{centerUuid}/staff (list staff, paginated)
  - DELETE /{centerUuid}/staff/{staffUuid}/expel (representative only, sets EXPELLED)
  - DELETE /{centerUuid}/staff/{staffUuid}/leave (self only, sets LEFT_VOLUNTARILY)
  - GET /api/v1/users/{userUuid}/equestrian-center-staff-affiliations (my centers)
- Employment history tracking: left_at, left_by, left_reason columns
- N+1 prevention with batch fetching for users and equestrian centers
- Re-join creates new staff record (preserves employment history)

**Documentation synchronized:**
- Updated CLAUDE.md Current Implementation Status:
  - Phase 2B: "IN PROGRESS (4/6)" → "COMPLETED (6/6)"
  - Phase 2C: "NOT IMPLEMENTED" → "COMPLETED (4/4)"
- Updated Next Steps: Removed completed Phase 2 items, strikethrough formatting
- Verified all endpoints exist: UseCases, Services, Controllers, DTOs
- Phase 1-2 fully completed, Phase 3 (Season + Enrollment) is next priority

### 2026-01-05: Phase 3 completion - SeasonEnrollment + Ticket domain implementation

**Phase 3: SeasonEnrollment completion (4/4 endpoints):**
- All enrollment endpoints implemented and verified:
  - POST /api/v1/equestrian-centers/{centerUuid}/seasons/{seasonUuid}/enrollments (참여 신청)
  - GET /{centerUuid}/seasons/{seasonUuid}/enrollments (신청 목록 조회, 직원용, 페이징/필터링)
  - POST /{centerUuid}/seasons/{seasonUuid}/enrollments/{enrollmentUuid}/approve (승인, 직원용)
  - POST /{centerUuid}/seasons/{seasonUuid}/enrollments/{enrollmentUuid}/reject (거절, 직원용)

**Approve API implementation (12-step logic):**
1. 승마장 존재 확인
2. 시즌 조회
3. 시즌-승마장 매칭 검증
4. Staff 권한 확인
5. SeasonEnrollment 조회
6. Enrollment-시즌 매칭 검증
7. PENDING 상태 확인
8. **시즌 정원 체크** (countBySeasonIdAndStatus 사용)
9. PENDING → APPROVED 전환
10. SeasonEnrollmentLog 생성 (APPROVED)
11. **SeasonTicketAccount 생성** (balance = season.defaultTicketCount)
12. **TicketLog 생성** (GRANT, grantedBy = staff.id)

**Reject API implementation (9-step logic):**
1-7. Approve와 동일한 검증
8. PENDING → REJECTED 전환
9. SeasonEnrollmentLog 생성 (REJECTED, note 포함)

**SeasonTicketAccount domain + persistence layer:**
- Domain: `SeasonTicketAccount.kt` (id, seasonId, memberId, balance, createdAt, updatedAt)
- Entity: `SeasonTicketAccountEntity.kt` with UNIQUE(season_id, member_id)
- Mapper: `SeasonTicketAccountMapper.kt`
- JpaRepository: `SeasonTicketAccountJpaRepository.kt`
  - findBySeasonIdAndMemberId, existsBySeasonIdAndMemberId
- RepositoryAdapter: `SeasonTicketAccountRepositoryAdapter.kt`
- Exceptions: `SeasonTicketAccountException` (sealed), DuplicateTicketAccountException, TicketAccountNotFoundException

**TicketLog domain + persistence layer:**
- Domain: `TicketLog.kt` (id, accountId, amount, type, description, reservationId, grantedBy, createdAt)
- TicketLogType enum: GRANT, USE, REFUND, ADDITIONAL
- Entity: `TicketLogEntity.kt` with @Enumerated(EnumType.STRING)
- Mapper: `TicketLogMapper.kt`
- JpaRepository: `TicketLogJpaRepository.kt` (basic save only, query methods deferred to Phase 4)
- RepositoryAdapter: `TicketLogRepositoryAdapter.kt`
- Exceptions: `TicketLogException` (sealed), TicketLogNotFoundException

**GlobalExceptionHandler updates:**
- Added `SeasonTicketAccountException` handler → 400 BAD_REQUEST
- Added `TicketLogException` handler → 400 BAD_REQUEST
- DataIntegrityViolationException: Added season_ticket_account duplicate check → 409 CONFLICT

**Key technical decisions:**
- **No pessimistic locking**: Low-concurrency staff operations don't need database locking
- **No note field for approval**: Approval process doesn't require notes (only rejection does)
- **Repository method naming**: `countBySeasonIdAndStatus` properly implemented across all layers
- **Audit tracking**: `grantedBy` references equestrian_center_staff.id, `actorId` references user.id

**Code quality:**
- Multiple review cycles ensuring 100% accuracy
- All Repository methods verified (countBySeasonIdAndStatus, findBySeasonIdAndMemberId, etc.)
- Full hexagonal architecture compliance
- Entity immutability maintained (all fields `val`)
- Sealed class exception hierarchy
- Full naming convention compliance

**Files created (20 files):**
- SeasonTicketAccount: Domain, Exception (3), Repository (port), Entity, Mapper, JpaRepository, RepositoryAdapter
- TicketLog: Domain, TicketLogType enum, Exception (2), Repository (port), Entity, Mapper, JpaRepository, RepositoryAdapter
- Enrollment APIs: ApproveSeasonEnrollmentUseCase, ApproveSeasonEnrollmentService, RejectSeasonEnrollmentUseCase, RejectSeasonEnrollmentService, RejectSeasonEnrollmentRequest DTO

**Documentation updated:**
- CLAUDE.md Current Implementation Status:
  - Added SeasonEnrollment (4/4 endpoints) to Completed section
  - Added SeasonTicketAccount & TicketLog to Completed section
  - Updated "Not Implemented Yet" to reflect Phase 4 remaining work
  - Updated Next Steps: 8, 9 marked complete, added step 10
- Phase 3 (Season + Enrollment) fully completed, ready for Phase 4 (Ticket APIs)



---

<a id="rbac-design"></a>

# RBAC Design (Phase 2 - Not Yet Implemented)

**Extracted from MODULAR_MONOLITH_RBAC_DESIGN.md for token optimization on 2026-01-06**

This section will be implemented in Phase 2. Currently archived for reference.

---

## 3. RBAC Design

### 3.1 Role Definition

```kotlin
// equestriancenter/staff/domain/StaffRole.kt
enum class StaffRole {
    INSTRUCTOR,  // 강사 - 출석 체크만
    MANAGER,     // 매니저 - 시즌/레슨/티켓 관리
    ADMIN,       // 관리자 - 모든 권한
}
```

**Role Permissions:**

| Role | Permission Description |
|------|----------------------|
| **INSTRUCTOR** | Attendance check only |
| **MANAGER** | Season, lesson, ticket, enrollment management |
| **ADMIN** | All permissions + staff management (except representative) |

**Representative:**
- Managed via `equestrian_center.representative_user_id`
- Special permissions regardless of role (invitation/expulsion)
- Post-MVP: Representative also has role (legal representative vs functional permissions separation)

### 3.2 Permission Definition

```kotlin
// equestriancenter/staff/domain/StaffPermission.kt
object StaffPermissions {
    enum class Permission {
        // Season
        CREATE_SEASON,
        UPDATE_SEASON,
        CLOSE_SEASON,

        // Enrollment
        APPROVE_ENROLLMENT,
        REJECT_ENROLLMENT,

        // Ticket
        GRANT_ADDITIONAL_TICKET,
        VIEW_TICKET_ACCOUNT,
        VIEW_TICKET_LOG,

        // Lesson
        CREATE_LESSON,
        UPDATE_LESSON,
        CANCEL_LESSON,
        ASSIGN_INSTRUCTOR,

        // Attendance
        CHECK_ATTENDANCE,

        // Staff (Admin only)
        MANAGE_STAFF,
    }

    private val rolePermissions = mapOf(
        StaffRole.INSTRUCTOR to setOf(
            Permission.CHECK_ATTENDANCE,
        ),
        StaffRole.MANAGER to setOf(
            Permission.CREATE_SEASON,
            Permission.UPDATE_SEASON,
            Permission.CLOSE_SEASON,
            Permission.APPROVE_ENROLLMENT,
            Permission.REJECT_ENROLLMENT,
            Permission.GRANT_ADDITIONAL_TICKET,
            Permission.VIEW_TICKET_ACCOUNT,
            Permission.VIEW_TICKET_LOG,
            Permission.CREATE_LESSON,
            Permission.UPDATE_LESSON,
            Permission.CANCEL_LESSON,
            Permission.ASSIGN_INSTRUCTOR,
            Permission.CHECK_ATTENDANCE,
        ),
        StaffRole.ADMIN to Permission.values().toSet(), // 모든 권한
    )

    fun hasPermission(role: StaffRole, permission: Permission): Boolean =
        rolePermissions[role]?.contains(permission) ?: false

    fun getPermissions(role: StaffRole): Set<Permission> =
        rolePermissions[role] ?: emptySet()
}
```

### 3.3 API Permission Mapping

#### Season APIs

| Endpoint | Method | Permission | Role |
|----------|--------|------------|------|
| `/seasons` | POST | CREATE_SEASON | MANAGER, ADMIN |
| `/seasons/{id}` | PATCH | UPDATE_SEASON | MANAGER, ADMIN |
| `/seasons/{id}/close` | PATCH | CLOSE_SEASON | MANAGER, ADMIN |
| `/seasons` | GET | - | Public |
| `/seasons/{id}` | GET | - | Public |

#### Enrollment APIs

| Endpoint | Method | Permission | Role |
|----------|--------|------------|------|
| `/enrollments` | POST | - | Any authenticated user |
| `/enrollments` | GET | - | Staff (any role) |
| `/enrollments/{id}/approve` | POST | APPROVE_ENROLLMENT | MANAGER, ADMIN |
| `/enrollments/{id}/reject` | POST | REJECT_ENROLLMENT | MANAGER, ADMIN |

#### Ticket APIs (Phase 4)

| Endpoint | Method | Permission | Role |
|----------|--------|------------|------|
| `/tickets/grant` | POST | GRANT_ADDITIONAL_TICKET | MANAGER, ADMIN |
| `/tickets/balance` | GET | - | Member (self-only) |
| `/tickets/logs` | GET | - | Member (self-only) |

#### Lesson APIs (Phase 5)

| Endpoint | Method | Permission | Role |
|----------|--------|------------|------|
| `/lessons` | POST | CREATE_LESSON | MANAGER, ADMIN |
| `/lessons/{id}` | PATCH | UPDATE_LESSON | MANAGER, ADMIN |
| `/lessons/{id}/cancel` | PATCH | CANCEL_LESSON | MANAGER, ADMIN |
| `/lessons/{id}/instructors` | POST | ASSIGN_INSTRUCTOR | MANAGER, ADMIN |

#### Attendance APIs (Phase 6)

| Endpoint | Method | Permission | Role |
|----------|--------|------------|------|
| `/attendance` | POST | CHECK_ATTENDANCE | All staff |

#### Staff Management (Representative only)

| Endpoint | Method | Permission | Role |
|----------|--------|------------|------|
| `/staff/{id}/expel` | DELETE | - | Representative only |
| `/invitations` | POST | - | Representative only |

### 3.4 Database Schema Changes

**Modify equestrian_center_staff table:**

```sql
-- role 컬럼 추가
ALTER TABLE equestrian_center_staff
ADD COLUMN role VARCHAR(50) NOT NULL DEFAULT 'INSTRUCTOR';

-- 또는 PostgreSQL ENUM 사용
CREATE TYPE staff_role AS ENUM ('INSTRUCTOR', 'MANAGER', 'ADMIN');

ALTER TABLE equestrian_center_staff
ADD COLUMN role staff_role NOT NULL DEFAULT 'INSTRUCTOR';

-- 기존 직원들을 MANAGER로 업그레이드
UPDATE equestrian_center_staff
SET role = 'MANAGER'
WHERE left_at IS NULL;

-- 인덱스 추가
CREATE INDEX idx_staff_role ON equestrian_center_staff(role);
```

### 3.5 Domain Model Changes

**Before:**
```kotlin
// equestriancenter/staff/domain/EquestrianCenterStaff.kt
data class EquestrianCenterStaff(
    val id: Long,
    val uuid: UUID,
    val equestrianCenterId: Long,
    val userId: Long,
    val joinedAt: Instant,
    val leftAt: Instant?,
    val leftBy: Long?,
    val leftReason: MemberLeftReason?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val updatedBy: Long,
)
```

**After:**
```kotlin
// equestriancenter/staff/domain/EquestrianCenterStaff.kt
data class EquestrianCenterStaff(
    val id: Long,
    val uuid: UUID,
    val equestrianCenterId: Long,
    val userId: Long,
    val role: StaffRole,  // 추가
    val joinedAt: Instant,
    val leftAt: Instant?,
    val leftBy: Long?,
    val leftReason: MemberLeftReason?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val updatedBy: Long,
)
```

**Entity 변경:**
```kotlin
// equestriancenter/staff/adapter/out/persistence/EquestrianCenterStaffEntity.kt
@Entity
@Table(name = "equestrian_center_staff")
class EquestrianCenterStaffEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val uuid: UUID,

    @Column(name = "equestrian_center_id", nullable = false)
    val equestrianCenterId: Long,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Enumerated(EnumType.STRING)  // 추가
    @Column(nullable = false)
    val role: StaffRole,

    @Column(name = "joined_at", nullable = false)
    val joinedAt: Instant,

    // ... 나머지 동일
)
```

### 3.6 Service Layer Permission Check

**Before (MVP):**
```kotlin
// season/enrollment/application/service/ApproveSeasonEnrollmentService.kt
override fun approveSeasonEnrollment(...) {
    // 4. Staff 권한 확인
    val staff = equestrianCenterStaffRepository
        .findActiveByEquestrianCenterIdAndUserId(...)
        ?: throw UnauthorizedSeasonEnrollmentOperationException()

    // 바로 승인 로직 진행...
}
```

**After (RBAC):**
```kotlin
// season/enrollment/application/service/ApproveSeasonEnrollmentService.kt
override fun approveSeasonEnrollment(...) {
    // 4. Staff 권한 확인
    val staff = equestrianCenterStaffRepository
        .findActiveByEquestrianCenterIdAndUserId(...)
        ?: throw UnauthorizedSeasonEnrollmentOperationException()

    // 4-1. RBAC 권한 체크 추가
    if (!StaffPermissions.hasPermission(staff.role, Permission.APPROVE_ENROLLMENT)) {
        throw InsufficientPermissionsException(
            "MANAGER 이상의 권한이 필요합니다."
        )
    }

    // 승인 로직 진행...
}
```

### 3.7 Exception Handling

**Add new exception:**
```kotlin
// common/domain/exception/InsufficientPermissionsException.kt
class InsufficientPermissionsException(
    message: String = "권한이 부족합니다",
) : BaseException(
    errorCode = "INSUFFICIENT_PERMISSIONS",
    message = message,
)
```

**GlobalExceptionHandler 추가:**
```kotlin
// common/adapter/in/web/GlobalExceptionHandler.kt
@ExceptionHandler(InsufficientPermissionsException::class)
fun handleInsufficientPermissions(e: InsufficientPermissionsException): ResponseEntity<ErrorResponse> {
    val errorResponse = ErrorResponse(
        code = e.errorCode,
        message = e.message ?: "권한이 부족합니다",
    )
    return ResponseEntity
        .status(HttpStatus.FORBIDDEN)
        .body(errorResponse)
}
```

---



---

<a id="migration-plan"></a>

# Migration Plan (Detailed Implementation Steps)

**Extracted from MODULAR_MONOLITH_RBAC_DESIGN.md for token optimization on 2026-01-06**

Detailed step-by-step migration plan for modular monolith architecture.

---

## 4. Migration Plan

### 4.1 Phase 1: Gradle Multi-Module Structure Migration

**Step 1: settings.gradle.kts 생성**
```kotlin
rootProject.name = "maldallija-be"

include(
    "common",
    "auth",
    "user",
    "equestriancenter",
    "season",
    "ticket",
    "lesson",
    "reservation",
    "app"
)
```

**Step 2: Root build.gradle.kts 공통 설정**
```kotlin
plugins {
    kotlin("jvm") version "1.9.25" apply false
    kotlin("plugin.spring") version "1.9.25" apply false
    kotlin("plugin.jpa") version "1.9.25" apply false
    id("org.springframework.boot") version "3.5.8" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
    }

    dependencies {
        // 공통 의존성
    }
}
```

**Step 3: 각 모듈 build.gradle.kts 생성**

**common/build.gradle.kts:**
```kotlin
plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}
```

**equestriancenter/build.gradle.kts:**
```kotlin
plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":user"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
}
```

**app/build.gradle.kts:**
```kotlin
plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":auth"))
    implementation(project(":user"))
    implementation(project(":equestriancenter"))
    implementation(project(":season"))
    implementation(project(":ticket"))
    implementation(project(":lesson"))
    implementation(project(":reservation"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.postgresql:postgresql")
}
```

**Step 4: 패키지 이동**
- 현재 `src/main/kotlin/dev/maldallija/maldallijabe/auth/`
- 이동 → `auth/src/main/kotlin/dev/maldallija/maldallijabe/auth/`

**Step 5: Import 경로 수정**
- 같은 모듈 내: 상대 경로
- 다른 모듈: 절대 경로

**Step 6: 빌드 테스트**
```bash
./gradlew clean build
./gradlew :app:bootRun
```

### 4.2 Phase 2: RBAC Implementation

**Step 1: DB 마이그레이션**
```sql
-- role 컬럼 추가
ALTER TABLE equestrian_center_staff
ADD COLUMN role VARCHAR(50) NOT NULL DEFAULT 'INSTRUCTOR';

-- 기존 직원 MANAGER로 업그레이드
UPDATE equestrian_center_staff
SET role = 'MANAGER'
WHERE left_at IS NULL;
```

**Step 2: 도메인 모델 수정**
1. `StaffRole.kt` enum 생성
2. `EquestrianCenterStaff.kt`에 `role` 필드 추가
3. `EquestrianCenterStaffEntity.kt`에 `role` 필드 추가
4. `EquestrianCenterStaffMapper.kt` 수정

**Step 3: StaffPermissions 구현**
1. `Permission` enum 정의
2. `rolePermissions` 매핑 구현
3. `hasPermission()` 메서드 구현

**Step 4: Service 레이어 권한 체크 추가**

수정 대상 Service (10개):
1. `CreateSeasonService`
2. `UpdateSeasonService`
3. `CloseSeasonService`
4. `ApproveSeasonEnrollmentService`
5. `RejectSeasonEnrollmentService`
6. `GrantAdditionalTicketService` (Phase 4)
7. `CreateLessonService` (Phase 5)
8. `UpdateLessonService` (Phase 5)
9. `CancelLessonService` (Phase 5)
10. `CheckAttendanceService` (Phase 6)

**Step 5: 예외 처리 추가**
1. `InsufficientPermissionsException` 생성
2. `GlobalExceptionHandler`에 핸들러 추가

**Step 6: 테스트**
- 각 Role별 권한 테스트
- 권한 없는 경우 403 반환 확인

### 4.3 Phase 3: Documentation Update

**Update targets:**
1. `CLAUDE.md` - Implementation Status
2. `database.md` - equestrian_center_staff table
3. `MEMO2.md` - Role definitions
4. `README.md` - Multi-Module structure explanation

---

