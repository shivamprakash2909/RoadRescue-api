# RoadRescue — Master Prompt for AI Code Assistants

> **Usage:** Copy this entire prompt and paste it into your AI code assistant (Codex, Gemini, Claude, etc.) at the start of a new session. Then say: **"Execute Phase N"** to build that phase. The assistant will have full product context.

---

## SYSTEM CONTEXT — READ THIS ENTIRELY BEFORE GENERATING ANY CODE

You are building **RoadRescue**, a real-time vehicle breakdown assistance platform. You are the sole developer. You will build this project **one phase at a time** — I will tell you which phase to execute. Do not skip ahead. Generate production-quality code with zero placeholders.

---

## PRODUCT SUMMARY

RoadRescue connects stranded vehicle owners with nearby mechanics and towing providers. It has three user interfaces:

1. **Customer App** — request assistance, track provider, pay, rate
2. **Provider App** — receive requests, accept/reject, perform service, submit invoice
3. **Admin Panel** — manage users, providers, bookings, payments, disputes

The platform automates: location-aware provider matching, booking lifecycle management, real-time status tracking, invoicing, simulated payment, and ratings.

---

## TECHNOLOGY STACK (mandatory — do not substitute)

| Layer | Technology |
|---|---|
| Backend | Java 17+, Spring Boot 3.x, separate microservices |
| Build | Maven multi-module (parent POM + child service modules) |
| API | REST, JSON, OpenAPI 3.0 / SpringDoc |
| Auth | Spring Security 6 + JWT (access + refresh tokens, BCrypt) |
| Database | PostgreSQL 15+ (one instance, separate schema per service) |
| Migrations | Flyway |
| Cache | Redis (availability, location GEO, rate limiting, token blacklist) |
| Events | Apache Kafka (async domain events, JSON serialization) |
| Real-time | WebSocket (STOMP over SockJS via Spring Messaging) |
| Frontend | React 18+, TypeScript 5+, Vite, React Router 6 |
| Styling | Tailwind CSS 3 |
| Maps | Mapbox GL JS (adapter pattern — swappable) |
| HTTP Client (FE) | Axios with interceptors |
| State (FE) | Zustand (lightweight) or React Context for auth |
| Containerization | Docker multi-stage builds, Docker Compose |
| Testing | JUnit 5, Mockito, Spring Boot Test, Testcontainers, React Testing Library |
| CI/CD | GitHub Actions |
| Code Quality | Lombok (reduce boilerplate), MapStruct (DTO mapping) |

---

## PROJECT FOLDER STRUCTURE (follow exactly)

