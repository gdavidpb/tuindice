---
name: implement-tuindice-module
description: Create or modify Kotlin Multiplatform modules and app frontend flows in this `tuindice` app repository. Use when adding a new feature or shared module, changing an existing module's architecture, Gradle setup, Koin wiring, navigation, platform bindings, root verification tasks, smoke tests, or local E2E coverage with Maestro/XCUITest/Compose/Espresso/UI Automator for modules such as `base`, `persistence`, `maincore`, `app`, `auth`, `about`, `summary`, `record`, `evaluations`, `enrollmentproof`, `subjects`, `pensum`, `wizard`, and `testkit`.
---

# Implement TuIndice Module

## Overview

Implement module work by copying the nearest existing module pattern instead of inventing a new architecture. This repo is a Kotlin Multiplatform app with shared feature modules, shared infrastructure, a shared assembler in `maincore`, an Android host in `app`, and an iOS host in `iosApp`.

## Start Here

- Load [references/project-map.md](references/project-map.md) when you need the current architecture, module roles, integration points, or configuration rules.
- Load [references/module-recipes.md](references/module-recipes.md) when you need the concrete checklist for creating or modifying a module.
- Load [references/scaffolding.md](references/scaffolding.md) when you want to bootstrap a new module or render boilerplate for individual architecture components.
- Load [references/pensum-layout.md](references/pensum-layout.md) when updating pensum fixtures, importer output, Mongo seed data, or frontend mocks that include graph node positions and edge routes.
- Load [references/e2e.md](references/e2e.md) when a change affects user-visible flows, navigation, selectors, local mock behavior, app host startup/reset, or module acceptance coverage.
- Use the closest existing module as a template:
  - `summary` for a feature with dialog destinations plus typed back results for lifecycle-sensitive platform effects
  - `evaluations` for a feature with multiple screens and dialog destinations that dispatch directly into the parent `ViewModel`
  - `record` for a straightforward feature
  - `about` for a lighter feature with platform adapters
  - `persistence` for shared data storage and KSP/Room wiring
  - `base` for shared contracts, MVI primitives, reusable UI, and cross-feature infrastructure
  - `maincore` for shared assembly, navigation host, and Koin bootstrap
- If you need a fast starting point, use `scripts/scaffold_feature_module.py` before hand-writing boilerplate. It can scaffold a baseline feature module, integrate that feature into the repo wiring, and render templates for `ViewModel`, `Route`, `ActionProcessor`, `UseCase`, `Repository`, `Navigation`, `Screen`, and smoke tests.

## Workflow

1. Classify the change first: existing feature, new feature, shared infrastructure, or host/bootstrap change.
2. Inspect the nearest existing module and mirror its package layout, Gradle plugins, and DI style.
3. For new work, prefer scaffolding from `scripts/scaffold_feature_module.py` or `assets/templates/` and then adapt the result instead of rewriting the same boilerplate by hand.
4. Default to `commonMain`; move code to `androidMain` or `iosMain` only for real platform needs.
5. Wire Koin in the owner module only:
   - shared infra in `maincore/.../CommonModule.kt`
   - feature dependencies in `<feature>/.../<Feature>Module.kt`
   - Android platform bindings in `app/.../AndroidPlatformModule.kt`
   - iOS platform bindings in `maincore/src/iosMain/.../IosPlatformModule.kt`
6. When a feature exposes repository-backed screen data, separate local observation from remote refresh:
   - repository interfaces should expose an `observe*Flow()` read path and an explicit `update*()` refresh path
   - if the local read is one-shot cached data instead of an observable flow, expose a local/fresh read such as `getFresh*()` separately from a remote refresh such as `refresh*()`
   - observe use cases should read local flows only
   - update use cases should fetch or recompute and then persist back into the local state
   - when an empty local snapshot is ambiguous before the first remote response, persist a local `hasSynced*` flag with the confirmed snapshot and expose it as `observeHasSynced*Flow()` from the repository
   - use the persisted `hasSynced*` flag, not transient global sync progress, to distinguish initial loading from a real empty state: local content wins; empty plus `hasSynced=false` stays `Loading`; empty plus `hasSynced=true` becomes `Empty`
   - `ViewModel` initial actions should usually start observation, while `Route` triggers the first refresh with `LaunchedEffect`
   - initial screen state should be neutral (`Idle`) when local data can arrive immediately; full-screen `Loading` should be emitted only by a remote refresh path, or by an observed synced-status branch that is explicitly waiting for remote data
