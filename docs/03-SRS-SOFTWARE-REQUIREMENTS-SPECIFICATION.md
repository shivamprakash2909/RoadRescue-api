# RoadRescue — Software Requirements Specification (SRS)

**Document ID:** RR-SRS-001  
**Version:** 1.0  
**Status:** Draft / Portfolio Project  
**Date:** 2026-10-01

## Document Basis

This SRS follows the requirements-engineering principles of ISO/IEC/IEEE 29148:2018. IEEE identifies ISO/IEC/IEEE 29148:2018 as the active requirements-engineering standard and notes that it covers the processes and information items used to engineer requirements throughout the system/software life cycle.

The document uses uniquely identified, testable "shall" requirements. Requirements are separated from implementation design where practical.

---

# 1. Introduction

## 1.1 Purpose

This Software Requirements Specification defines the functional and non-functional requirements for RoadRescue, a real-time vehicle breakdown assistance platform.

The document is intended for:

- Developers
- Test engineers
- Reviewers
- Project maintainers
- Portfolio/interview reviewers

## 1.2 Scope

RoadRescue enables customers to request roadside vehicle assistance and connects them with nearby mechanics and/or towing providers.

The system covers:

- Identity and authentication
- Customer profiles
- Vehicle management
- Provider onboarding
- Provider availability
- Location-based provider discovery
- Breakdown requests
- Mechanic/towing booking
- Provider assignment
- Real-time job status
- Service records
- Invoicing
- Payment simulation/integration boundary
- Ratings/reviews
- Notifications
- Administrative management
- Audit information

## 1.3 Product Perspective

RoadRescue is a multi-role client-server application.

```text
+-------------------+
| Customer Client   |
+---------+---------+
          |
+---------v---------+       +-------------------+
|                   |       | Provider Client   |
| RoadRescue API    +<----->+-------------------+
|                   |
+---------+---------+       +-------------------+
          ^                 | Admin Client     |
          |                 +-------------------+
          |
  +-------+--------+
  | PostgreSQL     |
  | Redis          |
  | Kafka          |
  | WebSocket      |
  +----------------+
```

## 1.4 Definitions

| Term            | Definition                                          |
| --------------- | --------------------------------------------------- |
| Customer        | User requesting vehicle assistance                  |
| Mechanic        | Provider capable of inspecting/repairing a vehicle  |
| Towing Provider | Provider capable of transporting a vehicle          |
| Provider        | Mechanic or towing provider                         |
| Booking         | A customer request that enters the service workflow |
| Service Request | Details describing the customer's breakdown         |
| ETA             | Estimated time of arrival                           |
| Invoice         | Amount due for completed/requested services         |
| Matching        | Process of selecting eligible nearby providers      |
| Active Booking  | Booking that has not reached a terminal state       |
| Terminal State  | Final state such as COMPLETED or CANCELLED          |

## 1.5 User Classes

### UC-01 Customer

A person with a vehicle requiring roadside assistance.

### UC-02 Mechanic

A service provider who can receive and execute repair-related jobs.

### UC-03 Towing Provider

A provider who transports faulty vehicles.

### UC-04 Administrator

A privileged operator managing the platform.

---

# 2. Overall Description

## 2.1 Product Functions

The system shall provide:

1. User registration/login.
2. Role-based access.
3. Vehicle management.
4. Provider profiles.
5. Provider availability.
6. Location-aware provider discovery.
7. Breakdown request creation.
8. Mechanic booking.
9. Towing booking.
10. Combined assistance booking.
11. Provider matching.
12. Provider acceptance/rejection.
13. Real-time status updates.
14. Provider location/ETA updates.
15. Service completion.
16. Invoice generation.
17. Payment processing/simulation.
18. Rating/review.
19. Booking history.
20. Administration and auditing.

## 2.2 Operating Environment

### Client

- Modern Android/iOS/mobile web environment.
- Modern Chromium/Firefox/Safari browser for web clients.

### Server

- Java runtime supported by the selected Spring Boot version.
- PostgreSQL.
- Redis.
- Kafka.
- Docker-compatible environment.

## 2.3 Design and Implementation Constraints

- Backend shall use Java/Spring Boot unless an approved project change is made.
- Persistent transactional data shall use PostgreSQL.
- The system shall support asynchronous events through Kafka.
- Real-time updates shall use WebSocket or an equivalent push mechanism.
- The application shall expose documented REST APIs.
- Development shall support local execution using Docker.

---

# 3. External Interface Requirements

## 3.1 User Interface

### Customer UI

The customer shall be able to access:

