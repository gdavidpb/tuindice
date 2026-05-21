# E2E fixture contract

Local E2E flows use the WireMock state under `mocks/` as the QA backend.

Rules:

- Keep backend fixture changes in the same change as app contract changes.
- Use WireMock scenarios for stateful flows such as auth lifecycle, profile picture mutation, evaluations mutation, and record mutation.
- Every E2E flow should start from a known app state and reset WireMock scenarios before the suite.
- Do not call production services from local E2E.
- If a Maestro flow needs a new backend state, add a mapping under `mocks/mappings/<domain>/` and referenced bodies under `mocks/__files/<domain>/`.
- Record the new fixture dependency in `flow-catalog.yaml`.

Current local backend entrypoint:

```bash
PORT=8080 ./mocks/start-mock-enviroment.sh
```

The E2E scripts wrap this command and store logs under `/tmp/tuindice-e2e`.