7. When a change alters an HTTP contract consumed by the app, update the local WireMock fixtures in the same change:
   - request and response mappings live under `mocks/mappings/<feature-or-domain>/`
   - referenced response bodies live under `mocks/__files/<feature-or-domain>/`
   - keep fixture payloads aligned with the current request shape, response shape, and status codes
   - update E2E fixture assumptions or scenario reset expectations when the contract is covered by a local flow
8. Keep the UI boundary explicit:
   - `Navigation` resolves the `ViewModel`
   - `Route` bridges `state/effect` and lifecycle to the pure `Screen`
   - `Screen` stays free of Koin and business wiring
9. Update smoke tests and focused contract/UI tests when constructor wiring or public entry points change.
10. For user-visible flow changes, update the E2E catalog and Maestro flow for the affected module unless the change is intentionally not covered yet; document platform-specific edge cases instead of duplicating them in Maestro.
11. Run the smallest truthful verification set and report anything left unverified.

## Non-Negotiable Project Rules

- Keep the dependency flow pointed inward. Do not add new feature-to-feature dependencies without explicit approval. There are no feature-to-feature exceptions today besides the intentional `wizard -> features` orchestration. The module graph is enforced by `./gradlew verifyModuleGraph`; update `scripts/validate-module-graph.sh` together with `README.md` when a module boundary changes.
- Preserve `presentation -> domain -> data -> di` separation. Interfaces live in `domain`; implementations live in `data`; `di` only wires them.
- Within that separation, use this finer layering order when a module needs multiple data origins: `presentation -> domain -> data/repository -> data/source -> di`.
- `domain/repository` contains business-facing contracts used by action processors, use cases, or view models. These interfaces should end with `Repository`.
- `data/repository` contains internal data-layer interfaces for compositions over multiple origins only when a real internal abstraction is needed. These interfaces should end with `DataRepository`.
- `data/source` contains concrete implementations, whether they implement a `domain/repository` or `data/repository` contract directly. These classes should end with `DataSource`.
- Keep domain models in `domain/model`.
- Keep domain-only mappers in `domain/mapper`.
- Keep data-layer models in `data/model`.
- Keep data-layer mappers in `data/mapper`.
- If a `domain` contract needs multiple internal origins or shared coordination, define an internal `*DataRepository` interface in `data/repository` only when the data layer truly needs that extra abstraction, and keep all concrete implementations in `data/source`.
- A `*DataSource` may implement a `domain/repository` contract directly when the domain contract maps cleanly to a single concrete origin.
- A `*DataSource` may also implement a `*DataRepository` contract when it is one concrete origin behind a multi-origin data flow.
- Do not create interface types with a `DataSource` suffix.
- Do not place concrete orchestrator classes in `data/repository`. `data/repository` is for interfaces; implementations stay in `data/source`.
- Do not create trivial `*DataRepository : *Repository` aliases. If a `DataSource` can implement the domain repository directly, prefer that.
- Do not create `*DataRepository` for single-origin repositories. If there is only one concrete origin, keep that implementation in `data/source` and have it implement the domain `Repository` directly.
- In `commonModule`, default shared runtime services and infrastructure repositories to `single`; use `factory` only when the object is intentionally transient or has no shared identity/state.
- `ViewModel` classes extend `BaseViewModel` and delegate work to `ActionProcessor` classes.
- Each `ActionProcessor` must own exactly one action subtype. Do not branch on `action` inside an `ActionProcessor`; dispatch from the `ViewModel`'s `processAction(...)` and keep each processor focused on a single `Action` class/object.
- Use `*Params` types only when a single use case actually needs input data. Do not use a sealed/object `Params` type to multiplex unrelated operations through one use case; split those flows into separate use cases instead.
- When a user flow has preparatory steps plus a final side effect, keep the final side effect in its own dedicated use case named after that final user action, and let presentation orchestrate when to call it.
- Do not hide that final side effect behind a shared helper like `perform*()` or inside preparatory use cases. Preparatory use cases should validate state, read pending work, or flush prerequisites; the processor may then invoke the final `*UseCase` explicitly.
- For repository-backed feature state, prefer the `summary` and `record` split:
  - `Observe*UseCase` reads local state only
  - `Update*UseCase` refreshes and persists explicitly
  - if absence of local data is ambiguous before the first completed remote refresh, persist a feature-owned sync-state row such as `EvaluationSyncStateEntity` or `AcademicRecordSyncStateEntity`, expose it as `observeHasSynced*Flow()`, and include it in the observed domain/presentation model
  - reducers should mirror `evaluations` and `record`: when observed content exists, show `Content`; when no content exists and `hasSynced*` is true, show `Empty`; when no content exists and `hasSynced*` is false, keep `Failed` if already failed, otherwise show `Loading`
  - do not use `SyncRepository.observeSyncInProgress()` to decide whether an empty local snapshot is real; that flow is transient and can miss the pre-sync or failed-first-sync state
  - `Observe*ActionProcessor` owns screen state reduction from the observed flow
  - `Refresh*ActionProcessor` owns startup/retry refresh and error messaging
  - `Observe*ActionProcessor` should ignore `UseCaseState.Loading` from local reads; do not show a full-screen loading illustration for a fast local cache read
  - use an `Idle` state for initial UI when the route immediately dispatches observation plus refresh; render `Idle` as no content and let refresh decide whether to move to `Loading`
  - when data uses freshness/expiry instead of a continuously observed local flow, follow `subjects`: local `getFresh*()` returns cached data or null, remote `refresh*()` fetches and persists, and the load use case emits an explicit remote-loading domain signal before calling refresh