- Login/register
- Home
- My vehicles
- Request assistance
- Nearby providers
- Active booking
- Tracking
- Invoice/payment
- Booking history
- Ratings

### Provider UI

The provider shall be able to access:

- Login/register
- Profile
- Availability toggle
- Incoming requests
- Active job
- Job history
- Earnings/invoices

### Admin UI

The administrator shall be able to access:

- Dashboard
- Customer management
- Provider management
- Booking management
- Payment overview
- Disputes/issues
- Audit information

## 3.2 Software Interfaces

The system may integrate with:

- Map/geolocation provider (Mapbox)
- Routing/ETA provider
- Payment gateway
- Push notification service
- Email/SMS service

External integrations shall be abstracted behind service interfaces.

## 3.3 Communication Interfaces

- HTTPS shall be used for REST APIs.
- WebSocket connections shall use secure WebSocket in production.
- JSON shall be the default REST payload format.
- Kafka shall carry asynchronous domain events.

---

# 4. Functional Requirements

## 4.1 Authentication

### FR-AUTH-001

The system shall allow a new customer to register using a unique supported identifier and password.

### FR-AUTH-002

The system shall authenticate registered users.

### FR-AUTH-003

The system shall issue an access token after successful authentication.

### FR-AUTH-004

The system shall reject invalid credentials.

### FR-AUTH-005

The system shall enforce role-based authorization for protected operations.

### FR-AUTH-006

The system shall allow a user to log out by invalidating or expiring the applicable client session/token according to the authentication strategy.

---

## 4.2 Customer Profile

### FR-CUS-001

The system shall allow customers to view their profile.

### FR-CUS-002

The system shall allow customers to update permitted profile information.

### FR-CUS-003

The system shall prevent a customer from accessing another customer's private profile data.

---

## 4.3 Vehicle Management

### FR-VEH-001

The system shall allow a customer to register a vehicle.

### FR-VEH-002

The system shall store vehicle type and model information.

### FR-VEH-003

The system shall allow customers to update their vehicle information.

### FR-VEH-004

The system shall allow customers to remove a vehicle that is not referenced by an active booking.

### FR-VEH-005

The system shall associate every customer booking with the vehicle requiring assistance.

---

## 4.4 Provider Management

### FR-PRO-001

The system shall allow eligible users to register as mechanics or towing providers.

### FR-PRO-002

The system shall maintain provider service capabilities.

### FR-PRO-003

The system shall maintain provider verification status.

### FR-PRO-004

The system shall allow providers to set their availability.

### FR-PRO-005

The system shall prevent unavailable providers from being selected for new assignments.

### FR-PRO-006

The system shall allow providers to view requests assigned to them.

---

## 4.5 Location

### FR-LOC-001

The system shall allow a customer to provide the location of a breakdown.

### FR-LOC-002

The system shall support latitude and longitude for location-aware requests.

### FR-LOC-003

The system shall allow the customer to confirm or correct an automatically detected location.

### FR-LOC-004

The system shall restrict precise location access to authorized users participating in the relevant service workflow.

### FR-LOC-005

The system shall allow an active provider to share its current location for tracking where the provider has enabled location sharing.

---

## 4.6 Breakdown Request

### FR-REQ-001

The system shall allow a customer to create a breakdown assistance request.

### FR-REQ-002

The request shall contain the customer, vehicle, location, requested service type, and request time.

### FR-REQ-003

The system shall allow the customer to describe the vehicle problem.

### FR-REQ-004

The system shall support at least:

- Mechanic assistance
- Towing
- Mechanic plus towing

### FR-REQ-005

The system shall assign a unique identifier to every request.

### FR-REQ-006

The system shall prevent an unauthorized user from modifying another customer's request.

---

## 4.7 Provider Matching

### FR-MAT-001

The system shall identify providers that are available for the requested service.

### FR-MAT-002

The system shall identify providers within a configurable search radius.

### FR-MAT-003

The system shall exclude providers without the required service capability where capability data is available.

### FR-MAT-004

The system shall rank eligible providers using configurable matching criteria.

### FR-MAT-005

The system shall provide at least distance and availability as matching inputs.

### FR-MAT-006

The system may use rating, estimated travel time, and provider workload as additional matching inputs.

### FR-MAT-007

If no eligible provider is found, the system shall inform the customer and allow a retry or broader search according to configured rules.

---

## 4.8 Booking

### FR-BOOK-001

The system shall create a booking from a valid assistance request.

### FR-BOOK-002

A newly created booking shall enter the REQUESTED state.

### FR-BOOK-003