```
roadrescue/
├── docker-compose.yml
├── docker-compose.override.yml        # Dev-only overrides (ports, volumes)
├── .env.example                       # Template for environment variables
├── .gitignore
├── Makefile                           # Shortcuts: make up, make down, make logs, make build
├── README.md
│
├── backend/
│   ├── pom.xml                        # Parent POM (dependency management, plugin management)
│   │
│   ├── shared-lib/                    # Shared module — all services depend on this
│   │   ├── pom.xml
│   │   └── src/main/java/com/roadrescue/shared/
│   │       ├── config/
│   │       │   └── JacksonConfig.java
│   │       ├── constants/
│   │       │   └── KafkaTopics.java            # Topic name constants
│   │       ├── dto/
│   │       │   ├── ApiErrorResponse.java        # Standard error envelope
│   │       │   └── PagedResponse.java           # Generic paginated response wrapper
│   │       ├── enums/
│   │       │   ├── BookingStatus.java
│   │       │   ├── ProviderType.java
│   │       │   ├── ServiceType.java
│   │       │   ├── PaymentStatus.java
│   │       │   └── Role.java
│   │       ├── event/                           # Kafka event schemas (shared across services)
│   │       │   ├── BaseEvent.java               # { eventId, eventType, timestamp, correlationId }
│   │       │   ├── BookingCreatedEvent.java
│   │       │   ├── ProviderAssignedEvent.java
│   │       │   ├── ProviderAcceptedEvent.java
│   │       │   ├── ProviderRejectedEvent.java
│   │       │   ├── ProviderEnRouteEvent.java
│   │       │   ├── ProviderArrivedEvent.java
│   │       │   ├── ServiceStartedEvent.java
│   │       │   ├── ServiceCompletedEvent.java
│   │       │   ├── InvoiceCreatedEvent.java
│   │       │   ├── PaymentInitiatedEvent.java
│   │       │   ├── PaymentSucceededEvent.java
│   │       │   ├── PaymentFailedEvent.java
│   │       │   ├── BookingCancelledEvent.java
│   │       │   └── ProviderAvailabilityChangedEvent.java
│   │       ├── exception/
│   │       │   ├── ResourceNotFoundException.java
│   │       │   ├── DuplicateResourceException.java
│   │       │   ├── InvalidStateTransitionException.java
│   │       │   ├── UnauthorizedAccessException.java
│   │       │   └── RateLimitExceededException.java
│   │       ├── security/
│   │       │   ├── JwtTokenProvider.java         # Issue, parse, validate tokens
│   │       │   ├── JwtAuthenticationFilter.java  # OncePerRequestFilter for all services
│   │       │   ├── UserPrincipal.java            # Implements UserDetails
│   │       │   └── CurrentUser.java              # @CurrentUser annotation
│   │       └── util/
│   │           ├── CorrelationIdFilter.java       # MDC-based correlation ID
│   │           └── GeoUtils.java                  # Haversine distance calculation
│   │
│   ├── api-gateway/
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/com/roadrescue/gateway/
│   │       │   │   ├── ApiGatewayApplication.java
│   │       │   │   └── config/
│   │       │   │       ├── RouteConfig.java       # Spring Cloud Gateway routes
│   │       │   │       ├── CorsConfig.java
│   │       │   │       └── RateLimitConfig.java
│   │       │   └── resources/
│   │       │       └── application.yml
│   │       └── test/java/com/roadrescue/gateway/
│   │
│   ├── auth-service/
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/com/roadrescue/auth/
│   │       │   │   ├── AuthServiceApplication.java
│   │       │   │   ├── config/
│   │       │   │   │   ├── SecurityConfig.java
│   │       │   │   │   ├── RedisConfig.java
│   │       │   │   │   └── OpenApiConfig.java
│   │       │   │   ├── controller/
│   │       │   │   │   └── AuthController.java
│   │       │   │   ├── dto/
│   │       │   │   │   ├── request/
│   │       │   │   │   │   ├── RegisterRequest.java
│   │       │   │   │   │   ├── LoginRequest.java
│   │       │   │   │   │   └── RefreshTokenRequest.java
│   │       │   │   │   └── response/
│   │       │   │   │       ├── AuthResponse.java      # { accessToken, refreshToken, user }
│   │       │   │   │       └── UserResponse.java
│   │       │   │   ├── entity/
│   │       │   │   │   └── User.java
│   │       │   │   ├── exception/
│   │       │   │   │   └── GlobalExceptionHandler.java
│   │       │   │   ├── mapper/
│   │       │   │   │   └── UserMapper.java            # MapStruct: User ↔ UserResponse
│   │       │   │   ├── repository/
│   │       │   │   │   └── UserRepository.java
│   │       │   │   ├── service/
│   │       │   │   │   ├── AuthService.java           # Interface
│   │       │   │   │   ├── AuthServiceImpl.java       # Implementation
│   │       │   │   │   └── TokenBlacklistService.java # Redis-based token invalidation
│   │       │   │   └── validation/
│   │       │   │       └── PasswordValidator.java
│   │       │   └── resources/
│   │       │       ├── application.yml
│   │       │       ├── application-dev.yml
│   │       │       └── db/migration/
│   │       │           └── V1__create_users_table.sql
│   │       └── test/java/com/roadrescue/auth/
│   │           ├── unit/
│   │           │   └── AuthServiceTest.java
│   │           ├── integration/
│   │           │   └── UserRepositoryTest.java
│   │           └── api/
│   │               └── AuthControllerTest.java
│   │
│   ├── customer-vehicle-service/
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/com/roadrescue/customer/
│   │       │   │   ├── CustomerVehicleServiceApplication.java
│   │       │   │   ├── config/
│   │       │   │   │   ├── SecurityConfig.java
│   │       │   │   │   └── OpenApiConfig.java
│   │       │   │   ├── controller/
│   │       │   │   │   ├── CustomerController.java
│   │       │   │   │   └── VehicleController.java
│   │       │   │   ├── dto/
│   │       │   │   │   ├── request/
│   │       │   │   │   │   ├── UpdateProfileRequest.java
│   │       │   │   │   │   └── VehicleRequest.java
│   │       │   │   │   └── response/
│   │       │   │   │       ├── CustomerProfileResponse.java
│   │       │   │   │       └── VehicleResponse.java
│   │       │   │   ├── entity/
│   │       │   │   │   ├── CustomerProfile.java
│   │       │   │   │   └── Vehicle.java
│   │       │   │   ├── exception/
│   │       │   │   │   └── GlobalExceptionHandler.java
│   │       │   │   ├── mapper/
│   │       │   │   │   ├── CustomerMapper.java
│   │       │   │   │   └── VehicleMapper.java
│   │       │   │   ├── repository/
│   │       │   │   │   ├── CustomerProfileRepository.java
│   │       │   │   │   └── VehicleRepository.java
│   │       │   │   └── service/
│   │       │   │       ├── CustomerService.java
│   │       │   │       ├── CustomerServiceImpl.java
│   │       │   │       ├── VehicleService.java
│   │       │   │       └── VehicleServiceImpl.java
│   │       │   └── resources/
│   │       │       ├── application.yml
│   │       │       └── db/migration/
│   │       │           └── V1__create_customer_vehicle_tables.sql
│   │       └── test/java/com/roadrescue/customer/
│   │           ├── unit/
│   │           ├── integration/
│   │           └── api/
│   │
│   ├── provider-service/
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/com/roadrescue/provider/
│   │       │   │   ├── ProviderServiceApplication.java
│   │       │   │   ├── config/
│   │       │   │   │   ├── SecurityConfig.java
│   │       │   │   │   ├── RedisConfig.java
│   │       │   │   │   ├── KafkaProducerConfig.java
│   │       │   │   │   └── OpenApiConfig.java
│   │       │   │   ├── controller/
│   │       │   │   │   └── ProviderController.java
│   │       │   │   ├── dto/
│   │       │   │   │   ├── request/
│   │       │   │   │   │   ├── UpdateProviderProfileRequest.java
│   │       │   │   │   │   ├── CapabilityRequest.java
│   │       │   │   │   │   ├── AvailabilityRequest.java
│   │       │   │   │   │   └── LocationUpdateRequest.java
│   │       │   │   │   └── response/
│   │       │   │   │       ├── ProviderProfileResponse.java
│   │       │   │   │       └── ProviderPublicResponse.java
│   │       │   │   ├── entity/
│   │       │   │   │   ├── ProviderProfile.java
│   │       │   │   │   ├── ServiceCapability.java
│   │       │   │   │   └── ProviderLocation.java
│   │       │   │   ├── exception/
│   │       │   │   │   └── GlobalExceptionHandler.java
│   │       │   │   ├── mapper/
│   │       │   │   │   └── ProviderMapper.java
│   │       │   │   ├── repository/
│   │       │   │   │   ├── ProviderProfileRepository.java
│   │       │   │   │   ├── ServiceCapabilityRepository.java
│   │       │   │   │   └── ProviderLocationRepository.java
│   │       │   │   ├── service/
│   │       │   │   │   ├── ProviderService.java
│   │       │   │   │   ├── ProviderServiceImpl.java
│   │       │   │   │   ├── AvailabilityService.java       # Redis-backed
│   │       │   │   │   └── ProviderLocationService.java   # Redis GEO-backed
│   │       │   │   └── event/
│   │       │   │       └── producer/
│   │       │   │           └── ProviderEventProducer.java
│   │       │   └── resources/
│   │       │       ├── application.yml
│   │       │       └── db/migration/
│   │       │           └── V1__create_provider_tables.sql
│   │       └── test/java/com/roadrescue/provider/
│   │           ├── unit/
│   │           ├── integration/
│   │           └── api/
│   │
│   ├── booking-service/
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/com/roadrescue/booking/
│   │       │   │   ├── BookingServiceApplication.java
│   │       │   │   ├── config/
│   │       │   │   │   ├── SecurityConfig.java
│   │       │   │   │   ├── KafkaConfig.java
│   │       │   │   │   └── OpenApiConfig.java
│   │       │   │   ├── controller/
│   │       │   │   │   ├── BookingController.java          # Customer-facing
│   │       │   │   │   └── ProviderBookingController.java  # Provider-facing
│   │       │   │   ├── dto/
│   │       │   │   │   ├── request/
│   │       │   │   │   │   ├── CreateBookingRequest.java
│   │       │   │   │   │   ├── CancelBookingRequest.java
│   │       │   │   │   │   ├── UpdateBookingStatusRequest.java
│   │       │   │   │   │   └── ServiceRecordRequest.java
│   │       │   │   │   └── response/
│   │       │   │   │       ├── BookingResponse.java
│   │       │   │   │       ├── BookingDetailResponse.java
│   │       │   │   │       └── BookingStatusHistoryResponse.java
│   │       │   │   ├── entity/
│   │       │   │   │   ├── Booking.java                    # @Version for optimistic locking
│   │       │   │   │   ├── BookingStatusHistory.java
│   │       │   │   │   └── ServiceRecord.java
│   │       │   │   ├── exception/
│   │       │   │   │   └── GlobalExceptionHandler.java
│   │       │   │   ├── mapper/
│   │       │   │   │   └── BookingMapper.java
│   │       │   │   ├── repository/
│   │       │   │   │   ├── BookingRepository.java
│   │       │   │   │   ├── BookingStatusHistoryRepository.java
│   │       │   │   │   └── ServiceRecordRepository.java
│   │       │   │   ├── service/
│   │       │   │   │   ├── BookingService.java
│   │       │   │   │   ├── BookingServiceImpl.java
│   │       │   │   │   └── BookingStateMachine.java        # Transition validation logic
│   │       │   │   └── event/
│   │       │   │       ├── producer/
│   │       │   │       │   └── BookingEventProducer.java
│   │       │   │       └── consumer/
│   │       │   │           └── ProviderResponseConsumer.java  # Handles ProviderAssigned etc.
│   │       │   └── resources/
│   │       │       ├── application.yml
│   │       │       └── db/migration/
│   │       │           └── V1__create_booking_tables.sql
│   │       └── test/java/com/roadrescue/booking/
│   │           ├── unit/
│   │           │   ├── BookingStateMachineTest.java   # All valid + invalid transitions
│   │           │   └── BookingServiceTest.java
│   │           ├── integration/
│   │           │   ├── BookingRepositoryTest.java
│   │           │   └── BookingWorkflowTest.java
│   │           └── api/
│   │               └── BookingControllerTest.java
│   │
│   ├── matching-service/
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/com/roadrescue/matching/
│   │       │   │   ├── MatchingServiceApplication.java
│   │       │   │   ├── config/
│   │       │   │   │   ├── SecurityConfig.java
│   │       │   │   │   ├── RedisConfig.java
│   │       │   │   │   ├── KafkaConfig.java
│   │       │   │   │   ├── MatchingProperties.java    # @ConfigurationProperties for radius, retries
│   │       │   │   │   └── OpenApiConfig.java
│   │       │   │   ├── controller/
│   │       │   │   │   └── NearbyProviderController.java
│   │       │   │   ├── dto/
│   │       │   │   │   ├── request/
│   │       │   │   │   │   └── NearbySearchRequest.java
│   │       │   │   │   └── response/
│   │       │   │   │       ├── NearbyProviderResponse.java
│   │       │   │   │       └── MatchResultResponse.java
│   │       │   │   ├── exception/
│   │       │   │   │   └── GlobalExceptionHandler.java
│   │       │   │   ├── service/
│   │       │   │   │   ├── MatchingService.java
│   │       │   │   │   ├── MatchingServiceImpl.java
│   │       │   │   │   ├── ProviderRankingStrategy.java       # Interface
│   │       │   │   │   └── DistanceRatingRankingStrategy.java # Default impl
│   │       │   │   ├── client/
│   │       │   │   │   └── ProviderServiceClient.java    # REST client to provider-service
│   │       │   │   └── event/
│   │       │   │       ├── consumer/
│   │       │   │       │   ├── BookingCreatedConsumer.java   # Triggers matching
│   │       │   │       │   └── ProviderRejectedConsumer.java # Re-triggers matching
│   │       │   │       └── producer/
│   │       │   │           └── MatchingEventProducer.java
│   │       │   └── resources/
│   │       │       └── application.yml
│   │       └── test/java/com/roadrescue/matching/
│   │           ├── unit/
│   │           │   ├── MatchingServiceTest.java
│   │           │   └── DistanceRatingRankingStrategyTest.java
│   │           └── integration/
│   │
│   ├── tracking-service/
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/com/roadrescue/tracking/
│   │       │   │   ├── TrackingServiceApplication.java
│   │       │   │   ├── config/
│   │       │   │   │   ├── SecurityConfig.java
│   │       │   │   │   ├── WebSocketConfig.java           # STOMP + SockJS
│   │       │   │   │   ├── WebSocketSecurityConfig.java   # JWT handshake auth
│   │       │   │   │   ├── RedisConfig.java
│   │       │   │   │   └── KafkaConsumerConfig.java
│   │       │   │   ├── controller/
│   │       │   │   │   ├── TrackingController.java         # REST fallback endpoints
│   │       │   │   │   └── LocationController.java         # Provider pushes location
│   │       │   │   ├── dto/
│   │       │   │   │   ├── request/
│   │       │   │   │   │   └── LocationUpdateRequest.java
│   │       │   │   │   └── response/
│   │       │   │   │       ├── BookingStatusResponse.java
│   │       │   │   │       └── ProviderLocationResponse.java
│   │       │   │   ├── service/
│   │       │   │   │   ├── TrackingService.java
│   │       │   │   │   └── TrackingServiceImpl.java
│   │       │   │   ├── websocket/
│   │       │   │   │   ├── WebSocketEventListener.java    # Connect/disconnect logging
│   │       │   │   │   └── BookingStatusHandler.java      # Pushes to /topic/booking/{id}/*
│   │       │   │   └── event/
│   │       │   │       └── consumer/
│   │       │   │           ├── BookingStatusConsumer.java      # All status events → WS push
│   │       │   │           └── ProviderLocationConsumer.java   # Location events → WS push
│   │       │   └── resources/
│   │       │       └── application.yml
│   │       └── test/java/com/roadrescue/tracking/
│   │
│   ├── billing-service/
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/com/roadrescue/billing/
│   │       │   │   ├── BillingServiceApplication.java
│   │       │   │   ├── config/
│   │       │   │   │   ├── SecurityConfig.java
│   │       │   │   │   ├── KafkaConfig.java
│   │       │   │   │   └── OpenApiConfig.java
│   │       │   │   ├── controller/
│   │       │   │   │   ├── InvoiceController.java
│   │       │   │   │   └── PaymentController.java
│   │       │   │   ├── dto/
│   │       │   │   │   ├── request/
│   │       │   │   │   │   ├── CreateInvoiceRequest.java
│   │       │   │   │   │   ├── AddLineItemRequest.java
│   │       │   │   │   │   └── InitiatePaymentRequest.java
│   │       │   │   │   └── response/
│   │       │   │   │       ├── InvoiceResponse.java
│   │       │   │   │       ├── LineItemResponse.java
│   │       │   │   │       └── PaymentResponse.java
│   │       │   │   ├── entity/
│   │       │   │   │   ├── Invoice.java
│   │       │   │   │   ├── InvoiceLineItem.java
│   │       │   │   │   └── Payment.java
│   │       │   │   ├── enums/
│   │       │   │   │   └── InvoiceStatus.java            # DRAFT, FINALIZED, PAID, VOID
│   │       │   │   ├── exception/
│   │       │   │   │   └── GlobalExceptionHandler.java
│   │       │   │   ├── mapper/
│   │       │   │   │   ├── InvoiceMapper.java
│   │       │   │   │   └── PaymentMapper.java
│   │       │   │   ├── repository/
│   │       │   │   │   ├── InvoiceRepository.java
│   │       │   │   │   ├── InvoiceLineItemRepository.java
│   │       │   │   │   └── PaymentRepository.java
│   │       │   │   ├── service/
│   │       │   │   │   ├── InvoiceService.java
│   │       │   │   │   ├── InvoiceServiceImpl.java
│   │       │   │   │   ├── PaymentService.java
│   │       │   │   │   └── PaymentServiceImpl.java
│   │       │   │   ├── gateway/
│   │       │   │   │   ├── PaymentGateway.java            # Interface (adapter pattern)
│   │       │   │   │   └── MockPaymentGateway.java        # Auto-success after 2s delay
│   │       │   │   └── event/
│   │       │   │       ├── consumer/
│   │       │   │       │   └── ServiceCompletedConsumer.java
│   │       │   │       └── producer/
│   │       │   │           └── BillingEventProducer.java
│   │       │   └── resources/
│   │       │       ├── application.yml
│   │       │       └── db/migration/
│   │       │           └── V1__create_billing_tables.sql
│   │       └── test/java/com/roadrescue/billing/
│   │           ├── unit/
│   │           │   ├── InvoiceServiceTest.java
│   │           │   └── PaymentServiceTest.java
│   │           └── integration/
│   │
│   └── notification-service/
│       ├── pom.xml
│       ├── Dockerfile
│       └── src/
│           ├── main/
│           │   ├── java/com/roadrescue/notification/
│           │   │   ├── NotificationServiceApplication.java
│           │   │   ├── config/
│           │   │   │   ├── SecurityConfig.java
│           │   │   │   ├── KafkaConsumerConfig.java
│           │   │   │   └── OpenApiConfig.java
│           │   │   ├── controller/
│           │   │   │   └── NotificationController.java
│           │   │   ├── dto/
│           │   │   │   └── response/
│           │   │   │       └── NotificationResponse.java
│           │   │   ├── entity/
│           │   │   │   └── Notification.java
│           │   │   ├── mapper/
│           │   │   │   └── NotificationMapper.java
│           │   │   ├── repository/
│           │   │   │   └── NotificationRepository.java
│           │   │   ├── service/
│           │   │   │   ├── NotificationService.java
│           │   │   │   ├── NotificationServiceImpl.java
│           │   │   │   ├── NotificationChannel.java          # Interface
│           │   │   │   ├── InAppNotificationChannel.java     # Saves to DB
│           │   │   │   └── EmailNotificationChannel.java     # Mock SMTP / Mailhog
│           │   │   └── event/
│           │   │       └── consumer/
│           │   │           └── DomainEventConsumer.java  # Listens to all booking/payment events
│           │   └── resources/
│           │       ├── application.yml
│           │       └── db/migration/
│           │           └── V1__create_notification_table.sql
│           └── test/java/com/roadrescue/notification/
│
├── frontend/
│   ├── package.json
│   ├── tsconfig.json
│   ├── vite.config.ts
│   ├── tailwind.config.js
│   ├── postcss.config.js
│   ├── index.html
│   ├── .env.example                               # VITE_API_BASE_URL, VITE_MAPBOX_TOKEN
│   ├── Dockerfile
│   ├── nginx.conf                                 # Production: serve static + proxy /api
│   └── src/
│       ├── main.tsx
│       ├── App.tsx                                 # Router setup
│       ├── routes.tsx                              # Centralized route definitions
│       │
│       ├── api/
│       │   ├── client.ts                           # Axios instance, JWT interceptor, refresh logic
│       │   ├── authApi.ts
│       │   ├── vehicleApi.ts
│       │   ├── bookingApi.ts
│       │   ├── providerApi.ts
│       │   ├── invoiceApi.ts
│       │   ├── paymentApi.ts
│       │   ├── notificationApi.ts
│       │   └── adminApi.ts
│       │
│       ├── hooks/
│       │   ├── useAuth.ts                          # Login/logout/register + token mgmt
│       │   ├── useWebSocket.ts                     # STOMP connect, subscribe, auto-reconnect
│       │   ├── useGeolocation.ts                   # Browser geolocation API wrapper
│       │   ├── useNotifications.ts
│       │   └── useBookingStatus.ts                 # Combines WS + REST fallback
│       │
│       ├── store/
│       │   ├── authStore.ts                        # Zustand: user, tokens, isAuthenticated
│       │   └── notificationStore.ts
│       │
│       ├── types/
│       │   ├── auth.ts                             # User, LoginRequest, RegisterRequest, AuthResponse
│       │   ├── vehicle.ts
│       │   ├── booking.ts                          # Booking, BookingStatus enum, CreateBookingRequest
│       │   ├── provider.ts
│       │   ├── invoice.ts
│       │   ├── payment.ts
│       │   ├── notification.ts
│       │   └── admin.ts
│       │
│       ├── components/
│       │   ├── common/
│       │   │   ├── Navbar.tsx
│       │   │   ├── Sidebar.tsx
│       │   │   ├── ProtectedRoute.tsx              # Redirects unauthenticated users
│       │   │   ├── RoleRoute.tsx                   # Restricts by role
│       │   │   ├── LoadingSpinner.tsx
│       │   │   ├── ErrorBoundary.tsx
│       │   │   ├── StatusBadge.tsx                 # Color-coded booking status pill
│       │   │   ├── EmptyState.tsx
│       │   │   ├── ConfirmDialog.tsx
│       │   │   └── Toast.tsx                       # Success/error notifications
│       │   ├── booking/
│       │   │   ├── StatusStepper.tsx               # Vertical stepper showing booking progress
│       │   │   ├── BookingCard.tsx                  # Summary card for history/lists
│       │   │   ├── RequestForm.tsx                  # Vehicle, location, service type, description
│       │   │   └── BookingDetail.tsx
│       │   ├── map/
│       │   │   ├── MapView.tsx                     # Mapbox GL wrapper
│       │   │   ├── LocationPicker.tsx              # Click-to-select + auto-detect
│       │   │   └── ProviderMarker.tsx              # Animated provider location marker
│       │   ├── provider/
│       │   │   ├── RequestCard.tsx                 # Incoming request with Accept/Reject
│       │   │   ├── AvailabilityToggle.tsx
│       │   │   └── CapabilitySelector.tsx
│       │   ├── invoice/
│       │   │   ├── InvoiceDetail.tsx
│       │   │   └── LineItemForm.tsx
│       │   └── rating/
│       │       └── RatingModal.tsx                 # Star selector + review text
│       │
│       ├── pages/
│       │   ├── auth/
│       │   │   ├── LoginPage.tsx
│       │   │   └── RegisterPage.tsx
│       │   ├── customer/
│       │   │   ├── CustomerDashboard.tsx
│       │   │   ├── MyVehiclesPage.tsx
│       │   │   ├── RequestAssistancePage.tsx
│       │   │   ├── ActiveBookingPage.tsx            # Live tracking + status
│       │   │   ├── BookingHistoryPage.tsx
│       │   │   ├── InvoicePage.tsx
│       │   │   └── NotificationsPage.tsx
│       │   ├── provider/
│       │   │   ├── ProviderDashboard.tsx
│       │   │   ├── ProviderProfilePage.tsx
│       │   │   ├── IncomingRequestsPage.tsx
│       │   │   ├── ActiveJobPage.tsx
│       │   │   ├── JobHistoryPage.tsx
│       │   │   └── EarningsPage.tsx
│       │   └── admin/
│       │       ├── AdminDashboard.tsx               # Stats cards
│       │       ├── CustomerManagementPage.tsx
│       │       ├── ProviderManagementPage.tsx
│       │       ├── BookingManagementPage.tsx
│       │       └── PaymentOverviewPage.tsx
│       │
│       └── utils/
│           ├── formatDate.ts
│           ├── formatCurrency.ts
│           ├── statusColors.ts                     # BookingStatus → tailwind color map
│           └── constants.ts                        # WS endpoints, route paths
│
├── infra/
│   ├── postgres/
│   │   └── init.sql                                # Create schemas: auth, customer, provider, booking, billing, notification
│   ├── kafka/
│   │   └── create-topics.sh                        # Bootstrap Kafka topics
│   └── nginx/
│       └── nginx.conf                              # Reverse proxy (optional, for prod-like setup)
│
└── .github/
    └── workflows/
        ├── ci.yml                                  # Build + test all services
        └── docker-publish.yml                      # Build + push Docker images
```

