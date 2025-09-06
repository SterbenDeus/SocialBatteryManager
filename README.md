# SocialBatteryManager

SocialBatteryManager is an Android application for managing your social energy.

## Release Signing

This project uses a private keystore to self‑sign release builds. The signing
keystore and its credentials are stored as CI secrets and supplied at build
time. GitHub Actions decodes the base64‑encoded keystore and exports the
following variables before running `assembleRelease`:

- `RELEASE_STORE_FILE`
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

For local release builds, set the same variables in your environment:

```bash
export RELEASE_STORE_FILE=/absolute/path/to/release.keystore
export RELEASE_STORE_PASSWORD=yourStorePassword
export RELEASE_KEY_ALIAS=yourKeyAlias
export RELEASE_KEY_PASSWORD=yourKeyPassword
./gradlew assembleRelease
```

The keystore file itself is **not** checked into the repository. Ensure the
secrets remain private and rotate them if the keystore is ever compromised.
