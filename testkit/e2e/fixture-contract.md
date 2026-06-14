# E2E fixture contract

Local E2E flows use the WireMock state under `mocks/` as the QA backend.

Canonical values are machine-readable in `fixture-contract.env` (single source of truth,
sourced and enforced by `validate-e2e-contract.sh`) and mirrored for platform tests in
`testkit/src/commonMain/kotlin/com/gdavidpb/tuindice/testkit/e2e/E2eFixtureContract.kt`.
This document keeps the semantics; the values live there.

Rules:

- Keep backend fixture changes in the same change as app contract changes.
- Use WireMock scenarios for stateful flows such as auth lifecycle, profile picture mutation, evaluations mutation, and record mutation.
- Every E2E flow should start from a known app state and reset WireMock scenarios before the suite.
- The seeded authenticated launch helper may set `login-token-lifecycle` directly to `TokensIssued`; keep that scenario compatible with the canonical successful login tokens.
- Do not call production services from local E2E.
- If a Maestro flow needs a new backend state, add a mapping under `mocks/mappings/<domain>/` and referenced bodies under `mocks/__files/<domain>/`.
- Record the new fixture dependency in `flow-catalog.yaml`.

Auth fixture contract:

- The canonical successful local login is raw USBID digits `1111111`, displayed/formatted by the app as `11-11111`, with password `123456`.
- Invalid credential flows must still enter a syntactically valid USBID, for example raw digits `0000000`, and vary the password or backend fixture to trigger the unauthorized path.
- Do not use short USBID values in Maestro flows; the app requires the formatted shape `NN-NNNNN`. iOS flows may repeat the final digit after the canonical 7 digits as an idempotent guard against dropped keystrokes; the UI rejects it once the field is full.

Record synthetic term search fixture contract:

- The `prioridad` search is reserved for deterministic ordering and visible availability states in the create-term search list. Its fixture must keep `EP1308` as already planned, `EP2308` as unavailable but addable, and `AA1001`/`AB1001` as catalog-only subjects after pensum subjects.
- Visible create-term status copy follows Pensum language for shared academic states: available subjects show `Disponible`, unavailable subjects show `Bloqueada`, and taken subjects show `Aprobada`. The technical selectors remain stable, for example `..._status_unavailable` still identifies the unavailable case even when the visible label is `Bloqueada`.
- The status tooltips in that flow depend on the same fixtures: `EP1308` is planned in `Jul - Ago 2026`, `EP2308` is missing `EP1308` and `EP5855`, and `MA1111` was taken in `Sep - Dic 2021`.
- Do not use historical failed or retired subjects such as `MA1112` in `prioridad` assertions. They can already exist in the local catalog with their canonical names, so they belong in the generic `ma` search coverage.
- The `ma` search covers historical outcomes: `MA1112` retired and `MA1121` failed are available/addable. The narrower `ma1111` search covers the approved historical case: `MA1111` is only visible after enabling the already-taken toggle.
- The local login sync fixture `mocks/__files/sync/post-sync-success.json` must keep those same historical outcomes; `/record/v5` transformer state alone is not enough for login-driven E2E.
- `verifyE2eContract` validates record search subject selectors against the WireMock subject-search fixture resolved for each query used by the Maestro flow, and validates the historical outcomes needed by the create-term search flow.

Current local backend entrypoint:

```bash
PORT=8080 ./mocks/start-mock-enviroment.sh
```

The E2E scripts wrap this command and store logs under `/tmp/tuindice-e2e`.
They pass `WIREMOCK_DELAY_PROFILE=fast` by default through
`E2E_WIREMOCK_DELAY_PROFILE`; use `legacy` to preserve checked-in delays.
