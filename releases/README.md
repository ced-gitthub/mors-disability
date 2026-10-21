# Release channels
Every built jar is kept here, in the folder for its channel.

- alpha/   experimental, may be broken. Version: `X.Y.Z-alpha.N+26.2`
- beta/    feature complete, still being tested. Version: `X.Y.Z-beta.N+26.2`
- release/ fully tested, stable. Version: `X.Y.Z+26.2`

To publish: set `version=` in gradle.properties, run `gradlew build`, copy `build/libs/mors-disability-<version>.jar` into the matching folder.