- Preserve the composable boundary:
  - `Navigation` injects or resolves `ViewModel` instances with `koinViewModel(...)`
  - `Route` observes `state` and `effect`, triggers initial actions with `LaunchedEffect`, and passes plain state/callbacks to `Screen`
  - `Screen` stays stateless with respect to DI
- Keep `Screen` files focused on orchestration, not component catalogs:
  - `Screen` may own screen-local state, derived filtering, list assembly, and callback wiring
  - Place each graphical Compose component in exactly one file named after its top-level composable
  - Keep destination screens in `ui/screen`, reusable or screen-owned views in `ui/view`, and dialog or bottom-sheet components in `ui/dialog`
  - Expose `Screen`, `View`, and `Dialog` composables as public top-level declarations; do not use `internal` or `private` for those component entry points
  - Do not group multiple Compose components in one file. If a component needs another visual child component, extract that child into its own file in the matching UI package
  - Move UI-only enums or small display helper types shared by multiple view files into `ui/model`; keep helper types private in the view file when only one component uses them
  - Do not leave large groups of private composables inside a `Screen` file once they represent independent UI components
- Every non-dialog destination `Screen` must paint an opaque full-screen root background, usually `Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)` or a feature-specific color such as Pensum's `ScreenBackground`. Cover all `Idle`, `Loading`, `Empty`, `Failed`, and `Content` branches, including `SealedCrossfade` wrappers; do not rely on parent `Scaffold`, `NavHost`, or child state views to hide the previous destination during edge-swipe back navigation.
- Prefer feature dialogs as navigation destinations instead of rendering them from the feature state. Keep state-driven dialogs only for small widget-local popups when promoting them to navigation would add unnecessary ceremony.
- Choose one dialog-result pattern deliberately:
  - If the dialog only needs to mutate the parent screen state, resolve the parent/shared `ViewModel` from the dialog destination and dispatch the action directly, as in `evaluations`.
  - If the dialog needs to hand intent back to the previous destination and the parent must execute a lifecycle-sensitive side effect after the dialog is gone, use `base/.../NavigationResult.kt` with `CollectBackResultWithLifecycle`, as in `summary`.
- For navigation back results, use dedicated `@Serializable` result types instead of raw `String` or `Boolean` values. The shared helper derives the key from the result base type and serializes the payload into `savedStateHandle`.
- When sending a sealed back result, call `navigateBackWithResult<BaseResult>(SubResult)` with the base type explicit so the writer and collector use the same key and serializer.
- Match the repo's Compose local-state style: when using `remember { mutableStateOf(...) }`, prefer `val state = ...` plus `.value` reads/writes instead of delegated `var ... by remember { ... }`, unless the file already follows a different established pattern.
- Keep presentation models in `presentation/model`. Each presentation `data class` should live in its own file named after the class.
- Use one public top-level declaration per file across feature code. Do not group multiple public models, DTOs, enums, entities, repository interfaces, or state types into catch-all files such as `SubjectModels.kt` or `*Repositories.kt`.
- Keep presentation mappers in `presentation/mapper`. If a file's primary purpose is mapping UI or presentation state, it belongs there.
- Prefer precomputing UI-ready fields in presentation mappers instead of recomputing them inside composables when the source inputs are already available at mapping time. Leave only truly UI-local, theme-local, or resource-local work in the composable layer.
- Treat composables that interpret domain state as a smell. If a composable is deciding display labels, grouping, chip colors, status/tone flags, ordinals, or visibility from domain entities, prefer introducing or extending a `presentation/model/*Item` plus a mapper instead of teaching the composable that business/display logic.
- Move this kind of work into `presentation/mapper` when the mapper already has the needed inputs:
  - grouping, sorting, and indexing for display
  - status resolution such as `isClickable`, `isOverdue`, delta tone, or filter checked state
  - text derivation such as names, grade labels, subtitles, or chip labels
  - stable visual derivation such as subject-code colors or icon selection from a stable enum/state