---

## CODE PRACTICES (enforce in all generated code)

### Backend Practices
1. **Clean layering:** `Controller → Service (interface + impl) → Repository`. Zero business logic in controllers — controllers only handle HTTP concerns (validation, status codes, response mapping).
2. **DTOs everywhere:** Never expose JPA `@Entity` classes in API responses. Use `request/` and `response/` DTO subpackages. Map with MapStruct.
3. **Interface-first services:** Every service class has an interface (`BookingService.java`) and an implementation (`BookingServiceImpl.java`). Enables mocking and future swapping.
4. **Global exception handling:** Each service has a `@RestControllerAdvice GlobalExceptionHandler` that returns the standard `ApiErrorResponse` envelope: `{ timestamp, status, message, path, correlationId }`.
5. **Flyway migrations:** All DDL in `db/migration/V{N}__description.sql`. Never use `spring.jpa.hibernate.ddl-auto=update` in any environment.
6. **Optimistic locking:** `@Version` on `Booking` entity. Use `@Retryable` or catch `OptimisticLockingFailureException` for retry.
7. **Idempotency:** Booking creation uses an `idempotency_key` (UUID from client). Payment callbacks deduplicate by `gateway_reference`.
8. **Adapter pattern for externals:** `PaymentGateway` (interface) → `MockPaymentGateway` (impl). `MapProvider` (interface) → `MapboxMapProvider` (impl). Swap via Spring `@Profile` or `@ConditionalOnProperty`.
9. **Kafka event discipline:** Every event extends `BaseEvent` (eventId, eventType, timestamp, correlationId). Consumers are idempotent — check if event was already processed.
10. **Lombok usage:** `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` on DTOs. `@Getter`, `@Setter`, `@Entity` on JPA entities (avoid `@Data` on entities — breaks equals/hashCode for Hibernate).
11. **Structured logging:** SLF4J with `{}` placeholders. Include `bookingId`, `userId`, `correlationId` in log messages. Never log passwords or tokens.
12. **Input validation:** `@Valid` on controller method parameters. Jakarta constraints (`@NotBlank`, `@Email`, `@Min`, `@Max`, `@Size`) on request DTOs.
13. **Pagination:** Use Spring's `Pageable` for list endpoints. Return `PagedResponse<T>` from shared-lib.
14. **Audit fields:** All entities extend a `@MappedSuperclass BaseEntity` with `id` (UUID), `createdAt`, `updatedAt` using `@PrePersist` / `@PreUpdate`.