The system shall support the following booking states:

```text
REQUESTED
SEARCHING
PROVIDER_ASSIGNED
PROVIDER_ACCEPTED
PROVIDER_EN_ROUTE
ARRIVED
SERVICE_STARTED
SERVICE_COMPLETED
PAYMENT_PENDING
COMPLETED
CANCELLED
```

### FR-BOOK-004

The system shall permit only valid state transitions.

### FR-BOOK-005

The system shall record booking state changes with timestamps.

### FR-BOOK-006

The system shall assign at most one active provider to a booking at a time.

### FR-BOOK-007

The system shall allow an eligible provider to accept a request.

### FR-BOOK-008

The system shall allow an eligible provider to reject a request.

### FR-BOOK-009

If an assigned provider rejects or becomes unavailable before service begins, the system shall support reassignment.

### FR-BOOK-010

The system shall allow cancellation according to configured cancellation rules.

### FR-BOOK-011

The system shall prevent unauthorized users from cancelling or modifying a booking.

---

## 4.9 Provider Tracking

### FR-TRK-001

The system shall display the current booking status to the customer.

### FR-TRK-002

The system shall allow an active provider to update its service status.

### FR-TRK-003

The system shall deliver status updates to connected clients in near real time.

### FR-TRK-004

The system shall allow a customer to retrieve the latest persisted booking state after reconnecting.

### FR-TRK-005

The system shall support provider location updates during the applicable active journey.

---

## 4.10 Service Execution

### FR-SVC-001

The provider shall be able to mark arrival.

### FR-SVC-002

The provider shall be able to start service.

### FR-SVC-003

The provider shall be able to record service details.

### FR-SVC-004

The provider shall be able to record parts, labor, or other billable items.

### FR-SVC-005

The provider shall be able to mark the service completed.

### FR-SVC-006

The system shall prevent completion of a booking by an unauthorized provider.

---

## 4.11 Invoice

### FR-INV-001

The system shall generate an invoice for a completed billable service.

### FR-INV-002

An invoice shall contain a unique identifier.

### FR-INV-003

An invoice shall identify the booking to which it belongs.

### FR-INV-004

An invoice shall contain line items and a calculated total.

### FR-INV-005

The system shall prevent unauthorized modification of finalized invoice totals.

---

## 4.12 Payment

### FR-PAY-001

The system shall allow a customer to initiate payment for an eligible invoice.

### FR-PAY-002

The system shall support at least SUCCESS and FAILED payment outcomes.

### FR-PAY-003

The system shall associate payment status with the relevant invoice and booking.

### FR-PAY-004

Payment processing shall be idempotent for repeated callbacks or retries.

### FR-PAY-005

The system shall not store raw payment credentials.

### FR-PAY-006

For the portfolio MVP, payment may use a mock/sandbox gateway.

---

## 4.13 Notifications

### FR-NOT-001

The system shall notify the customer when a provider is assigned.

### FR-NOT-002

The system shall notify the customer when the provider accepts the request.

### FR-NOT-003

The system shall notify the customer when the provider changes relevant journey/service status.

### FR-NOT-004

The system shall notify the provider when a request is assigned.

### FR-NOT-005

The system shall notify the customer when payment succeeds or fails.

Notifications may be implemented through WebSocket, push, email, SMS, or a combination.

---

## 4.14 Ratings and Reviews

### FR-RAT-001

The system shall allow a customer to rate a completed service.

### FR-RAT-002

The rating shall be associated with the completed booking.

### FR-RAT-003

A customer shall not submit multiple ratings for the same completed booking.

### FR-RAT-004

The system shall maintain an aggregate provider rating.

---

## 4.15 Booking History

### FR-HIS-001

The customer shall be able to view their historical bookings.

### FR-HIS-002

The provider shall be able to view completed jobs associated with the provider.

### FR-HIS-003

The system shall allow filtering by booking status and date where supported.

---

## 4.16 Administration

### FR-ADM-001

The system shall restrict administrative functions to authorized administrators.

### FR-ADM-002

Administrators shall be able to view customer accounts.

### FR-ADM-003

Administrators shall be able to view provider accounts.

### FR-ADM-004

Administrators shall be able to review bookings.

### FR-ADM-005

Administrators shall be able to view payment status.

### FR-ADM-006

Administrators shall be able to review provider verification status.

### FR-ADM-007

The system shall maintain audit records for security-sensitive administrative actions.

---

# 5. Non-Functional Requirements

## 5.1 Performance

### NFR-PERF-001

Under the defined portfolio load test, 95% of standard API requests shall complete within 500 ms excluding external service latency.