- Keep this kind of work in UI when it is genuinely local to Compose:
  - transient interaction state such as scroll position, pager position, or a draft slider value
  - animation, `AnimatedVisibility`, and layout-only branching
  - final `MaterialTheme`/resource reads that only the composable can know at render time
- If displayed text or status depends on transient UI input, do not spread that derivation inline across the composable body. Prefer a small UI-local adapter or display model derived from the presentation item plus the transient value.
- Current repo examples to copy:
  - `evaluations/presentation/mapper/EvaluationItem.kt` precomputes ordinals, grouped headers, icons, semantic highlight tone, clickability, and grade/date texts before `EvaluationItemView` renders them
  - `record/presentation/mapper/QuarterItem.kt` precomputes quarter summary text, deltas, short names, and subject items before the summary/view composables render them
- Additional repo examples of this pattern:
  - `record/ui/model/SubjectItemDisplay.kt` shows the preferred escape hatch when a displayed status/text depends on transient UI input such as a draft slider grade
  - `evaluations/presentation/mapper/Filter.kt` maps raw filters into `EvaluationFilterGroupItem` and `EvaluationFilterChipItem` so `EvaluationsFilterView` and `FilterView` stay render-only
  - `evaluations/presentation/mapper/SubjectPicker.kt` and `evaluations/presentation/mapper/TypePicker.kt` precompute chip items for `EvaluationSubjectPicker` and `EvaluationTypePicker`
  - `evaluations/presentation/mapper/GradeSection.kt` precomputes the grade-section label and value texts for `EvaluationContentView`
- When a screen is item-heavy, prefer state shaped for the screen over raw domain state. A screen that renders lists of cards/chips usually wants `List<QuarterItem>`, `List<EvaluationsGroupItem>`, or dedicated chip items rather than `List<Quarter>`, `List<Evaluation>`, or raw filter/domain objects.
- Apply the same rule one level higher in MVI: `State.Content` should usually expose screen-ready collections such as `evaluationGroups`, `filterGroups`, `subjectItems`, `typeItems`, or `gradeSection`, and action processors should update those items together with the source fields they depend on.
- Not every `if`, `remember`, or `when` in UI is a problem. Small theme-only decisions such as `summary/ui/view/SummaryContentView.kt` status icon tinting or `maincore/ui/screen/TuIndiceScreen.kt` bottom-bar icon selection can stay in UI when they do not encode reusable presentation mapping.
- Keep `commonMain` portable:
  - no `android.*`
  - no `BuildConfig`
  - no Java IO types in shared code
  - no Android-specific Koin ViewModel DSL in KMP source sets
- Feature modules expose a single public Koin module named `<feature>Module`.
- Platform wiring stays centralized in `androidPlatformModule` and `iosPlatformModule`; do not create per-feature platform modules.
- New user-facing text goes through `composeResources`.
- If the change alters architectural boundaries, update `README.md` in the same change.

## Validation

- Start with targeted compilation:
  - `./gradlew --continue --console=plain :<module>:compileAndroidMain`
  - `./gradlew --continue --console=plain :<module>:compileKotlinIosSimulatorArm64`
- For module dependency changes, run `./gradlew verifyModuleGraph` and update `scripts/validate-module-graph.sh` plus `README.md` together.
- For feature DI changes, run the module smoke test.
- For shared bootstrap changes, run the relevant `maincore` smoke tests and the iOS bootstrap smoke test when applicable.
- For navigation or shared UI work, run focused module tests or the shared UI gate if the change is broad.
- For user-visible flow or selector changes, run `./gradlew verifyE2eContract`; run `./gradlew e2eMaestroAndroid` or `./gradlew e2eMaestroIos` when the local device/simulator and Maestro CLI are available.
- When a new shared module is added, also update and run the relevant root verification tasks listed in [references/project-map.md](references/project-map.md).
- Never claim checks you did not run.

## References

- [references/project-map.md](references/project-map.md): architecture, dependency rules, integration points, and root file map.
- [references/module-recipes.md](references/module-recipes.md): concrete recipes for feature, infrastructure, and host-module changes.
- [references/scaffolding.md](references/scaffolding.md): scaffolding workflow, template catalog, and explicit `ViewModel` plus `Route` patterns.
- [references/e2e.md](references/e2e.md): local E2E architecture, selector policy, module rollout, platform-specific test boundaries, and validation commands.
