# RoadRescue — 10-Phase Development Plan

> **Profile:** Solo developer · Microservices from Day 1 · Full-stack (Spring Boot + React/TS) · Target: ≤ 4 weeks

---

## Timeline Overview

```
Week 1  ██ Phase 1 (1d) ██ Phase 2 (2d) ██ Phase 3 (1.5d) ██ Phase 4 (2d)
Week 2  ██ Phase 5 (3d) ██ Phase 6 (2d)
Week 3  ██ Phase 7 (2d) ██ Phase 8 (2.5d)
Week 4  ██ Phase 9 (2d) ██ Phase 10 (3d) ── Buffer/Polish (1-2d)
```

> **IMPORTANT:** Each phase is designed to produce a **working, testable vertical slice**. Never move forward with a broken build.

---

## Phase 1 — Project Scaffolding & Infrastructure (Day 1)

### Objective
Stand up the entire multi-service skeleton, Docker Compose environment, and shared infrastructure so every subsequent phase just adds business logic.

### Deliverables

**Backend (Java/Spring Boot)**
- [ ] Parent Maven/Gradle multi-module project
- [ ] Service modules (empty Spring Boot apps):
  - `api-gateway`
  - `auth-service`
  - `customer-vehicle-service`
  - `provider-service`
  - `booking-service`
  - `matching-service`
  - `tracking-service`
  - `billing-service`
  - `notification-service`
- [ ] Shared library module: common DTOs, exceptions, event schemas, JWT util
- [ ] Spring Cloud Gateway or simple reverse-proxy config in `api-gateway`
- [ ] Each service has its own `application.yml` with DB, Redis, Kafka config
- [ ] Global exception handler template
- [ ] OpenAPI/Swagger config per service
- [ ] Structured logging config (correlation ID via MDC filter)

**Infrastructure (Docker Compose)**
- [ ] `docker-compose.yml` with:
  - PostgreSQL (single instance, separate schemas per service OR separate DBs)
  - Redis
  - Kafka + Zookeeper (or KRaft)
  - Each microservice container
  - React frontend container
- [ ] Init SQL scripts for schema creation
- [ ] `.env` file for secrets/config
- [ ] Makefile or shell scripts: `make up`, `make down`, `make logs`

**Frontend (React + TypeScript)**
- [ ] Vite + React + TypeScript project
- [ ] Folder structure: `pages/`, `components/`, `services/`, `hooks/`, `types/`, `store/`
- [ ] Axios/fetch client with base URL, JWT interceptor (placeholder)
- [ ] React Router with placeholder routes: login, register, dashboard, booking, admin
- [ ] Tailwind CSS or MUI setup
- [ ] Environment config (`.env` for API base URL)

### Requirements Covered
`TR-ARCH-001` `TR-ARCH-002` `NFR-MNT-001` `NFR-MNT-004` `NFR-OBS-001` `NFR-OBS-002`

### Definition of Done
- `docker compose up` starts all services, DB, Redis, Kafka — no crashes
- Each service responds on its health endpoint (`/actuator/health`)
- Frontend loads in browser and shows a placeholder page
- API Gateway routes `/api/auth/**` → auth-service (returns 404, not connection refused)

---

## Phase 2 — Authentication & Identity (Days 2–3)

### Objective
Complete user registration, login, JWT issuance/validation, role-based access control, and the frontend auth flow.

### Deliverables

**auth-service**
- [ ] Entities: `User`, `Role` (enum: CUSTOMER, MECHANIC, TOW_PROVIDER, ADMIN)
- [ ] Tables: `users`, `user_roles`
- [ ] Endpoints:
  - `POST /api/v1/auth/register` — create user with role
  - `POST /api/v1/auth/login` — validate credentials, return JWT
  - `POST /api/v1/auth/logout` — token blacklist (Redis TTL) or client-side
  - `GET /api/v1/auth/me` — current user from token
