# RoadRescue — Technical Requirements Document (TRD)

**Document ID:** RR-TRD-001  
**Version:** 1.0  
**Status:** Draft / Portfolio Project  
**Date:** 2026-10-01

## 1. Purpose

This Technical Requirements Document defines the technical requirements, architecture constraints, major components, interfaces, data requirements, non-functional targets, and implementation boundaries for RoadRescue.

The TRD translates the product/problem requirements into an implementable technical direction. Detailed externally observable software requirements are defined in the SRS.

## 2. Reference Standards

This document is structured with reference to ISO/IEC/IEEE 29148:2018, which defines requirements-engineering processes and characteristics for requirements. IEEE identifies the 2018 edition as an active standard. IEEE 830-1998, historically used for SRS structure, has been superseded by ISO/IEC/IEEE 29148.

## 3. System Overview

RoadRescue is a multi-role platform consisting of:

```text
Customer Client
       |
Provider Client
       |
Admin Client
       |
       v
 API Gateway / Backend
       |
  +----+-------------------------------+
  |                                    |
  v                                    v
Core Application                  Real-Time/Event Layer
  |                                    |
  +-- Auth                             +-- Kafka
  +-- User                             +-- WebSocket
  +-- Vehicle                          +-- Notification
  +-- Provider
  +-- Booking
  +-- Matching
  +-- Payment
  +-- Rating
  |
  +----------+-------------+
             |
       PostgreSQL
       Redis
```

## 4. Proposed Technology Stack

| Layer                   | Technology                                       |
| ----------------------- | ------------------------------------------------ |
| Backend                 | Java, Spring Boot, Microservice                  |
| API                     | REST, JSON, OpenAPI                              |
| Authentication          | Spring Security, JWT                             |
| Database                | PostgreSQL                                       |
| Cache / ephemeral state | Redis                                            |
| Event streaming         | Apache Kafka                                     |
| Real-time communication | WebSocket                                        |
| Frontend                | React + TypeScript                               |
| Maps                    | Map provider API (Mapbox) / configurable adapter |
| Containerization        | Docker / Docker Compose                          |
| Testing                 | JUnit, Mockito, Spring Boot Test                 |
| API testing             | Postman / automated API tests                    |
| CI/CD                   | GitHub Actions                                   |
| Documentation           | OpenAPI / Swagger                                |
| Logging                 | Structured application logging                   |

The stack is an implementation proposal and may be changed if the resulting system satisfies the functional and non-functional requirements.

## 5. Architectural Requirements

### TR-ARCH-001

The backend shall use a microservice architecture with clear separation between API, application/service, domain, persistence, and infrastructure concerns.

### TR-ARCH-002

The system shall expose REST APIs for client-to-server operations.

### TR-ARCH-003

The system shall use JWT-based authentication for authenticated API access.

### TR-ARCH-004

Role-based authorization shall distinguish at minimum:

- CUSTOMER
- MECHANIC
- TOW_PROVIDER
- ADMIN

### TR-ARCH-005

The system shall persist business-critical state in PostgreSQL.

### TR-ARCH-006

Redis shall be used for data that benefits from low-latency access, such as provider availability, temporary location state, caching, and rate-limiting support.

### TR-ARCH-007

Kafka shall be used for asynchronous domain events where eventual consistency is acceptable.

### TR-ARCH-008

Real-time booking/status updates shall be delivered through WebSocket or an equivalent push mechanism.

### TR-ARCH-009

Business-critical state transitions shall remain persisted independently of WebSocket connectivity.

### TR-ARCH-010

External map, notification, and payment providers shall be accessed through adapter interfaces so that development/test implementations can be substituted.

## 6. Core micro-services

### 6.1 Authentication and Identity

Responsibilities:

- Registration
- Login
- Password management
- JWT issuance/validation
- Role management
- Account status

### 6.2 Customer and Vehicle

Responsibilities:

- Customer profile
- Vehicle registration
- Vehicle type/model
- Vehicle identification metadata
- Saved vehicles