### Frontend Practices
1. **Strict TypeScript:** `strict: true` in tsconfig. No `any` types.
2. **API layer separation:** All HTTP calls go through `src/api/` files. Pages/components never call Axios directly.
3. **Custom hooks:** Encapsulate logic in hooks (`useAuth`, `useWebSocket`, `useBookingStatus`). Pages are thin — they compose hooks and components.
4. **Error handling:** Every API call wrapped in try/catch. Display user-friendly error via Toast component. Log detailed error to console.
5. **Loading states:** Every async action shows `<LoadingSpinner />`. Buttons disable while submitting.
6. **Consistent naming:** Files use PascalCase for components/pages, camelCase for hooks/utils/api.
7. **Type-safe API:** TypeScript interfaces in `src/types/` mirror backend DTOs exactly.

---

## ROLES & AUTHORIZATION

| Role | Access |
|---|---|
| CUSTOMER | Own profile, own vehicles, create bookings, view own bookings/invoices, pay, rate |
| MECHANIC | Own provider profile, receive/accept/reject requests, advance job status, create invoices |
| TOW_PROVIDER | Same as MECHANIC but for towing jobs |
| ADMIN | All data (read), verify providers, manage users, view all bookings/payments, audit log |

---

## BOOKING STATE MACHINE (critical — implement exactly)

