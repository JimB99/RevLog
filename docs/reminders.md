# Maintenance reminders

**Status: implemented** (RevLog 1.1.0)

Users configure reminders per vehicle and service type via the bell icon on each service row.

## Model

See `ReminderRule` in `core-domain` and `reminder_rules` Room table in `core-data`.

## Due date calculation

`NextDueCalculator.nextDue(rule, lastServiceDate)` — last service date plus interval months.

`NextDueCalculator.shouldNotify(rule, lastServiceDate, today)` — true when today is within `leadDays` before due.

## Scheduling

- `ReminderCheckWorker` — daily WorkManager job
- Master toggle: Settings → **Erinnerungen**
- Notification channel: **Wartung**
- Tap opens vehicle Service tab

## UI

- Bell icon on each service row (filled when reminder enabled)
- Row tap still adds a service entry
- `ReminderSetupSheet` — interval presets 6 / 12 / 24 / 36 months + custom, lead days picker
