# Technical Architecture & Technology Stack (Java + Spring Boot)

## 1. Stack Decision & Overview

The project follows a decoupled client-server architecture:
- **Frontend:** React SPA (Vite + Tailwind CSS + shadcn/ui) deployed on **Vercel**
- **Backend:** Java 21 + Spring Boot 3.4 REST API deployed on **Render** (kept active 24/7 via Cron-job.org keep-alive ping)
- **Database:** Managed PostgreSQL 16 with PostGIS extension hosted on **Supabase** (used strictly as managed DB, zero BaaS lock-in)
- **Media/Object Storage:** **Cloudflare R2** (10 GB free tier, S3-compatible, zero egress fees)
- **Authentication:** Email + 6-digit OTP via **Spring Mail (Gmail SMTP)** + JWT stored in `httpOnly` secure cookies
- **AI Query Engine:** **Google Gemini API** (`gemini-2.0-flash-lite` / `gemini-1.5-flash`) for structured criteria extraction

### Tech Stack Summary Table

| Layer | Technology | Free Tier Limits | Cost | Reasoning |
|---|---|---|:---:|---|
| **Frontend** | React 18 + Vite | Unlimited static hosting on Vercel | **$0** | Standard SPA; fast build times; avoids unnecessary SSR complexity |
| **Styling & UI** | Tailwind CSS + shadcn/ui + Lucide | Open source | **$0** | Accessible, copy-paste components; mobile-first styling |
| **Backend Framework** | Java 21 (LTS) + Spring Boot 3.4 | Render Free Web Service (512 MB RAM, 750 hrs/mo) | **$0** | Strong academic standing; 3-tier architecture (Controller-Service-Repo) |
| **Keep-Alive Worker** | Cron-job.org / UptimeRobot | Unlimited 10-min interval pings | **$0** | Prevents Render free tier from sleeping after 15 min; 0 cold starts |
| **Database** | PostgreSQL 16 + PostGIS | 500 MB storage on Supabase (No 90-day expiry) | **$0** | True spatial queries (`ST_DWithin`, `ST_Distance`); JSONB custom fields |
| **Schema Migrations** | Flyway | Open source | **$0** | Version-controlled, reproducible SQL migration files in version control |
| **Object Storage** | Cloudflare R2 | 10 GB storage, 10M read req/mo | **$0** | S3-compatible API; stores compressed room/menu/business images |
| **Authentication** | Spring Security + JWT + Spring Mail | Gmail SMTP (500 emails/day) | **$0** | Universal access (no phone SMS costs); secure `httpOnly` cookie delivery |
| **Task Scheduling** | Spring `@Scheduled` | In-process JVM worker thread | **$0** | Handles freshness expiration and staleness sweeps hourly without external cron |
| **Push Notifications** | Firebase Cloud Messaging (FCM) | Unlimited web push | **$0** | Direct delivery to customer PWA browsers via Firebase Admin SDK |
| **API Documentation** | springdoc-openapi (Swagger UI) | In-process | **$0** | Interactive API sandbox for evaluations, testing, and codegen |
| **Maps** | Leaflet + OpenStreetMap tiles | Free open tiles | **$0** | No API keys or credit card needed; lightweight pin-drop and radius view |
| **Total** | | | **$0/month** | **100% Free, Zero Card Required** |

---

## 2. System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                       CLIENTS (PWA / Web)                       │
│                                                                 │
│   ┌──────────────┐    ┌──────────────┐    ┌──────────────┐      │
│   │  Customer UI │    │  Vendor UI   │    │  Admin UI    │      │
│   │ (React Vite) │    │ (React Vite) │    │ (React Vite) │      │
│   └──────┬───────┘    └──────┬───────┘    └──────┬───────┘      │
│          │                   │                   │              │
│          └───────────────────┼───────────────────┘              │
│                              │ HTTPS (Axios + httpOnly Cookie)  │
│                              ▼                                  │
│              ┌───────────────────────────────┐                  │
│              │      Service Worker (PWA)     │                  │
│              │      FCM Push Notifications   │                  │
│              └───────────────────────────────┘                  │
└──────────────────────────────┬──────────────────────────────────┘
                               │
            REST API (JSON)    │    ┌─────────────────────────────┐
                               │    │ Cron-job.org (Free Ping)    │
                               │    │ Pings /api/v1/health q10m   │
                               │    └──────────────┬──────────────┘
                               ▼                   │ Keep-Alive
