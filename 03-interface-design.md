# Interface Design & User Experience

## 1. Design Philosophy

### Core Principle
**Organise the experience around what the user cares about — not around a map, a directory, or a social feed.**

The customer side has three modes of use:
1. **Active discovery** — "I need to find a business right now"
2. **Passive consumption** — "Show me what's new from places I follow"
3. **Reference lookup** — "What are the hours/prices at that shop I saved?"

The vendor side has one primary job:
- **Update my information quickly and see that it reaches people**

### Design System
- **Tailwind CSS + shadcn/ui** — consistent, accessible, customisable components
- **Mobile-first** — design for 360px viewport, then adapt upward
- **Indian context** — support Hindi/English content in the same view, ₹ currency, phone number formats
- **Dark mode** — support from day one (shadcn/ui makes this trivial)
- **Touch targets** — minimum 44×44px for all interactive elements (vendor side especially)

---

## 2. Customer Interface — Information Architecture

### Navigation Structure

```
Bottom Navigation Bar (4 tabs):

┌──────────┬──────────┬──────────┬──────────┐
│  🏠 Home │ 🔍 Explore│ ⭐ My    │ 👤 You  │
│  (Feed)  │          │ Places   │          │
└──────────┴──────────┴──────────┴──────────┘
```

| Tab | Purpose | Why it exists |
|---|---|---|
| **Home** | Updates feed from followed businesses | The core value — "what's new from my places" |
| **Explore** | Discover new businesses nearby | Primary acquisition surface — find and follow |
| **My Places** | Manage followed businesses + preferences | Makes the follow relationship visible and valuable |
| **You** | Profile, settings, notification controls | Account management |

---

### Screen: Home / Updates Feed

This is the screen that proves the product. If this feed is empty or useless, nothing else matters.

**Layout:**
```
┌─────────────────────────────┐
│  NearBuzz          🔔 (3)   │  ← Header with notification bell + unread count
├─────────────────────────────┤
│                             │
│  Today                      │  ← Date separator
│  ┌─────────────────────┐    │
│  │ 🍛 Annapurna Mess   │    │  ← Business name + category icon
│  │ Today's Dinner Menu  │    │  ← Update title (structured type)
│  │                     │    │
│  │ Paneer Butter Masala │    │  ← Structured content
│  │ Dal Tadka, Rice      │    │
│  │ Roti (4), Salad      │    │
│  │                     │    │
│  │ 🟢 Available now     │    │  ← Status indicator
│  │ ₹80 per plate       │    │  ← Price
│  │                     │    │
│  │ 📞 Call  💬 WhatsApp │    │  ← Action buttons
│  │ Updated 45 min ago   │    │  ← Freshness timestamp
│  └─────────────────────┘    │
│                             │
│  ┌─────────────────────┐    │
│  │ 🖨️ QuickPrint        │    │
│  │ ⏰ Hours Changed     │    │  ← Update type badge
│  │                     │    │
│  │ Extended hours this  │    │
│  │ week (exams):       │    │
│  │ Open till 11 PM     │    │
│  │ (normally 8 PM)     │    │
│  │                     │    │
│  │ 📍 Directions        │    │  ← Contextual action
│  │ Updated 2 hours ago  │    │
│  └─────────────────────┘    │
│                             │
│  Yesterday                  │  ← Date separator
│  ┌─────────────────────┐    │
│  │ 📚 Campus Books      │    │
│  │ 📦 Item Restocked    │    │
│  │                     │    │
│  │ "Engineering Math    │    │
│  │ by BS Grewal"       │    │
│  │ Back in stock        │    │
│  │ ₹450                │    │
│  │                     │    │
│  │ 📞 Call  📍 Visit    │    │
│  └─────────────────────┘    │
│                             │
└─────────────────────────────┘
```

**Empty State (new user, no follows):**
```
┌─────────────────────────────┐
│                             │
│       📭                    │
│                             │
│   Your feed is empty        │
│                             │
│   Follow nearby businesses  │
│   to see their updates      │
│   here.                     │
│                             │
│   [Explore Nearby →]        │  ← CTA button to Explore tab
│                             │
│  ── Popular nearby ──       │  ← Show 3-4 nearby businesses
│                             │
│  ┌─────────────────────┐    │
│  │ Annapurna Mess      │    │
│  │ 📍 0.3 km · Mess    │    │
│  │        [+ Follow]   │    │
│  └─────────────────────┘    │
│                             │
└─────────────────────────────┘
```