```
REQUESTED → SEARCHING → PROVIDER_ASSIGNED → PROVIDER_ACCEPTED →
PROVIDER_EN_ROUTE → ARRIVED → SERVICE_STARTED → SERVICE_COMPLETED →
PAYMENT_PENDING → COMPLETED
```

**Cancellation** allowed from: REQUESTED, SEARCHING, PROVIDER_ASSIGNED, PROVIDER_ACCEPTED, PROVIDER_EN_ROUTE → transitions to CANCELLED.

**Rules:**
- Invalid transitions MUST be rejected with HTTP 400 + descriptive error.
- Every transition MUST be recorded in `booking_status_history` with timestamp and `changed_by`.
- Every transition MUST publish a Kafka event with unique event ID.
- At most ONE active provider per booking (enforced with `@Version` optimistic locking).
- Booking creation MUST be idempotent (`idempotency_key` unique constraint).
- Two providers MUST NOT accept the same booking simultaneously (optimistic lock + `SELECT FOR UPDATE`).

---

## KAFKA EVENTS

Publish to these topics (one topic per event type or a single `booking-events` topic with type header):

```
BookingCreated, ProviderSearchStarted, ProviderAssigned, ProviderAccepted,
ProviderRejected, ProviderEnRoute, ProviderArrived, ServiceStarted,
ServiceCompleted, InvoiceCreated, PaymentInitiated, PaymentSucceeded,
PaymentFailed, BookingCancelled, ProviderAvailabilityChanged
```

