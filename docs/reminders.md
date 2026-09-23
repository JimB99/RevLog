# Maintenance reminders

**Status: implemented** (RevLog 1.1.0)

Users configure reminders per vehicle and service type via the bell icon on each service row.

## Model

See `ReminderRule` in `core-domain` and `reminder_rules` Room table in `core-data`.

## Due date calculation

`NextDueCalculator.nextDue(rule, lastServiceDate)` — last service date plus interval months.

`NextDueCalculator.shouldNotify(rule, lastServiceDate, today)` — true when today is within `leadDays` before due.

## Scheduling

- `ReminderCheckWorker` — daily WorkManager job with initial delay aligned to the configured notify time
- `ReminderScheduler` — reschedules work when settings or reminder rules change (uses earliest effective time across enabled rules)
- Default notify time: **09:00** (Settings → **Standard-Erinnerungszeit** / **Default reminder time**)
- Per-reminder override: optional custom time in `ReminderSetupSheet` (null = use global default)
- Per-rule gate: notifications only fire when current time is at or past that rule's effective notify time
- Master toggle: Settings → **Erinnerungen**
- Notification channel: **Wartung**
- Tap opens vehicle Service tab

WorkManager periodic jobs may drift slightly from the exact minute; acceptable for maintenance reminders within `leadDays`.

## UI

- Bell icon on each service row (filled when reminder enabled)
- Row tap still adds a service entry
- `ReminderSetupSheet` — interval presets 6 / 12 / 24 / 36 months + custom, lead days picker, optional notify time override
