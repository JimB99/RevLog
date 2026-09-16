# Maintenance reminders (future)

RevLog v1 does not schedule notifications. `POST_NOTIFICATIONS` is declared in the manifest for a future release.

## Goal

Let users configure reminders per vehicle and service type, e.g.:

- Motorölwechsel every 12 months
- Pickerl every 24 months
- Custom interval: 6 / 12 / 24 / 36 months or user-defined

## Proposed model

```kotlin
data class ReminderRule(
    val id: Long,
    val vehicleId: Long,
    val target: ServiceType,
    val intervalMonths: Int,
    val leadDays: Int = 14,
    val enabled: Boolean = true,
    val anchor: ReminderAnchor = ReminderAnchor.LAST_SERVICE_DATE,
)

enum class ReminderAnchor {
    LAST_SERVICE_DATE,
    FIXED_DATE,
}
```

## Due date calculation (`core-domain`)

Pure function `NextDueCalculator.nextDue(rule, logs, vehicleData, today)`:

1. Find last service log for `rule.target`.
2. If present: `nextDue = lastDate + intervalMonths`.
3. Else optional fallback: Erstzulassung or purchase date (user setting).
4. Notify when `today >= nextDue - leadDays`.

## Scheduling (`app` module)

- `ReminderCheckWorker` — daily WorkManager job (same pattern as Gatekeep `UsageSyncWorker`).
- Scan enabled rules, post notification if due.
- Notification channel: **Wartung**.
- Tap opens vehicle Service tab.

## UI (future)

- Settings → **Erinnerungen** (master toggle).
- Service row long-press → **Erinnerung einrichten**.
- Presets: 6 Monate, 1 Jahr, 2 Jahre, 3 Jahre, Benutzerdefiniert.

## Why WorkManager

Daily batch is sufficient for maintenance windows, survives reboots, and avoids per-rule `AlarmManager` complexity.