Every event contains: `{ eventId (UUID), eventType, bookingId, timestamp, correlationId, ...payload }`.
Consumers MUST be idempotent.

---

## DATA MODEL

### Core Entities & Relationships

```
User (id, name, email, password_hash, role, status, created_at, updated_at)
  └── 1:N → Vehicle (id, customer_id, vehicle_type, make, model, year, registration_number, color)
  └── 1:0..1 → ProviderProfile (id, user_id, provider_type, verification_status, availability_status, rating)
       └── 1:N → ServiceCapability (id, provider_id, capability_name)
       └── 1:1 → ProviderLocation (id, provider_id, latitude, longitude, updated_at)

Booking (id, customer_id, vehicle_id, provider_id, service_type, problem_description,
         latitude, longitude, status, idempotency_key, version, created_at, updated_at)
  └── 1:N → BookingStatusHistory (id, booking_id, from_status, to_status, changed_by, timestamp)
  └── 1:0..1 → ServiceRecord (id, booking_id, notes, parts_used, labor_hours)
  └── 1:0..1 → Invoice (id, booking_id, status, total, created_at)
       └── 1:N → InvoiceLineItem (id, invoice_id, description, quantity, unit_price, subtotal)
       └── 1:0..1 → Payment (id, invoice_id, amount, currency, status, gateway_reference, created_at)
  └── 1:0..1 → Rating (id, booking_id, customer_id, provider_id, score, review_text, created_at)

Notification (id, user_id, type, title, message, read, created_at)
AuditLog (id, admin_id, action, entity_type, entity_id, details, timestamp)
```

