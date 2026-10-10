# E2E fixture contract

Local E2E scenarios use the WireMock state under `mocks/` as the QA backend.

The accounts, tokens, texts and ids the scenarios use are Kotlin values in
`scenarios/src/commonMain/kotlin/com/gdavidpb/tuindice/scenarios/fixture/` (`E2eAccounts.kt`, `E2eFixtures.kt`,
`Copy.kt`). Host tests in `scenarios/src/androidHostTest/` check them against the mappings (`AccountFixturesTest`,
`AccountSessionFixturesTest`, `EntityFixturesTest`, `CopyTest`, `MockContractTest`, `MockRulesTest`,
`PendingFlushMocksTest`, `RecordSearchFixturesTest`, `SubjectSearchFixturesTest`, `RetryOrderMocksTest`,
`E2eClockFixtureTest`) and run in `verifyE2eContract`. This document keeps the semantics; the values live there.

Rules:

- Keep backend fixture changes in the same change as app contract changes.
- Use WireMock scenarios for stateful behavior such as auth lifecycle, profile picture mutation, evaluations mutation, and record mutation.
- Every scenario declares its own start, clean or seeded, and none inherits one. Before the first step the interpreter
  resets the WireMock scenario states and the request journal, resets the stateful datasets of the custom transformers
  (`/evaluations/v3/reset`, `/record/v5/reset`; `BackendEngine.resetPaths`) and sets the `mockStates` of the start.
  A scenario that needs the backend in another state later sets it with the `mockState(scenario, state)` step, which
  makes the same `PUT /__admin/scenarios/{scenario}/state` request in the middle of the run (a reply that is not 2xx
  fails the step as `BACKEND_UNAVAILABLE`). A new stateful transformer needs its reset path in that list
  (`MockContractTest` fails otherwise).
- A seeded start puts the login scenario of the account (for example `login-token-lifecycle` for `Canonical`) in the
  `TokensIssued` state, which is where the backend stays after a real sign-in; keep that scenario compatible with the
  canonical successful login tokens. An account without a session only signs in through the UI.
- Protected backend fixtures must require a bearer `Authorization` header. Gateway-backed services use the session token to identify the user and scope, even when the endpoint also forwards a DST password in the JSON body.
- Fixtures that answer a sign-in sync must match its real request shape: `/record/v5/sync` receives both the bearer session token and the password in the JSON body. The summary and record retry scenarios (`summary-refresh-retry`, `record-refresh-retry`) start seeded as a dedicated account whose sync answers 503, so the target machine has no usable content; the first read of the screen (`GET /users/v1`, `GET /record/v5`) also answers 503 whichever of the sync and the read reaches the server first (the `...-from-start` stubs and the 503 stubs of the intermediate states), and the retry button then succeeds. `RetryOrderMocksTest` replays every arrival order against the mapping files. The retry button exercises the machine-owned refresh action and transitions to content on success.
- A mock that refuses what only some scenarios send (a 503 on a change that must stay pending, for example) is not
  in the state every WireMock scenario starts in: a stub of a protected route that answers 400 or more in `Started`, or
  in no state, answers every scenario that sends that request with a token it matches. The scenario that needs the
  refusal puts the mock in a state of its own from its first step (`Start.Seeded(account, mockStates = ...)`; the
  state is `Unavailable` for `evaluations-pending-sign-out` and `evaluations-pending-sign-out-flush-success`) and
  nothing else enters that state. `MockRulesTest` fails a refusal in the default state that is not aimed at one
  account (one exact token, the password of an account other than the canonical one in the body, or one resource in
  the path), `CatalogStartTest` lists the scenarios that start with states besides their account's, and
  `PendingFlushMocksTest` replays that the dated and undated adds of the other scenarios (`evaluations-add-submit`
  among them) are accepted. The flush mock accepts the dated add from `Available` and moves to `Delivered`.
- A refusal that only one step of one scenario needs lives in a WireMock scenario state the scenario sets in the middle
  of its run with `mockState`, not in the default state: `enrollment-proof-saved-copy` answers the canonical account's
  proof request with 503 only in `Unavailable`, which `enrollmentproof-saved-copy-dialog` sets after the download that
  leaves the saved copy and `enrollmentproof-saved-copy-gone-after-sign-out` sets after signing out and in again, to
  see that no copy is offered (`SavedCopyMocksTest`).
- Do not call production services from local E2E.
- If a scenario needs a new backend state, add a mapping under `mocks/mappings/<domain>/` and referenced bodies under `mocks/__files/<domain>/`, and declare any new account in `E2eAccounts.kt` with the mapping that accepts its credential.

Auth fixture contract:

- The canonical successful local login is raw USBID digits `1111111`, displayed/formatted by the app as `11-11111`, with password `123456`.
- The canonical USB email login fixture is `mail@usb.ve`, canonized by the app/backend as local identifier `mail`, with the same canonical password.
- Invalid credential scenarios must still enter a syntactically valid USBID, for example raw digits `0000000`, and vary the password or backend fixture to trigger the unauthorized path.
- Do not use short USBID values; the app requires the formatted shape `NN-NNNNN`. The sign-in field is typed once with `enterText` (the raw digits, with the formatted text as `expect`), and the interpreter re-reads it and fails with both texts when they differ. No digit is typed twice in case a key is lost: a lost key is a defect to find.

Texts the scenarios assert (`Copy`):

- Every text constant of `Copy` has exactly one binding that says where the text comes from, and `CopyTest` fails when it stops being true: `Resource` (a string of a module's `strings.xml`, checked to read exactly like the constant, with its placeholders filled), `MockData` (the text must be inside the stated mock file), `Supplied` (the scenario itself hands the app the text through a launch argument; the test only requires the stated source) and `Derived` (the app computes the text at runtime from data, such as a formatted period; the test only requires the stated reason, so the text is checked against the app only by the scenario running).

Fixed clock:

- Every `Start` passes the launch argument `TUINDICE_E2E_NOW` (`DebugLaunchArguments.NOW`) with `E2eFixtures.Now` (`2026-10-15T12:00:00Z`), and debug builds read the date through the injected clock (`OverridableClock`). What the app derives from the date therefore does not depend on the day of the run: the term the record offers next (`E2eFixtures.NextTermKey`, `Copy.TermSepDec2026`) and the evaluations calendar do not expire. `E2eClockFixtureTest` checks that the frozen instant falls in the term those fixtures name; changing `E2eFixtures.Now` means re-checking them.
- Mock data that carries absolute dates (the evaluations base state, `lastSuccessfulSyncAt`) is independent of the app clock.

Record synthetic term search fixture contract:

- The `pr` search is reserved for deterministic ordering and visible availability states in the create-term search list. Its fixture must keep `EP1308` as already planned, `EP2308` as blocked but addable, and `AA1001`/`AB1001` as catalog-only subjects after pensum subjects.
- Visible create-term status copy follows Pensum language for shared academic states: available subjects show `Disponible`, blocked subjects show `Bloqueada`, and approved subjects show `Aprobada`. Technical selectors use those same state names, for example `..._status_blocked`.
- The status tooltips in that scenario depend on the same fixtures: `EP1308` is planned in `Jul - Ago 2026`, `EP2308` is missing `EP1308` and `EP5855`, and `MA1111` was approved in `Sep - Dic 2021`.
- Do not use historical failed or retired subjects such as `MA1112` in `pr` assertions. They can already exist in the local catalog with their canonical names, so they belong in the generic `ma` search coverage.
- The `ma` search covers historical outcomes: `MA1112` retired and `MA1121` failed are available/addable. The narrower `ma1111` search covers the approved historical case: `MA1111` is only visible after enabling the approved-subjects toggle.
- The local login sync fixture `mocks/__files/sync/post-sync-success.json` must keep those same historical outcomes; `/record/v5` transformer state alone is not enough for sign-in-driven E2E.
- `RecordSearchFixturesTest` validates the searched subjects against the WireMock subject-search fixture resolved for each query, and the historical outcomes the create-term search needs.

Subject search and enum-named inputs:

- The subjects scenarios type the queries of `E2eFixtures.subjectSearches`: the catalog mock lists `ci`, and the debug
  source of the subjects module lists `qa`, `qb` and `rx`; `E2eInputs.SubjectQueryTooShort` is under the minimum length
  the search takes and `E2eInputs.SubjectQueryNoMatch` matches no subject. `SubjectSearchFixturesTest` reads both
  sources and fails a query that does not list its subject.
- A tag the app builds from the name of an enum (the evaluation type chip, the status of an attempt, the status of a
  search result) is built from that enum in `E2eInputs`, never typed again; `EnumNamedInputsTest` fails a literal.
- The week the evaluations strip opens on is `E2eInputs.LastAcademicWeek`: the frozen clock is past the term's last
  week and the strip clamps to it (`E2eClockFixtureTest`).

Pensum equivalence fixture contract:

- A dedicated user (raw USBID digits `6666666`, the pensum-equivalence password) exercises the server-curated legacy-code equivalence path: its pensum serves the canonical node `LLA111` carrying an `EQUIVALENCE` fulfillment rule for `LL1111`, and its sync record approves `LL1111` — never `LLA111` — so the node can only resolve through the rule.
- The rule mirrors the backend synthesis shape exactly (`id: equiv:<nodeId>:<canonical>`, `rule_type: EQUIVALENCE`, legacy codes in `subject_codes`); keep it in sync with what pensums-api emits for curated `subject_equivalences`.
- The pensum response is gated by the user's session bearer token and the sync response by the password in the JSON body, so the canonical login datasets stay untouched.
- Stats for the fulfilled code reuse the shared `get-subject-stats-slow-success.json` body, like the other per-code stats mappings; the scenario asserts segment metrics, not per-subject figures.

```bash
PORT=8080 ./mocks/start-mock-enviroment.sh
```

The harness starts WireMock itself, one instance per platform on its own port (`E2E_ANDROID_WIREMOCK_PORT`,
`E2E_IOS_WIREMOCK_PORT`), and keeps its log inside the run directory. It passes
`WIREMOCK_DELAY_PROFILE=fast` by default through `E2E_WIREMOCK_DELAY_PROFILE`; use `legacy` to preserve checked-in delays.
A mapping whose behavior depends on its delay (cancel windows, reveal timers, a loading state a scenario looks at)
pins its own value in `metadata.fastDelayMilliseconds`; one that does not mind the collapse says so with the fast
default, `250`. Without the marker the fast profile collapses the delay to 250 ms in silence, which is how a scenario
that waits on it goes red on a mock nobody changed, so `MockRulesTest` fails any mapping whose legacy delay is at least
`Timeouts.Probe` (1.5 s, the shortest delay a step can look at) and that lacks it. The reissue of the update-password
scenario holds its answer for 5 s in both profiles so that the loading state of the sheet can be asserted after the
request is seen.

University states fixture contract:

- Each dedicated user below logs in with raw USBID digits and a password that selects its `/record/v5/sync` fixture and its own `auth-<key>-login` WireMock scenario: `3030303` / `not-enrolled-pass` (enrollment source `not_enrolled`, no current term), `3131313` / `annulled-provisional-pass` (a `situation` while the current term is still in the record, with `section`, `schedule`, `enrollment_errors` on its attempts), `3232323` / `annulled-final-pass` (a `situation` and no current term), `3333344` / `new-student-pass` (424 with reason `NEW_STUDENT_NO_RECORD`) and `3434343` / `record-denied-pass` (503 with reason `DST_RECORD_ACCESS_DENIED`).
- The annulled-provisional user's current term carries a withdrawn subject (`CSA213`, attempt `5288ee9acb2b3a199331d0dc908a494a`, with a schedule of its own): `record-withdrawn-subject` reads it in the record and checks it has no row in the schedule.
- The annulled-provisional user also gets a 404 from `/enrollment-proof/v1`, which the app explains from the last sync instead of offering a retry.
- The annulled-provisional user can sign out and sign in a second time in the same run: its session id starts with `auth-`, which is what the attestation stub of `auth.revoke_tokens` matches, and `auth-annulled-provisional-bootstrap-success-while-issued.json` answers the second bootstrap. `record-schedule-view-remembered` uses both to prove the schedule view chosen by one session does not reach the next.
- The sync fixtures must decode both the form deployed today (`situation` without a current term) and the one that follows the provisional window rule (`situation` with the current term); `MockSyncFixturesContractTest` in the maincore module decodes every one of them with the production DTOs.
- Their field names (`day_of_week` with 1 = Sunday, `start_block`, `end_block`, `classroom`, `section`, `enrollment_errors`, `withdrawn`, and `current_revision` in the overlay 409) are the ones the backend's `openapi.yaml` declares.
- `mocks/config/record-base-state.json` carries a schedule on the current term's attempts (one with an enrollment error, one withdrawn). The record transformer answers `GET /record/v5` from that state for every user and passes `section`, `schedule`, `enrollment_errors` and `withdrawn` through, so a record read after sign-in still carries a schedule on those attempts. No scenario opens the schedule as the canonical user, whose sign-in sync fixture (`post-sync-success.json`) carries none: the scenarios that reach the schedule (`record-annulled-provisional-schedule`, `record-schedule-view-remembered`) sign in as the annulled-provisional user, and `record-annulled-final-notice` only asserts its top bar action is absent for the annulled-final one.
- The record transformer answers a stale overlay write with the real 409 body (`reason: STALE_PRECONDITION`, `current_revision`).
- `auth-retry-bootstrap-unavailable*.json` answer 503 with `Retry-After: 10`: the sign-in keeps its button disabled for that wait, and `auth-login-retry-after-unavailable` waits for it to end (`Within.Long`, 30 s) before tapping again.