**Feed Rules:**
- Show updates from followed businesses only (never inject ads or unrelated content in MVP)
- Group by date (Today / Yesterday / Earlier this week)
- Within a day, most recent first
- Expired updates (past `expires_at`) are hidden
- A "menu" update from the same business replaces the previous day's menu (not stacked)
- Distinguish update types visually: 🍛 Menu, ⏰ Hours, 📦 Stock, 💰 Price, 📢 Announcement
- Each update card shows: business name, update type, structured content, freshness, relevant action(s)

---

### Screen: Explore / Discovery

**Default State:**
```
┌─────────────────────────────┐
│  🔍 Search businesses...    │  ← Search bar (always visible)
├─────────────────────────────┤
│                             │
│  📍 Near: Kothrud, Pune     │  ← Current location (tappable to change)
│                             │
│  Categories                 │
│  ┌────┐ ┌────┐ ┌────┐      │
│  │ 🍛 │ │ 🖨️ │ │ 📚 │      │  ← Category chips (horizontal scroll)
│  │Mess│ │Print│ │Books│     │
│  └────┘ └────┘ └────┘      │
│                             │
│  Nearby                     │
│  ┌─────────────────────┐    │
│  │ [Photo]              │    │
│  │ Annapurna Mess       │    │
│  │ 🍛 Mess · 📍 0.3 km  │    │
│  │ ₹60-100/meal · 🟢 Open│    │  ← Key info at glance
│  │ "Updated menu today" │    │  ← Latest activity hint
│  │              [Follow] │    │
│  └─────────────────────┘    │
│                             │
│  ┌─────────────────────┐    │
│  │ [Photo]              │    │
│  │ QuickPrint Centre    │    │
│  │ 🖨️ Printing · 📍 0.5 km│
│  │ ₹1/page B&W · 🟢 Open│    │
│  │              [Follow] │    │
│  └─────────────────────┘    │
│                             │
│  [🗺️ View on Map]          │  ← Secondary: toggle to map view
│                             │
└─────────────────────────────┘
```

**Search Results State:**
```
┌─────────────────────────────┐
│  🔍 spiral binding ×        │  ← Active search
├─────────────────────────────┤
│                             │
│  3 results near Kothrud     │
│                             │
│  ┌─────────────────────┐    │
│  │ QuickPrint Centre    │    │
│  │ Spiral Binding: ₹30  │    │  ← Matched service highlighted
│  │ 📍 0.5 km · 🟢 Open  │    │
│  │ Updated 3 hours ago  │    │
│  └─────────────────────┘    │
│  ...                        │
└─────────────────────────────┘
```

**Map View (secondary):**
```
┌─────────────────────────────┐
│  🔍 Search...    [List ↕]   │
├─────────────────────────────┤
│                             │
│       ┌──────────────┐      │
│       │              │      │
│       │   Map with   │      │
│       │   pins for   │      │
│       │   businesses │      │
│       │              │      │
│       │    📍  📍     │      │
│       │  📍    📍     │      │
│       │     📍       │      │
│       └──────────────┘      │
│                             │
│  ┌─────────────────────┐    │  ← Bottom card for selected pin
│  │ QuickPrint · 0.5 km │    │
│  │ 🖨️ Printing · Open  │    │
│  │ [View Profile →]    │    │
│  └─────────────────────┘    │
│                             │
└─────────────────────────────┘
```

**Design Decisions:**
- List view is the default (not map). Users scan info faster in a list.
- Map is one tap away as a secondary view — useful for directions and spatial context.
- Category chips provide quick filtering. Search handles everything else.
- Each result card shows enough information (price range, distance, open/closed, latest activity) to decide whether to tap.
- Follow button visible directly on result cards — low friction.

---

### Screen: Business Profile Page

This is the page a user sees when they tap on a business. It must answer practical questions fast.

