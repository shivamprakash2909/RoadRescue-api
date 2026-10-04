# RoadRescue — Software Documentation

This directory contains the requirements documentation for the RoadRescue portfolio project.

## Documents

1. [Problem Statement](01-PROBLEM-STATEMENT.md)
2. [Technical Requirements Document (TRD)](02-TRD-TECHNICAL-REQUIREMENTS-DOCUMENT.md)
3. [Software Requirements Specification (SRS)](03-SRS-SOFTWARE-REQUIREMENTS-SPECIFICATION.md)

## Requirements Standard

The documents are structured with reference to **ISO/IEC/IEEE 29148:2018 — Systems and software engineering — Life cycle processes — Requirements engineering**.

IEEE identifies 29148:2018 as an active standard. IEEE 830-1998, historically used for Software Requirements Specifications, has been superseded by ISO/IEC/IEEE 29148.

## Project Positioning

RoadRescue is a portfolio-focused software engineering project. The requirements emphasize realistic engineering depth, testability, architectural clarity, and interview value rather than commercial-market completeness.

## Recommended Development Order

1. Freeze MVP requirements.
2. Crdomeate ain/data model.
3. Design API contracts.
4. Implement authentication and roles.
5. Implement vehicle/provider management.
6. Implement booking state machine.
7. Implement location-based matching.
8. Add Redis.
9. Add Kafka events.
10. Add WebSocket tracking.
11. Implement invoice/payment sandbox.
12. Implement ratings/admin.
13. Add tests and CI/CD.
14. Containerize and document deployment.
