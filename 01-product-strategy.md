# Product Strategy & Direction

## 1. Restating the Product

A **hyperlocal two-sided platform** where small businesses maintain structured digital profiles and publish updates, while nearby customers discover those businesses, follow the ones they care about, and receive a personalised feed of relevant changes — stock arrivals, menu updates, timing changes, new availability — that helps them act without calling, messaging, or making wasted trips.

The product is **not** a map, a directory, a chat app, or a marketplace. It is a **local follow-and-updates network**: the ongoing information layer between a business and its interested local customers.

### One-line positioning

> **Discover local businesses. Follow what matters. Know when things change.**

---

## 2. Critical Evaluation of the Core Thesis

### What makes this compelling

The concept identifies a **real behavioural gap**. Today's information flow between local businesses and nearby customers is fragmented across Google Maps (static listings), WhatsApp (requires prior contact), Instagram (unstructured, algorithm-gated), phone calls (repetitive), and physical visits (wasted trips).

No existing product combines:
- **Discovery** across everyday local business categories (not just restaurants)
- **Structured, category-specific profiles** (not generic listing templates)
- **An explicit follow relationship** that creates an ongoing information channel
- **A consolidated feed of structured updates** from followed businesses
- **User-controlled notification preferences** per business and update type

Google Maps "follow" is a dead bookmark. WhatsApp Business has no discovery. Instagram is unstructured. JustDial is a frozen phonebook. Zomato/Swiggy only serve food delivery. The whitespace is **narrow but real**.

### What could kill it

| Risk | Severity | Mitigation |
|---|---|---|
| **"Why not just WhatsApp?"** | 🔴 Critical | The product must make multi-business discovery, structured info, and notification control *obviously* better than scattered chats. If it doesn't, nobody switches. |
| **Vendor adoption** | 🔴 Critical | If vendors don't update, the feed is empty and useless. MVP must launch with pre-onboarded vendors who commit to updating. |
| **Cold start / empty feed** | 🟠 High | Start hyper-local (one neighbourhood, 10-15 businesses). Density > breadth. |
| **Notification fatigue** | 🟠 High | Notifications must be user-controlled and update-type aware from day one. Get this wrong and users mute everything. |
| **Category sprawl** | 🟡 Medium | Launching too many categories means each is half-built. Start with 2 categories maximum. |
| **"Just a student project"** | 🟡 Medium | Polish, real data, and a complete loop matter more than feature count. |

### My independent assessment

The thesis is sound **if and only if** the execution delivers two things:
1. A vendor experience so simple that updating takes under 30 seconds on a phone.
2. A user experience where following a business delivers clearly useful information within the first few days.

Everything else — AI, advanced search, analytics, monetisation — is secondary to proving this loop.

---

## 3. Confirmed Direction vs. Open Decisions

### Confirmed (treating as settled)

- Two-sided platform: vendor workspace + customer discovery/feed
- Category-specific structured profiles (not generic templates)
- Follow relationship with ongoing update delivery
- User-controlled notification preferences
- Mobile-first for both sides
- Start hyperlocal (one neighbourhood / college area)
- MVP proves the full vendor-update → follower-receives → user-acts loop
- Map supports but does not dominate the experience
- No payments, booking, delivery, or chat in MVP
- Direct contact (call/WhatsApp) as the action layer

### Decisions I'm making now

| Decision | Chosen direction | Reasoning |
|---|---|---|
| **Product name** | **NearBuzz** (working name) | Captures "nearby" + "buzz/updates" without limiting to students. Short, memorable, domain-available patterns exist. Can be changed later. |
| **Initial audience** | Students + local residents near one college hub | Students are the accessible test group, but don't artificially exclude others in the same area. |
| **Launch categories** | **Mess/Tiffin** + **Printing/Services** | Mess has daily update cadence (menus). Printing has periodic relevance (exam peaks). Both have simple enough data models. Together they prove the concept across two different update patterns. |
| **Follow model** | Follow the business → then choose update categories | Simplest action (one-tap follow), then a lightweight preference picker. Default: all important updates ON, promotional OFF. |
| **Update classification** | 3 tiers: Important / Informational / Promotional | Important (stock, hours, availability changes) → push notification eligible. Informational (menu, new item) → feed. Promotional → feed only, opt-in notification. |
| **Platform type** | Progressive Web App (PWA) | Fastest to build, no app store friction, installable on Android (good push support), works on all devices. |
| **Third category (ready but not launched)** | **Books/Stationery** | Design the schema, but only activate if the first two categories work and team capacity allows. |

---

## 4. Primary Personas

### Persona 1: Neha — The Nearby Student

- 20 years old, BTech student, lives in a PG near college
- Uses Google Maps to find places, WhatsApp to ask friends
- Eats at 2-3 nearby messes, gets printing done before exams
- **Frustration:** Calls the mess daily to ask "kya bana hai aaj?" / Walks to the print shop to find it closed
- **Job to be done:** Know what's happening at the places I regularly visit, without repeatedly asking
- **Success metric:** Opens the app, sees today's mess menu and print shop hours, acts without calling

### Persona 2: Ravi — The Local Mess Owner

- 45 years old, runs a small mess serving 50-80 students daily
- Manages orders via a register, communicates via WhatsApp status and word of mouth
- Changes menu daily, occasionally changes timings or adds new meal plans
- **Frustration:** Students keep calling to ask the same questions. Can't reach new students easily.
- **Job to be done:** Update today's menu once, have it reach all interested students automatically
- **Success metric:** Updates menu in under 30 seconds from his phone. Gets fewer repetitive calls. Sees new students discovering his mess.