```
┌─────────────────────────────┐
│  ← Back                     │
├─────────────────────────────┤
│  ┌─────────────────────┐    │
│  │                     │    │
│  │   [Business Photos] │    │  ← Swipeable photo carousel
│  │                     │    │
│  └─────────────────────┘    │
│                             │
│  Annapurna Mess             │  ← Business name
│  🍛 Mess/Tiffin · 📍 0.3 km │  ← Category + distance
│                             │
│  🟢 Open now · Closes 9 PM  │  ← Live status
│  Updated 45 min ago         │  ← Freshness
│                             │
│  ┌──────────────────────┐   │
│  │ [♡ Follow]  [📞 Call] │   │  ← Primary actions
│  │ [💬 WhatsApp] [📍 Map]│   │
│  └──────────────────────┘   │
│                             │
│  ── About ──                │
│  Home-style veg meals near  │
│  MIT College. Monthly plans │
│  available.                 │
│                             │
│  ── Today's Menu ──         │  ← Category-specific section
│  Lunch (12-2 PM)            │
│  • Dal Fry, Rice, Roti (4)  │
│  • Aloo Gobi, Papad, Salad  │
│  ₹70/plate                  │
│                             │
│  Dinner (7-9 PM)            │
│  • Paneer Butter Masala     │
│  • Dal Tadka, Rice, Roti    │
│  ₹80/plate                  │
│                             │
│  ── Subscription Plans ──   │
│  Monthly Lunch: ₹2,100      │
│  Monthly Dinner: ₹2,400     │
│  Both Meals: ₹4,000         │
│                             │
│  ── Hours ──                │
│  Mon-Sat: 12 PM - 9 PM     │
│  Sunday: Closed             │
│  ⚠️ Closed Nov 5 (holiday)  │  ← Temporary exception
│                             │
│  ── Recent Updates ──       │
│  • Menu changed (2h ago)    │
│  • Added Diwali special     │
│    thali (yesterday)        │
│                             │
│  ── Location ──             │
│  Shop 4, Near MIT Gate,     │
│  Kothrud, Pune              │
│  [See on Map →]             │
│                             │
└─────────────────────────────┘
```

**Printing Shop Profile (different layout):**
```
│  ── Services & Prices ──    │  ← Category-specific section
│                             │
│  Printing                   │
│  • B&W (A4): ₹1/page       │
│  • Colour (A4): ₹5/page    │
│  • B&W (A3): ₹3/page       │
│                             │
│  Binding                    │
│  • Spiral: ₹30             │
│  • Soft bound: ₹50         │
│  • Hard bound: ₹150        │
│                             │
│  Other Services             │
│  • Lamination (A4): ₹20    │
│  • Scanning: ₹5/page       │
│  • Passport photos: ₹50    │
│                             │
│  📎 Accepts: PDF, DOC, JPG  │
│  💾 USB / WhatsApp / Email  │
│                             │
│  All prices updated 2d ago  │  ← Freshness for price data
```

**Key Profile Design Rules:**
- The profile layout adapts based on business category. Mess profiles emphasise menus and meal plans. Print shops emphasise services and per-unit pricing.
- Most important information (status, key offerings, actions) is above the fold.
- Freshness timestamps appear on information that changes (prices, menus, availability).
- Follow + Contact actions are always accessible (sticky or above the fold).
- "Recent Updates" section shows the latest 3-5 updates from this business.

---

### Screen: My Places (Following)

```
┌─────────────────────────────┐
│  My Places                  │
├─────────────────────────────┤
│                             │
│  Following (4)              │
│                             │
│  ┌─────────────────────┐    │
│  │ 🍛 Annapurna Mess    │    │
│  │ 📍 0.3 km · 🟢 Open  │    │
│  │ Last update: 45m ago │    │
│  │                     │    │
│  │ Notifications: Menu ✓│    │  ← Shows active prefs
│  │              Hours ✓ │    │
│  │              Promos ✗│    │
│  │                     │    │
│  │ [⚙️ Preferences]     │    │  ← Tap to change
│  │ [🔇 Mute] [Unfollow] │    │
│  └─────────────────────┘    │
│                             │
│  ┌─────────────────────┐    │
│  │ 🖨️ QuickPrint        │    │
│  │ 📍 0.5 km · 🔴 Closed│    │
│  │ Last update: 2h ago  │    │
│  │                     │    │
│  │ Notifications: All ✓ │    │
│  │ [⚙️ Preferences]     │    │
│  │ [🔇 Mute] [Unfollow] │    │
│  └─────────────────────┘    │
│                             │
└─────────────────────────────┘
```