- [ ] Password hashing: BCrypt
- [ ] JWT: access token (15 min) + refresh token (7d) in `shared-lib`
- [ ] Input validation: email format, password strength, duplicate check
- [ ] Rate limiting on `/auth/login` (Redis-backed, e.g., bucket4j)
- [ ] Unit tests: registration, login, invalid creds, duplicate email
- [ ] Integration tests: full register → login → access protected endpoint

**shared-lib**
- [ ] `JwtTokenProvider`: issue, parse, validate
- [ ] `JwtAuthenticationFilter`: Spring Security filter for all services
- [ ] `@CurrentUser` annotation / `SecurityContextHolder` util
- [ ] Role-based `@PreAuthorize` configuration

**Frontend**
- [ ] Login page with form validation
- [ ] Register page (role selection: Customer / Mechanic / Tow Provider)
- [ ] Auth context/store (JWT in memory, refresh token in httpOnly cookie or localStorage)
- [ ] Protected route wrapper
- [ ] Auto-redirect on 401

### Requirements Covered
`FR-AUTH-001–006` `TR-ARCH-003` `TR-ARCH-004` `TR-SEC-001–004` `TR-SEC-007–009` `NFR-SEC-001–006` `BR-001`

### Definition of Done
- Register a customer → login → receive JWT → call `/auth/me` → get user profile
- Register a mechanic → login → role = MECHANIC in token
- Invalid password → 401
- Duplicate email → 409
- Rate limit triggers after N rapid login attempts
- Frontend: register → login → land on dashboard → logout → redirect to login

---

## Phase 3 — Customer Profile & Vehicle Management (Days 4–5)

### Objective
Customers can manage their profile and CRUD their vehicles. This is the data foundation for bookings.

### Deliverables

**customer-vehicle-service**
- [ ] Entities: `CustomerProfile`, `Vehicle`
- [ ] Tables: `customer_profiles`, `vehicles`
- [ ] Endpoints:
  - `GET /api/v1/customers/profile` — view own profile
  - `PUT /api/v1/customers/profile` — update profile
  - `GET /api/v1/vehicles` — list customer's vehicles
  - `POST /api/v1/vehicles` — register a vehicle
  - `PUT /api/v1/vehicles/{id}` — update vehicle
  - `DELETE /api/v1/vehicles/{id}` — remove (only if no active booking)
  - `GET /api/v1/vehicles/{id}` — get single vehicle
- [ ] Authorization: customers can only access their own data
- [ ] Vehicle attributes: type, make, model, year, registration number, color
- [ ] Validation: required fields, registration number format
- [ ] Tests: CRUD operations, ownership enforcement, delete with active booking blocked

**Frontend**
- [ ] My Profile page (view/edit)
- [ ] My Vehicles page (list, add, edit, delete)
- [ ] Add Vehicle modal/form
- [ ] Vehicle card component

### Requirements Covered
`FR-CUS-001–003` `FR-VEH-001–005` `BR-002`

### Definition of Done
- Customer registers a vehicle → sees it in list → edits → deletes
- Customer A cannot see Customer B's vehicles (403)
- Cannot delete vehicle referenced by active booking (409)

---

## Phase 4 — Provider Management & Availability (Days 5–7)

### Objective
Providers can onboard with profiles, define service capabilities, set availability, and share location. Redis stores live availability state.

### Deliverables

**provider-service**
- [ ] Entities: `ProviderProfile`, `ServiceCapability`, `ProviderLocation`
- [ ] Tables: `provider_profiles`, `service_capabilities`, `provider_locations`
- [ ] Endpoints:
  - `GET /api/v1/providers/profile` — view own profile
  - `PUT /api/v1/providers/profile` — update profile
  - `POST /api/v1/providers/capabilities` — add/set service capabilities
  - `POST /api/v1/providers/availability` — toggle AVAILABLE / UNAVAILABLE
  - `POST /api/v1/providers/location` — update current lat/lng
  - `GET /api/v1/providers/{id}/public` — public-facing provider card (name, rating, type)
