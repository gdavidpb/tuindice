# TuIndice Project Map

## Repo Shape

- `app`: Android host app, Android bootstrap, Android platform services, BuildConfig-backed environment values.
- `iosApp`: Swift host app and Xcode project. Boots the shared `maincore` framework and passes iOS runtime capabilities.
- `maincore`: shared app assembler. Owns shared Koin bootstrap, feature registration, shared navigation host, and shared host UI.
- `base`: shared contracts, base repositories, MVI primitives, reusable UI, logging, common helpers, and networking foundations.
- `persistence`: shared Room database, DAOs, entities, and platform database factories.
- `testkit`: shared test doubles and helpers for Koin, coroutines, flows, and Compose UI tests.
- Feature modules:
  - `auth`
  - `about`
  - `summary`
  - `record`
  - `evaluations`
  - `enrollmentproof`

## Current Module Dependency Rules

- `base`: no project dependencies.
- `persistence`: depends on `:base`.
- `about`: depends on `:base`.
- `auth`: depends on `:base`.
- `summary`: depends on `:base`, `:persistence`.
- `record`: depends on `:base`, `:persistence`.
- `enrollmentproof`: depends on `:base`, `:persistence`.
- `evaluations`: depends on `:base`, `:persistence`, and legacy `:record`.
- `maincore`: depends on `:base`, `:persistence`, and every feature module.
- `app`: depends on `:base`, `:maincore`, `:persistence`, and every feature module used by the Android host.

Default rule: features should point to shared infrastructure, not to each other.

## Root Files That Define The Architecture

- `README.md`
  - source of truth for architecture, boundaries, Koin conventions, and validation expectations
- `settings.gradle.kts`
  - registers every Gradle module
- `build.gradle.kts`
  - root verification tasks such as `verifySharedCompilation`, `verifySharedTests`, `verifyCommonUiGate`, and iOS host checks
- `gradle/libs.versions.toml`
  - shared dependency and plugin catalog

If you create a new shared module, inspect `build.gradle.kts` and extend any root task lists that should include it.

## Mock Environment Map

- `mocks/mappings/<feature-or-domain>/`
  - WireMock request matchers and inline stub responses grouped by backend area
- `mocks/__files/<feature-or-domain>/`
  - larger JSON bodies referenced from mappings via `bodyFileName`
- `mocks/start-mock-enviroment.sh`
  - starts the local WireMock server used by the app mock environment

If an app-facing HTTP contract changes, update the relevant mock mappings and referenced JSON bodies in the same change so the mock environment does not drift from the real client contract.

## KMP Build Conventions

Typical shared UI feature module pattern:

- plugins:
  - `kotlin("multiplatform")`
  - `id("com.android.kotlin.multiplatform.library")`
  - `alias(libs.plugins.compose.multiplatform)`
  - `alias(libs.plugins.compose.compiler)`
  - `alias(libs.plugins.kotlin.serialization)` when needed
- targets:
  - `android { namespace; compileSdk = 36; minSdk = 24; androidResources { enable = true } }`
  - `iosX64()`
  - `iosArm64()`
  - `iosSimulatorArm64()`
- source sets:
  - `commonMain`
  - `commonTest`
  - optional `androidMain`
  - optional `iosMain`

Exceptions:

- `persistence` does not use Compose, adds KSP and Room compiler wiring.
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

Shared bootstrap smoke tests:

- `maincore/src/commonTest/.../MainModuleKoinSmokeTest.kt`
- `maincore/src/iosTest/.../IosAppKoinSmokeTest.kt`

Useful checks already present in the repo:

- `./gradlew --continue --console=plain :<module>:compileAndroidMain`
- `./gradlew --continue --console=plain :<module>:compileKotlinIosSimulatorArm64`
- `./gradlew --continue --console=plain :<module>:allTests`
- `./gradlew --continue --console=plain :maincore:iosSimulatorArm64Test --tests '*IosAppKoinSmokeTest*'`
- `./gradlew --continue --console=plain verifySharedCompilation`
- `./gradlew --continue --console=plain verifySharedTests`
- `./gradlew --continue --console=plain verifyCommonUiGate`
- `./gradlew --continue --console=plain verifyIosHostTypecheck`

Match the validation scope to the change. Do not jump to full-repo checks unless the edit actually crosses module boundaries broadly.