**Follow Preferences Sheet (when tapping ⚙️):**
```
┌─────────────────────────────┐
│  Annapurna Mess             │
│  Notification Preferences   │
├─────────────────────────────┤
│                             │
│  🔔 Push notifications      │
│                             │
│  Menu updates        [✓ ON] │
│  Hours changes       [✓ ON] │
│  Price changes       [✓ ON] │
│  Availability        [✓ ON] │
│  Promotions/Offers   [✗ OFF]│
│  Announcements       [✗ OFF]│
│                             │
│  ──────────────────         │
│  🔇 Mute for...             │
│  [ 1 day ] [ 1 week ] [Off] │
│                             │
│  [Save Preferences]         │
│                             │
└─────────────────────────────┘
```

---

## 3. Vendor Interface — Information Architecture

### Navigation Structure

```
Bottom Navigation Bar (4 tabs):

┌──────────┬──────────┬──────────┬──────────┐
│ 🏠 Home  │ 📦 Items │ 📢 Update│ 📊 Stats │
│(Overview)│          │          │          │
└──────────┴──────────┴──────────┴──────────┘
```

---

### Screen: Vendor Home / Overview

The vendor opens the app and sees what matters right now.

```
┌─────────────────────────────┐
│  Annapurna Mess     [Edit ✏️]│  ← Business name + edit profile
├─────────────────────────────┤
│                             │
│  🟢 You're currently Open    │  ← Toggle to mark closed
│  Closes at 9:00 PM          │
│  [Mark as Closed Early]     │
│                             │
│  ── Quick Actions ──        │  ← Most common vendor tasks
│  ┌────────┐ ┌────────┐     │
│  │ 🍛     │ │ ⏰     │     │
│  │ Update │ │ Change │     │
│  │ Menu   │ │ Hours  │     │
│  └────────┘ └────────┘     │
│  ┌────────┐ ┌────────┐     │
│  │ 📢     │ │ 📦     │     │
│  │ Post   │ │ Update │     │
│  │ Update │ │ Stock  │     │
│  └────────┘ └────────┘     │
│                             │
│  ── Today ──                │
│  👥 12 followers             │
│  📢 1 update posted          │
│  👁️ 8 people viewed your    │
│     menu update              │
│                             │
│  ── Needs Attention ──      │
│  ⚠️ Tomorrow's menu not set  │  ← Actionable reminders
│  ⚠️ Holiday hours for Nov 5? │
│                             │
└─────────────────────────────┘
```

**Design Principle:** The vendor home is not a dashboard with charts. It's a **to-do list**: "Here's what you need to update, and here are shortcuts to do it fast."

---

### Screen: Vendor Quick Update Flow (Menu Example)

The single most important vendor flow. If this takes more than 30 seconds, the product fails.

```
Step 1: Select meal slot
┌─────────────────────────────┐
│  Update Menu                │
├─────────────────────────────┤
│                             │
│  Which meal?                │
│  ┌─────────┐ ┌─────────┐   │
│  │Breakfast│ │  Lunch  │   │  ← Tap to select
│  └─────────┘ └─────────┘   │
│  ┌─────────┐               │
│  │ Dinner  │               │
│  └─────────┘               │
│                             │
└─────────────────────────────┘

Step 2: Enter today's items
┌─────────────────────────────┐
│  Today's Dinner Menu        │
├─────────────────────────────┤
│                             │
│  Items (one per line):      │
│  ┌─────────────────────┐    │
│  │ Paneer Butter Masala │    │  ← Simple text input
│  │ Dal Tadka            │    │  ← Auto-suggest from previous menus
│  │ Rice                 │    │
│  │ Roti (4)             │    │
│  │ Salad                │    │
│  │ + Add item...        │    │
│  └─────────────────────┘    │
│                             │
│  Price: [₹ 80 ] per plate   │
│                             │
│  📸 Add photo (optional)    │
│                             │
│  🟢 Available now            │
│                             │
│  [Publish Menu Update →]    │
│                             │
│  This will notify followers │
│  who enabled menu updates.  │
│                             │
└─────────────────────────────┘

Step 3: Confirmation
┌─────────────────────────────┐
│  ✅ Menu Published!          │
│                             │
│  8 followers will see this  │
│  in their feed.             │
│  3 will receive a push      │
│  notification.              │
│                             │
│  [View as Customer →]       │  ← Preview what followers see
│  [Done]                     │
│                             │
└─────────────────────────────┘
```

