# Pipeline de release

Este repo replica el flujo de backend para el front:

- Las ramas de trabajo deben ser `feat/*` y abrir PR contra `production`.
- `production` es la rama protegida y el único disparador de deploy.
- `preflight-production-pr.yml` corre en PRs hacia `production`.
- `deploy-production.yml` corre solo en `push` a `production`, bajo el environment `production`.
- Firebase Test Lab queda fuera del pipeline; los E2E requeridos se certifican localmente mediante GitHub commit statuses.

## Versión única

La versión visible de Android e iOS vive en `gradle/app-version.properties`:

```properties
versionName=6.0.0
androidVersionCode=39
iosBuildNumber=25
```

Android lee estos valores desde `app/build.gradle.kts`. iOS consume `iosApp/Config/Version.xcconfig`, pero ese
archivo es generado y no se edita a mano. Para regenerarlo localmente:

```bash
./gradlew syncAppVersion
```

Los wrappers de build iOS y el scheme compartido de Xcode lo sincronizan antes de compilar. La validación es
no mutante: si el `Version.xcconfig` commiteado quedó desincronizado, falla en vez de repararlo. Valida localmente con:

```bash
./gradlew verifyAppVersionSync
```

Para cambios runtime o release se debe subir `versionName`, `androidVersionCode` e `iosBuildNumber`. El tag anotado se crea como `app-<versionName>` solo después de publicar ambos drafts.

## Preflight

El detector compara el PR contra el merge-base de `production` y ejecuta solo piezas impactadas. El alcance
(módulos a recompilar/testear y suites E2E requeridas) se deriva del grafo de módulos en
`scripts/module-graph.txt` — la fuente única que también valida `./gradlew verifyModuleGraph` contra los
`build.gradle.kts` reales:

- Cambios docs/skills no disparan release ni tests de app.
- Cambios de feature prueban el módulo, sus dependientes transitivos (incluido `wizard`, que consume casi
  todas las features) y hosts relevantes; las suites E2E requeridas incluyen las de los dependientes.
- Cambios en `base`, `persistence`, `academiccore`, `maincore`, Gradle raíz o hosts amplían el alcance.
- Cambios en cualquier `build.gradle.kts`, `settings.gradle.kts` o en el propio grafo ejecutan
  `verifyModuleGraph` en preflight, así el grafo no puede derivar en silencio.
- Cada módulo impactado (y `app`) pasa por `:módulo:detekt` contra su baseline; cambios en
  `config/detekt/`, `.editorconfig`, cualquier `detekt-baseline.xml` o el Gradle raíz ejecutan `detekt`
  completo sin marcar impacto de runtime.
- Cambios runtime exigen bump de versión.
- Cambios user-visible cubiertos por E2E exigen commit statuses locales exitosos.
- Cambios en `iosApp/scripts/build-kmp-framework.sh` o `ci-build-ios-host.sh` compilan el host device release;
  cambios en `ci-typecheck-ios-host.sh` ejecutan el typecheck; el resto de `iosApp/scripts/*` solo dispara un
  smoke liviano en macOS.

El preflight de PR no recibe secretos de producción: `:app:bundleRelease` firma con un keystore descartable y
configs Firebase placeholder (`.github/scripts/materialize-ci-placeholders.sh`). La firma real ocurre solo en
deploy bajo el environment `production`.

Validación local del detector:

```bash
merge_base="$(git merge-base production HEAD)"
STATE_DIR=/tmp/tuindice-changes bash ./.github/scripts/detect-changed-app.sh "$merge_base" HEAD
```

## E2E local y statuses

Los E2E pesados se ejecutan localmente, no en Firebase Test Lab. Cada corrida genera evidencia en:

```text
build/e2e/certifications/<sha>/<platform>/<suite>/
```

La evidencia contiene `maestro.log`, `junit.xml`, salidas de Maestro y `manifest.json` con SHA, suite, plataforma, dispositivo, versión y hash del log.

Comandos principales:

```bash
./gradlew e2eMaestroEvidenceAndroid
./gradlew e2eMaestroEvidenceIos
./gradlew e2eMaestroEvidenceLocal
```

Por defecto estos comandos calculan el diff de la rama actual contra `production` u `origin/production`, ejecutan solo
las suites requeridas por ese alcance y publican los GitHub commit statuses exitosos que preflight exige. Si no pueden
resolver esa base, falla la resolucion de alcance; se puede pasar `E2E_BASE_SHA` para forzarla.

