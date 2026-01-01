# Future Feature Ideas

## OAuth2 Social Login
- Google login
- Apple login

## Token Enhancement IMPLEMENTED
- ~~Split into Access Token (short-lived) + Refresh Token (long-lived)~~
- ~~Current: single opaque token with 1 day expiry~~
- **Implemented**: Dual-session system (authentication_access_session 1h + authentication_refresh_session 30d)
- Rotating refresh token pattern with session revocation tracking

## Notification System
- Season enrollment approval notice
- Reservation confirmation
- Cancellation notice
- Lesson reminder (D-1, etc.)
- Push notification / Email

## Auto Status Transition (Batch)
- Lesson: auto transition to COMPLETED after end time
- Season: auto transition to CLOSED after end date
- Requires Spring @Scheduled or external scheduler

## Riding Center Management
- Instructor can register multiple riding centers
- When creating a Lesson, select from registered centers instead of text input

## Horse Assignment
- Assign specific horse to Lesson or Reservation
- Horse management per Instructor/Center

## Member Level System
- Beginner / Intermediate / Advanced levels
- Level requirement for certain Lessons

## Waitlist
- Queue system when Lesson capacity is full
- Auto-assign when spot opens

## User Account Features
- Password change
- Password reset (email verification)
- Profile edit (name, phone, etc.)
- Instructor profile view (Member can see Instructor info)

## Season Review System (Post-MVP)
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

## Additional Recommendations
- Lesson Review/Rating: Member rates Lesson after completion (separate from Season Review)
- Lesson history: Past lessons with attendance records
- Statistics dashboard: Instructor sees booking rate, attendance rate
- Recurring Lesson: Template for weekly repeated lessons
- Multi-language support

## Admin Features (TBD)
- User account deactivation/activation
- Force cancel Season/Lesson
- System monitoring dashboard
- Role assignment to users

## Group Leader Permission Settings
- Group leader can configure permissions for regular instructors
- Configurable permissions:
  - Season CRUD (create/update/delete)
  - Season enrollment approval/rejection
  - Ticket grant (default/additional)
- MVP: All instructors have full permissions
- Post-MVP: Group leader restricts specific permissions per instructor

## Instructor Permission Scope (Post-MVP)
- **Lesson Management**:
  - MVP: All instructors in group can modify/cancel any lesson
  - Post-MVP: Restrict to lesson creator or assigned instructors only
- **Enrollment Approval**:
  - MVP: All instructors can approve/reject season enrollment
  - Post-MVP: Restrict to season creator or group leader only
- These settings will be configurable by group leader in future releases

## Search & Filter
- Lesson search by date, instructor, riding center
- Calendar view for lessons

## API Documentation
- Swagger/OpenAPI integration (springdoc-openapi for MVP)
- Consider Spring REST Docs for production

## Test Coverage
- Target coverage TBD
- Tools: JaCoCo

## Deployment
- AWS (EC2/ECS/EKS TBD)
- Docker containerization
- CI/CD pipeline

## Architecture Improvements (Post-Phase 7)

### Multi-Module Architecture
**Current**: Single module with Hexagonal Architecture
**Future Consideration**: Split into multiple Gradle modules

**Benefits:**
- Compile-time dependency validation between domains
- Incremental build (faster build times)
- Easier MSA migration (module → microservice)
- Clear layer separation (domain/application/adapter modules)

**When to Consider:**
- After Phase 6-7 completion (all domains implemented)
- When domain boundaries are stable
- When team size grows (2+ developers)
- When actual service launch requires scalability

**Proposed Structure:**
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

**Current Alternative:** ArchUnit tests to enforce package dependencies

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

**Benefits of execute() pattern:**
- Consistent interface across all UseCases
- Easier AOP/Decorator application (logging, transaction, validation)
- Command object reusability in tests
- Audit trail (store Command objects for history)

**When to Consider:**
- When implementing CQRS (Command Query Responsibility Segregation)
- When Command Bus is needed
- When audit logging/event sourcing is required
- When team adopts DDD tactical patterns heavily

**Current Approach Rationale:**
- Clean Architecture recommends explicit method names (business language)
- Project size suitable for simple approach
- Most UseCases have 3-5 parameters (Command objects would add boilerplate)
- Faster development speed in early phases

**Hybrid Approach:**
- Keep explicit names for simple UseCases (≤3 parameters)
- Use Command pattern for complex UseCases (≥4 parameters)
- Example: Season creation might benefit from CreateSeasonCommand
