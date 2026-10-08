# Pipeline de release

Este repo replica el flujo de backend para el front:

- Las ramas de trabajo deben ser `feat/*` y abrir PR contra `production`.
- `production` es la rama protegida y el único disparador del release.
- `preflight-production-pr.yml` corre en PRs hacia `production`.
- `stage-production-artifacts.yml` corre en `push` a `production` (y `workflow_dispatch`): revalida el
  preflight de deploy y construye los artefactos **firmados** bajo el environment `production`.
- `deploy-production.yml` no escucha `push`: corre por `workflow_run` cuando *Stage production artifacts*
  termina, o a mano con `workflow_dispatch`. Descarga el artefacto ya firmado, publica y taggea, bajo el
  environment `production`.
- Firebase Test Lab queda fuera del pipeline; los E2E requeridos se certifican localmente mediante GitHub commit statuses.

## Versión única

La versión visible de Android e iOS vive en `gradle/app-version.properties`:

```properties
versionName=6.3.2
androidVersionCode=60
iosBuildNumber=48
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
(módulos a recompilar/testear y plataformas con evidencia E2E requerida) se deriva del grafo de módulos en
`scripts/module-graph.txt` — la fuente única que también valida `./gradlew verifyModuleGraph` contra los
`build.gradle.kts` reales:

- Cambios en `docs/`, `README.md`, `AGENTS.md` y `.codex/` no disparan release ni tests de app; ejecutan solo la
  puerta de vocabulario E2E (`e2e/tools/verify/verify-e2e-vocabulary.sh`, paso del job compartido). El skill de
  certificación (`.codex/skills/certify-tuindice-pr/`) además corre `verifyE2eContract`, porque sus herramientas
  las prueban los tests del harness.
- Cambios de feature prueban el módulo, sus dependientes transitivos (incluido `wizard`, que consume casi
  todas las features) y hosts relevantes; si un archivo está en el fingerprint E2E de una plataforma (fuentes de runtime, scripts del
  harness, mocks, catálogo; ver `e2e/scripts/shared/e2e-fingerprint.sh`) se exige la evidencia de esa plataforma.
- Cambios en `base`, `persistence`, `academiccore`, `maincore`, Gradle raíz o hosts amplían el alcance.
- Cambios en cualquier `build.gradle.kts`, `settings.gradle.kts` o en el propio grafo ejecutan
  `verifyModuleGraph` en preflight, así el grafo no puede derivar en silencio.
- Cada módulo impactado (y `app`) pasa por `:módulo:detekt` contra su baseline; cambios en
  `config/detekt/`, `.editorconfig` o cualquier `detekt-baseline.xml` ejecutan `detekt` completo sin marcar
  impacto de runtime ni evidencia E2E. El Gradle raíz también ejecuta `detekt` completo, pero además marca
  `has_release_impact=true` y exige la `local-certification-suite` en ambas plataformas.
- Cambios runtime exigen bump de versión.
- Cambios user-visible cubiertos por E2E exigen commit statuses locales exitosos.
- Cambios en `iosApp/scripts/build-kmp-framework.sh` o `ci-build-ios-host.sh` compilan el host device release;
  cambios en `ci-typecheck-ios-host.sh` ejecutan el typecheck; los demás `ci-*` solo disparan un smoke liviano en
  macOS. El resto de `iosApp/scripts/*` (`build-scenario-kit.sh`, `verify-ui-test-target.sh`,
  `add-ui-test-target.rb`) entra en la huella de iOS: exige la evidencia de iOS y el build del target de UI tests
  (`ios-uitest-preflight`); lo mismo vale para `.github/scripts/sync-app-version.sh` y
  `materialize-firebase-configs.sh`.
- El detector separa las tareas iOS en `ios_test_tasks` (compilación y tests de simulador) e `ios_host_tasks`
  (typecheck y builds del host); `ios_tasks` sigue emitiéndose como unión. Preflight las corre en cuatro jobs
  macOS: `ios-test-preflight` e `ios-host-preflight` en paralelo (así el wall-clock es el mayor de los dos y
  una falla de tests no espera al build del host), `ios-uitest-preflight` (construye la app y el bundle
  `TuIndiceUITests` con el mismo script que el harness local) y `e2e-harness-preflight` (los tests del harness
  completos, sin saltos, cuando cambia el contrato E2E). `verifyE2eContract` corre además en Linux, donde esos
  tests saltan con su razón impresa.

El preflight de PR no recibe secretos de producción: `:app:bundleRelease` firma con un keystore descartable y
configs Firebase placeholder (`.github/scripts/materialize-ci-placeholders.sh`). La firma real ocurre en
`stage-production-artifacts.yml` (jobs `stage-android` y `stage-ios`) bajo el environment `production`;
`deploy-production.yml` no firma nada, publica el artefacto que stage produjo.

### Caches de build

Los jobs de PR escriben el Gradle User Home cache (`cache-read-only: false`, scoped al PR), por lo que pushes
sucesivos del mismo PR reusan compilaciones del intento anterior. Como nada en `production` construye las
variantes de simulador iOS, `warm-ios-caches.yml` las compila tras cada push a `production` y publica el cache
bajo la identidad del job `ios-test-preflight` (vía `GRADLE_BUILD_ACTION_CACHE_KEY_JOB`), de modo que los PRs
nuevos restauran módulos no tocados como `FROM-CACHE`. Ese workflow es independiente de stage/deploy: su
resultado nunca bloquea un release. La cuota de cache del repo es 10 GB con evicción LRU; `cache-cleanup:
on-success` recorta las entradas antes de guardarlas.

Validación local del detector:

```bash
merge_base="$(git merge-base production HEAD)"
STATE_DIR=/tmp/tuindice-changes bash ./.github/scripts/detect-changed-app.sh "$merge_base" HEAD
```

## E2E local y statuses

Los E2E pesados se ejecutan localmente, no en Firebase Test Lab: escenarios nativos (UI Automator en Android,
XCUITest en iOS) contra el WireMock de `mocks/`. La evidencia es por plataforma y por fingerprint: el harness corre el
catálogo completo, un escenario por invocación, y guarda en un libro mayor qué escenarios pasaron para ese
fingerprint, de modo que una segunda invocación solo corre lo que todavía no está en verde. Cada corrida deja:

```text
build/e2e/runs/<runId>/                                   resumen, log, manifest.json, junit.xml y artefactos por intento
build/e2e/ledger/<platform>/<fingerprint>/ledger.json     libro mayor del fingerprint
build/e2e/certifications/<sha>/<platform>/manifest.json   copia del manifiesto de una corrida de evidencia que pasó
```

Comandos principales:

```bash
./gradlew e2eEvidenceAndroid
./gradlew e2eEvidenceIos
./gradlew e2eEvidence            # ambas plataformas
```

Dependen de `verifyE2eArtifactsFresh`, exigen árbol limpio y, cuando una plataforma queda completa con `HEAD` empujado
y visible en GitHub, publican el commit status exitoso que preflight exige para esa plataforma
(`E2E_PUBLISH_GITHUB_STATUS=0` corre sin publicar). Para diagnosticar un escenario sin producir evidencia:
`E2E_SCENARIOS=<id> ./gradlew e2eAndroid` (o `e2eIos`). El detalle del harness está en `e2e/README.md`; cómo certificar
una rama (veredictos, condiciones de parada, umbrales de entorno) en `.codex/skills/certify-tuindice-pr/`.

Los contextos publicados tienen formato:

```text
local-e2e/android/local-certification-suite
local-e2e/ios/local-certification-suite
```

con una única definición, `e2e_status_context` en `e2e/scripts/shared/ci-common.sh` (la carga `.github/scripts/common.sh`; `e2e.py contexts` la imprime). La
descripción es `Local E2E <plataforma> <N>/<N> passed for <sha7> fp <fp12>.`, seguida de los conteos que apliquen
(`retried`, `env`, `quarantined`, `overrides`).

Un status `success` no basta por sí solo: preflight exige que la descripción contenga el fingerprint
(`fp <12 hex>`) que corresponde al árbol del commit y la plataforma, y que el creator del status sea confiable
(dueño del repo o `github-actions[bot]`; configurable con la variable de repo
`E2E_TRUSTED_STATUS_CREATORS`, que `preflight-production-pr.yml` pasa al script). Un status fabricado
sin el fingerprint correcto se rechaza. El reuso por fingerprint también considera los heads de PRs asociados
al commit (API de GitHub), por lo que sobrevive a merges por squash.

## Stage y deploy

El release está partido en dos workflows encadenados: stage construye y firma, deploy publica.

`stage-production-artifacts.yml` (`push` a `production` o `workflow_dispatch`):

- `stage-preflight` corre `deploy-production.sh` en fase `preflight`: recalcula el diff contra el
  `production` anterior, decide `should_deploy` y **revalida los commit statuses E2E** contra el SHA de
  `production` reutilizando evidencia del head del PR por fingerprint; si `production` avanzó y el árbol ya
  no coincide, exige recertificación. Este es el único preflight del release: `deploy-production.yml` no
  corre ninguno.
- `stage-android` construye el AAB con el keystore real y el `google-services.json` real.
- `stage-ios` archiva y exporta el `.ipa` firmado con `DRY_RUN=1 ci-upload-ios-appstore.sh`, sin tocar App
  Store Connect.
- `assemble-artifact` junta AAB, IPA, `mapping.txt` y dSYMs en el artefacto `production-release-<sha>`
  (retención 14 días) con su `release-manifest.json`.

`deploy-production.yml` (`workflow_run` de *Stage production artifacts*, o `workflow_dispatch` con
`target_sha`):

- `resolve-artifact` localiza el artefacto `production-release-<sha>` de la corrida de stage; si no existe,
  el deploy se salta en vez de construir nada.
- Google Play: sube el AAB ya firmado del artefacto como draft en el track `production`.
- Apple: `xcrun altool --upload-app` con el `.ipa` del artefacto. En deploy no hay archive ni firma, y no se
  hace submit a review.
- Tag: `app-<versionName>` anotado al `target_sha`, creado solo después de ambos uploads.

Garantías:

- Los deploys se encolan (`cancel-in-progress: false`); un push posterior no cancela un upload en curso.
- Ambos uploads son idempotentes: consultan primero si el release de Play o el build de App Store Connect ya
  existen para esa versión y salen sin subir.
- Recuperación de un deploy perdido: la lógica de `should_deploy` vive en el preflight de **stage**, no en
  deploy. Si el tag `app-<versionName>` de la versión actual no existe, stage fuerza `should_deploy=true`
  aunque el push que lo disparó no tenga cambios de release — pero eso implica volver a correr *stage*, que
  no reanuda nada: reconstruye los artefactos desde el SHA sobre el que se dispara (la punta de `production`,
  no el SHA perdido), y el tag termina ahí. Para publicar exactamente el SHA perdido hay que disparar
  `deploy-production.yml` con `target_sha`, y solo funciona mientras el artefacto de aquella corrida de stage
  no haya expirado.

El mapping de Crashlytics viaja dentro del artefacto (`android/mapping.txt`) pero hoy no se sube a
Crashlytics: `uploadCrashlyticsMappingFileRelease` está deshabilitada salvo que
`TUINDICE_UPLOAD_CRASHLYTICS_MAPPING=1`, y ningún workflow la define. Como `:app:bundleRelease` corre en
stage, habilitarla subiría el mapping desde stage, no desde deploy.

Preflight local sin secretos (sin token, la revalidación E2E se omite con un warning):

```bash
DEPLOY_DIFF_BASE_SHA="$(git merge-base production HEAD)" \
DEPLOY_PRODUCTION_PHASE=preflight \
bash ./.github/scripts/deploy-production.sh
```

Dry-run local del deploy: valida el artefacto ya staged y no publica nada. Desde el split ya no construye ni
firma, así que necesita un `build/production-release/` con su `release-manifest.json` (por ejemplo, el
artefacto descargado de una corrida de stage):

```bash
DRY_RUN=1 bash ./.github/scripts/deploy-production.sh
```

Para reproducir localmente lo que sí construye y firma stage en iOS — archive y export del `.ipa` con
`destination=export`, sin tocar App Store Connect — se corre el script directamente (requiere los secretos de
firma):

```bash
DRY_RUN=1 bash ./iosApp/scripts/ci-upload-ios-appstore.sh
```

## Branch protection

Configurar `production` en GitHub con:

- Require a pull request before merging.
- Block direct pushes.
- Require status checks before merging.
- Require branches to be up to date before merging (el árbol certificado por E2E debe ser el que se mergea).
- Requerir `preflight-production-pr`.
- El environment `production` cubre tanto los jobs de firma de stage como los de publicación de deploy: una
  aprobación manual configurada ahí frena primero a `stage-android`/`stage-ios`.

## Secrets y variables

El preflight de PR corre completo **sin secretos**. Los secretos de producción se reparten entre los dos
workflows de release, ambos bajo el environment `production`, y la mayoría — incluidas las credenciales de
firma — se usa en **stage**, no en deploy.

Solo `stage-production-artifacts.yml` (firma y construcción de artefactos):

```text
ANDROID_GOOGLE_SERVICES_JSON_BASE64
ANDROID_RELEASE_KEYSTORE_BASE64
TU_INDICE_KEY_ALIAS
TU_INDICE_KEY_PASSWORD
TU_INDICE_KEY_STORE_PASSWORD
IOS_GOOGLE_SERVICE_INFO_PLIST_BASE64
APPLE_TEAM_ID
APPLE_DISTRIBUTION_CERTIFICATE_P12_BASE64
APPLE_DISTRIBUTION_CERTIFICATE_PASSWORD
APPLE_PROVISIONING_PROFILE_BASE64
```

Solo `deploy-production.yml` (publicación en Google Play):

```text
GCP_WORKLOAD_IDENTITY_PROVIDER
GCP_PLAY_PUBLISHER_SERVICE_ACCOUNT
```

En los dos workflows — stage autentica el `xcodebuild archive`/export, deploy hace el chequeo de idempotencia
y el `altool --upload-app`:

```text
APP_STORE_CONNECT_KEY_ID
APP_STORE_CONNECT_ISSUER_ID
APP_STORE_CONNECT_API_KEY_P8_BASE64
```

Al auditar permisos: el keystore de Android, el certificado de distribución de Apple y el provisioning
profile los ve `stage-production-artifacts.yml`, que se dispara en cada `push` a `production`.

Variables de repo opcionales (`vars`, no secrets):

```text
E2E_TRUSTED_STATUS_CREATORS
```

`preflight-production-pr.yml` la pasa a `preflight-production.sh`; si no está definida, la lista de creators
confiables sigue siendo el dueño del repo más `github-actions[bot]`.

La cuenta de Google debe tener permisos de Android Publisher sobre `com.gdavidpb.tuindice`, y la key de App Store Connect debe poder subir builds para el bundle iOS.
