# Hotel Management Microservices

Production-style Spring Boot hotel management backend based on the ER diagram in this chat.

## Stack

- Java 21, Spring Boot 3
- Spring Security OAuth2 Resource Server
- Keycloak for OAuth2/OpenID Connect, JWT, users, and roles
- PostgreSQL per service
- Flyway migrations
- Spring Cloud Gateway
- Stripe PaymentIntent-ready payment service
- OpenAPI UI on each MVC service

## Services

| Service | Port | Responsibility |
| --- | ---: | --- |
| `api-gateway` | 8080 | Routes API requests and validates JWT |
| `user-service` | 8082 | Local user profile table linked to Keycloak user ID |
| `hotel-service` | 8083 | Hotels, rooms, pricing, availability, optimistic room version |
| `booking-service` | 8084 | Booking lifecycle and overlap prevention |
| `payment-service` | 8085 | Payment intents, idempotency keys, Stripe webhook |
| `ai-concierge-service` | 8086 | Stores AI interactions and returns a local placeholder response |
| `keycloak` | 8081 | Identity provider |

## Run Infrastructure

Start Keycloak and PostgreSQL databases:

```bash
docker compose up -d keycloak keycloak-db user-db hotel-db booking-db payment-db ai-db
```

Run services locally from separate terminals:

```bash
mvn -pl user-service spring-boot:run
mvn -pl hotel-service spring-boot:run
mvn -pl booking-service spring-boot:run
mvn -pl payment-service spring-boot:run
mvn -pl ai-concierge-service spring-boot:run
mvn -pl api-gateway spring-boot:run
```

Build all services:

```bash
mvn clean package
```

You can also build and run app containers with:

```bash
docker compose --profile apps up --build
```

For local browser token testing, running services on the host is simpler because the default issuer is `http://localhost:8081/realms/hotel-management`.

## Keycloak

- Admin console: `http://localhost:8081`
- Admin username/password: `admin` / `admin`
- Realm: `hotel-management`
- Public client: `hotel-management-web`

Demo users:

| Username | Password | Role |
| --- | --- | --- |
| `admin` | `admin123` | `ADMIN` |
| `guest` | `guest123` | `GUEST` |
| `staff` | `staff123` | `HOTEL_STAFF` |

Get a token:

```bash
curl -X POST http://localhost:8081/realms/hotel-management/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=hotel-management-web" \
  -d "grant_type=password" \
  -d "username=guest" \
  -d "password=guest123"
```

Use the returned `access_token`:

```bash
curl http://localhost:8080/api/bookings/me \
  -H "Authorization: Bearer ACCESS_TOKEN"
```

## DBeaver Connections

Use host `localhost`, username `hotel_user`, password `hotel_password`.

| Database | Port |
| --- | ---: |
| `user_service_db` | 5433 |
| `hotel_service_db` | 5434 |
| `booking_service_db` | 5435 |
| `payment_service_db` | 5436 |
| `ai_service_db` | 5437 |

Keycloak database:

| Database | Port | Username | Password |
| --- | ---: | --- | --- |
| `keycloak_db` | 5438 | `keycloak` | `keycloak` |

## Main API Paths

- `GET /api/hotels`
- `POST /api/hotels` with `ADMIN`
- `POST /api/hotels/{hotelId}/rooms` with `ADMIN` or `HOTEL_STAFF`
- `POST /api/bookings` with `GUEST`
- `PATCH /api/bookings/{id}/confirm` with `ADMIN` or `HOTEL_STAFF`
- `POST /api/payments/intents` with `GUEST`
- `POST /api/payments/webhook` for Stripe
- `POST /api/ai/ask`

## Notes

- Keycloak owns authentication, passwords, OAuth2, and JWT issuance.
- `user-service` stores only business profile data linked by `keycloak_user_id`.
- `booking-service` has a PostgreSQL exclusion constraint that prevents overlapping active bookings for the same room.
- `payment-service` is idempotent by `idempotency_key`. If `STRIPE_API_KEY` is empty, it creates simulated payment intent IDs for local development.
