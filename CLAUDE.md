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

### User Roles
- **System Admin**: Has `is_system_admin = true`, manages system-level operations (group creation, etc.)
- **Group Member**: User belongs to InstructorGroup(s)
  - **Group Leader**: Designated leader (instructor_group.leader_user_id), manages group members
  - **Instructor**: Creates seasons/lessons, manages enrollments, checks attendance
  - MVP: All group members have equal permissions (no role-based restrictions)
- **General Member**: Regular user who applies to seasons and books lessons

### Core Entities
- **User**: System account
  - `is_system_admin`: boolean flag for system administrators
  - Can belong to multiple InstructorGroups
- **InstructorGroup**: Group (academy/center)
  - Created by System Admin
  - Has 1 leader (group leader)
  - MVP: All group members have equal instructor permissions
- **InstructorGroupMember**: N:M relationship between User and InstructorGroup
  - Links user to group
  - One user can belong to multiple groups
  - MVP: No role differentiation (Post-MVP: role-based permissions)
- **Season**: Period (start~end date) created at group level
  - `capacity`: season enrollment limit
  - `default_ticket_count`: tickets granted upon enrollment approval
  - `created_by`: tracks which group member created the season
- **SeasonEnrollment**: Member applies to Season, Instructor approves/rejects
  - Status: PENDING → APPROVED / REJECTED / WITHDRAWN
  - Upon approval, Member receives default tickets for that Season
- **SeasonEnrollmentLog**: History of enrollment status changes
  - Tracks APPLIED, REAPPLIED, APPROVED, REJECTED, WITHDRAWN events
  - Records actor (who performed the action) and notes
- **SeasonTicketAccount**: Virtual currency account per (Season, Member)
  - Balance tracked separately for each season
  - Created when enrollment is APPROVED
- **TicketLog**: Transaction history (GRANT/USE/REFUND/ADDITIONAL)
  - `granted_by`: tracks which group member granted tickets
  - Links to season_ticket_account instead of season+member
- **Lesson**: Class within Season
  - Instructor sets: date, time (1-hour unit), capacity, riding center (text)
  - Duration determines ticket cost (e.g., 2-hour lesson = 2 tickets)
  - Multiple Lessons allowed at same time slot within a Season
  - Lesson datetime must be within Season period
  - `created_by`: tracks which group member created the lesson
- **LessonInstructor**: N:M relationship between Lesson and InstructorGroupMember
  - 1+ instructors can be assigned to a lesson
  - References InstructorGroupMember (not User directly)
- **Reservation**: Approved Member books Lesson using Tickets
  - Cancel before D-3: Ticket refunded
  - Cancel from D-2: No refund
  - Links to season_ticket_account for payment tracking
- **LessonAttendance**: Attendance tracking
  - Status: ATTENDED / NO_SHOW
  - `checked_by`: tracks which group member checked attendance
  - `checked_at`: timestamp of attendance check

### Business Rules
- Member must have APPROVED enrollment to book Lessons in that Season
- Member can book multiple Lessons simultaneously
- Instructor can only manage seasons/lessons within their group(s)
- Approved member can book any lesson in the season (regardless of instructor)
- MVP: All group members have equal permissions (no role-based restrictions)
- Group creation process: System Admin creates group → Admin invites instructors → Admin designates leader
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

### Lesson Cancellation by Instructor
- When instructor cancels lesson (SCHEDULED → CANCELLED):
  1. All RESERVED reservations → CANCELLED_BY_INSTRUCTOR
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
├── token
├── instructorgroup
│   └── member                 # InstructorGroupMember subdomain
├── season
│   ├── enrollment             # SeasonEnrollment subdomain
│   ├── enrollmentlog          # SeasonEnrollmentLog subdomain
│   └── ticketaccount          # SeasonTicketAccount subdomain
├── ticketlog
├── lesson
│   ├── instructor             # LessonInstructor subdomain
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

## Related Documents

- `docs/database.md` - DB schema design (13 tables: user, instructor_group, instructor_group_member, token, season, season_enrollment, season_enrollment_log, season_ticket_account, ticket_log, lesson, lesson_instructor, reservation, lesson_attendance)
- `docs/MEMO.md` - Future feature ideas (OAuth2, notifications, batch, role-based permissions, etc.)
- `docs/MEMO2.md` - Role-based feature definitions + Development order (Phase 1~7)

## Current Implementation Status

### Completed
- **User domain** (Phase 1 partial) - NEEDS UPDATE for new schema
  - Domain: `User.kt` (has old `role` field, needs `is_system_admin`)
  - Ports/Service/Persistence/Web implemented with old structure
  - **TODO**: Migrate to new structure (remove role, add is_system_admin)

### Not Implemented Yet
- **Token** (Phase 1) - login/logout, opaque token management
- **InstructorGroup** (Phase 2)
  - InstructorGroup - group CRUD, leader designation
  - InstructorGroupMember - member management (N:M user-group)
  - MVP: All members have equal permissions (no role system)
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

1. Migrate User domain to new schema (remove role, add is_system_admin)
2. Implement Token domain (login, logout, token validation)
3. Add Spring Security configuration
4. Implement InstructorGroup + Permission system (5 entities)
5. Implement Season + Enrollment (with enrollment log)
6. Continue with Ticket → Lesson → Reservation → Attendance

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
