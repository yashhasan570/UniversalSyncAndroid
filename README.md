# Universal Sync Android

Continuous file synchronization for Android: keep your folders in sync between your phone and your other devices, open-source, end-to-end encrypted, no cloud account required.

Universal Sync Android is a fork of the Syncthing-Fork Android codebase, maintained as a private build.

## Contact

Questions or feedback? Reach us on Telegram: [@johnconstantine570](https://t.me/johnconstantine570)

## Building

Requires the Android SDK, JDK and the Go toolchain for the core:

```
./gradlew :app:assembleDebug
```

## Sync

`.github/workflows/sync.yml` pulls upstream changes from the Syncthing-Fork Android project daily, re-applies the Universal Sync Android branding via `scripts/migrate.py`, and pushes.
