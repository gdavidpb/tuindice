# E2E selector policy

`testkit/e2e` owns the selector contract for local E2E implementation. The scenarios that use it live in
`scenarios/`; the drivers that resolve it are `scenariorunner/` (Android) and `iosApp/UITests/` (iOS).

Rules:

- A scenario addresses an element by tag (`Query.Tag`, the value of a `Modifier.testTag`), and the tag always comes from a
  constant or builder function of a `*UiTags` object of the module that owns the screen; no tag is a string literal in
  the catalog. `CatalogTagsTest` resolves every tag of the generated catalog against the `*UiTags.kt` files and fails
  on a tag that no constant declares and no builder template fits (a template needs a non-empty literal prefix).
- Android exposes Compose test tags to black-box runners through `testTagsAsResourceId` at the app root, and the driver
  finds them with `By.res(tag)`. On iOS the tag is the accessibility identifier XCUITest matches.
- New critical E2E interactions must add or reuse a stable `UiTags` constant in the owning module. Removing or renaming
  a constant breaks the compilation of `:scenarios`, which depends on the modules that own the tags.
- Avoid text selectors for user-visible Spanish copy unless no stable selector exists yet or the text is the assertion.
  A text query (`text(...)`) must use a text from `Copy` or `E2eFixtures`, never a literal (`CatalogContentRulesTest`);
  `Copy` binds each text to a string resource, mock data, a launch argument or a stated derivation, and `CopyTest`
  fails when the binding stops matching.
- A system surface outside the app tree (the share sheet, a permission dialog) is addressed with `system(label)`,
  which matches the resource id or the text on Android and the label or identifier on iOS, the springboard
  included. Those labels follow the language of the device; the toolchain locks pin it (Android `en`, iOS `es`).
- Dynamic selectors must be deterministic and sanitized: a builder function in the `*UiTags` object that adds a stable
  suffix (an id, a status name) to a constant prefix.
- If a selector is needed only on one platform, say so in the scenario with `onPlatform` (the number of branches per
  scenario is fixed in `PlatformBranchBudget`) and, when a platform edge is not driven at all, in
  `ActionDispositions.kt` and the note under `e2e/platform/`.

When adding a scenario that needs a new element:

1. Add the constant to the `*UiTags` of the owning module and put it on the composable with `Modifier.testTag`.
2. Use it from the scenario in `scenarios/.../catalog/<Module>Scenarios.kt`.
3. Run `./gradlew syncE2eArtifacts verifyE2eContract`.
