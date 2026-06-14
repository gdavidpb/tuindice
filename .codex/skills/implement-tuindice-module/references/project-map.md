# TuIndice Project Map

## Repo Shape

- `app`: Android host app, Android bootstrap, Android platform services, BuildConfig-backed environment values.
- `iosApp`: Swift host app and Xcode project. Boots the shared `maincore` framework and passes iOS runtime capabilities.
- `maincore`: shared app assembler. Owns shared Koin bootstrap, feature registration, shared navigation host, and shared host UI.
- `base`: shared contracts, base repositories, MVI primitives, reusable UI, logging, common helpers, and networking foundations.
- `academiccore`: shared academic-domain model and projection engine used by academic features.
- `persistence`: shared Room database, DAOs, entities, and platform database factories.
- `testkit`: shared test doubles and helpers for Koin, coroutines, flows, and Compose UI tests.
- Feature modules:
  - `auth`
  - `about`
  - `summary`
  - `record`
  - `evaluations`
  - `enrollmentproof`
  - `subjects`
  - `pensum`
  - `wizard`

## Current Module Dependency Rules

- `base`: no project dependencies.
- `academiccore`: no project dependencies.
- `persistence`: depends on `:base`.
- `about`: depends on `:base`.
- `auth`: depends on `:base`.
- `summary`: depends on `:base`, `:persistence`.
- `record`: depends on `:academiccore`, `:base`, `:persistence`.
- `enrollmentproof`: depends on `:base`, `:persistence`.
- `evaluations`: depends on `:academiccore`, `:base`, `:persistence`.
- `subjects`: depends on `:academiccore`, `:base`, `:persistence`.
- `pensum`: depends on `:academiccore`, `:base`, `:persistence`.
- `wizard`: depends on `:academiccore`, `:base`, `:summary`, `:record`, `:evaluations`, `:subjects`, `:pensum`, `:about`, and `:enrollmentproof`.
- `maincore`: depends on `:base`, `:persistence`, and every feature module.
- `app`: depends on `:base`, `:maincore`, `:persistence`, and every feature module used by the Android host.

Default rule: features should point to shared infrastructure, not to each other.
Current intentional exception: `wizard -> features`, because `wizard` is an onboarding orchestrator that reuses real screens with synthetic state.

## Root Files That Define The Architecture

- `README.md`
  - source of truth for architecture, boundaries, Koin conventions, and validation expectations
- `settings.gradle.kts`
  - registers every Gradle module
- `build.gradle.kts`
  - root verification tasks such as `verifySharedCompilation`, `verifySharedTests`, `verifyCommonUiGate`, `verifyModuleGraph`, and iOS host checks
- `scripts/validate-module-graph.sh`
  - machine-checked module dependency graph; must stay in sync with the graph documented in `README.md`
- `gradle/libs.versions.toml`
  - shared dependency and plugin catalog

If you create a new shared module, inspect `build.gradle.kts` and extend any root task lists that should include it.

## Mock Environment Map

- `mocks/mappings/<feature-or-domain>/`
  - WireMock request matchers grouped by backend area; dynamic domains can stay intentionally small and delegate behavior to transformers
- `mocks/__files/<feature-or-domain>/`
  - larger JSON bodies referenced from mappings via `bodyFileName`
- `mocks/config/*.json`
  - declarative base-state inputs consumed by the stateful mock runtime
- `mocks/extensions/src/`
  - Kotlin WireMock extensions and response transformers for stateful behavior
- `mocks/start-mock-enviroment.sh`
  - starts the local WireMock server used by the app mock environment

If an app-facing HTTP contract changes, update the relevant mock mappings and referenced JSON bodies in the same change so the mock environment does not drift from the real client contract.

## Local E2E Map

- `e2e/maestro/flows/`
  - executable Maestro flows grouped by module, with `_shared/` for reusable setup, reset, login, and navigation helpers
- `e2e/scripts/`
  - local build, reset, WireMock startup, and Maestro runner scripts for Android and iOS
- `e2e/platform/android/`
  - Android-only edge tests or instructions for Compose/Espresso/UI Automator cases Maestro cannot cover stably