### NFR-PERF-002

Provider matching shall return a result within 1 second at the application layer under the defined test load, excluding external routing-service latency.

### NFR-PERF-003

Booking status updates should reach connected clients within 2 seconds under normal test conditions.

## 5.2 Availability and Reliability

### NFR-REL-001

The system shall persist critical booking state in durable storage.

### NFR-REL-002

A temporary WebSocket failure shall not cause loss of booking state.

### NFR-REL-003

Asynchronous event consumers shall support safe retry.

### NFR-REL-004

Duplicate event delivery shall not cause duplicate business actions where the operation is designed to be idempotent.

## 5.3 Security

### NFR-SEC-001

All authenticated APIs shall require valid authentication credentials.

### NFR-SEC-002

The system shall enforce server-side authorization.

### NFR-SEC-003

Passwords shall be securely hashed.

### NFR-SEC-004

Sensitive secrets shall not be stored in source control.

### NFR-SEC-005

Sensitive data shall be transmitted over HTTPS in production.

### NFR-SEC-006

The system shall validate user-controlled input.

### NFR-SEC-007

The system shall protect against unauthorized access to customer/provider location data.

## 5.4 Scalability

### NFR-SCA-001

Stateless API instances should be horizontally scalable.

### NFR-SCA-002

Asynchronous processing should be used for operations that do not require synchronous completion.

### NFR-SCA-003

Caching may be used for frequently accessed non-authoritative data.

## 5.5 Maintainability

### NFR-MNT-001

The codebase shall use clear module boundaries.

### NFR-MNT-002

Public APIs shall be documented using OpenAPI.

### NFR-MNT-003

Critical business logic shall have automated tests.

### NFR-MNT-004

Configuration shall be externalized from application code.

### NFR-MNT-005

The project shall include setup and development documentation.

## 5.6 Observability

### NFR-OBS-001

The system shall generate structured logs for important application events.

### NFR-OBS-002

Requests shall have correlation identifiers where practical.

### NFR-OBS-003

Booking and payment operations shall include traceable identifiers in logs.

### NFR-OBS-004

The system shall expose application health information.

## 5.7 Usability

### NFR-USA-001

The customer shall be able to initiate a breakdown request through a clearly identifiable primary action.

### NFR-USA-002

The current booking status shall be visible without requiring the customer to repeatedly refresh the application.

### NFR-USA-003

Error messages shall provide actionable information where possible.

### NFR-USA-004

The customer workflow should minimize the amount of information required before requesting emergency assistance.

---

# 6. Business Rules

### BR-001

Only authenticated customers may create assistance requests.

### BR-002

A customer must have a valid vehicle associated with a booking.

### BR-003

Only providers marked available may receive new assignments.

### BR-004

A provider must possess the required service capability for a capability-restricted booking.

### BR-005

A provider cannot accept two incompatible assignments if doing so would violate its availability/workload rules.

### BR-006

A booking may have only one active provider assignment at a time.

### BR-007

A rating may only be submitted after service completion.

### BR-008

A payment may only be initiated for an eligible invoice.

### BR-009

A completed booking cannot return to an active service state.

### BR-010

Administrative actions affecting users, providers, bookings, or payments shall be auditable.

---

# 7. Use Cases

## UC-01 — Customer Requests Assistance

**Actor:** Customer

**Preconditions:**

- Customer is authenticated.
- Customer has a registered vehicle.

**Main Flow:**

1. Customer selects Request Assistance.
2. System displays registered vehicles.
3. Customer selects a vehicle.
4. System obtains location.
5. Customer confirms location.
6. Customer selects service type.
7. Customer describes the problem.
8. System creates the request.
9. System searches for eligible providers.
10. System assigns or presents an eligible provider.
11. Provider receives the request.

**Postconditions:**

- Booking exists in REQUESTED, SEARCHING, or PROVIDER_ASSIGNED state.

## UC-02 — Provider Accepts Booking

**Actor:** Mechanic/Towing Provider

**Preconditions:**

- Provider is authenticated.
- Provider is available.
- Provider has received an eligible request.

**Main Flow:**

1. Provider views request.
2. Provider accepts.
3. System validates that the booking is still available.
4. System assigns provider.
5. System changes state to PROVIDER_ACCEPTED.
6. Customer receives an update.

## UC-03 — Provider Completes Service

**Actor:** Provider

**Main Flow:**

1. Provider marks EN_ROUTE.
2. Provider reaches customer.
3. Provider marks ARRIVED.
4. Provider starts service.
5. Provider records service details.
6. Provider completes service.
7. System creates/finalizes invoice.
8. Booking enters PAYMENT_PENDING.

