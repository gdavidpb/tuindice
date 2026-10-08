# Enrollment Proof Saved Copy Dialog Edge (Android)

Status: platform-edge note; no scenario drives it.

Scenario coverage: `enrollmentproof-error-unavailable` verifies the same transient failure (503) when there is no saved copy: the snackbar, its retry, and the record staying on screen. `enrollmentproof-smoke` only reaches the download button.

Platform-only scope:

- download the proof once so the app saves it, come back from the PDF viewer, make `/enrollment-proof/v1` answer 503 for that same session, and ask for the proof again
- verify the dialog (`enrollment_saved_copy_message`) asks before anything opens, that its positive button reaches `enrollmentproof.Enrollment.OpenSavedEnrollmentProof` and the saved PDF lands in the viewer, and that cancelling leaves the record on screen

Covered elsewhere:

- `enrollmentproof/src/commonTest/.../presentation/route/EnrollmentProofRouteUiTest.kt` (`when_onlyTheSavedCopyIsLeft_then_asksBeforeOpeningIt`) verifies that the dialog is shown, that nothing opens until it is answered, and that the positive button hands the saved file to the opener
- `enrollmentproof/src/commonTest/.../ui/dialog/EnrollmentProofSavedCopyDialogUiTest.kt` verifies the dialog's copy and both callbacks
- `enrollmentproof/src/commonTest/.../data/repository/EnrollmentProofRepositoryContractTest.kt` verifies when a saved copy stands in for the download: only for transient failures (`getEnrollmentProof_savedCopyBacksOnlyTransientFailures`), never for a 409 or a 400, never for a 404
- `enrollmentproof/src/commonTest/.../domain/usecase/FetchEnrollmentProofUseCaseContractTest.kt` (`execute_tellsWhenTheFileIsTheSavedCopy`) and `EnrollmentProofStateMachineContractTest` verify the `ConfirmingSavedCopy` state and its `OpenSavedEnrollmentProof` row

Reason: the saved copy only exists after a successful download, and a successful download ends in an `Intent.ACTION_VIEW` hand-off to the device's PDF viewer (`AndroidFileOpenerDataSource`). A scenario can come back from another app with `foreground()`, but the same session would then need `/enrollment-proof/v1` to answer 503 after a success: the local backend picks that answer by the account's password, and the interpreter sets the WireMock states before the first launch (`Start.mockStates`) and, with the `mockState` step, also in the middle of a scenario, which would let a scenario flip the answer once the download has succeeded; what still has no way in is the saved copy itself. No launch argument writes a proof into app storage either.