---

## ALL API ENDPOINTS

### Auth Service
```
POST   /api/v1/auth/register
POST   /api/v1/auth/login
POST   /api/v1/auth/refresh
POST   /api/v1/auth/logout
GET    /api/v1/auth/me
```

### Customer & Vehicle Service
```
GET    /api/v1/customers/profile
PUT    /api/v1/customers/profile
GET    /api/v1/vehicles
POST   /api/v1/vehicles
GET    /api/v1/vehicles/{id}
PUT    /api/v1/vehicles/{id}
DELETE /api/v1/vehicles/{id}
```

### Provider Service
```
GET    /api/v1/providers/profile
PUT    /api/v1/providers/profile
POST   /api/v1/providers/capabilities
POST   /api/v1/providers/availability
POST   /api/v1/providers/location
GET    /api/v1/providers/{id}/public
```

### Booking Service
```
POST   /api/v1/bookings
GET    /api/v1/bookings/{id}
GET    /api/v1/bookings
POST   /api/v1/bookings/{id}/cancel
POST   /api/v1/provider/bookings/{id}/accept
POST   /api/v1/provider/bookings/{id}/reject
POST   /api/v1/provider/bookings/{id}/status
```

### Matching Service
```
GET    /api/v1/providers/nearby?lat=X&lng=Y&radius=Z&serviceType=T
```

### Tracking Service
```
POST   /api/v1/tracking/location
GET    /api/v1/bookings/{id}/status
GET    /api/v1/bookings/{id}/location
WS     /ws/tracking → /topic/booking/{bookingId}/status, /topic/booking/{bookingId}/location
```

