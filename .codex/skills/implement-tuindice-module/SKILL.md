---
name: implement-tuindice-module
description: Create or modify Kotlin Multiplatform modules in this `tuindice` app repository. Use when adding a new feature or shared module, changing an existing module's architecture, Gradle setup, Koin wiring, navigation, platform bindings, root verification tasks, or smoke tests for modules such as `base`, `persistence`, `maincore`, `app`, `auth`, `about`, `summary`, `record`, `evaluations`, `enrollmentproof`, and `testkit`.
---

# Implement TuIndice Module

## Overview

Implement module work by copying the nearest existing module pattern instead of inventing a new architecture. This repo is a Kotlin Multiplatform app with shared feature modules, shared infrastructure, a shared assembler in `maincore`, an Android host in `app`, and an iOS host in `iosApp`.

## Start Here

- Load [references/project-map.md](references/project-map.md) when you need the current architecture, module roles, integration points, or configuration rules.
- Load [references/module-recipes.md](references/module-recipes.md) when you need the concrete checklist for creating or modifying a module.
- Load [references/scaffolding.md](references/scaffolding.md) when you want to bootstrap a new module or render boilerplate for individual architecture components.
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
   - observe use cases should read local flows only
   - update use cases should fetch or recompute and then persist back into the local state
   - `ViewModel` initial actions should usually start observation, while `Route` triggers the first refresh with `LaunchedEffect`
7. When a change alters an HTTP contract consumed by the app, update the local WireMock fixtures in the same change:
   - request and response mappings live under `mocks/mappings/<feature-or-domain>/`
   - referenced response bodies live under `mocks/__files/<feature-or-domain>/`
   - keep fixture payloads aligned with the current request shape, response shape, and status codes
8. Keep the UI boundary explicit:
   - `Navigation` resolves the `ViewModel`
   - `Route` bridges `state/effect` and lifecycle to the pure `Screen`
   - `Screen` stays free of Koin and business wiring
9. Update smoke tests and focused contract/UI tests when constructor wiring or public entry points change.
10. Run the smallest truthful verification set and report anything left unverified.

## Non-Negotiable Project Rules

- Keep the dependency flow pointed inward. Do not add new feature-to-feature dependencies without explicit approval. Current legacy exception: `evaluations -> record`.
- Preserve `presentation -> domain -> data -> di` separation. Interfaces live in `domain`; implementations live in `data`; `di` only wires them.
- Within that separation, use this finer layering order when a module needs multiple data origins: `presentation -> domain -> data/repository -> data/contract -> data/source -> di`.
- `domain/repository` contains business-facing contracts used by action processors, use cases, or view models. These interfaces should end with `Repository`.
- `data/repository` contains internal data-layer interfaces for compositions over multiple origins. These interfaces should end with `DataRepository`.
- `data/contract` contains internal contracts for leaf origins such as API, DB, settings, or platform bridges.
- `data/source` contains concrete implementations, whether they implement a `domain/repository`, `data/repository`, or `data/contract` contract directly. These classes should end with `DataSource`.
- If a `domain` contract needs multiple internal origins or shared coordination, define an internal `*DataRepository` interface in `data/repository` and keep all concrete implementations in `data/source`.
- If a module needs to abstract a single leaf origin internally, keep that contract in `data/contract` and the implementation in `data/source`.
- A `*DataSource` may implement a `domain/repository` contract directly when the domain contract maps cleanly to a single concrete origin.
- A `*DataSource` may also implement a `*DataRepository` contract when it is one concrete origin behind a multi-origin data flow.
- A `*DataSource` may also implement a `data/contract` interface when it is the concrete adapter for a leaf origin.
- Do not place concrete orchestrator classes in `data/repository`. `data/repository` is for interfaces; implementations stay in `data/source`.
- In `commonModule`, default shared runtime services and infrastructure repositories to `single`; use `factory` only when the object is intentionally transient or has no shared identity/state.
- `ViewModel` classes extend `BaseViewModel` and delegate work to `ActionProcessor` classes.
- For repository-backed feature state, prefer the `summary` and `record` split:
  - `Observe*UseCase` reads local state only
  - `Update*UseCase` refreshes and persists explicitly
  - `Observe*ActionProcessor` owns screen state reduction from the observed flow
  - `Refresh*ActionProcessor` owns startup/retry refresh and error messaging
- Preserve the composable boundary:
  - `Navigation` injects or resolves `ViewModel` instances with `koinViewModel(...)`
  - `Route` observes `state` and `effect`, triggers initial actions with `LaunchedEffect`, and passes plain state/callbacks to `Screen`
  - `Screen` stays stateless with respect to DI
- Prefer feature dialogs as navigation destinations instead of rendering them from the feature state. Keep state-driven dialogs only for small widget-local popups when promoting them to navigation would add unnecessary ceremony.
- Choose one dialog-result pattern deliberately:
  - If the dialog only needs to mutate the parent screen state, resolve the parent/shared `ViewModel` from the dialog destination and dispatch the action directly, as in `evaluations`.
  - If the dialog needs to hand intent back to the previous destination and the parent must execute a lifecycle-sensitive side effect after the dialog is gone, use `base/.../NavigationResult.kt` with `CollectBackResultWithLifecycle`, as in `summary`.
- For navigation back results, use dedicated `@Serializable` result types instead of raw `String` or `Boolean` values. The shared helper derives the key from the result base type and serializes the payload into `savedStateHandle`.
- When sending a sealed back result, call `navigateBackWithResult<BaseResult>(SubResult)` with the base type explicit so the writer and collector use the same key and serializer.
- Match the repo's Compose local-state style: when using `remember { mutableStateOf(...) }`, prefer `val state = ...` plus `.value` reads/writes instead of delegated `var ... by remember { ... }`, unless the file already follows a different established pattern.
- Keep presentation models in `presentation/model`. Each presentation `data class` should live in its own file named after the class.
- Keep presentation mappers in `presentation/mapper`. If a file's primary purpose is mapping UI or presentation state, it belongs there.
- Prefer precomputing UI-ready fields in presentation mappers instead of recomputing them inside composables when the source inputs are already available at mapping time. Leave only truly UI-local, theme-local, or resource-local work in the composable layer.
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
- For feature DI changes, run the module smoke test.
- For shared bootstrap changes, run the relevant `maincore` smoke tests and the iOS bootstrap smoke test when applicable.
- For navigation or shared UI work, run focused module tests or the shared UI gate if the change is broad.
- When a new shared module is added, also update and run the relevant root verification tasks listed in [references/project-map.md](references/project-map.md).
- Never claim checks you did not run.

## References

- [references/project-map.md](references/project-map.md): architecture, dependency rules, integration points, and root file map.
- [references/module-recipes.md](references/module-recipes.md): concrete recipes for feature, infrastructure, and host-module changes.
- [references/scaffolding.md](references/scaffolding.md): scaffolding workflow, template catalog, and explicit `ViewModel` plus `Route` patterns.