Para forzar una suite enfocada durante debugging:

```bash
E2E_MAESTRO_SUITE="$PWD/e2e/maestro/flows/suites/auth-suite.yaml" \
E2E_PUBLISH_GITHUB_STATUS=1 \
./gradlew e2eMaestroEvidenceAndroid
```

Los contextos publicados tienen formato:

```text
local-e2e/android/<suite>
local-e2e/ios/<suite>
```

Un status `success` no basta por sí solo: preflight exige que la descripción contenga el fingerprint
(`fp <12 hex>`) que corresponde al árbol del commit y la suite, y que el creator del status sea confiable
(dueño del repo o `github-actions[bot]`; configurable con `E2E_TRUSTED_STATUS_CREATORS`). Un status fabricado
sin el fingerprint correcto se rechaza. El reuso por fingerprint también considera los heads de PRs asociados
al commit (API de GitHub), por lo que sobrevive a merges por squash.

## Deploy

El deploy construye artefactos firmados y publica drafts:

- Google Play: AAB firmado, draft en track `production`.
- Apple: archive Release y upload a App Store Connect/TestFlight, sin submit a review.
- Crashlytics: el mapping file se sube solo en deploy real con `TUINDICE_UPLOAD_CRASHLYTICS_MAPPING=1`.
- Tag: `app-<versionName>` anotado al SHA de `production`, creado solo después de ambos uploads.

Garantías del deploy:

- Los deploys se encolan (`cancel-in-progress: false`); un push posterior no cancela un upload en curso.
- `should_deploy` no depende solo del diff: si el tag `app-<versionName>` de la versión actual no existe,
  el deploy se reanuda aunque el push que lo disparó no tenga cambios de release (recuperación de deploys
  perdidos). También se puede disparar a mano con `workflow_dispatch`.
- El deploy preflight revalida los statuses E2E contra el SHA de `production` reutilizando evidencia del
  head del PR por fingerprint; si `production` avanzó y el árbol ya no coincide, exige recertificación.

Preflight local sin secretos (sin token, la revalidación E2E se omite con un warning):

```bash
DEPLOY_DIFF_BASE_SHA="$(git merge-base production HEAD)" \
DEPLOY_PRODUCTION_PHASE=preflight \
bash ./.github/scripts/deploy-production.sh
```

Dry-run local o en CI, construyendo artefactos firmados pero sin publicar (requiere los secretos de firma;
en iOS archiva y exporta el `.ipa` localmente con `destination=export`, sin tocar App Store Connect):

```bash
DRY_RUN=1 bash ./.github/scripts/deploy-production.sh
```

## Branch protection

Configurar `production` en GitHub con:

- Require a pull request before merging.
- Block direct pushes.
- Require status checks before merging.
- Require branches to be up to date before merging (el árbol certificado por E2E debe ser el que se mergea).
- Requerir `preflight-production-pr`.
- Usar el environment `production` para deploy y aprobaciones si se quieren gates manuales.

## Secrets y variables

Los secretos solo se usan en `deploy-production.yml` bajo el environment `production`; el preflight de PR
corre completo sin secretos. Secrets requeridos para CI/CD:

```text
GCP_WORKLOAD_IDENTITY_PROVIDER
GCP_PLAY_PUBLISHER_SERVICE_ACCOUNT
ANDROID_GOOGLE_SERVICES_JSON_BASE64
IOS_GOOGLE_SERVICE_INFO_PLIST_BASE64
ANDROID_RELEASE_KEYSTORE_BASE64
TU_INDICE_KEY_ALIAS
TU_INDICE_KEY_PASSWORD
TU_INDICE_KEY_STORE_PASSWORD
APP_STORE_CONNECT_KEY_ID
APP_STORE_CONNECT_ISSUER_ID
APP_STORE_CONNECT_API_KEY_P8_BASE64
APPLE_TEAM_ID
APPLE_DISTRIBUTION_CERTIFICATE_P12_BASE64
APPLE_DISTRIBUTION_CERTIFICATE_PASSWORD
APPLE_PROVISIONING_PROFILE_BASE64
```

La cuenta de Google debe tener permisos de Android Publisher sobre `com.gdavidpb.tuindice`, y la key de App Store Connect debe poder subir builds para el bundle iOS.