### 6.3 Provider Management

Responsibilities:

- Mechanic profile
- Towing provider profile
- Service capabilities
- Service area
- Verification state
- Availability state
- Provider location

### 6.4 Booking

Responsibilities:

- Assistance request
- Service type
- Location
- Booking lifecycle
- Provider assignment
- Cancellation
- Completion

### 6.5 Matching

Responsibilities:

- Nearby-provider discovery
- Provider eligibility
- Distance calculation
- Availability filtering
- Service capability filtering
- Ranking/matching
- Reassignment

### 6.6 Tracking

Responsibilities:

- Provider location updates
- ETA
- Job status
- Customer-facing progress

### 6.7 Billing and Payment

Responsibilities:

- Service estimate
- Invoice
- Payment intent
- Payment result
- Refund/cancellation state where applicable

### 6.8 Notification

Responsibilities:

- Booking confirmation
- Provider assignment
- Provider arrival
- Cancellation
- Payment confirmation
- Job completion

### 6.9 Rating

Responsibilities:

- Customer rating
- Review
- Provider rating aggregation
- One-review-per-completed-booking constraint

## 7. Booking State Model

The booking lifecycle shall support:

```text
REQUESTED
    |
SEARCHING
    |
PROVIDER_ASSIGNED
    |
PROVIDER_ACCEPTED
    |
PROVIDER_EN_ROUTE
    |
ARRIVED
    |
SERVICE_STARTED
    |
SERVICE_COMPLETED
    |
PAYMENT_PENDING
    |
COMPLETED
```

Cancellation may occur from appropriate pre-completion states:

```text
REQUESTED
SEARCHING
PROVIDER_ASSIGNED
PROVIDER_ACCEPTED
PROVIDER_EN_ROUTE
```

Invalid state transitions shall be rejected.

## 8. Event Architecture

Representative Kafka events:

```text
BookingCreated
ProviderSearchStarted
ProviderAssigned
ProviderAccepted
ProviderRejected
ProviderEnRoute
ProviderArrived
ServiceStarted
ServiceCompleted
InvoiceCreated
PaymentInitiated
PaymentSucceeded
PaymentFailed
BookingCancelled
```

Events shall contain a unique event ID and sufficient identifiers to support idempotent processing.

## 9. Data Requirements

Core entities:

```text
User
Role
Vehicle
MechanicProfile
TowingProviderProfile
ServiceCapability
ProviderLocation
Booking
BookingStatusHistory
ServiceRequest
ServiceRecord
Invoice
Payment
Rating
Notification
AuditLog
```

### Relationship overview

```text
User 1 ---- N Vehicle
User 1 ---- 1 ProviderProfile (for provider roles)
Vehicle 1 ---- N Booking
User 1 ---- N Booking
Provider 1 ---- N Booking
Booking 1 ---- N BookingStatusHistory
Booking 1 ---- 0..1 ServiceRecord
Booking 1 ---- 0..1 Invoice
Invoice 1 ---- 0..1 Payment
Booking 1 ---- 0..1 Rating
```

## 10.1 Data Integrity

- User email/phone identifiers shall be unique according to the selected identity policy.
- A vehicle shall belong to one customer.
- A booking shall reference exactly one customer and vehicle.
- A completed booking shall have a terminal completion state.
- A booking shall not have more than one active provider assignment.
- A completed booking shall not be modified except through authorized post-completion operations.
- Payment callbacks/results shall be idempotent.

## 11. Geospatial Requirements

### TR-GEO-001

The system shall store latitude and longitude for customer assistance locations where location-based matching is required.

### TR-GEO-002

The matching subsystem shall support searching for providers within a configurable radius.

### TR-GEO-003

The matching subsystem shall filter providers by availability.

### TR-GEO-004

The matching subsystem shall filter providers by requested service capability where provider capability information is available.

### TR-GEO-005

The system shall calculate or obtain estimated distance and/or travel time using a configurable location provider.

### TR-GEO-006

The system shall not expose provider/customer precise location to unauthorized users.

## 12. API Requirements