- [ ] Provider types: MECHANIC, TOW_PROVIDER
- [ ] Verification status: PENDING, VERIFIED, REJECTED (admin controls this — Phase 9)
- [ ] Redis: store `provider:{id}:availability` and `provider:{id}:location` with TTL
- [ ] Kafka event: `ProviderAvailabilityChanged`
- [ ] Authorization: providers access own profile; customers see public view only
- [ ] Tests: availability toggle, location update, capability CRUD

**Frontend (Provider view)**
- [ ] Provider dashboard page
- [ ] Profile setup/edit form
- [ ] Service capabilities form (checkboxes: flat tire, engine, battery, towing, etc.)
- [ ] Availability toggle (prominent switch)
- [ ] Current location display (browser geolocation API)

### Requirements Covered
`FR-PRO-001–006` `TR-ARCH-006` `TR-GEO-001` `BR-003` `BR-004`

### Definition of Done
- Provider registers → sets capabilities → toggles available → location stored in Redis
- Unavailable provider's Redis key reflects status
- Public profile endpoint returns safe subset of data

---

## Phase 5 — Booking Engine & State Machine (Days 8–10)

### Objective
Implement the core booking lifecycle — the most critical piece. A customer creates a breakdown request; the system manages the entire state machine with validation, timestamps, and event publishing.

### Deliverables

**booking-service**
- [ ] Entities: `Booking`, `BookingStatusHistory`, `ServiceRequest`
- [ ] Tables: `bookings`, `booking_status_history`, `service_requests`
- [ ] Booking state enum:
  ```
  REQUESTED → SEARCHING → PROVIDER_ASSIGNED → PROVIDER_ACCEPTED →
  PROVIDER_EN_ROUTE → ARRIVED → SERVICE_STARTED → SERVICE_COMPLETED →
  PAYMENT_PENDING → COMPLETED | CANCELLED
  ```
- [ ] State machine implementation: explicit transition validation (e.g., `EnumMap<BookingStatus, Set<BookingStatus>>`)
- [ ] Endpoints:
  - `POST /api/v1/bookings` — create booking (customer)
  - `GET /api/v1/bookings/{id}` — get booking detail
  - `GET /api/v1/bookings` — list customer's bookings (paginated)
  - `POST /api/v1/bookings/{id}/cancel` — cancel (with state guard)
  - `POST /api/v1/provider/bookings/{id}/accept` — provider accepts
  - `POST /api/v1/provider/bookings/{id}/reject` — provider rejects → triggers reassignment
  - `POST /api/v1/provider/bookings/{id}/status` — provider advances status (EN_ROUTE, ARRIVED, SERVICE_STARTED, SERVICE_COMPLETED)
- [ ] Booking creation requires: customer_id, vehicle_id, lat/lng, service_type, problem_description
- [ ] Concurrency: optimistic locking (`@Version`) on booking + `SELECT FOR UPDATE` on provider assignment
- [ ] Idempotency: idempotency key on booking creation
- [ ] Kafka events published on every state transition:
  - `BookingCreated`, `ProviderAssigned`, `ProviderAccepted`, `ProviderRejected`, `ProviderEnRoute`, `ProviderArrived`, `ServiceStarted`, `ServiceCompleted`, `BookingCancelled`
- [ ] BookingStatusHistory: every transition recorded with timestamp
- [ ] Authorization: customer owns booking; assigned provider operates on it; admin can view any
- [ ] Tests:
  - Unit: every valid transition, every invalid transition rejected, cancellation guards
  - Integration: create → assign → accept → complete full flow
  - Concurrency: two providers cannot accept the same booking

**Frontend (Customer)**
- [ ] "Request Assistance" page:
  - Select vehicle (dropdown)
  - Location (auto-detect + map confirm)
  - Service type (mechanic / towing / both)
  - Problem description (textarea)
  - Submit button
- [ ] Active Booking page (status tracker — vertical stepper showing current state)
- [ ] Booking History page (list with status badges)