┌──────────────────────────────────────────────────┴──────────────┐
│             SPRING BOOT MODULAR MONOLITH (Render)                │
│             (JVM Heap Capped: -Xms128m -Xmx256m)                │
│                                                                 │
│  ┌───────────────────────┐       ┌───────────────────────────┐  │
│  │ Spring Security       │       │ springdoc-openapi         │  │
│  │ (JWT Filter + RBAC)   │       │ (Swagger UI: /swagger-ui) │  │
│  └──────────┬────────────┘       └───────────────────────────┘  │
│             ▼                                                   │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                      API Controllers                      │  │
│  │  AuthController · BusinessController · OfferingController │  │
│  │  UpdateController · FeedController · SearchController     │  │
│  │  AiController · MediaController · AdminController         │  │
│  └──────────────────────────┬────────────────────────────────┘  │
│                             ▼                                   │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                       Service Layer                       │  │
│  │  AuthService · BusinessService · FeedDistributionService  │  │
│  │  FreshnessEngine · PostgisSearchService · GeminiAiService │  │
│  └──────────────┬────────────────────────────┬───────────────┘  │
│                 │                            │                  │
│  ┌──────────────▼────────────┐  ┌────────────▼───────────────┐  │
│  │ Spring Data JPA Repos     │  │ @Scheduled Background Job  │  │
│  │ (Hibernate Spatial JTS)   │  │ (Hourly Staleness Sweeper) │  │
│  └──────────────┬────────────┘  └────────────────────────────┘  │
└─────────────────┼──────────────────────────┬─────────────────┬──┘
                  │ JDBC (PostgreSQL Wire)   │ S3 Java Client  │ HTTP
                  ▼                          ▼                 ▼
       ┌─────────────────────┐    ┌────────────────────┐ ┌──────────────┐
       │ Supabase PostgreSQL │    │ Cloudflare R2      │ │ Google       │
       │ + PostGIS Extension │    │ (Media Storage)    │ │ Gemini API   │
       │ (Tables, Geo Index) │    │ (Photos, Menus)    │ │ (AI Search)  │
       └─────────────────────┘    └────────────────────┘ └──────────────┘
```

---

## 3. Database Design & Schemas

### 3.1 Relational Structure Overview

```
users (id, email, role, name, status, created_at)
  │
  ├──< otp_requests (id, email, otp_hash, purpose, expires_at, used)
  │
  ├──< businesses (id, owner_id, name, category, location [GEOGRAPHY], address, phone, whatsapp, status)
  │      │
  │      ├──< business_photos (id, business_id, storage_key, sort_order)
  │      ├──< business_hours (id, business_id, day_of_week, opens_at, closes_at, is_closed)
  │      │
  │      ├──< offerings (id, business_id, offering_type, title, price, status, last_confirmed_at, attributes [JSONB])
  │      │
  │      └──< updates (id, business_id, update_type, title, description, offering_id, expires_at, created_at)
  │             │
  │             └──< notifications (id, user_id, update_id, is_read, sent_at)
  │
  ├──< follows (id, user_id, business_id, created_at)
  │      │
  │      └──< follow_preferences (follow_id, update_type, is_enabled)
  │
  └──< listing_reports (id, reporter_id, business_id, reason, status, created_at)
