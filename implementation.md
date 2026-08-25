# Brillo Shared WhatsApp Marketplace Implementation Plan

## Goal

Adjust Brillo Mall to support a `shared WhatsApp number first` marketplace model while preserving the option to add `dedicated business WhatsApp numbers` later.

This plan:

- keeps the current WhatsApp integration approach
- does **not** introduce Infobip or any other provider right now
- focuses on user experience, conversation routing, attribution, and phased backend changes

## Product Direction

Each business should be able to onboard and receive:

- a storefront link
- a shared WhatsApp entry link

The WhatsApp link should open WhatsApp with a **prefilled text message** that identifies the business the customer started from using the business slug.

Example:

```text
Hi, I'm interested in Brillo store whatsapp-test-mart
```

Brillo should then treat the conversation using three concepts:

- `entryBusiness`: the business whose link started the chat
- `activeBusiness`: the business the customer is currently shopping with
- `marketplaceMode`: chat started without a resolvable business context

This is the core UX correction:

- the conversation should **start with business context**
- the customer should still be able to **explore the marketplace**
- attribution should remain tied to the original entry business unless product rules say otherwise

## UX Principles

### Customer UX

- Customers should always know whether they are chatting with:
  - Brillo for a specific store
  - the general Brillo marketplace
- Customers should not feel trapped in one store forever.
- If they switch stores, Brillo should make that visible in-chat.
- Store context should be explicit before checkout, booking, or support escalation.

Suggested copy:

- Business entry:
  - `Welcome to Brillo. You're here for {Store Name}.`
- Marketplace fallback:
  - `Welcome to Brillo Marketplace. Choose a store or tell me what you need.`
- Store switch:
  - `You're now exploring {New Store Name}. You originally entered via {Entry Store Name}.`

### Business Owner UX

- Businesses should understand that the shared WhatsApp link:
  - starts a Brillo chat with their store preselected
  - attributes the customer entry to them
  - does not mean they own the shared WhatsApp number
- Business owners should later see:
  - chats started from their link
  - customers attributed to their link
  - orders/bookings created from their link
  - customers who later explored other stores

## Current-State Summary

The current codebase already has useful groundwork:

- businesses default to `WhatsappType.SHARED`
- there is fallback routing for a shared number when no dedicated business is resolved
- businesses may also have a stored `whatsappNumber` for future dedicated routing

However, the current behavior is not fully aligned yet:

- conversations are keyed too globally
- shared routing currently uses a shortlist, not a marketplace-wide eligible set
- dedicated routing is supported, but not fully isolated from shared routing rules

## Phased Implementation Plan

## Phase 0: Product and Domain Lock-In

### Objective

Freeze the behavior rules before deeper implementation.

### Decisions to lock

- A WhatsApp link uses a **prefilled text message**, not arbitrary interactive message types.
- A resolved business at chat entry becomes `entryBusiness`.
- `activeBusiness` may later change.
- If the entry text cannot resolve to a business, the conversation becomes `marketplaceMode`.
- Customers may browse other stores without losing original attribution.
- Checkout, booking, and service requests must always execute against the `activeBusiness`.

### Deliverables

- product rule document approved by engineering and product
- link/message slug format approved
- conversation-state vocabulary approved

### Recommended entry identifier format

Use the business slug inside the prefilled text.

Examples:

```text
Hi, I'm interested in store whatsapp-test-mart
Hi, I want to order from whatsapp-test-mart
```

Recommended rule:

- the slug should be machine-resolvable
- it should not rely on business display name alone
- it should remain stable even if storefront display name changes

## Phase 1: Shared Entry Link and Attribution Foundation

### Objective

Support store-specific shared-number entry into WhatsApp using the current provider integration.

### Scope

- generate a shared WhatsApp link for each business
- include the business slug in the prefilled message
- detect this slug from first inbound customer message
- resolve `entryBusiness`
- start the conversation in that business context
- fall back to marketplace mode if unresolved

### Backend tasks

- Add a clear business entry slug strategy.
- Add parser logic that extracts business slug from inbound text.
- Resolve the business before normal menu routing.
- Persist entry metadata on the conversation/session.

### Data/state additions

Add conversation-level or session-level fields for:

- `entryBusinessId`
- `activeBusinessId`
- `entrySource`
- `entrySlug`
- `marketplaceMode`

Recommended `entrySource` values:

- `WHATSAPP_SHARED_LINK`
- `WHATSAPP_DIRECT_TEXT`
- `MARKETPLACE_FALLBACK`
- `STORE_SWITCH`

### UX behavior

- If resolved:
  - welcome user into that business context
- If unresolved:
  - welcome user into the marketplace

### Exit criteria

- every business can expose a shared WhatsApp link
- first inbound message can reliably resolve business entry
- unresolved messages still enter a useful marketplace chat

## Phase 2: Conversation Identity and Routing Correction

### Objective

Fix the conversation model so shared routing now and dedicated routing later are both safe.

### Why this matters

Current conversation identity is too global. A customer should not have one flat conversation record that gets overwritten across contexts.

### Required design correction

Conversation identity should support:

- customer identity
- receiving business number / shared number identity
- conversation context

At minimum, the system must distinguish:

- customer chatting via shared Brillo number
- customer chatting via future dedicated merchant number
- customer moving between marketplace mode and business-specific mode

### Backend tasks

- Redesign conversation lookup strategy.
- Stop relying only on one global WhatsApp conversation key.
- Support separate persisted context for:
  - shared marketplace chat
  - dedicated business chat
  - business-attributed entry state