Representative endpoints:

```text
POST   /api/v1/auth/register
POST   /api/v1/auth/login

GET    /api/v1/vehicles
POST   /api/v1/vehicles
PUT    /api/v1/vehicles/{id}
DELETE /api/v1/vehicles/{id}

POST   /api/v1/bookings
GET    /api/v1/bookings/{id}
POST   /api/v1/bookings/{id}/cancel

GET    /api/v1/providers/nearby
POST   /api/v1/provider/availability
POST   /api/v1/provider/bookings/{id}/accept
POST   /api/v1/provider/bookings/{id}/reject
POST   /api/v1/provider/bookings/{id}/status

GET    /api/v1/invoices/{bookingId}
POST   /api/v1/payments/{invoiceId}/initiate
POST   /api/v1/payments/callback

POST   /api/v1/bookings/{id}/rating
```

All API endpoints shall be versioned.

## 13. Security Requirements

### TR-SEC-001

Passwords shall never be stored in plaintext.

### TR-SEC-002

Authenticated endpoints shall validate access tokens.

### TR-SEC-003

Authorization shall be enforced server-side.

### TR-SEC-004

Users shall only access resources for which they have permission.

### TR-SEC-005

Sensitive configuration shall not be committed to source control.

### TR-SEC-006

Production secrets shall be supplied through environment/configuration management.

### TR-SEC-007

Input validation shall be applied to externally supplied data.

### TR-SEC-008

APIs shall implement appropriate rate limiting for authentication and other abuse-sensitive operations.

### TR-SEC-009

Security-sensitive actions shall be auditable.

## 14. Reliability and Concurrency

The system shall:

- Prevent duplicate booking creation from repeated client requests where an idempotency mechanism is applicable.
- Prevent two providers from being assigned to the same active booking.
- Ensure valid state transitions under concurrent requests.
- Persist booking state before publishing dependent asynchronous events where required.
- Support event retry and idempotent event handling.
- Allow clients to retrieve current booking state after reconnection.

## 15. Performance Targets

Initial portfolio targets:

| Metric                       | Target                                            |
| ---------------------------- | ------------------------------------------------- |
| Standard API response        | p95 < 500 ms under defined test load              |
| Provider lookup              | p95 < 1 s excluding external map-provider latency |
| WebSocket status propagation | < 2 s under normal test conditions                |
| Availability update          | < 1 s for internal state update                   |
| Database transaction         | < 300 ms for common operations under test load    |

These are engineering targets for the portfolio implementation, not production SLAs.

## 16. Observability

The system shall provide:

- Structured logs
- Correlation/request IDs
- Booking/event IDs in relevant logs
- Error logging
- Authentication/security event logging
- Basic application metrics
- Health/readiness endpoints

## 17. Deployment Requirements

The system shall be executable locally through Docker Compose or an equivalent reproducible environment.

Minimum containerized components:

```text
backend
frontend
postgresql
redis
kafka
zookeeper or Kafka-compatible controller
```

The architecture should allow external managed services to replace local infrastructure.

## 18. Testing Requirements

Testing shall include:

### Unit tests

- Matching logic
- State transition validation
- Billing calculations
- Authorization rules

### Integration tests

- Repository/database behavior
- Booking workflow
- Kafka event handling
- Redis interaction

### API tests

- Authentication
- Booking
- Provider workflow
- Payment workflow

### End-to-end tests

At least one complete happy-path workflow:

```text
Customer request
→ Provider match
→ Provider acceptance
→ Arrival
→ Service
→ Invoice
→ Payment
→ Completion
→ Rating
```

## 19. Technical Acceptance Criteria

The implementation shall be considered technically acceptable when:

- All critical APIs are documented.
- Core business rules have automated tests.
- Invalid booking transitions are rejected.
- Concurrent assignment is protected.
- Kafka events can be retried safely.
- Real-time updates are delivered to connected clients.
- The system can recover client state after WebSocket reconnection.
- Docker-based local setup is reproducible.
- Sensitive credentials are externalized.