**Frontend (Provider)**
- [ ] Incoming Requests page (cards with customer location, vehicle, problem)
- [ ] Accept / Reject buttons
- [ ] Active Job page with status advance buttons (EN_ROUTE → ARRIVED → etc.)

### Requirements Covered
`FR-REQ-001–006` `FR-BOOK-001–011` `FR-LOC-001–004` `FR-SVC-001–006` `FR-TRK-002` `TR-ARCH-007` `TR-ARCH-009` `NFR-REL-001` `NFR-REL-004` `BR-002` `BR-005` `BR-006` `BR-009`

### Definition of Done
- Customer creates booking → state = REQUESTED
- Invalid transition (e.g., REQUESTED → ARRIVED) → 400
- Provider accepts → PROVIDER_ACCEPTED → customer sees update
- Provider advances through all states to SERVICE_COMPLETED
- Cancellation works from valid states, rejected from invalid states
- Two providers cannot accept same booking (optimistic lock test)
- Kafka topics receive events for every transition
- Status history table has full audit trail

---

## Phase 6 — Provider Matching & Geospatial Search (Days 11–12)

### Objective
When a booking enters SEARCHING, automatically find eligible nearby providers based on location, availability, service capability, and rating.

### Deliverables

**matching-service**
- [ ] Consumes `BookingCreated` Kafka event → triggers matching
- [ ] Matching algorithm:
  1. Query Redis for available providers (status = AVAILABLE)
  2. Filter by service capability (mechanic vs. towing)
  3. Filter by configurable radius (default: 10 km, configurable)
  4. Calculate distance: Haversine formula or PostGIS `ST_DWithin`
  5. Rank by: distance (primary) → rating (secondary) → optionally ETA
  6. Select top candidate
- [ ] Geospatial options:
  - **Option A:** Redis GEO commands (`GEOADD`, `GEORADIUS`) for provider locations
  - **Option B:** PostGIS extension on PostgreSQL
  - Recommendation: **Redis GEO** for speed + PostGIS for persistence
- [ ] Endpoints:
  - `GET /api/v1/providers/nearby?lat=X&lng=Y&radius=Z&serviceType=T` — customer-facing nearby search
  - Internal: matching-service calls provider-service + booking-service via REST or shared Kafka
- [ ] Publishes `ProviderSearchStarted`, `ProviderAssigned`, or `NoProviderFound` events
- [ ] If no provider found → notify customer, allow retry / expand radius
- [ ] If provider rejects → re-trigger matching (consume `ProviderRejected` → re-run)
- [ ] Configurable: max retries, expanded radius on retry, timeout
- [ ] Tests:
  - Unit: Haversine distance, capability filtering, ranking
  - Integration: create booking → matching finds provider → assigns

**Frontend (Customer)**
- [ ] "Searching for providers..." loading state with animation
- [ ] Nearby Providers map view (optional — list view minimum)
- [ ] Provider card: name, rating, distance, ETA, service type

### Requirements Covered
`FR-MAT-001–007` `FR-LOC-002` `TR-GEO-001–006` `NFR-PERF-002`

### Definition of Done
- Create booking → matching-service auto-assigns closest available provider
- Provider outside radius excluded
- Provider without matching capability excluded
- Unavailable provider excluded
- If assigned provider rejects → next provider assigned
- If no providers → customer notified
- Nearby search endpoint returns sorted list with distances

---

## Phase 7 — Real-Time Tracking & WebSocket (Days 13–14)

### Objective
Live status updates and provider location tracking pushed to customers via WebSocket. Clients can reconnect and resync.

### Deliverables

**tracking-service**
- [ ] WebSocket endpoint: `/ws/tracking` (STOMP over WebSocket with SockJS fallback)
- [ ] Channels:
  - `/topic/booking/{bookingId}/status` — booking state changes
  - `/topic/booking/{bookingId}/location` — provider location updates
- [ ] Kafka consumers:
  - Consume all booking state events → push to WebSocket topic
  - Consume `ProviderLocationUpdated` → push to WebSocket topic