- `e2e/platform/ios/`
  - iOS-only edge tests or instructions for XCUITest cases Maestro cannot cover stably
- `testkit/e2e/`
  - reusable E2E contract: flow catalog, selector policy, fixture contract, local runbook, critical selector list, and validator

Rules that matter:

- Maestro is the default local E2E runner for app flows on Android and iOS.
- Platform-specific UI tests are reserved for system/host/technology edges, not duplicate happy paths.
- Update `testkit/e2e/flow-catalog.yaml` and the affected flow when a user-visible module flow changes.
- Keep local E2E compatible with future Firebase Test Lab by producing stable artifacts from root scripts, but do not add `gcloud` commands in the local-only phase.

## KMP Build Conventions

Typical shared UI feature module pattern:

- plugins:
  - `kotlin("multiplatform")`
  - `id("com.android.kotlin.multiplatform.library")`
  - `alias(libs.plugins.compose.multiplatform)`
  - `alias(libs.plugins.compose.compiler)`
  - `alias(libs.plugins.kotlin.serialization)` when needed
- targets:
  - `android { namespace; compileSdk = 37; minSdk = 24; androidResources { enable = true } }`
  - `iosArm64()`
  - `iosSimulatorArm64()`
- source sets:
  - `commonMain`
  - `commonTest`
  - optional `androidMain`
  - optional `iosMain`

Exceptions:

- `persistence` does not use Compose, adds KSP and Room compiler wiring.
- `academiccore` is pure shared domain and does not use Compose.
- `base` is also shared UI/infrastructure and exports many shared APIs.
- `maincore` produces the iOS framework binary and assembles all features.
- `app` is a plain Android application module, not a KMP module.

## Feature Module Anatomy

User-facing feature modules usually keep this shape in `src/commonMain/kotlin/.../<feature>/`:

- `data/`
- `di/`
- `domain/`
- `presentation/`
- `ui/`

Common subpackages already used in this repo:

- `domain/repository`
- `domain/usecase`
- `domain/usecase/error`
- `domain/usecase/exceptionhandler`
- `domain/usecase/validator`
- `presentation/action`
- `presentation/contract`
- `presentation/mapper`
- `presentation/model`
- `presentation/navigation`
- `presentation/route`
- `presentation/utils`
- `presentation/viewmodel`
- `ui/screen`
- `ui/view`
- `ui/dialog`

Not every feature needs every subpackage, but new modules should stay close to existing patterns.

## Koin File Map

Shared bootstrap and module registration live in:

- `maincore/src/commonMain/kotlin/com/gdavidpb/tuindice/di/CommonModule.kt`
- `maincore/src/commonMain/kotlin/com/gdavidpb/tuindice/di/MainModule.kt`
- `maincore/src/commonMain/kotlin/com/gdavidpb/tuindice/di/SharedModules.kt`
- `maincore/src/commonMain/kotlin/com/gdavidpb/tuindice/di/AppKoinBootstrap.kt`

Platform bootstrap lives in:

- `app/src/main/kotlin/com/gdavidpb/tuindice/di/AndroidKoinBootstrap.kt`
- `app/src/main/kotlin/com/gdavidpb/tuindice/di/AndroidPlatformModule.kt`
- `maincore/src/iosMain/kotlin/com/gdavidpb/tuindice/di/IosKoin.kt`
- `maincore/src/iosMain/kotlin/com/gdavidpb/tuindice/di/IosPlatformModule.kt`

Rules that matter:

- shared infra goes into `commonModule`
- feature modules register their own view models, processors, use cases, repositories, and data sources
- Android-only feature bindings are added to `androidPlatformModule`
- iOS-only feature bindings are added to `iosPlatformModule`
- do not introduce per-feature `AndroidModule` or `IosModule`

## Navigation Integration Map

Feature navigation lives inside each feature module, for example:

- `summary/.../presentation/navigation/SummaryNavigation.kt`
- `record/.../presentation/navigation/RecordNavigation.kt`
- `evaluations/.../presentation/navigation/EvaluationsNavigation.kt`
- `auth/.../presentation/navigation/AuthNavigation.kt`
- `pensum/.../presentation/navigation/PensumNavigation.kt`
- `subjects/.../presentation/navigation/SubjectsNavigation.kt`
- `wizard/.../presentation/navigation/WizardNavigation.kt`