### Billing Service
```
POST   /api/v1/invoices
POST   /api/v1/invoices/{id}/items
POST   /api/v1/invoices/{id}/finalize
GET    /api/v1/invoices/{bookingId}
POST   /api/v1/payments/{invoiceId}/initiate
POST   /api/v1/payments/callback
GET    /api/v1/payments/{id}
```

### Ratings (via Booking Service or standalone)
```
POST   /api/v1/bookings/{id}/rating
GET    /api/v1/providers/{id}/ratings
```

### Notification Service
```
GET    /api/v1/notifications
POST   /api/v1/notifications/{id}/read
```

### Admin (via API Gateway or Admin Service)
```
GET    /api/v1/admin/customers
GET    /api/v1/admin/providers
PUT    /api/v1/admin/providers/{id}/verification
GET    /api/v1/admin/bookings
GET    /api/v1/admin/bookings/{id}
GET    /api/v1/admin/payments
GET    /api/v1/admin/stats
```

---

## MATCHING ALGORITHM

When a booking enters SEARCHING:

1. Get all providers from Redis where `availability = AVAILABLE`.
2. Filter by `serviceType` capability.
3. Filter by configurable radius (default 10 km) using Redis `GEOSEARCH` or Haversine.
4. Rank by: distance (ascending) → rating (descending).
5. Assign top-ranked → booking becomes PROVIDER_ASSIGNED.
6. On rejection → re-run excluding rejected providers.
7. No provider found → notify customer → allow retry with expanded radius.

---

## PAYMENT FLOW

Adapter pattern: `PaymentGateway` interface → `MockPaymentGateway`.
- Mock: auto-returns SUCCESS after 2s simulated delay.
- Callback endpoint: idempotent (deduplicate by `gateway_reference`).
- SUCCESS → booking → COMPLETED. FAILED → stays PAYMENT_PENDING.

---

## SECURITY REQUIREMENTS

- BCrypt password hashing. JWT access (15 min) + refresh (7 days).
- `@PreAuthorize` on every endpoint. Ownership checks in service layer.
- Rate limiting on `/auth/login`. Input validation (`@Valid`) everywhere.
- No secrets in source. Environment variables via `.env`.
- HTTPS + WSS in production.

---

## DOCKER COMPOSE SERVICES

```yaml
services:
  postgresql:       # Port 5432
  redis:            # Port 6379
  kafka:            # Port 9092
  zookeeper:        # Port 2181
  api-gateway:      # Port 8080
  auth-service:     # Port 8081
  customer-vehicle-service: # Port 8082
  provider-service: # Port 8083
  booking-service:  # Port 8084
  matching-service: # Port 8085
  tracking-service: # Port 8086
  billing-service:  # Port 8087
  notification-service: # Port 8088
  frontend:         # Port 3000
```

---

## 10 DEVELOPMENT PHASES

I will ask you to execute these one at a time. When I say **"Execute Phase N"**, build everything listed for that phase with complete, production-quality code.

| Phase | Name | Key Output |
|---|---|---|
| 1 | Scaffolding & Infrastructure | Parent POM, all service modules, Docker Compose, React skeleton, health checks |
| 2 | Authentication & Identity | Register, login, JWT, RBAC, frontend auth flow, Flyway migration |
| 3 | Customer Profile & Vehicles | Profile CRUD, vehicle CRUD, ownership enforcement, frontend pages |
| 4 | Provider Management | Profiles, capabilities, availability (Redis), location (Redis GEO), frontend |
| 5 | Booking Engine & State Machine | Full state machine, Kafka events, concurrency control, booking UI |
| 6 | Provider Matching | Geospatial search, ranking, auto-assignment, reassignment on reject |
| 7 | Real-Time Tracking (WebSocket) | STOMP WebSocket, live status + location push, reconnect resync |
| 8 | Billing & Payment | Invoices, line items, mock gateway, payment state machine |
| 9 | Ratings, Admin & Notifications | Star ratings, admin CRUD + audit log, event-driven notifications |
| 10 | Testing, CI/CD & Docker | Comprehensive tests, GitHub Actions, multi-stage Dockerfiles, OpenAPI docs |

---

## RESPONSE FORMAT

When executing a phase:

1. **List every file you will create/modify** with full paths from the project root.
2. **Generate complete, production-quality code** — no `// TODO`, no placeholder methods, no skeleton implementations. Every method must be fully implemented.
3. **Include all imports** — do not use `import ...` ellipsis.
4. **Include tests** for that phase (unit + integration where applicable).
5. **Include Docker/config/migration changes** needed for that phase.
6. **End with a "Verification Checklist"** — exact commands or steps I should run to confirm the phase works.

---

## ACCEPTANCE TEST (full project)

The complete MVP is successful when this 20-step scenario works end-to-end:

```
1.  Customer registers.
2.  Customer logs in.
3.  Customer registers a vehicle.
4.  Customer creates a breakdown request with location.
5.  Customer confirms location.
6.  System searches for eligible providers.
7.  Provider becomes available.
8.  Provider receives request.
9.  Provider accepts.
10. Customer receives assignment notification.
11. Provider status → EN_ROUTE.
12. Customer receives live status update (WebSocket).
13. Provider status → ARRIVED.
14. Provider starts and completes service with details.
15. System generates invoice with line items.
16. Customer views invoice.
17. Customer initiates payment.
18. Mock gateway returns SUCCESS.
19. Booking becomes COMPLETED.
20. Customer submits a 5-star rating.
```

---

**I will now tell you which phase to execute. Wait for my instruction.**
