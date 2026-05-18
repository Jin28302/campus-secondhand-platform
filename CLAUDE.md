# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
npm run dev      # Start dev server
npm run build    # Production build → /dist
npm run preview  # Preview production build
```

No test or lint scripts are configured.

## Architecture

Vue 3 e-commerce SPA with three user roles: `user`, `merchant`, `admin`.

**Auth:** Token stored in `localStorage` as `token`, role as `role`. `src/utils/request.js` attaches the Bearer token to every request and unwraps `response.data`. All API calls go through this instance with `baseURL: '/api'`.

**Routing:** `src/router/index.js` guards routes via `meta.requiresAuth` and `meta.roles`. Unauthenticated users are redirected to `/login`; wrong-role users are redirected to `/products`.

**Views by role:**
- Public: `Login`, `Register`, `Products`, `ProductDetail`
- User (`requiresAuth`): `Cart`, `Checkout`, `Orders`, `Profile`
- Merchant: `Merchant`, `MerchantProducts`, `MerchantOrders`
- Admin: `Admin`, `AdminAuditUsers`, `AdminAuditProducts`, `AdminUsers`, `AdminMerchantLevel`

**API modules** live in `src/api/` and import from `@/utils/request`. Add new API files there rather than calling axios directly.

**UI:** Element Plus is the component library — use its components for forms, tables, dialogs, etc. `RefundDialog` and `ReviewDialog` in `src/components/` are reusable dialog wrappers.

**Path alias:** `@` resolves to `src/`.