- [ ] Provider location ingestion:
  - `POST /api/v1/tracking/location` — provider pushes lat/lng periodically
  - Stored in Redis with TTL (e.g., 30s)
  - Published to Kafka → tracking-service → WebSocket
- [ ] REST fallback:
  - `GET /api/v1/bookings/{id}/status` — latest persisted status
  - `GET /api/v1/bookings/{id}/location` — latest provider location
- [ ] Connection management:
  - JWT authentication on WebSocket handshake
  - On reconnect: client calls REST fallback to resync state
- [ ] Tests:
  - Integration: status change → WebSocket message received
  - Reconnection: disconnect → state change → reconnect → REST returns latest state

**Frontend (Customer)**
- [ ] Active Booking page enhanced:
  - Live status stepper (auto-updates via WebSocket)
  - Provider location on map (moving marker)
  - ETA display
- [ ] WebSocket hook (`useWebSocket`) with auto-reconnect
- [ ] Fallback: poll REST on reconnect

**Frontend (Provider)**
- [ ] Background geolocation sharing (when job is active)
- [ ] Status auto-syncs across tabs/devices

### Requirements Covered
`FR-TRK-001–005` `FR-LOC-005` `TR-ARCH-008` `TR-ARCH-009` `NFR-PERF-003` `NFR-REL-002` `NFR-USA-002`

### Definition of Done
- Provider goes EN_ROUTE → customer's browser receives live update within 2s
- Provider shares location → customer sees marker move on map
- Customer disconnects Wi-Fi → provider advances status → customer reconnects → sees correct state
- JWT-less WebSocket connection rejected

---

## Phase 8 — Billing, Invoicing & Payment (Days 15–17)

### Objective
After service completion, generate invoices with line items, process payments through a sandbox gateway, and manage the payment state machine.

### Deliverables

**billing-service**
- [ ] Entities: `Invoice`, `InvoiceLineItem`, `Payment`
- [ ] Tables: `invoices`, `invoice_line_items`, `payments`
- [ ] Invoice flow:
  - Triggered by `ServiceCompleted` Kafka event OR provider manual submission
  - Provider adds line items: labor, parts, towing fee, etc.
  - System calculates total
  - Invoice status: DRAFT → FINALIZED → PAID / VOID
- [ ] Endpoints:
  - `POST /api/v1/invoices` — create invoice for booking (provider)
  - `POST /api/v1/invoices/{id}/items` — add line item
  - `POST /api/v1/invoices/{id}/finalize` — lock invoice
  - `GET /api/v1/invoices/{bookingId}` — customer views invoice
  - `POST /api/v1/payments/{invoiceId}/initiate` — customer initiates payment
  - `POST /api/v1/payments/callback` — gateway callback (idempotent)
  - `GET /api/v1/payments/{id}` — payment status
- [ ] Payment gateway: **mock/sandbox adapter**
  - Interface: `PaymentGateway` with `initiatePayment()`, `handleCallback()`
  - Mock implementation: auto-succeeds after 2s delay (simulates async)
  - Stripe sandbox adapter (optional stretch)
- [ ] Payment status: INITIATED → SUCCESS / FAILED
- [ ] Idempotency: payment callback with `gateway_reference` dedup
- [ ] On SUCCESS → booking transitions to COMPLETED → Kafka `PaymentSucceeded`
- [ ] On FAILED → booking stays PAYMENT_PENDING → Kafka `PaymentFailed` → customer notified
- [ ] Authorization: customer sees own invoices; provider creates invoices for own jobs
- [ ] Tests:
  - Unit: total calculation, idempotent callback
  - Integration: service complete → invoice → pay → booking COMPLETED

**Frontend (Customer)**
- [ ] Invoice detail page (line items, total)
- [ ] "Pay Now" button → mock payment flow → success/failure screen
- [ ] Payment confirmation

**Frontend (Provider)**
- [ ] "Add Service Details" form: line items (description, quantity, rate)
- [ ] Finalize Invoice button
- [ ] Earnings history page

