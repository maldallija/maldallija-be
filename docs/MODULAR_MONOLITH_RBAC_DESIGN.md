# Modular Monolith + RBAC 설계 문서

## 1. Project Goals

**Current State:**
- Single-module Gradle project
- Package-based module separation
- MVP: All staff have equal permissions

**Goals:**
- Transition to Gradle Multi-Module Monolith structure
- Implement RBAC (Role-Based Access Control)
- Maintain Hexagonal Architecture
- Keep Opaque Token authentication (current method)

---

## 2. Gradle Multi-Module Architecture Design

### 2.1 Module Structure

**Phase 1 (Initial): user + auth separation**

```
maldallija-be/
├── settings.gradle.kts              # 모듈 등록
├── build.gradle.kts                 # Root 공통 설정
├── gradle/                          # Gradle wrapper
│
├── user/                            # 사용자 모듈 (완전 독립)
│   ├── build.gradle.kts
│   └── src/main/kotlin/
│       └── dev.maldallija.maldallijabe.user/
│           ├── domain/
│           │   ├── User.kt
│           │   └── exception/      # UserException (RuntimeException 직접 상속)
│           ├── application/
│           └── adapter/
│
├── auth/                            # 인증/인가 모듈 (user 의존)
│   ├── build.gradle.kts
│   └── src/main/kotlin/
│       └── dev.maldallija.maldallijabe.auth/
│           ├── domain/
│           │   └── exception/      # AuthException (RuntimeException 직접 상속)
│           ├── application/
│           └── adapter/
│
├── equestriancenter/                # 승마장 모듈
│   ├── build.gradle.kts
│   └── src/main/kotlin/
│       └── dev.maldallija.maldallijabe.equestriancenter/
│           ├── center/             # 승마장 CRUD
│           ├── invitation/         # 초대 시스템
│           └── staff/              # 직원 관리 (RBAC 핵심)
│
├── season/                          # 시즌 모듈
│   ├── build.gradle.kts
│   └── src/main/kotlin/
│       └── dev.maldallija.maldallijabe.season/
│           ├── season/             # 시즌 CRUD
│           ├── enrollment/         # 참여 신청
│           ├── enrollmentlog/      # 신청 이력
│           └── ticketaccount/      # 티켓 계좌
│
├── ticket/                          # 티켓 모듈
│   ├── build.gradle.kts
│   └── src/main/kotlin/
│       └── dev.maldallija.maldallijabe.ticket/
│           └── ticketlog/          # 티켓 로그
│
├── lesson/                          # 레슨 모듈 (Phase 5)
│   ├── build.gradle.kts
│   └── src/main/kotlin/
│       └── dev.maldallija.maldallijabe.lesson/
│
├── reservation/                     # 예약 모듈 (Phase 6)
│   ├── build.gradle.kts
│   └── src/main/kotlin/
│       └── dev.maldallija.maldallijabe.reservation/
│
└── app/                             # 메인 애플리케이션
    ├── build.gradle.kts            # 모든 모듈 의존
    └── src/main/kotlin/
        └── dev.maldallija.maldallijabe/
            └── MaldallijaBeApplication.kt
```

### 2.2 Module Dependency Rules

**Phase 1 (Initial): user + auth only**

```
Dependency Direction: Top -> Bottom (unidirectional)

app
 |-> auth
 '-> user

auth
 '-> user

user
 '-> (no dependencies)  # Complete independence!
```

**Phase 2 (Later): common module (optional)**

```
app
 |-> auth
 |-> user
 '-> common (GlobalExceptionHandler, ErrorResponse only)

auth
 |-> user
 '-> common (optional, if GlobalExceptionHandler needed)

user
 '-> common (optional, if GlobalExceptionHandler needed)

common
 '-> (no dependencies)
```

**Core Principles:**
- Each module's Exception is **completely independent** (extends RuntimeException directly)
- No BaseException (3-line duplication < MSA independence)
- No circular dependencies (strictly enforced)
- Lower modules cannot reference upper modules

### 2.3 Module Responsibilities

**Phase 1 (Initial):**

| Module | Responsibility | Key Domains |
|--------|---------------|-------------|
| **user** | User management (complete independence) | User, UserException (no BaseException!) |
| **auth** | Authentication/Authorization (depends on user) | Session, Token, Filter, AuthException |
| **app** | Main application + other domains | Spring Boot App, EquestrianCenter, Season, etc. |

**Phase 2 (Later - Optional):**

| Module | Responsibility | Key Domains |
|--------|---------------|-------------|
| **common** | Global exception handling only | GlobalExceptionHandler, ErrorResponse |
| **user** | User management | User, UserException |
| **auth** | Authentication/Authorization | Session, Token, Filter, AuthException |
| **app** | Main application + other domains | Spring Boot App, EquestrianCenter, Season, etc. |

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

## 5. Implementation Checklist

### 5.1 Gradle Multi-Module (Phase 1: user + auth only)

**Step 1: user module (complete independence)**
- [ ] UserException: Remove BaseException, extend RuntimeException directly
- [ ] Add errorCode and message fields to UserException
- [ ] Create `user/` module directory structure
- [ ] Create `user/build.gradle.kts` (no dependencies except Spring)
- [ ] Move 10 user files to user module
- [ ] Update `settings.gradle.kts` (include "user")

