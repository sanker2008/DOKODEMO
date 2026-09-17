# Release signing

Release packages must use a private production key; the tracked debug.keystore is public and only suitable for development.

Configure GitHub Actions secrets: DOKODEMO_KEYSTORE_BASE64, DOKODEMO_STORE_PASSWORD, DOKODEMO_KEY_ALIAS, DOKODEMO_KEY_PASSWORD. The workflow decodes the keystore into runner temporary storage, verifies source in CI, signs the release, then removes the key. Manual runs upload artifacts; only tag runs publish releases.

For another build environment, supply DOKODEMO_KEYSTORE (absolute path) and the three password/alias variables via environment or private Gradle properties. Never commit credentials. Release packaging fails when credentials are absent.

Existing installations signed with the public debug key cannot ordinarily be upgraded with a new unrelated key. The release owner must plan the signing transition and data export/reinstallation or supported signing lineage. Changing the old key password does not fix its exposure. No new production key has been generated in this change.

No local build or package was run for these fixes at the user request. CI and device verification remain required before release.
