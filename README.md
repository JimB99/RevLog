# RevLog

Personal vehicle logbook for cars and motorcycles—track specifications and service history, export/import `.revlog` backups.

**Status:** Personal Android app; maintained for daily use. Default UI language is **German** (`de-AT`); `en-GB` and `es-ES` scaffolding exists. Screenshots: add PNGs under `docs/screenshots/` when available.

---

## Why

RevLog keeps structured vehicle data and service entries offline-first in Room, with a versioned JSON backup format for sharing between devices.

---

## Stack

- Kotlin, Jetpack Compose, Material 3
- Hilt, Room, Navigation Compose
- Modules: `app`, `core-domain`, `core-data`

---

## Build

See **[docs/BUILD.md](docs/BUILD.md)** for SDK paths, release keystore, and workspace toolchain.

```bash
./gradlew assembleDebug
./scripts/check.sh
```

---

## Export format

Files use extension `.revlog` (JSON, versioned). See `core-data/.../RevLogBackupManager.kt`.

---

## Localization

- Default: German (`values-de-rAT`)
- Infrastructure for `en-GB` and `es-ES` prepared

---

## Roadmap

Maintenance reminders: [docs/reminders.md](docs/reminders.md).

---

## License

MIT — see [LICENSE](LICENSE).