Shared integration lives in:

- `maincore/src/commonMain/kotlin/com/gdavidpb/tuindice/ui/screen/TuIndiceNavHost.kt`
- `maincore/src/commonMain/kotlin/com/gdavidpb/tuindice/presentation/model/BottomBarConfig.kt`
- `maincore/src/commonMain/kotlin/com/gdavidpb/tuindice/ui/screen/TuIndiceScreen.kt`

Implications:

- new user-facing modules usually need a navigation builder inside the feature
- if the feature is reachable from the main host, `TuIndiceNavHost.kt` must integrate it
- if the feature is a top-level tab, also update `BottomBarConfig.kt` and `TuIndiceScreen.kt`

## What `maincore` Really Owns

`maincore` is not just another feature module. It owns:

- `commonModule`
- `mainModule`
- `featureModules()` and `commonModules()`
- shared app startup via `startAppKoin(...)`
- the shared `NavHost`
- the shared top-level screen shell and bottom bar
- the iOS framework binary setup

If a new module is part of the shared app runtime, expect `maincore` to change.

## Platform Integration Rules

Android-specific services belong in `app`, for example:

- environment values via `BuildConfig`
- Firebase
- Play services integrations
- Android browser, file opener, network, device, reporting, and update services

iOS-specific services belong in `maincore/src/iosMain`, for example:

- `IOSContext` capabilities
- iOS storage and networking factories
- iOS adapters for browser, device, reporting, review, update, and app info

If a feature needs a new platform capability on iOS, thread it through the existing iOS host capability model instead of inventing a new global access pattern.

## Test And Validation Map

Feature module smoke tests:

- `<feature>/src/commonTest/.../<Feature>ModuleKoinSmokeTest.kt`

Shared helpers:

- `testkit/src/commonMain/kotlin/com/gdavidpb/tuindice/testkit/koin/KoinSmokeTestUtils.kt`
- `testkit/src/commonMain/kotlin/com/gdavidpb/tuindice/testkit/mvi/MachineRandomWalk.kt` (seeded model-based walks for machine contract tests)
- `testkit/src/commonMain/kotlin/com/gdavidpb/tuindice/testkit/e2e/E2eFixtureContract.kt` (Kotlin mirror of `testkit/e2e/fixture-contract.env`)

E2E contract and local runners:

- `./gradlew verifyE2eContract`
- `./gradlew e2eMaestroAndroid`
- `./gradlew e2eMaestroIos`
- `./gradlew e2eMaestroLocal`
- `./gradlew e2ePlatformAndroid`
- `./gradlew e2ePlatformIos`

Shared bootstrap smoke tests:

- `maincore/src/commonTest/.../MainModuleKoinSmokeTest.kt`
- `maincore/src/iosTest/.../IosAppKoinSmokeTest.kt`

Useful checks are already present in the repo:

- `./gradlew --continue --console=plain :<module>:compileAndroidMain`
- `./gradlew --continue --console=plain :<module>:compileKotlinIosSimulatorArm64`
- `./gradlew --continue --console=plain :<module>:allTests`
- `./gradlew --continue --console=plain :maincore:iosSimulatorArm64Test --tests '*IosAppKoinSmokeTest*'`
- `./gradlew --continue --console=plain verifyModuleGraph`
- `./gradlew --continue --console=plain verifySharedCompilation`
- `./gradlew --continue --console=plain verifySharedTests`
- `./gradlew --continue --console=plain verifySharedHostTests` (android host JVM — the only platform where machine alphabet/Λ validators enforce)
- `./gradlew --continue --console=plain detekt` (per-module baselines)
- `./gradlew --continue --console=plain koverHtmlReport` (coverage measurement, no thresholds)
- `./gradlew --continue --console=plain verifyCommonUiGate`
- `./gradlew --continue --console=plain verifyIosHostTypecheck`

Match the validation scope to the change. Do not jump to full-repo checks unless the edit actually crosses module boundaries broadly.
