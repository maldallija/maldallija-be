# Future Feature Ideas

## OAuth2 Social Login
- Google login
- Apple login

## Token Enhancement ✅ IMPLEMENTED
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

## Additional Recommendations
- Review/Rating: Member rates Lesson after completion
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
