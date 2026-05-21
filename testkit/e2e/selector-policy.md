# E2E selector policy

`testkit/e2e` owns the selector contract for local E2E implementation.

Rules:

- Maestro selectors use `id` and should map to existing `Modifier.testTag` values.
- Android exposes Compose test tags to black-box runners through `testTagsAsResourceId` at the app root.
- New critical E2E interactions must add or reuse a stable `UiTags` constant in the owning module.
- Avoid text selectors for user-visible Spanish copy unless no stable selector exists yet.
- Dynamic selectors must be deterministic, sanitized, and listed in `flow-catalog.yaml` with a representative value.
- If a selector is needed by a platform-only test, document it in the platform edge suite README or test name.

When adding a Maestro flow:

1. Add the flow under `e2e/maestro/flows/<module>/`.
2. Add the path and primary selectors to `flow-catalog.yaml`.
3. Add any suite-critical selector to `critical-selectors.txt`.
4. Run `./gradlew verifyE2eContract`.