```

### 3.2 Key Database Features

* **Spatial Indexing:**
  ```sql
  CREATE EXTENSION IF NOT EXISTS postgis;
  CREATE EXTENSION IF NOT EXISTS pgcrypto;

  CREATE INDEX idx_businesses_location ON businesses USING GIST(location);
  ```
* **Distance Radius Query:**
  ```sql
  SELECT b.*, ST_Distance(b.location::geography, ST_MakePoint(:lon, :lat)::geography) AS distance_meters
  FROM businesses b
  WHERE b.category = :category
    AND b.status != 'CLOSED'
    AND ST_DWithin(b.location::geography, ST_MakePoint(:lon, :lat)::geography, :radiusMeters)
  ORDER BY distance_meters ASC;
  ```
* **Category Extensibility via JSONB:**
  The `attributes` column in `offerings` holds dynamic category fields (e.g., room Wi-Fi/AC flags, daily meal menu items, printing paper types, book ISBNs) without altering relational schema.

---

## 4. Backend Implementation Blueprint (Spring Boot 3.4)

### 4.1 Maven Dependencies (`pom.xml`)

```xml
<dependencies>
    <!-- Web & REST -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Security & JWT -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.6</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.12.6</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.12.6</version>
        <scope>runtime</scope>
    </dependency>

    <!-- Persistence & PostGIS -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.hibernate.orm</groupId>
        <artifactId>hibernate-spatial</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-database-postgresql</artifactId>
    </dependency>

    <!-- Email (SMTP OTP) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-mail</artifactId>
    </dependency>

    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Keep-Alive & Health Monitoring -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

    <!-- OpenAPI / Swagger UI -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.8.0</version>
    </dependency>

    <!-- AWS SDK for Cloudflare R2 -->
    <dependency>
        <groupId>software.amazon.awssdk</groupId>
        <artifactId>s3</artifactId>
        <version>2.29.15</version>
    </dependency>

    <!-- Firebase Admin (Push Notifications) -->
    <dependency>
        <groupId>com.google.firebase</groupId>
        <artifactId>firebase-admin</artifactId>
        <version>9.4.2</version>
    </dependency>

    <!-- Compile-time Mapping & Productivity -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.6.3</version>
    </dependency>
</dependencies>
```

### 4.2 Application Configuration (`application.yml`)

```yaml
server:
  port: ${PORT:8080}
  tomcat:
    threads:
      max: 20
      min-spare: 2
    max-connections: 100

spring:
  application:
    name: nearbuzz-api

  datasource:
    url: ${DB_URL}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 5
      minimum-idle: 1
      idle-timeout: 300000

  jpa:
    database-platform: org.hibernate.spatial.dialect.postgis.PostgisPG95Dialect
    open-in-view: false
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: false

  flyway:
    enabled: true
    locations: classpath:db/migration

  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_APP_PASSWORD}
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true

app:
  jwt:
    secret: ${JWT_SECRET}
    expiration-ms: 86400000 # 24 Hours
    cookie-name: nearbuzz_token

  r2:
    endpoint: ${R2_ENDPOINT}
    access-key: ${R2_ACCESS_KEY_ID}
    secret-key: ${R2_SECRET_ACCESS_KEY}
    bucket-name: nearbuzz-media
    public-url-base: ${R2_PUBLIC_URL}

  gemini:
    api-key: ${GEMINI_API_KEY}
    model: gemini-2.0-flash-lite

management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: never
```

### 4.3 Containerization & Render JVM Tuning (`Dockerfile`)

```dockerfile
# Multi-stage build for minimal final image
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Restrict heap to 256 MB so process never exceeds Render's 512 MB ceiling
ENTRYPOINT ["java", "-Xms128m", "-Xmx256m", "-XX:+UseG1GC", "-jar", "app.jar"]
```

---

## 5. Security & Authentication Model

### 5.1 Email OTP Handshake

```
Customer/Vendor                  Spring Boot API              Gmail SMTP
      │                                 │                          │
      │── 1. POST /api/v1/auth/send-otp ─▶│                          │
      │      { email: "user@..." }      │── 2. Create 6-digit OTP ─▶│
      │                                 │      Send Email (TLS)    │──▶ User Inbox
      │◀─ 3. Returns { success: true } ─│                          │
      │                                 │
      │── 4. POST /api/v1/auth/verify ──▶│
      │      { email, otp, name? }      │── Validate OTP Hash & TTL
      │                                 │── Create/Fetch User Record
      │                                 │── Generate JWT
      │◀─ 5. Set-Cookie: httpOnly; ─────│
      │      Secure; SameSite=Lax       │