**Step 2: auth module (depends on user)**
- [ ] AuthException: Remove BaseException, extend RuntimeException directly
- [ ] Create `auth/` module directory structure
- [ ] Create `auth/build.gradle.kts` (depends on user module)
- [ ] Move auth files to auth module
- [ ] Update `settings.gradle.kts` (include "auth")

**Step 3: app module (main application)**
- [ ] Rename `src/` to `app/`
- [ ] Create `app/build.gradle.kts` (depends on user, auth)
- [ ] Update `settings.gradle.kts` (include "app")
- [ ] Root `build.gradle.kts` common configuration

**Step 4: Testing**
- [ ] Import path corrections
- [ ] Build test: `./gradlew clean build`
- [ ] Application test: `./gradlew :app:bootRun`
- [ ] Verify module independence (user has no dependencies)

### 5.2 RBAC Implementation

**Domain Layer:**
- [ ] Create `StaffRole.kt` enum (INSTRUCTOR, MANAGER, ADMIN)
- [ ] Create `StaffPermissions.kt` object
- [ ] Define `Permission` enum (15 permissions)
- [ ] Implement `rolePermissions` mapping
- [ ] Implement `hasPermission()` method
- [ ] Add `role` field to `EquestrianCenterStaff.kt`
- [ ] Create `InsufficientPermissionsException.kt`

**Persistence Layer:**
- [ ] Add `role` field to `EquestrianCenterStaffEntity.kt`
- [ ] Update `EquestrianCenterStaffMapper.kt`

**Database:**
- [ ] Add `role` column to `equestrian_center_staff` table
- [ ] Upgrade existing staff to MANAGER
- [ ] Add index (`idx_staff_role`)

**Service Layer (add permission checks):**
- [ ] `CreateSeasonService` - CREATE_SEASON
- [ ] `UpdateSeasonService` - UPDATE_SEASON
- [ ] `CloseSeasonService` - CLOSE_SEASON
- [ ] `ApproveSeasonEnrollmentService` - APPROVE_ENROLLMENT
- [ ] `RejectSeasonEnrollmentService` - REJECT_ENROLLMENT

**Web Layer:**
- [ ] Add `InsufficientPermissionsException` handler to `GlobalExceptionHandler`

**Tests:**
- [ ] StaffPermissions unit tests
- [ ] Permission check integration tests for each Service
- [ ] 403 response tests

### 5.3 Documentation Update

- [ ] `CLAUDE.md` - Update Current Implementation Status
- [ ] `CLAUDE.md` - Add Development Log
- [ ] `database.md` - Update equestrian_center_staff table schema
- [ ] `MEMO2.md` - Update Role definitions
- [ ] `README.md` - Add Multi-Module structure explanation (optional)

---

## 6. Estimated Work Time

**Phase 1: user + auth module separation only**

| Task | Estimated Time |
|------|----------------|
| UserException refactoring (remove BaseException) | 15min |
| user module creation + file migration | 30min |
| AuthException refactoring (remove BaseException) | 15min |
| auth module creation + file migration | 45min |
| app module setup | 30min |
| Import path corrections | 30min |
| Build & test | 30min |
| Documentation update | 15min |
| **Total (Phase 1)** | **3.5 hours** |

**Phase 2: RBAC implementation (later)**

| Task | Estimated Time |
|------|----------------|
| RBAC Domain/Persistence | 1 hour |
| Service permission checks (5 services) | 2 hours |
| Tests | 1 hour |
| Documentation | 30min |
| **Total (Phase 2)** | **4.5 hours** |

---

## 7. Precautions

**Prevent circular dependencies:**
- Module dependencies must be unidirectional only
- `common` module cannot reference other modules
- Upper modules cannot reference lower modules

**Existing code compatibility:**
- Maintain Opaque Token authentication
- Maintain Hexagonal Architecture
- Maintain Entity immutability (all fields `val`)

**Testing strategy:**
- Independent testing per module
- Integration tests in `app` module

**Performance:**
- Permission checks are in-memory operations (no DB queries)
- Minimal performance overhead

---

## 8. Rollback Plan

**If problems occur:**
1. Create Git branch (`feature/modular-monolith-rbac`)
2. Rollback to main branch if issues arise
3. Analyze issues and retry

**Checkpoints:**
- Checkpoint 1: Successful build after Multi-Module migration
- Checkpoint 2: RBAC Domain/Persistence completed
- Checkpoint 3: Service permission checks completed
- Checkpoint 4: All tests passing

---

## 9. Future Expansion Plan

**Post-MVP (After Phase 7):**
1. Role management in UI (Representative assigns Roles to Staff)
2. Custom Permission per Center (center-specific custom permissions)
3. Permission Audit Log (permission change history)
4. JWT migration (when splitting into MSA)

**Full RBAC (if needed):**
- `equestrian_center_role` table
- `equestrian_center_permission` table
- `equestrian_center_role_permission` table
- Dynamic permission management UI

---

## 10. Conclusion

**Benefits of Modular Monolith + RBAC:**
1. Systematic code structure
2. Clear dependencies between modules
3. Production-level permission system
4. Enhanced portfolio quality
5. Ready for MSA migration

**Pre-implementation checklist:**
- [ ] Create Git branch
- [ ] Commit current code
- [ ] Database backup
- [ ] Design document review complete

**Ready to implement!**
