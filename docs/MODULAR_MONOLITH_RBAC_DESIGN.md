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

n## 3. RBAC Design

**Note**: RBAC Design section (297 lines) has been moved to [`docs/backup-archive-eng.md`](./backup-archive-eng.md#rbac-design) for token optimization.

Phase 2 implementation details are available in the archive file.

---

## 4. Migration Plan

**Note**: Migration Plan section (173 lines) has been moved to [`docs/backup-archive-eng.md`](./backup-archive-eng.md#migration-plan) for token optimization.

Detailed step-by-step migration instructions are available in the archive file.

---
## 5. Implementation Checklist

### 5.1 Gradle Multi-Module (Phase 1: user + auth only)

**Step 1: user module (complete independence)** ✅ COMPLETED (2026-01-06)
- [x] UserException: Remove BaseException, extend RuntimeException directly
- [x] Add errorCode and message fields to UserException
- [x] Create `user/` module directory structure
- [x] Create `user/build.gradle.kts` (kotlin-spring, kotlin-jpa, ktlint)
- [x] Move 10 user files to user module
- [x] Update `settings.gradle.kts` (include "user")
- [x] Apply soft delete policy to all UserRepository methods
- [x] Root build.gradle.kts: Add user module dependency

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

**Phase 1 (user module):**
- [x] `CLAUDE.md` - Update Project Structure section (Modular Monolith)
- [x] `CLAUDE.md` - Add Development Log (2026-01-06 entry)
- [x] `MODULAR_MONOLITH_RBAC_DESIGN.md` - Mark Step 1 as completed

**Phase 2 (RBAC):**
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