```

### 5.2 Cookie Security Settings
* Cookie is marked **`httpOnly = true`**: Protected against XSS credential exfiltration.
* Cookie is marked **`SameSite = Lax`** and **`Secure = true`**: Protected against CSRF attacks.
* Spring Security validates JWT on incoming requests via a custom `OncePerRequestFilter`.

### 5.3 Cross-Origin Resource Sharing (CORS)

```java
@Configuration
public class WebMvcCorsConfig implements WebMvcConfigurer {
    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins(frontendUrl, "https://nearbuzz.vercel.app")
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
```

---

## 6. Freshness & Staleness Scheduling Engine

To enforce information accuracy, Spring Boot executes an in-process hourly batch job:

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class FreshnessScheduler {

    private final OfferingRepository offeringRepository;

    // Runs once every hour
    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void evaluateFreshnessWindows() {
        Instant now = Instant.now();
        
        // Hostels/Rooms: 72 Hours
        offeringRepository.markStale(OfferingType.HOSTEL_ROOM, now.minus(72, ChronoUnit.HOURS));
        
        // Mess Daily Menus: 24 Hours
        offeringRepository.markStale(OfferingType.MESS_MENU, now.minus(24, ChronoUnit.HOURS));
        
        // Printing Services: 24 Hours
        offeringRepository.markStale(OfferingType.PRINT_SERVICE, now.minus(24, ChronoUnit.HOURS));
        
        // Study Space Seats: 4 Hours
        offeringRepository.markStale(OfferingType.LIBRARY_SEAT, now.minus(4, ChronoUnit.HOURS));
    }
}
```

---

## 7. Media Upload Architecture (Cloudflare R2)

1. Client compresses image locally on mobile browser (`browser-image-compression`, max 2 MB).
2. Client sends `multipart/form-data` to `POST /api/v1/media/upload`.
3. Spring Boot validates MIME type (`image/jpeg`, `image/png`, `image/webp`).
4. Backend streams file bytes to Cloudflare R2 bucket using AWS S3 Java SDK.
5. Returns public CDN image URL to client; saves storage key in PostgreSQL.

---

## 8. Real-Time Updates & Notification Model

For the MVP, updates follow a clean, low-overhead model:
* **Feed Updates:** Client uses SWR / TanStack Query polling on `/api/v1/feed` with a 30-second interval when active.
* **Push Notifications:** When a vendor publishes an update marked as `IMPORTANT`, Spring Boot asynchronously triggers FCM to push alerts to subscribed devices.
* **Zero WebSocket Overhead:** Eliminates persistent socket connection drain on Render's 512 MB memory limit.

---

## 9. Zero-Cost Infrastructure Verification Plan

| Component | Target URL / Service | Verification Method |
|---|---|---|
| **Backend Keep-Alive** | `GET https://nearbuzz-api.onrender.com/actuator/health` | Set up 10-min interval on Cron-job.org. Verify Render service uptime stays at 100% without sleep. |
| **Geospatial Queries** | `GET /api/v1/search?category=MESS&lat=18.52&lon=73.85&radius=2000` | Verify database executes `ST_DWithin` using PostGIS spatial index. |
| **Email OTP Dispatch** | `POST /api/v1/auth/send-otp` | Check Gmail inbox for receipt of 6-digit OTP within 5 seconds. |
| **Media Pipeline** | `POST /api/v1/media/upload` | Confirm image appears in Cloudflare R2 bucket and renders via CDN URL. |
| **Memory Ceiling** | Render Metrics Tab | Ensure JVM process memory remains between 180 MB and 260 MB under load. |

