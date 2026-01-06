# Project Roadmap & Feature Planning

This document defines role-based features, development phases, and future enhancements.

---

## Table of Contents

1. [Naming Convention](#naming-convention)
2. [Role-Based Features](#role-based-features)
3. [Development Phases](#development-phases)
4. [Future Feature Ideas](#future-feature-ideas)
5. [Architecture Improvements](#architecture-improvements)

---

<a id="naming-convention"></a>

## Naming Convention

- **Admin**: System operator (`user.is_system_admin = true`)
- **Representative**: Equestrian center legal/business owner (`equestrian_center.representative_user_id`)
- **Staff**: Equestrian center employees (instructors, managers, etc. via `equestrian_center_staff`)
- **Member**: Season participant (student via `season_enrollment`)
- **User**: General registered user

---

<a id="role-based-features"></a>

## Role-Based Features

### System Admin
- Distinguished by `user.is_system_admin = true` flag
- Create equestrian centers
- Designate representative users
- View all users
- View all equestrian centers/seasons/lessons/reservations
- (Additional system-level features TBD)

### Equestrian Center Representative
- User where `equestrian_center.representative_user_id` matches `user.id`
- Update equestrian center information
- Invite/expel staff members
- Configure staff list visibility (public vs staff-only) - Post-MVP
- MVP: Same permissions as regular staff (no role-based restrictions)
- Post-MVP: Representative is legal owner, functional permissions separated by role

### Equestrian Center Staff
- User with record in `equestrian_center_staff` table
- MVP: All staff have equal permissions (instructor role only, no role differentiation)
- Post-MVP: Role differentiation (INSTRUCTOR, MANAGER, ADMIN)
- **Key Features**:
  - Create/update/close seasons (start~end date, capacity, default ticket count) - center level
  - Approve/reject season enrollment applications
  - Grant default/additional tickets
  - Create/update/cancel lessons (date, time, capacity, riding location, assign staff)
    - Lesson time: 1-hour units (2-hour lesson = 2 tickets)
    - Multiple lessons allowed at same time slot within a season
    - 1+ staff (instructors) can be assigned to a lesson
  - Refund all tickets when lesson is cancelled
  - Check attendance (mark ATTENDED/NO_SHOW, record in `lesson_attendance`)
- View reservation list for lessons in own center(s)
- One user can be staff member at multiple centers

### Season Member (Student)
- User with APPROVED record in `season_enrollment`
- Apply for season participation
- View lesson list in approved seasons (all staff lessons)
- Book lessons (deducted from season ticket account, calculated by lesson duration)
- Cancel reservations:
  - D-3 or earlier: Ticket refunded
  - D-2 onwards: No refund
- View own reservation history
- View own ticket balance/transaction log (per season, based on `season_ticket_account`)

### General User
- Registered user who is neither staff nor season member
- Sign up/Sign in
- View public season list

---

<a id="development-phases"></a>

## Development Phases

### Phase 1: Authentication & Authorization ✅ COMPLETED
1. **User** - Registration with `is_system_admin` flag ✅
2. ~~**Token** - Opaque token issuance/validation/deletion, login/logout~~ ✅ IMPLEMENTED
   - **AuthenticationAccessSession** (1 hour) + **AuthenticationRefreshSession** (30 days)
   - Login/Logout/Session refresh implemented
   - Rotating refresh token pattern (invalidates old sessions on SESSION_REFRESH)
   - Single device policy (invalidates all sessions on NEW_SIGN_IN)
   - HttpOnly cookie transmission, AuthenticationFilter implemented
   - Session revocation tracking: revoked_at, revoked_reason (NEW_SIGN_IN/SIGN_OUT/SESSION_REFRESH)

### Phase 2: Equestrian Center (MVP)

#### Phase 2A: EquestrianCenter CRUD ✅ COMPLETED
3. **EquestrianCenter** - CRUD, representative user designation
   - CREATE: POST /api/v1/administration/equestrian-centers (System Admin only)
   - READ List: GET /api/v1/equestrian-centers (public, paginated)
   - READ Detail: GET /api/v1/equestrian-centers/{uuid} (public)
   - UPDATE: PATCH /api/v1/equestrian-centers/{uuid} (representative only, name/description)
   - DELETE: Deferred (soft delete via deleted_at)
   - Renamed: leader → representative (11 files)

#### Phase 2B: Invitation System ✅ COMPLETED
4. **EquestrianCenterInvitation** - Invitation system (log-style table)
   - POST /api/v1/equestrian-centers/{centerUuid}/invitations (send invitation, representative only)
   - GET /api/v1/equestrian-centers/{centerUuid}/invitations (list sent invitations)
   - DELETE /api/v1/equestrian-centers/{centerUuid}/invitations/{invitationUuid} (withdraw)
   - GET /api/v1/users/{userUuid}/equestrian-center-invitations (received invitations)
   - POST /api/v1/users/{userUuid}/equestrian-center-invitations/{invitationUuid}/approve
   - POST /api/v1/users/{userUuid}/equestrian-center-invitations/{invitationUuid}/reject
   - Status: INVITED → APPROVED/REJECTED/EXPIRED/WITHDRAWN
   - 7-day expiration (checked at query time, no batch job)
   - Re-invitation policy: Allowed after REJECTED/EXPIRED/WITHDRAWN, forbidden if INVITED exists

#### Phase 2C: Staff Management ✅ COMPLETED
5. **EquestrianCenterStaff** - Staff management (join/leave history tracking)
   - GET /api/v1/equestrian-centers/{centerUuid}/staff (list staff, paginated)
   - DELETE /api/v1/equestrian-centers/{centerUuid}/staff/{staffUuid}/expel (representative only)
   - DELETE /api/v1/equestrian-centers/{centerUuid}/staff/{staffUuid}/leave (self only)
   - GET /api/v1/users/{userUuid}/equestrian-center-staff-affiliations (my centers as staff)
   - Tracks: joined_at, left_at, left_by, left_reason
   - Leave reasons: LEFT_VOLUNTARILY, EXPELLED
   - Re-join creates new record (preserves employment history)
   - N+1 prevention: batch fetching implemented

6. MVP: All center staff have equal permissions (role/permission system is Post-MVP)
   - Post-MVP: Add role column (INSTRUCTOR, MANAGER, ADMIN)

### Phase 3: Season & Enrollment ✅ COMPLETED
7. **Season** - CRUD, status management, capacity management (center level, created_by = equestrian_center_staff.id)
   - API #1: POST /api/v1/equestrian-centers/{centerUuid}/seasons
   - API #2: GET /api/v1/equestrian-centers/{centerUuid}/seasons (status/date filtering)
   - API #3: GET /api/v1/equestrian-centers/{centerUuid}/seasons/{seasonUuid}
   - API #4: PATCH /api/v1/equestrian-centers/{centerUuid}/seasons/{seasonUuid}
   - API #5: PATCH /api/v1/equestrian-centers/{centerUuid}/seasons/{seasonUuid}/close

8. **SeasonEnrollment** - Apply, approve/reject, withdraw
   - POST /api/v1/equestrian-centers/{centerUuid}/seasons/{seasonUuid}/enrollments
   - GET /api/v1/equestrian-centers/{centerUuid}/seasons/{seasonUuid}/enrollments
   - POST /{centerUuid}/seasons/{seasonUuid}/enrollments/{enrollmentUuid}/approve
   - POST /{centerUuid}/seasons/{seasonUuid}/enrollments/{enrollmentUuid}/reject

9. **SeasonEnrollmentLog** - Enrollment status change history (APPLIED/REAPPLIED/APPROVED/REJECTED/WITHDRAWN)

### Phase 4: Ticket System (Partial ✅, Remaining APIs)
10. **SeasonTicketAccount** - Per-season member ticket account (created on APPROVED) ✅
11. **TicketLog** - Transaction history (GRANT/USE/REFUND/ADDITIONAL), granted_by = equestrian_center_staff.id ✅
    - Remaining APIs:
      - Grant additional tickets (Staff → Member)
      - View ticket balance (Member, self-only)
      - View ticket log (Member, self-only)

### Phase 5: Lesson
12. **Lesson** - CRUD, status management, time validation (created_by = equestrian_center_staff.id)
13. **LessonInstructor** - Lesson-staff assignment (N:M, references equestrian_center_staff.id)

### Phase 6: Reservation & Attendance
14. **Reservation** - Book/cancel, ticket deduction/refund (references season_ticket_account_id)
15. **LessonAttendance** - Attendance check (checked_by = equestrian_center_staff.id, checked_at recorded)

### Phase 7: Admin & Post-MVP
16. **Admin Features** - System admin UI, equestrian center management, global views (TBD)
17. **Permission System** - Role-based permission management (Post-MVP RBAC implementation)

---

<a id="future-feature-ideas"></a>

## Future Feature Ideas

### OAuth2 Social Login
- Google login
- Apple login

### Token Enhancement ✅ IMPLEMENTED
- ~~Split into Access Token (short-lived) + Refresh Token (long-lived)~~
- ~~Current: single opaque token with 1 day expiry~~
- **Implemented**: Dual-session system (authentication_access_session 1h + authentication_refresh_session 30d)
- Rotating refresh token pattern with session revocation tracking

### Notification System
- Season enrollment approval notice
- Reservation confirmation
- Cancellation notice
- Lesson reminder (D-1, etc.)
- Push notification / Email

### Auto Status Transition (Batch)
- Lesson: auto transition to COMPLETED after end time
- Season: auto transition to CLOSED after end date
- Requires Spring @Scheduled or external scheduler

### Riding Center Management
- Instructor can register multiple riding centers
- When creating a Lesson, select from registered centers instead of text input

### Horse Assignment
- Assign specific horse to Lesson or Reservation
- Horse management per Instructor/Center

### Member Level System
- Beginner / Intermediate / Advanced levels
- Level requirement for certain Lessons

### Waitlist
- Queue system when Lesson capacity is full
- Auto-assign when spot opens

### User Account Features
- Password change
- Password reset (email verification)
- Profile edit (name, phone, etc.)
- Instructor profile view (Member can see Instructor info)

### Lesson Date Range Validation (Phase 5)
**Issue**: When updating Season dates, need to validate that all existing Lessons fall within the new date range

**Current State (Phase 3)**:
- Season update allows changing startDate/endDate without checking existing Lessons
- This can cause Lessons to exist outside Season period

**Required Validation (Phase 5)**:
```kotlin
// In UpdateSeasonService or UpdateLessonService
val hasLessonsOutsideRange = lessonRepository.existsBySeasonIdAndDateOutsideRange(
    seasonId = season.id,
    startDate = newStartDate,
    endDate = newEndDate
)
if (hasLessonsOutsideRange) {
    throw CannotUpdateSeasonDateWithExistingLessonsException()
}
```

**Alternative Approach**:
- Allow date update but auto-cancel Lessons outside new range
- Notify affected members

**Decision Required**: Discuss with product team during Phase 5 implementation

### Season Review System (Post-MVP)
**Feature**: Members who participated in a season can write reviews after the season ends

**Authorization**:
- Eligible writers: Members with APPROVED enrollment status (SeasonEnrollment.status = APPROVED)
- Available after: Season ends (Season.status = CLOSED)
- Restriction: One review per member per season

**Data Structure** (Expected):
```sql
CREATE TABLE season_review (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID UNIQUE NOT NULL,
    season_id BIGINT NOT NULL,  -- Which season this review is for
    member_id BIGINT NOT NULL,   -- Review author (member)
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),  -- Rating 1-5 stars
    content TEXT,  -- Review content (optional)
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    deleted_at TIMESTAMPTZ,
    UNIQUE (season_id, member_id) WHERE deleted_at IS NULL  -- One review per member
);
```

**API Design** (Expected):
- POST /api/v1/seasons/{seasonUuid}/reviews - Create review
- GET /api/v1/seasons/{seasonUuid}/reviews - List season reviews
- PATCH /api/v1/seasons/{seasonUuid}/reviews/{reviewUuid} - Update review (author only)
- DELETE /api/v1/seasons/{seasonUuid}/reviews/{reviewUuid} - Delete review (author only)

**Validation Logic**:
1. Season status check: status = CLOSED
2. Author eligibility check: Verify APPROVED status in SeasonEnrollment
3. Duplicate prevention: Check if review already exists

**Display**:
- Season detail page shows average rating + review count
- Review list: Sortable by latest/rating

**Additional Considerations**:
- Staff reply feature (equestrian center representative/staff can respond)
- Report/hide mechanism (for inappropriate reviews)
- Photo reviews (image attachments)

### Additional Recommendations
- Lesson Review/Rating: Member rates Lesson after completion (separate from Season Review)
- Lesson history: Past lessons with attendance records
- Statistics dashboard: Instructor sees booking rate, attendance rate
- Recurring Lesson: Template for weekly repeated lessons
- Multi-language support

### Admin Features (TBD)
- User account deactivation/activation
- Force cancel Season/Lesson
- System monitoring dashboard
- Role assignment to users

### Group Leader Permission Settings
- Group leader can configure permissions for regular instructors
- Configurable permissions:
  - Season CRUD (create/update/delete)
  - Season enrollment approval/rejection
  - Ticket grant (default/additional)
- MVP: All instructors have full permissions
- Post-MVP: Group leader restricts specific permissions per instructor

### Instructor Permission Scope (Post-MVP)
- **Lesson Management**:
  - MVP: All instructors in group can modify/cancel any lesson
  - Post-MVP: Restrict to lesson creator or assigned instructors only
- **Enrollment Approval**:
  - MVP: All instructors can approve/reject season enrollment
  - Post-MVP: Restrict to season creator or group leader only
- These settings will be configurable by group leader in future releases

### Search & Filter
- Lesson search by date, instructor, riding center
- Calendar view for lessons

### API Documentation
- Swagger/OpenAPI integration (springdoc-openapi for MVP)
- Consider Spring REST Docs for production

### Test Coverage
- Target coverage TBD
- Tools: JaCoCo

### Deployment
- AWS (EC2/ECS/EKS TBD)
- Docker containerization
- CI/CD pipeline

---

<a id="architecture-improvements"></a>

## Architecture Improvements (Post-Phase 7)

### Multi-Module Architecture ✅ IN PROGRESS
**Current**: Single module with Hexagonal Architecture
**Future Consideration**: Split into multiple Gradle modules

**Progress**:
- ✅ Phase 1 Step 1: user module separated (2026-01-06)
- ⏸️ Phase 1 Step 2: auth module separation (pending)

**Benefits**:
- Compile-time dependency validation between domains
- Incremental build (faster build times)
- Easier MSA migration (module → microservice)
- Clear layer separation (domain/application/adapter modules)

**When to Consider**:
- After Phase 6-7 completion (all domains implemented)
- When domain boundaries are stable
- When team size grows (2+ developers)
- When actual service launch requires scalability

**Proposed Structure**:
```
maldallija-be/
├── common/
│   ├── common-domain/
│   └── common-util/
├── user-service/
│   ├── user-domain/
│   ├── user-application/
│   └── user-adapter/
├── equestriancenter-service/
└── season-service/
```

**Current Alternative**: ArchUnit tests to enforce package dependencies

### Command Pattern (execute() method)
**Current**: Explicit method names per UseCase
```kotlin
interface SignInUseCase {
    fun signIn(email: String, password: String): SignInResult
}
```

**Alternative**: Unified execute() with Command objects
```kotlin
interface SignInUseCase {
    fun execute(command: SignInCommand): SignInResult
}
data class SignInCommand(val email: String, val password: String)
```

**Benefits of execute() pattern**:
- Consistent interface across all UseCases
- Easier AOP/Decorator application (logging, transaction, validation)
- Command object reusability in tests
- Audit trail (store Command objects for history)

**When to Consider**:
- When implementing CQRS (Command Query Responsibility Segregation)
- When Command Bus is needed
- When audit logging/event sourcing is required
- When team adopts DDD tactical patterns heavily

**Current Approach Rationale**:
- Clean Architecture recommends explicit method names (business language)
- Project size suitable for simple approach
- Most UseCases have 3-5 parameters (Command objects would add boilerplate)
- Faster development speed in early phases

**Hybrid Approach**:
- Keep explicit names for simple UseCases (≤3 parameters)
- Use Command pattern for complex UseCases (≥4 parameters)
- Example: Season creation might benefit from CreateSeasonCommand
