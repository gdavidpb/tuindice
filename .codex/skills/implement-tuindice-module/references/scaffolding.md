# Scaffolding

## Purpose

This skill now includes a small scaffolding layer for repetitive boilerplate:

- a generator for a baseline KMP feature module
- renderable templates for individual architecture components
- explicit patterns for `ViewModel`, `Route`, and the `Navigation -> Route -> Screen` split used in this repo

Use scaffolding to save time, not to skip architecture review. The generated files are a starting point that still needs integration into `settings.gradle.kts`, `maincore`, `app`, smoke tests, and real domain/data wiring.

## Generator

Use the script from the skill directory:

```bash
python3 scripts/scaffold_feature_module.py list-templates
python3 scripts/scaffold_feature_module.py scaffold-feature --module attendance --feature-name Attendance --screen-title "Asistencia"
python3 scripts/scaffold_feature_module.py scaffold-feature --module attendance --feature-name Attendance --screen-title "Asistencia" --with-persistence --with-device-tests
python3 scripts/scaffold_feature_module.py integrate-feature --module attendance --feature-name Attendance --screen-title "Asistencia"
python3 scripts/scaffold_feature_module.py render-template viewmodel --module attendance --feature-name Attendance --screen-title "Asistencia"
```

Important flags:

- `--module`: Gradle module name and package segment. Keep it lowercase alphanumeric.
- `--feature-name`: exact PascalCase type prefix. Use it for names like `EnrollmentProof`.
- `--screen-title`: visible title used in contract defaults and string resources.
- `--with-persistence`: adds `:persistence` to `build.gradle.kts`.
- `--with-device-tests`: adds `androidDeviceTest` boilerplate to `build.gradle.kts`.
- `--top-bar-config Summary|Record`: picks the current available `TopBarConfig`.
- `--hide-bottom-bar`: flips the default `ViewState.isBottomBarVisible`.

## Generated Feature Skeleton

`scaffold-feature` creates a minimal but coherent baseline for a shared feature:

- `build.gradle.kts`
- `src/commonMain/composeResources/values/strings.xml`
- `src/commonMain/kotlin/.../di/<Feature>Module.kt`
- `src/commonMain/kotlin/.../presentation/contract/<Feature>.kt`
- `src/commonMain/kotlin/.../presentation/viewmodel/<Feature>ViewModel.kt`
- `src/commonMain/kotlin/.../presentation/action/Load<Feature>ActionProcessor.kt`
- `src/commonMain/kotlin/.../presentation/navigation/<Feature>Destination.kt`
- `src/commonMain/kotlin/.../presentation/navigation/<Feature>Navigation.kt`
- `src/commonMain/kotlin/.../presentation/route/<Feature>Route.kt`
- `src/commonMain/kotlin/.../ui/screen/<Feature>Screen.kt`
- `src/commonMain/kotlin/.../domain/repository/<Feature>Repository.kt`
- `src/commonMain/kotlin/.../domain/usecase/Load<Feature>UseCase.kt`
- `src/commonMain/kotlin/.../domain/usecase/error/Load<Feature>UseCaseError.kt`
- `src/commonMain/kotlin/.../domain/usecase/exceptionhandler/Load<Feature>ExceptionHandler.kt`
- `src/commonMain/kotlin/.../data/repository/<Feature>DataRepository.kt`
- `src/commonTest/kotlin/.../di/<Feature>ModuleKoinSmokeTest.kt`
- `src/commonTest/kotlin/.../testing/<Feature>TestDoubles.kt`

The baseline uses a trivial `String` content flow so the domain/data path and smoke test are easy to replace incrementally.

## Integration Mode

`integrate-feature` performs the standard shared-runtime wiring for a scaffolded feature:

- `settings.gradle.kts`
- `maincore/build.gradle.kts`
- `app/build.gradle.kts`
- `maincore/.../SharedModules.kt`
- `maincore/.../TuIndiceNavHost.kt`

Assumptions:

- the module already exists, typically because it was created with `scaffold-feature`
- the feature public module is named `<featureLowerCamel>Module`
- the navigation entry point is `<featureLowerCamel>Navigation(...)`
- the navigation signature matches the scaffold baseline:
  - `onViewStateChanged`
  - `showSnackBar`

It is intentionally conservative. It does not update:

- `BottomBarConfig.kt`
- `TuIndiceScreen.kt`
- root `build.gradle.kts` verification task lists
- platform modules

Use `--dry-run` to verify whether any repo wiring still needs to be applied.

## Template Catalog

The component templates live in `assets/templates/` and can be rendered individually:

- `build-gradle`
- `module`
- `contract`
- `viewmodel`
- `action-processor`
- `repository-interface`
- `repository-implementation`
- `use-case`
- `use-case-error`
- `exception-handler`
- `validator`
- `destination`
- `navigation`
- `route`
- `screen`
- `smoke-test`
- `test-doubles`
- `strings`

Use `render-template` when only one component needs to be added or rewritten.

## ViewModel Pattern

The repo pattern is:

- `ViewModel` extends `BaseViewModel<State, Action, Effect>`
- it owns public action methods that call `sendAction(...)`
- it does not talk to repositories directly
- it delegates each action branch to a dedicated `ActionProcessor`
- `initialState` and `initialAction` are declared at the `BaseViewModel(...)` call site

Generated `ViewModel` templates intentionally start with a single action processor so the shape stays simple and easy to expand.

## Route Pattern

The repo split is:

- `Navigation`:
  - resolves the `ViewModel` with `koinViewModel(viewModelStoreOwner = backStackEntry)`
  - pushes `ViewState` changes upward when needed
- `Route`:
  - collects `state` with lifecycle
  - collects `effect`
  - triggers startup work with `LaunchedEffect`
  - maps effects to navigation or snackbars
  - passes plain state and callbacks to `Screen`
- `Screen`:
  - pure composable UI
  - no Koin
  - no lifecycle collection
  - no repository or use case wiring

If you are adding a user-facing flow, keep that split. Do not inject a repository or `ViewModel` into `Screen`.