### Requirements Covered
`FR-INV-001–005` `FR-PAY-001–006` `FR-SVC-003–004` `BR-008` `NFR-OBS-003`

### Definition of Done
- Provider completes service → adds line items → finalizes invoice
- Customer views invoice → initiates payment → mock gateway succeeds → booking = COMPLETED
- Duplicate callback doesn't create duplicate payment
- Failed payment → booking stays PAYMENT_PENDING → customer can retry
- Customer cannot modify finalized invoice

---

## Phase 9 — Ratings, Admin Panel & Notifications (Days 18–19)

### Objective
Customers rate completed services, admins manage the platform, and the notification service delivers key alerts.

### Deliverables

**Rating (extend booking-service or separate module)**
- [ ] Entity: `Rating`
- [ ] Endpoints:
  - `POST /api/v1/bookings/{id}/rating` — submit rating (1-5 stars + review text)
  - `GET /api/v1/providers/{id}/ratings` — provider's rating summary
- [ ] Constraints: one rating per completed booking, only after COMPLETED state
- [ ] Aggregate: update provider's average rating on new review
- [ ] Tests: submit, duplicate blocked, non-completed booking blocked

**notification-service**
- [ ] Kafka consumers for key events:
  - `ProviderAssigned` → notify customer
  - `ProviderAccepted` → notify customer
  - `ProviderEnRoute` → notify customer
  - `ProviderArrived` → notify customer
  - `PaymentSucceeded` / `PaymentFailed` → notify customer
  - `BookingCreated` → notify assigned provider
- [ ] Notification channels (implement at least one):
  - In-app (WebSocket push — reuse tracking-service channel)
  - Email (mock SMTP or Mailhog in Docker)
- [ ] Entity: `Notification` (type, recipient, message, read status, timestamp)
- [ ] Endpoints:
  - `GET /api/v1/notifications` — user's notifications (paginated)
  - `POST /api/v1/notifications/{id}/read` — mark as read
- [ ] Tests: event triggers notification, correct recipient

**Admin (extend api-gateway or dedicated admin-service)**
- [ ] Admin-only endpoints (ADMIN role required):
  - `GET /api/v1/admin/customers` — list customers (paginated, search)
  - `GET /api/v1/admin/providers` — list providers (paginated, search)
  - `PUT /api/v1/admin/providers/{id}/verification` — approve/reject provider
  - `GET /api/v1/admin/bookings` — list all bookings (filterable by status, date)
  - `GET /api/v1/admin/bookings/{id}` — booking detail
  - `GET /api/v1/admin/payments` — payment overview
  - `GET /api/v1/admin/stats` — dashboard stats (total bookings, active bookings, total providers, revenue)
- [ ] Audit log: `AuditLog` entity — records admin actions (who, what, when)
- [ ] Tests: admin access works, non-admin gets 403, audit log written

**Frontend (Customer)**
- [ ] Rating modal after booking completion (star selector + text)
- [ ] Notification bell icon with unread count
- [ ] Notification dropdown/page

**Frontend (Admin)**
- [ ] Admin dashboard: stats cards (total users, active bookings, revenue)
- [ ] Customer management table (search, view details)
- [ ] Provider management table (search, verify/reject)
- [ ] Booking management table (filter by status)
- [ ] Payment overview table

### Requirements Covered
`FR-RAT-001–004` `FR-NOT-001–005` `FR-ADM-001–007` `FR-HIS-001–003` `BR-007` `BR-010` `NFR-SEC-002`

### Definition of Done
- Customer completes booking → rates provider → provider's average updates
- Duplicate rating rejected
- Customer receives notification when provider accepts
- Admin logs in → sees dashboard → can verify a provider → audit log written
- Non-admin calling admin endpoint → 403

---

## Phase 10 — Testing, CI/CD, Docker & Documentation (Days 20–22+)

### Objective
Harden the system with comprehensive tests, automated CI/CD pipeline, production-like Docker deployment, and complete API documentation.

### Deliverables

