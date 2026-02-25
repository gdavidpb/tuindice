# Contributing

## Koin DI conventions

### 1) Module naming by source set
- `commonMain` -> `*CommonModule`
- `androidMain` -> `*AndroidModule`
- `iosMain` -> `*IosModule`

Examples:
- `aboutCommonModule`
- `loginAndroidModule`
- `summaryIosModule`

### 2) File naming by source set
- `commonMain` -> `FeatureCommonModule.kt`
- `androidMain` -> `FeatureAndroidModule.kt`
- `iosMain` -> `FeatureIosModule.kt`

Examples:
- `AboutCommonModule.kt`
- `LoginAndroidModule.kt`
- `SummaryIosModule.kt`

### 3) Preferred module declaration
- Use `val ... = module { ... }` by default.
- Use `fun ...(...): Module = module { ... }` only when runtime input is required.

Current valid dynamic case:
- `persistenceCommonModule(database: TuIndiceDatabase)` (database instance is created at runtime in iOS host/bootstrap).

### 4) Aggregation rules
- Add only common modules in `sharedCommonModules()`.
- Add only platform modules in platform-specific assembly (`androidReleaseModules`, `iosFeatureModules`, etc.).
- Do not add common modules again inside platform module lists.

### 5) Disallowed patterns
- `*CoreModule` names.
- Empty Koin modules (`module {}`).

### 6) PR checklist (DI changes)
- New/renamed module follows `Common/Android/Ios` naming.
- File name matches exported symbol.
- Module is registered once in the correct aggregation level.
- If module is function-based, include a short comment explaining why it needs runtime parameters.