**Key Vendor UX Decisions:**
- **Auto-suggest from history:** If the vendor posted "Dal Tadka" before, it should appear as a suggestion. Reduces typing.
- **Pre-fill from last time:** If dinner is usually ₹80 and has similar items, pre-fill and let the vendor edit only what changed.
- **One-tap quick updates:** "Mark unavailable today" for a specific item should be a single toggle, not a multi-step form.
- **Show the impact:** "8 followers will see this" reinforces why updating matters.
- **Preview:** "View as Customer" builds confidence in what's being published.

---

### Screen: Vendor Items/Products Management

```
┌─────────────────────────────┐
│  Items & Services           │
├─────────────────────────────┤
│                             │
│  🍛 Meals                    │
│  ┌─────────────────────┐    │
│  │ Lunch Thali          │    │
│  │ ₹70 · 🟢 Available   │    │
│  │ [Edit] [Unavailable] │    │  ← One-tap status toggle
│  └─────────────────────┘    │
│  ┌─────────────────────┐    │
│  │ Dinner Thali         │    │
│  │ ₹80 · 🟢 Available   │    │
│  │ [Edit] [Unavailable] │    │
│  └─────────────────────┘    │
│                             │
│  📋 Plans                    │
│  ┌─────────────────────┐    │
│  │ Monthly Lunch Plan   │    │
│  │ ₹2,100/month         │    │
│  │ [Edit] [Hide]        │    │
│  └─────────────────────┘    │
│                             │
│  [+ Add New Item]           │
│                             │
└─────────────────────────────┘
```

For a **Printing Shop**, the same screen adapts:

```
│  🖨️ Printing Services       │
│  ┌─────────────────────┐    │
│  │ B&W Print (A4)       │    │
│  │ ₹1/page · Available  │    │
│  │ [Edit] [Unavailable] │    │
│  └─────────────────────┘    │
│  ┌─────────────────────┐    │
│  │ Colour Print (A4)    │    │
│  │ ₹5/page · Available  │    │
│  │ [Edit] [Unavailable] │    │
│  └─────────────────────┘    │
│                             │
│  📎 Binding & Finishing     │
│  ┌─────────────────────┐    │
│  │ Spiral Binding       │    │
│  │ ₹30 · Available      │    │
│  │ [Edit] [Unavailable] │    │
│  └─────────────────────┘    │
```

---

### Screen: Publish Update

```
┌─────────────────────────────┐
│  Post an Update             │
├─────────────────────────────┤
│                             │
│  What changed?              │
│  ┌────────┐ ┌────────┐     │
│  │ 🍛 Menu│ │ ⏰ Hours│     │  ← Structured update types
│  └────────┘ └────────┘     │
│  ┌────────┐ ┌────────┐     │
│  │ 💰 Price│ │ 📦 Stock│    │
│  └────────┘ └────────┘     │
│  ┌────────┐ ┌────────┐     │
│  │ 🔴 Closed│ │ 📢 Other│   │
│  └────────┘ └────────┘     │
│                             │
│  (After selecting "Price")  │
│                             │
│  Which item?                │
│  [Lunch Thali ▼]            │  ← Select from existing items
│                             │
│  New price: [₹ 75]          │
│  Was: ₹70                   │
│                             │
│  Add a note (optional):     │
│  ┌─────────────────────┐    │
│  │ Price reduced for    │    │
│  │ exam week!           │    │
│  └─────────────────────┘    │
│                             │
│  📸 Add photo (optional)    │
│                             │
│  Importance:                │
│  (●) Important  ( ) Routine │
│                             │
│  Expires: [Never ▼]         │
│  Options: Never / 1 day /   │
│  3 days / 1 week / Custom   │
│                             │
│  [Publish Update →]         │
│                             │
└─────────────────────────────┘
```

---

## 4. Notification Design

### Notification Taxonomy

| Update Type | Default Importance | Default Push? | Notification Text Example |
|---|---|---|---|
| **Menu Change** | Important | Yes (if opted in) | "🍛 Annapurna Mess posted tonight's dinner menu" |
| **Hours Change** | Important | Yes | "⏰ QuickPrint is now open till 11 PM this week" |
| **Temporary Closure** | Important | Yes | "🔴 Annapurna Mess is closed tomorrow (Nov 5)" |
| **Stock/Availability** | Important | Yes (if opted in) | "📦 BS Grewal Engineering Math is back in stock at Campus Books" |
| **Price Change** | Informational | Feed only | "💰 Lunch Thali price changed to ₹75 at Annapurna Mess" |
| **New Item Added** | Informational | Feed only | "🆕 QuickPrint now offers passport photo printing" |
| **Promotion/Offer** | Promotional | Feed only (opt-in push) | "🎉 10% off on binding this week at QuickPrint" |
| **Announcement** | Varies (vendor chooses) | Depends on importance | "📢 Annapurna Mess: Diwali special thali available Nov 3-5" |