### Recommended approach

Treat conversation state as:

- `channelIdentity`: the receiving WhatsApp number
- `customerIdentity`: customer WhatsApp ID
- `currentContext`: marketplace or business

This gives a stable path for dedicated numbers later without changing product semantics again.

### Exit criteria

- a customer can start from one business link without corrupting other chat contexts
- future dedicated routing can be added without redesigning the entire conversation model again

## Phase 3: Marketplace Mode Experience

### Objective

Make shared-number fallback feel intentional instead of accidental.

### Scope

- unresolved entry becomes a true marketplace experience
- customers can search products, services, or stores from the shared chat
- business selection becomes marketplace-friendly

### UX changes

Current fallback should evolve from a narrow shortlist into:

- featured stores
- search prompt
- category prompt
- support option

Suggested first menu:

- `Shop this store`
- `Explore marketplace`
- `Find services`
- `Track order`
- `Talk to support`

If no store is resolved:

- `Browse stores`
- `Search products`
- `Find services`
- `Track order`
- `Talk to support`

### Backend tasks

- Replace limited shared route candidate selection with marketplace-eligible businesses.
- Align WhatsApp route candidates with storefront visibility rules.
- Reuse public search and public listing logic where possible.

### Exit criteria

- marketplace mode feels like a first-class user journey
- all eligible storefronts can participate in shared discovery

## Phase 4: Store Switching Without Losing Attribution

### Objective

Allow customers to move from the entry store to another store while preserving business attribution and clarity.

### Scope

- customer starts from Store A
- customer later explores Store B
- system preserves `entryBusiness = A`
- system changes `activeBusiness = B`

### UX rules

- On first switch, inform the user clearly.
- Before cart/order/service actions, confirm the active store.
- Support command flows like:
  - `switch store`
  - `browse marketplace`
  - `back to entry store`

### Analytics/attribution rules

Track separately:

- `entry business`
- `active business`
- `converted business`

This prevents confusion in reporting later.

### Exit criteria

- customers can switch stores safely
- order/booking actions always reference the right active business
- attribution still preserves original acquisition source

## Phase 5: Merchant Dashboard Visibility

### Objective

Make the shared-link model legible to business owners.

### Scope

Business owners should be able to see:

- their storefront link
- their shared WhatsApp link
- chats started from their link
- customers acquired from their link
- orders/bookings attributed to their link

Later enhancements:

- chats that switched away
- conversions completed in another store after starting from their link

### UX requirements

Dashboard language should be explicit:

- `Shared WhatsApp link`
- `Customers who started with your store`
- `Orders placed from your shared link`

Avoid implying:

- that the merchant owns the shared Brillo number
- that every chat from their link stays forever inside their store

### Exit criteria

- merchants can understand the value of the shared link
- attribution metrics are visible and trustworthy

## Phase 6: Dedicated Number Readiness

### Objective

Prepare the system so premium merchants can later move to dedicated WhatsApp numbers without breaking shared mode.

### Scope

- keep current shared mode as default
- preserve `WhatsappType.SHARED` and `WhatsappType.DEDICATED`
- ensure routing rules can branch cleanly later

### Readiness tasks

- isolate dedicated-number resolution from shared-link resolution
- ensure a dedicated business must match explicit dedicated routing rules
- avoid ambiguous `whatsappNumber` matching
- define migration behavior from shared to dedicated

### Migration vision

Future dedicated mode should mean:

- customers message merchant-specific number
- Brillo still powers automation
- business attribution becomes direct

Shared mode should continue to mean:

- customers message Brillo shared number
- Brillo attributes entry via shared link token

### Exit criteria

- dedicated support can be added later without redesigning shared mode

## Implementation Order Recommendation

Build in this order:

1. Phase 0: finalize product rules
2. Phase 1: shared link + entry token + business resolution
3. Phase 2: conversation identity correction
4. Phase 3: real marketplace fallback
5. Phase 4: store switching + attribution model
6. Phase 5: dashboard visibility
7. Phase 6: dedicated-number readiness hardening

## Testing Plan

### Unit tests

- business token parsing from inbound text
- resolved vs unresolved entry behavior
- attribution field assignment
- active business switching rules

### Integration tests

- customer enters through Store A shared link
- customer enters with invalid token and lands in marketplace mode
- customer starts in Store A and switches to Store B
- customer places order after switching stores
- customer returns to entry store context
- future-proof test: same customer on shared route vs dedicated route

### UX validation scenarios

- merchant shares WhatsApp link successfully
- customer understands they are in Brillo for a specific store
- customer understands when they enter marketplace mode
- customer understands when they switch stores

## Risks and Watchouts

- Free-text business resolution without stable tokens will be brittle.
- If conversation identity is not corrected early, later shared and dedicated support will conflict.
- If marketplace fallback remains a limited shortlist, shared mode will not feel like a real mall.
- If attribution and active context are mixed together, merchant reporting will become confusing.

## Non-Goals for This Plan

- provider migration from current WhatsApp API
- Infobip integration
- dedicated merchant number implementation now
- advanced paid attribution integrations

## Recommended First Engineering Slice

The best first slice is:

1. define the shared WhatsApp link format
2. define the entry token format
3. parse inbound entry token
4. persist `entryBusiness` and `marketplaceMode`
5. respond with business-context or marketplace-context welcome flows

This delivers visible product value quickly without immediately forcing the deeper dedicated-number work.
