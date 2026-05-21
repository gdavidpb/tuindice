# E2E fixture contract

Local E2E flows use the WireMock state under `mocks/` as the QA backend.

Rules:

- Keep backend fixture changes in the same change as app contract changes.
- Use WireMock scenarios for stateful flows such as auth lifecycle, profile picture mutation, evaluations mutation, and record mutation.
- Every E2E flow should start from a known app state and reset WireMock scenarios before the suite.
- Do not call production services from local E2E.
- If a Maestro flow needs a new backend state, add a mapping under `mocks/mappings/<domain>/` and referenced bodies under `mocks/__files/<domain>/`.
- Record the new fixture dependency in `flow-catalog.yaml`.

Auth fixture contract:

- The canonical successful local login is raw USBID digits `1111111`, displayed/formatted by the app as `11-11111`, with password `123456`.
- Invalid credential flows must still enter a syntactically valid USBID, for example raw digits `0000000`, and vary the password or backend fixture to trigger the unauthorized path.
- Do not use short USBID values in Maestro flows; the app requires the formatted shape `NN-NNNNN`.

Current local backend entrypoint:

```bash
PORT=8080 ./mocks/start-mock-enviroment.sh
```

The E2E scripts wrap this command and store logs under `/tmp/tuindice-e2e`.
