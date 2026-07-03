---
title: Persistence Schema
description: Basic description and linkage to the different schemas managed on the program
author: TheAbsdag
---

# Persistence Schema

This document is a version index for the project's JSON persistence schema. Each version corresponds to the state of the data files at a specific commit or release on the project history.

---

## Version Index

| Version | App Version | Commit | Date | Description |
|---------|-------------|--------|------|-------------|
| [V0.1.2](V0.1.2.md) | 0.1.2 | `b73fbaf` | 2025 - May 2026 | Initial stable schema after recovering a lost version. No version field, durations in hours, `swaped` typo. |
| [V0.1.3](V0.1.3.md) | 0.1.3 | `4b57cd48a4d6` | May 2026 | Standardised V2. All files carry `"version": 2`, durations in seconds, `swapped` typo fix, refund breakdown, sealed activity hierarchy. |
| [V0.1.4.1](V0.1.4.1.md) | 0.1.4.1 | `aa1b232` (HEAD) | July 2026 | CSV export format + JSON v3. Tower numbers 0-based, `RoomData` records, automated v2→v3 migration. |

---

## Quick comparison

| Aspect | V0.1.2 | V0.1.3 — V0.1.4 | V0.1.4.1 |
|--------|--------|--------|--------|
| Version field | None | `"version": 2` in every file | `"version": 3` in every file |
| Room durations | hours (`service`, `extension`) | seconds (`serviceDuration`, `extensionDuration`) | seconds (unchanged) |
| Durations backward compat | — | Read `service`/`extension` → `* 3600` if current key missing | Same + v2→v3 migration |
| `endStatus` for free rooms | empty string `""` | absent / not set | absent / not set |
| RoomSwap spelling | `swaped*` (typo) | `swapped*` (corrected) — reads both | `swapped*` — `@JsonAlias` handles legacy |
| Refund totals | lump `totalRefunds` | split `totalItemRefunds` + `totalRoomRefunds` | split (unchanged) |
| Turn activity model | `model.Turn` (raw JSON) | `model.turn.TurnDetails` (sealed subtypes) | `model.turn.TurnDetails` (sealed + `RoomData`) |
| Inventory selling cart | `Register.sellingList` (JSONArray) | `CartItem` records + `SaleItem` records | unchanged |
| Tower numbering | 1-based | 1-based | 0-based (migrated from v2) |
| Room coordinates | Individual fields per activity | Individual fields per activity | `RoomData` record embedded |
| Report export | None | XLSX only | XLSX + CSV companion |

---

## File inventory

All versions use the same 5 persistence file types:

| Directory | Contents | Since |
|-----------|----------|-------|
| `data/applicationProperties` | Motel identity, printer, transaction counter, room grid | V0.1.2 |
| `data/roomsInformation` | Room states (status, timers, durations) | V0.1.2 |
| `data/turn` | Active turn: financial totals + activity log | V0.1.2 |
| `data/inventory` | Item catalog: prices, stock | V0.1.2 |
| `history/{turn}-{timestamp}` | Completed turn archives (same schema as `data/turn`) | V0.1.2 |
| `reports/` | XLSX + CSV turn report exports (not JSON) | V0.1.4 |

---

## Conventions

- **Timezone**: `America/Bogota` — all timestamps use `ZonedDateTime` with this zone.
- **Version field**: Always at the top level. Read via `optInt("version", 0)` — no automated migration; field-level fallbacks only.
- **Monetary values**: All amounts in COP (Colombian pesos), stored as `long`.
- **Data directories**: All gitignored.