## UC-04 — Customer Pays

**Actor:** Customer

**Main Flow:**

1. Customer views invoice.
2. Customer initiates payment.
3. Payment provider returns success/failure.
4. System records payment status.
5. If successful, booking becomes COMPLETED.
6. Customer can submit a rating.

## UC-05 — Provider Rejects Request

**Actor:** Provider

**Main Flow:**

1. Provider rejects request.
2. System records rejection.
3. System searches for another eligible provider.
4. If found, provider reassignment occurs.
5. If none is found, customer is informed.

---

# 8. Data Requirements

## 8.1 User

Minimum attributes:

```text
id
name
email/phone
password_hash
role
status
created_at
updated_at
```

## 8.2 Vehicle

```text
id
customer_id
vehicle_type
make
model
registration_number
created_at
updated_at
```

## 8.3 Provider

```text
id
user_id
provider_type
verification_status
availability_status
rating
created_at
updated_at
```

## 8.4 Booking

```text
id
customer_id
vehicle_id
provider_id
service_type
problem_description
latitude
longitude
status
created_at
updated_at
```

## 8.5 Payment

```text
id
invoice_id
amount
currency
status
gateway_reference
created_at
updated_at
```

---

# 9. Requirements Traceability

| Requirement Area | Primary Use Cases        | Validation             |
| ---------------- | ------------------------ | ---------------------- |
| Authentication   | UC-01, UC-02             | API/security tests     |
| Vehicle          | UC-01                    | Integration tests      |
| Location         | UC-01                    | Service/API tests      |
| Matching         | UC-01, UC-05             | Unit/integration tests |
| Booking          | UC-01–UC-05              | State-machine tests    |
| Tracking         | UC-03                    | WebSocket tests        |
| Service          | UC-03                    | API/integration tests  |
| Invoice          | UC-03, UC-04             | Unit/API tests         |
| Payment          | UC-04                    | Integration tests      |
| Rating           | UC-04                    | API tests              |
| Administration   | Administrative workflows | Authorization tests    |

---

# 10. Acceptance Criteria

The MVP shall satisfy the following end-to-end scenario:

```text
1. Customer registers.
2. Customer logs in.
3. Customer registers a vehicle.
4. Customer creates a breakdown request.
5. Customer provides/ confirms location.
6. System searches for eligible providers.
7. Provider becomes available.
8. Provider receives request.
9. Provider accepts.
10. Customer receives assignment notification.
11. Provider changes status to EN_ROUTE.
12. Customer receives status update.
13. Provider changes status to ARRIVED.
14. Provider starts service.
15. Provider completes service.
16. System generates invoice.
17. Customer initiates payment.
18. Payment succeeds in sandbox/mock gateway.
19. Booking becomes COMPLETED.
20. Customer submits a rating.
```

---

# 11. Out of Scope for MVP

The following are intentionally deferred:

- Real-world emergency call center.
- Insurance claims.
- Government/traffic authority integration.
- OEM diagnostic APIs.
- Automated physical dispatch hardware.
- Production financial settlement between providers.
- Advanced AI diagnosis.
- Predictive vehicle maintenance.
- International localization.
- Multi-country taxation.

---

# 12. Future Enhancements

Potential future releases may include:

1. AI-assisted symptom diagnosis.
2. Agentic booking assistant.
3. MCP-based service tools.
4. Dynamic pricing.
5. Provider bidding.
6. Subscription-based roadside assistance.
7. Insurance integration.
8. Fleet/customer business accounts.
9. Advanced analytics.
10. Fraud detection.
11. Provider earnings dashboard.
12. Predictive maintenance recommendations.

---

# 13. Verification Strategy

Each requirement should be assigned a verification method:

| Method        | Meaning                                |
| ------------- | -------------------------------------- |
| Test          | Automated/manual software test         |
| Inspection    | Review of implementation/documentation |
| Analysis      | Calculation or technical analysis      |
| Demonstration | Observable execution of functionality  |

Critical functional requirements should use automated tests where practical.

---

# 14. Version History

| Version | Date       | Author         | Description |
| ------- | ---------- | -------------- | ----------- |
| 1.0     | 2026-10-01 | Project Author | Initial SRS |

---

# 15. Approval

| Role                  | Name | Status  |
| --------------------- | ---- | ------- |
| Product/Project Owner | TBD  | Pending |
| Technical Reviewer    | TBD  | Pending |
| QA/Reviewer           | TBD  | Pending |
