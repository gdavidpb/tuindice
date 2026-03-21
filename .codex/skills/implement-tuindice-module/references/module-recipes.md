# Module Recipes

## 1. Pick The Correct Module Kind

- Existing feature change:
  - use the nearest existing feature as the template
- New user-facing feature:
  - start from `summary`, `record`, `about`, or `evaluations`
- Shared infrastructure:
  - start from `base`, `persistence`, or `testkit`
- Host/bootstrap change:
  - work in `maincore`, `app`, or `iosApp`

If the request does not fit one of those buckets, pause and explain why before inventing a new module category.

## 2. Modify An Existing Feature Module

1. Inspect these files first:
   - `<module>/build.gradle.kts`
   - `<module>/src/commonMain/.../di/<Feature>Module.kt`
   - view model, action processor, use case, repository, and navigation files closest to the change
2. Keep constructor and interface changes mirrored in:
   - the feature Koin module
   - the Koin smoke test overrides
   - any route or screen entry points that resolve the changed view model
3. If the module already uses validators and exception handlers for similar use cases, keep that pattern for new use cases too.
4. For repository-backed screen state, prefer the split already used in `summary` and `record`:
   - repository exposes `observe*Flow()` and `update*()`
   - observe use case reads local state only
   - update use case refreshes remote or recomputes, then persists local state
   - `ViewModel` starts observation as `initialAction`
   - `Route` triggers the first refresh and retries with `LaunchedEffect` or retry callbacks
5. Add visible strings to `src/commonMain/composeResources/values/`.
6. If the change updates an HTTP contract consumed by the app, update the mock environment in the same change:
   - WireMock mappings live under `mocks/mappings/<feature-or-domain>/`
   - referenced JSON bodies live under `mocks/__files/<feature-or-domain>/`
   - check both request matchers and response payloads, not just the happy-path body
7. If the change adds or changes destinations:
   - update the feature `*Navigation.kt`
   - update `maincore/.../TuIndiceNavHost.kt` if the host must navigate to it
   - if the destination is a feature dialog, prefer `dialog<Destination>` in navigation over rendering the dialog from feature state
   - for dialog destinations, pick one result pattern explicitly:
     - resolve the parent/shared `ViewModel` from the dialog destination and dispatch actions directly when the dialog only edits parent state
     - use `base/.../NavigationResult.kt` when the dialog must return an intent to the previous destination and that destination must run a lifecycle-sensitive side effect after the dialog closes
   - when using `NavigationResult`, create a dedicated `@Serializable` result type per flow instead of raw primitives; for sealed results, send them with the base generic type so the writer and collector share the same key
8. Validate with targeted compilation plus the feature smoke test and the smallest relevant contract/UI tests.

## 3. Create A New KMP Feature Module

1. Prefer scaffolding first:
   - `python3 scripts/scaffold_feature_module.py scaffold-feature --module attendance --feature-name Attendance --screen-title "Asistencia"`
   - add `--with-persistence` when the feature depends on `:persistence`
   - add `--with-device-tests` when the feature starts with Android device UI tests
2. If the feature follows the baseline shared-runtime pattern, wire the repo automatically:
   - `python3 scripts/scaffold_feature_module.py integrate-feature --module attendance --feature-name Attendance --screen-title "Asistencia"`
3. Inspect the generated baseline and adapt names, resources, contracts, domain types, and integrations to the real feature.
4. Add the module to `settings.gradle.kts`.
5. Create `<module>/build.gradle.kts` from the closest feature:
   - keep namespace, `compileSdk = 36`, and `minSdk = 24`
   - add iOS targets
   - use Compose Multiplatform only if the module has shared UI
   - add `implementation(project(":base"))` by default
   - add `implementation(project(":persistence"))` only if the feature truly needs persistence
   - add `implementation(project(":testkit"))` in `commonTest`
6. Create the `src/commonMain` package tree with the same package prefix used elsewhere:
   - `com.gdavidpb.tuindice.<module>`
7. Add the feature public Koin module:
   - `val <feature>Module = module { ... }`
   - keep the same registration order used in current modules:
     - view models
     - action processors
     - use cases
     - validators
     - repositories
     - data sources
     - exception handlers
