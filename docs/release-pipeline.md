# Pipeline de release

Este branch de `production` contiene la app Android legacy. El flujo replica el backend con ramas `feat/*`, PR obligatorio hacia `production`, preflight obligatorio y deploy automatico solo cuando el merge llega a `production`.

## Alcance actual

- Preflight en PR hacia `production`: valida version, detecta cambios relevantes y ejecuta solo tareas Android necesarias.
- Deploy en push a `production`: construye un AAB firmado, publica un draft en Google Play track `production` y crea el tag anotado `app-<versionName>`.
- Firebase Test Lab y E2E locales quedan fuera de este branch legacy.
- App Store Connect ya puede tener secretos cargados, pero el upload iOS queda diferido hasta que `iosApp` exista en `production`.

## Version unica

La fuente de version vive en `gradle/app-version.properties`:

```properties
versionName=6.0.0
androidVersionCode=39
iosBuildNumber=25
```

Android lee `versionName` y `androidVersionCode` desde `app/build.gradle`. `iosBuildNumber` queda reservado para mantener la misma fuente cuando entre la app iOS.

Valida localmente con:

```bash
./gradlew verifyAppVersionSync
```

Para cambios runtime o release se debe subir al menos `versionName` y `androidVersionCode`. El deploy falla si `app-<versionName>` ya existe apuntando a otro SHA.

## Preflight

El detector compara el PR contra el merge-base de `production`.

- Cambios en docs no disparan tests ni release.
- Cambios solo en tests ejecutan `:app:testDebugUnitTest`.
- Cambios en app runtime, Gradle, version o CI ejecutan la bateria enfocada de Android.
- Cambios release ejecutan `:app:testDebugUnitTest`, `:app:bundleRelease` y exigen bump de version.
- Cambios de CI ejecutan tambien `verifyAppVersionSync`.

Validacion local del detector:

```bash
merge_base="$(git merge-base production HEAD)"
STATE_DIR=/tmp/tuindice-changes bash ./.github/scripts/detect-changed-app.sh "$merge_base" HEAD
```

## Deploy

El deploy corre en `ubuntu-latest` bajo el environment `production`.

1. Revalida version, tag y bump requerido.
2. Materializa `app/google-services.json` desde `ANDROID_GOOGLE_SERVICES_JSON_BASE64`.
3. Materializa el keystore release desde `ANDROID_RELEASE_KEYSTORE_BASE64`.
4. Construye `:app:bundleRelease`.
5. Sube mapping de Crashlytics solo en deploy con `TUINDICE_UPLOAD_CRASHLYTICS_MAPPING=1`.
6. Crea un edit en Google Play, sube el AAB y deja un draft en el track `production`.
7. Crea y pushea el tag anotado `app-<versionName>` solo despues del draft.

Dry-run local:

```bash
DRY_RUN=1 bash ./.github/scripts/deploy-production.sh
```

## Branch protection

`production` debe tener:

- Require a pull request before merging.
- Block direct pushes.
- Require status checks before merging.
- Required check: `preflight-production-pr`.
- Environment `production` para el deploy.

## Secrets requeridos ahora

```text
GCP_WORKLOAD_IDENTITY_PROVIDER
GCP_PLAY_PUBLISHER_SERVICE_ACCOUNT
ANDROID_GOOGLE_SERVICES_JSON_BASE64
ANDROID_RELEASE_KEYSTORE_BASE64
TU_INDICE_KEY_ALIAS
TU_INDICE_KEY_PASSWORD
TU_INDICE_KEY_STORE_PASSWORD
```

La service account debe tener permisos de Android Publisher sobre `com.gdavidpb.tuindice`.

## Secrets preparados para iOS futuro

Estos secretos no son usados por este branch legacy, pero quedan listos para el branch donde `iosApp` llegue a `production`:

```text
IOS_GOOGLE_SERVICE_INFO_PLIST_BASE64
APP_STORE_CONNECT_KEY_ID
APP_STORE_CONNECT_ISSUER_ID
APP_STORE_CONNECT_API_KEY_P8_BASE64
APPLE_TEAM_ID
APPLE_DISTRIBUTION_CERTIFICATE_P12_BASE64
APPLE_DISTRIBUTION_CERTIFICATE_PASSWORD
APPLE_PROVISIONING_PROFILE_BASE64
```
