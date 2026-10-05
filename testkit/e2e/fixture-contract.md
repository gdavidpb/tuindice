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
- Protected backend fixtures must require a bearer `Authorization` header. Gateway-backed services use the session token to identify the user and scope, even when the endpoint also forwards a DST password in the JSON body.
- Login-driven retry fixtures must match the actual sign-in sync request shape: `/record/v5/sync` receives both the bearer session token and the password in the JSON body. Summary and record failed-state retry flows intentionally use an initial sync unavailable response so the target machine starts without usable content; that sync response must open a flow-specific WireMock scenario before retry GET mappings can respond. Summary retry keeps an Android-specific second 503 because first-launch contextual coachmark dismissal can re-enter Summary and trigger an automatic refresh before the user retry. When that automatic refresh does not occur, the Android flow performs the visible second retry before expecting content. iOS transitions from the first 503 directly to a successful retry. The retry button then exercises the machine-owned refresh action and transitions to content on success.
- Do not call production services from local E2E.
- If a Maestro flow needs a new backend state, add a mapping under `mocks/mappings/<domain>/` and referenced bodies under `mocks/__files/<domain>/`.
- Record the new fixture dependency in `flow-catalog.yaml`.

Auth fixture contract:

- The canonical successful local login is raw USBID digits `1111111`, displayed/formatted by the app as `11-11111`, with password `123456`.
- The canonical USB email login fixture is `mail@usb.ve`, canonized by the app/backend as local identifier `mail`, with the same canonical password.
- Invalid credential flows must still enter a syntactically valid USBID, for example raw digits `0000000`, and vary the password or backend fixture to trigger the unauthorized path.
- Do not use short USBID values in Maestro flows; the app requires the formatted shape `NN-NNNNN`. iOS flows may repeat the final digit after the canonical 7 digits as an idempotent guard against dropped keystrokes; the UI rejects it once the field is full.

Record synthetic term search fixture contract:

- The `pr` search is reserved for deterministic ordering and visible availability states in the create-term search list. Its fixture must keep `EP1308` as already planned, `EP2308` as blocked but addable, and `AA1001`/`AB1001` as catalog-only subjects after pensum subjects.
- Visible create-term status copy follows Pensum language for shared academic states: available subjects show `Disponible`, blocked subjects show `Bloqueada`, and approved subjects show `Aprobada`. Technical selectors use those same state names, for example `..._status_blocked`.
- The status tooltips in that flow depend on the same fixtures: `EP1308` is planned in `Jul - Ago 2026`, `EP2308` is missing `EP1308` and `EP5855`, and `MA1111` was approved in `Sep - Dic 2021`.
- Do not use historical failed or retired subjects such as `MA1112` in `pr` assertions. They can already exist in the local catalog with their canonical names, so they belong in the generic `ma` search coverage.
- The `ma` search covers historical outcomes: `MA1112` retired and `MA1121` failed are available/addable. The narrower `ma1111` search covers the approved historical case: `MA1111` is only visible after enabling the approved-subjects toggle.
- The local login sync fixture `mocks/__files/sync/post-sync-success.json` must keep those same historical outcomes; `/record/v5` transformer state alone is not enough for login-driven E2E.
- `verifyE2eContract` validates record search subject selectors against the WireMock subject-search fixture resolved for each query used by the Maestro flow, and validates the historical outcomes needed by the create-term search flow.

Pensum equivalence fixture contract:

- A dedicated user (raw USBID digits `6666666`, the pensum-equivalence password) exercises the server-curated legacy-code equivalence path: its pensum serves the canonical node `LLA111` carrying an `EQUIVALENCE` fulfillment rule for `LL1111`, and its sync record approves `LL1111` — never `LLA111` — so the node can only resolve through the rule.
- The rule mirrors the backend synthesis shape exactly (`id: equiv:<nodeId>:<canonical>`, `rule_type: EQUIVALENCE`, legacy codes in `subject_codes`); keep it in sync with what pensums-api emits for curated `subject_equivalences`.
- The pensum response is gated by the user's session bearer token and the sync response by the password in the JSON body, so the canonical login datasets stay untouched.
- Stats for the fulfilled code reuse the shared `get-subject-stats-slow-success.json` body, like the other per-code stats mappings; the flow asserts segment metrics, not per-subject figures.

```bash
PORT=8080 ./mocks/start-mock-enviroment.sh
```

The E2E scripts wrap this command and store logs under `/tmp/tuindice-e2e`.
They pass `WIREMOCK_DELAY_PROFILE=fast` by default through
`E2E_WIREMOCK_DELAY_PROFILE`; use `legacy` to preserve checked-in delays.

University states fixture contract:

- Each dedicated user below logs in with raw USBID digits and a password that selects its `/record/v5/sync` fixture and its own `auth-<key>-login` WireMock scenario: `3030303` / `not-enrolled-pass` (enrollment source `not_enrolled`, no current term), `3131313` / `annulled-provisional-pass` (a `situation` while the current term is still in the record, with `section`, `schedule`, `enrollment_errors` on its attempts), `3232323` / `annulled-final-pass` (a `situation` and no current term), `3333344` / `new-student-pass` (424 with reason `NEW_STUDENT_NO_RECORD`) and `3434343` / `record-denied-pass` (503 with reason `DST_RECORD_ACCESS_DENIED`).
- The annulled-provisional user also gets a 404 from `/enrollment-proof/v1`, which the app explains from the last sync instead of offering a retry.
- The annulled-provisional user can sign out and sign in a second time in the same run: its session id starts with `auth-`, which is what the attestation stub of `auth.revoke_tokens` matches, and `auth-annulled-provisional-bootstrap-success-while-issued.json` answers the second bootstrap. `record-schedule-view-remembered` uses both to prove the schedule view chosen by one session does not reach the next.
- The sync fixtures must decode both the form deployed today (`situation` without a current term) and the one that follows the provisional window rule (`situation` with the current term); `MockSyncFixturesContractTest` in the app module decodes every one of them with the production DTOs.
- Their field names (`day_of_week` with 1 = Sunday, `start_block`, `end_block`, `classroom`, `section`, `enrollment_errors`, `withdrawn`, and `current_revision` in the overlay 409) are the ones the backend's `openapi.yaml` declares.
- `mocks/config/record-base-state.json` carries a schedule on the current term's attempts (one with an enrollment error, one withdrawn). The record transformer answers `GET /record/v5` from that state for every user and passes `section`, `schedule`, `enrollment_errors` and `withdrawn` through, so a record read after sign-in still carries a schedule on those attempts. No flow opens the schedule as the canonical user, whose sign-in sync fixture (`post-sync-success.json`) carries none: the flows that reach the schedule (`record-annulled-provisional-schedule`, `record-schedule-view-remembered`) sign in as the annulled-provisional user, and `record-annulled-final-notice` only asserts its top bar action is absent for the annulled-final one.
- The record transformer answers a stale overlay write with the real 409 body (`reason: STALE_PRECONDITION`, `current_revision`).
- `auth-retry-bootstrap-unavailable*.json` answer 503 with `Retry-After: 3`, so the sign-in wait finishes inside a flow.