8. Implement the feature stack:
   - `presentation`: contracts, processors, routes, navigation, view models
   - `domain`: models, repository interfaces, use cases, validators, exception handlers
   - `data`: repository implementation, data sources, mappers
   - `ui`: screens, views, dialogs
9. Keep the UI boundary intact:
   - `Navigation` resolves the `ViewModel`
   - `Route` owns lifecycle collection and side-effect bridging
   - `Screen` receives plain state and callbacks only
10. If the feature is part of the app runtime:
   - add it to `maincore/build.gradle.kts`
   - add it to `app/build.gradle.kts` if Android host code must package it
   - register it in `maincore/.../SharedModules.kt`
11. If the feature has navigation:
   - create `<Feature>Destination`
   - create `<feature>Navigation(...)`
   - integrate it in `maincore/.../TuIndiceNavHost.kt`
12. If the feature is a top-level tab:
   - update `maincore/.../BottomBarConfig.kt`
   - update `maincore/.../TuIndiceScreen.kt`
   - keep icon selection, order, and test tags aligned
13. If the feature needs platform adapters:
   - Android bindings go in `app/.../AndroidPlatformModule.kt`
   - iOS bindings go in `maincore/src/iosMain/.../IosPlatformModule.kt`
   - do not create per-feature platform modules
14. Add `<Feature>ModuleKoinSmokeTest.kt` and resolve every public view model or entry point.
15. Add focused contract or UI tests for non-trivial behavior.

## 4. Create Or Modify Shared Infrastructure Modules

Use existing shared modules as templates:

- `base`
  - shared contracts, repositories, MVI primitives, reusable UI, logging, common helpers
- `persistence`
  - Room entities, DAOs, converters, database creation, platform DB factories, KSP wiring
- `testkit`
  - fakes, test helpers, smoke-test utilities, UI-test helpers

Rules:

- keep shared modules narrow and reusable
- avoid putting feature behavior into shared infrastructure
- update consumers intentionally rather than widening dependencies casually
- if you add a new shared module, update root verification tasks in `build.gradle.kts` where that module should participate

## 5. Modify `maincore`, `app`, Or `iosApp`

Work in `maincore` when the change affects:

- shared Koin bootstrap
- shared host UI or navigation shell
- feature registration
- iOS framework generation
- iOS shared platform bindings

Work in `app` when the change affects:

- Android bootstrap
- Android-only services or SDKs
- `BuildConfig` environment values
- Android platform Koin bindings

Work in `iosApp` when the change affects:

- Swift host lifecycle
- Xcode project config
- host bootstrap wrappers
- iOS runtime capability plumbing

When an iOS capability changes, keep the existing `IOSContext` / host-capability pattern instead of introducing ad hoc globals.

## 6. Root-Level Files Commonly Forgotten

These files are easy to miss when adding or widening a module:

- `settings.gradle.kts`
- `build.gradle.kts`
- `gradle/libs.versions.toml`
- `README.md`
- `maincore/build.gradle.kts`
- `app/build.gradle.kts`
- `maincore/.../SharedModules.kt`
- `maincore/.../TuIndiceNavHost.kt`
- `maincore/.../BottomBarConfig.kt`
- `maincore/.../TuIndiceScreen.kt`

Treat that as a checklist, not a guarantee that every file must change.

## 7. Validation Matrix

Use the smallest truthful set:

- Gradle or dependency change in one module:
  - `:<module>:compileAndroidMain`
  - `:<module>:compileKotlinIosSimulatorArm64`
- Feature DI or constructor change:
  - feature smoke test
- `maincore` Koin/bootstrap change:
  - `:maincore:commonTest`
  - `:maincore:iosSimulatorArm64Test --tests '*IosAppKoinSmokeTest*'` when iOS bootstrap is affected
- Shared UI or navigation change:
  - focused module tests
  - `verifyCommonUiGate` only when the change is broad enough to justify it
- Root shared-module change:
  - `verifySharedCompilation`
  - `verifySharedTests` when relevant
- iOS host Swift or framework boundary change:
  - `verifyIosHostTypecheck`

Report any gaps explicitly.

## 8. Stop Conditions

Pause and call out the issue before implementing if the requested change would:

- add a new feature-to-feature dependency
- require per-feature platform Koin modules
- move Android-only logic into `commonMain`
- bypass `maincore` for shared app assembly
- break the repo rules documented in `README.md` without updating them