### Persona 3: Amit — The Print Shop Owner

- 35 years old, runs a print/photocopy shop near a college
- Services: B&W printing, colour printing, spiral binding, lamination, scanning
- Changes hours during exams, occasionally adds new services
- **Frustration:** Students show up after closing or ask about services he doesn't offer. No easy way to announce extended exam-week hours.
- **Job to be done:** Keep my service list, prices, and hours accurate. Announce changes to people who actually use my shop.
- **Success metric:** Updates hours in a few taps. Followers see the change immediately. Fewer wasted visits.

---

## 5. MVP Feature Scope

### In scope (must ship)

| # | Feature | Priority | Notes |
|---|---|---|---|
| 1 | User registration & login (phone OTP + email) | P0 | Phone-first for India |
| 2 | Vendor registration & business profile creation | P0 | Guided, category-aware setup |
| 3 | Category-specific business profiles (Mess, Printing) | P0 | Structured fields per category |
| 4 | Add/edit products, services, menu items | P0 | Category-specific CRUD |
| 5 | Location-based business discovery | P0 | List view primary, map secondary |
| 6 | Search by name, category, service keyword | P0 | PostgreSQL full-text search |
| 7 | Follow / unfollow a business | P0 | One-tap action |
| 8 | Update preferences per followed business | P0 | Toggle update types on/off |
| 9 | Vendor publishes structured update | P0 | Linked to specific offering or business-level |
| 10 | User updates feed (from followed businesses) | P0 | Reverse-chronological, grouped by day |
| 11 | Business profile page (public, structured) | P0 | Adapted layout per category |
| 12 | Push notifications for important updates | P1 | FCM web push, respects preferences |
| 13 | Contact actions (Call, WhatsApp, Directions) | P1 | Deep links |
| 14 | Freshness timestamps on key information | P1 | "Updated 2 hours ago" |
| 15 | Basic vendor dashboard (overview, followers count) | P1 | Simple metrics |
| 16 | Report a business or update | P2 | Flag for admin review |
| 17 | Basic admin panel (review reports, manage businesses) | P2 | Lightweight |

### Explicitly excluded from MVP

- Payments, transactions, or ordering
- In-app messaging or chat
- Booking/reservation workflows
- Delivery tracking
- AI-powered search or recommendations
- Advanced analytics for vendors
- Rating/review system
- Multi-city or multi-branch
- Third-party integrations
- Loyalty programs or coupons

---

## 6. Launch Strategy for the College Project

### Phase 0: Seed (Week 1-2 of launch)
1. Identify 10-15 businesses in one tight neighbourhood around college
2. Visit owners, explain the concept, get verbal commitment
3. Team manually creates initial business profiles with accurate data
4. Onboard 3-5 vendors to update independently (hands-on training)
5. Recruit 20-30 student beta users

### Phase 1: Validate (Week 3-4)
1. Observe: Do vendors actually update? How often?
2. Observe: Do users open the feed? Do they act on updates?
3. Collect qualitative feedback from both sides
4. Fix the biggest friction points

### Phase 2: Demo (Week 5+)
1. Polish the experience for project presentation
2. Prepare metrics: businesses onboarded, updates published, follows, feed opens, actions taken
3. Show the complete loop working with real local data

### Success criteria for the project demo

| Metric | Target |
|---|---|
| Active businesses with complete profiles | ≥ 10 |
| Businesses publishing updates regularly | ≥ 5 |
| Users who followed at least 1 business | ≥ 20 |
| Updates that led to a user action (view, call, direction) | Demonstrate at least 5 examples |
| Average vendor update time | < 60 seconds |
| End-to-end loop demo | Live demo: vendor posts update → user sees it in feed → notification → action |

---

## 7. Post-MVP Evolution (for the brief, not for building now)

```
MVP                          Phase 2                    Phase 3
─────────────────────────────────────────────────────────────────
Mess + Printing              + Stationery/Books         + PG/Hostel
                             + Kirana                   + More categories
Basic search                 + Fuzzy search (Meilisearch)
                             + Need-based search
Feed (chronological)         + Relevance-ranked feed    + Personalised
                             + Digest notifications       recommendations
Basic vendor dashboard       + Update reach metrics     + Analytics
                             + Enquiry tracking
Direct contact only          + In-app quick questions   + Request workflow
Manual onboarding            + Self-serve onboarding    + Vendor referrals
                             + Verification flow
Phone + email auth           + Google OAuth              
Report only                  + Admin moderation panel   + Trust scoring
PWA                          + PWA improvements         + Consider native
                                                          if traction warrants
```

---

## 8. Product Naming

**Working name: NearBuzz**

Rationale:
- "Near" → hyperlocal, nearby
- "Buzz" → updates, activity, what's happening
- Not student-specific (unlike "StudentSeva" which was correctly abandoned)
- Short, two syllables, easy to say in Hindi and English
- Doesn't limit to any single category

Alternatives considered:
- **LocalPulse** — slightly generic, "pulse" overused in tech
- **LoopLocal** — references the "update loop" concept, decent
- **NearLoop** — combines both, but sounds technical
- **Buzznear** — reversed, less natural

The name can change. Don't block on it. "NearBuzz" is good enough to give the project identity during development.