**Testing**
- [ ] Unit test coverage:
  - State machine transitions (all valid + all invalid)
  - Matching algorithm (distance, capability, availability)
  - Billing calculations
  - Authorization rules per service
- [ ] Integration test coverage:
  - Each service's repository layer (Testcontainers + PostgreSQL)
  - Kafka event publish/consume round-trip (Testcontainers + Kafka)
  - Redis interactions (Testcontainers + Redis)
  - Booking workflow end-to-end
- [ ] API test coverage:
  - Auth flow (register, login, protected routes)
  - Booking CRUD + state transitions
  - Provider workflow (availability, accept, status updates)
  - Payment workflow (invoice, pay, callback)
- [ ] End-to-end happy path test:
  ```
  Register → Login → Add Vehicle → Create Booking → Match Provider →
  Provider Accept → EN_ROUTE → ARRIVED → SERVICE → Invoice → Pay →
  COMPLETED → Rate
  ```
- [ ] Concurrency tests: dual-accept, duplicate payment callback
- [ ] Target: ≥ 70% line coverage on critical services

**CI/CD (GitHub Actions)**
- [ ] `.github/workflows/ci.yml`:
  - Trigger: push to main / PR
  - Steps: checkout → Java setup → build all services → run tests → report coverage
  - Testcontainers for integration tests in CI
- [ ] `.github/workflows/docker.yml`:
  - Build Docker images for each service
  - Push to GitHub Container Registry (ghcr.io) or Docker Hub
- [ ] Branch protection: PRs require passing CI

**Docker Hardening**
- [ ] Multi-stage Dockerfiles per service (build + runtime)
- [ ] Docker Compose production profile: resource limits, restart policies
- [ ] Health checks in Docker Compose for all services
- [ ] Environment-based config: `.env.dev`, `.env.prod`
- [ ] Startup order: `depends_on` with health checks (DB → Redis → Kafka → services)

**Documentation**
- [ ] OpenAPI/Swagger: all endpoints documented with request/response schemas
- [ ] `README.md` at project root:
  - Project overview
  - Architecture diagram
  - Prerequisites
  - How to run (`docker compose up`)
  - How to run tests
  - API docs URL
  - Environment variables reference
- [ ] `CONTRIBUTING.md` (optional)
- [ ] Postman collection export (optional)

**Frontend Polish**
- [ ] Error boundaries
- [ ] Loading states for every async action
- [ ] Toast/snackbar notifications
- [ ] Responsive layout (mobile-first)
- [ ] 404 page
- [ ] Logout from all views

### Requirements Covered
`TR-SEC-005–006` `NFR-MNT-001–005` `NFR-OBS-004` `NFR-PERF-001` `NFR-USA-001–004` `NFR-SCA-001`

### Definition of Done (Project MVP)
- [ ] `docker compose up` → all services healthy within 60s
- [ ] Full 20-step acceptance scenario passes (SRS §10)
- [ ] CI pipeline green on main
- [ ] Swagger UI accessible at `http://localhost:{port}/swagger-ui.html` per service
- [ ] README sufficient for a new developer to run the project in < 10 minutes
- [ ] No hardcoded secrets in source code

---

## Phase Dependency Map

```
Phase 1 (Scaffolding) ──→ Phase 2 (Auth) ──→ Phase 3 (Customer/Vehicle)
                                           ──→ Phase 4 (Provider)
                                                     ↓
Phase 3 + Phase 4 ──→ Phase 5 (Booking Engine) ──→ Phase 6 (Matching)
                                                ──→ Phase 7 (Tracking/WS)
                                                ──→ Phase 8 (Billing/Payment)
                                                          ↓
Phase 7 + Phase 8 ──→ Phase 9 (Rating/Admin/Notif) ──→ Phase 10 (Test/CI/Docker)
```

> **CAUTION:** **Phase 5 (Booking Engine) is the highest-risk phase.** It contains the state machine, concurrency controls, and Kafka events that every subsequent phase depends on. Budget extra time here if needed — cut from Phase 9 (admin polish) or Phase 10 (coverage targets), not from Phase 5.
