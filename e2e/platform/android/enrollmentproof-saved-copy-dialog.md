# Enrollment Proof Saved Copy Dialog Edge (Android)

Status: platform-edge note for the positive button only; the scenario `enrollmentproof-saved-copy-dialog` drives the rest.

What the scenario does (3 of 3 on Android and on iOS, 2026-10-08; see `docs/e2e-mediciones.md`, section 3): downloads the proof once so the app saves it (the file is handed to the device's PDF viewer: `waitBackgrounded`, `foreground()`), sets the WireMock scenario `enrollment-proof-saved-copy` to `Unavailable` with the `mockState` step so `/enrollment-proof/v1` answers 503, asks for the proof again, asserts the dialog (`enrollment_saved_copy_message`) and cancels it, and the record stays on screen.

Platform-only scope that remains:

- verify that the dialog's positive button reaches `enrollmentproof.Enrollment.OpenSavedEnrollmentProof` and that the saved PDF lands in the viewer

Scenario coverage: `enrollmentproof-error-unavailable` verifies the same transient failure (503) when there is no saved copy: the snackbar, its retry, and the record staying on screen.

Covered elsewhere:

- `enrollmentproof/src/commonTest/.../presentation/route/EnrollmentProofRouteUiTest.kt` (`when_onlyTheSavedCopyIsLeft_then_asksBeforeOpeningIt`) verifies that the dialog is shown, that nothing opens until it is answered, and that the positive button hands the saved file to the opener
- `enrollmentproof/src/commonTest/.../ui/dialog/EnrollmentProofSavedCopyDialogUiTest.kt` verifies the dialog's copy and both callbacks
- `enrollmentproof/src/commonTest/.../data/repository/EnrollmentProofRepositoryContractTest.kt` verifies when a saved copy stands in for the download: only for transient failures (`getEnrollmentProof_savedCopyBacksOnlyTransientFailures`), never for a 409 or a 400, never for a 404
- `enrollmentproof/src/commonTest/.../domain/usecase/FetchEnrollmentProofUseCaseContractTest.kt` (`execute_tellsWhenTheFileIsTheSavedCopy`) and `EnrollmentProofStateMachineContractTest` verify the `ConfirmingSavedCopy` state and its `OpenSavedEnrollmentProof` row

Reason: the positive button ends in an `Intent.ACTION_VIEW` hand-off to the device's PDF viewer (`AndroidFileOpenerDataSource`); it was not tried when the scenario was written, which covered the dialog and its cancel (`docs/e2e-mediciones.md`, section 3), and stays here as the part nobody asserts.