### User Control Rules

1. **On follow:** User gets a quick preference picker (pre-checked with sensible defaults)
2. **Defaults:** Important updates → push ON. Informational → feed only. Promotional → feed only.
3. **Override:** User can change any setting per business, any time.
4. **Mute:** Temporarily silence a business (1 day / 1 week / indefinitely).
5. **Global quiet hours:** No push notifications between 10 PM and 8 AM (user-configurable).
6. **Digest option (post-MVP):** "Send me a daily summary at 8 AM instead of individual pushes."

### Notification Format (Push)

```
┌─────────────────────────────┐
│ NearBuzz                    │
│ 🍛 Annapurna Mess            │
│ Tonight's dinner: Paneer    │
│ Butter Masala, Dal Tadka,   │
│ Rice, Roti · ₹80            │
│                      2m ago │
└─────────────────────────────┘
```

- Tap opens the update in the feed (not a generic app home screen).
- Notification includes enough info to be useful without opening the app.

---

## 5. Critical Empty & Error States

### Empty Explore (no businesses nearby)
```
🏪 No businesses here yet

NearBuzz is just getting started in your area.
Check back soon, or tell your favourite
local shops about us!

[Change Location]
```

### Empty Feed (follows exist but no updates)
```
😴 No new updates

The businesses you follow haven't posted
anything recently. Check their profiles
for the latest info.

[Explore More Businesses →]
```

### Vendor — No Followers Yet
```
👥 No followers yet

Your business is live! Students nearby can
find you in Explore. Post your first update
to start attracting followers.

Share your NearBuzz profile:
[Copy Link 📋]  [Share via WhatsApp]
```

### Offline State
```
📡 You're offline

We'll show your feed when you're
back online. Your followed businesses
are saved.
```

### Business Marked as Closed
```
🔴 This business is temporarily closed

Annapurna Mess marked itself closed.
Follow to get notified when it reopens.

[Follow for Reopening Update]
```

---

## 6. Visual Design Direction

### Colour Palette (Suggested)

| Role | Colour | Usage |
|---|---|---|
| **Primary** | Warm orange (#F97316) | Buttons, active states, brand accent — energetic, local, warm |
| **Secondary** | Deep teal (#0D9488) | Vendor interface accent, success states |
| **Background** | Off-white (#FAFAF9) / Dark (#1C1917) | Light/dark mode |
| **Surface** | White (#FFFFFF) / Dark (#292524) | Cards, sheets |
| **Text** | Stone-900 (#1C1917) / Stone-100 (#F5F5F4) | Primary text |
| **Muted** | Stone-500 (#78716C) | Secondary text, timestamps |
| **Status Green** | #22C55E | Open, available |
| **Status Red** | #EF4444 | Closed, unavailable |
| **Status Amber** | #F59E0B | Attention needed, expiring |

### Typography
- **Headings:** Inter (clean, modern, excellent readability)
- **Body:** Inter or system font stack
- **Hindi content:** Noto Sans Devanagari (Google Fonts, free)

### Iconography
- Lucide icons (included with shadcn/ui) — consistent, clean, open-source
- Category icons: custom or emoji-based (🍛 🖨️ 📚 🏠 🛒 📖)

### Motion
- Subtle transitions: card entries, tab switches, sheet reveals
- Framer Motion for feed update animations (new update slides in from top)
- No gratuitous animations — every motion should communicate state change

---

## 7. Responsive Considerations

| Viewport | Layout |
|---|---|
| **Mobile (< 640px)** | Primary design target. Bottom tab nav. Full-width cards. |
| **Tablet (640-1024px)** | Two-column explore results. Wider cards. |
| **Desktop (> 1024px)** | Left sidebar nav replacing bottom tabs. Three-column explore. Side-by-side feed + business detail. Vendor workspace with more visible sections. |

Mobile is the priority. Desktop should work but is not the primary use context for either students or shop owners.

