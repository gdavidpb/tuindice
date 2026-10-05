# Enrollment Proof Saved Copy Dialog Edge (iOS)

Status: platform-edge placeholder for `e2ePlatformIos`.

Maestro coverage: `e2e/maestro/flows/enrollmentproof/enrollmentproof-error-unavailable.yaml` verifies the same transient failure (503) when there is no saved copy: the snackbar, its retry, and the record staying on screen. `enrollmentproof-smoke.yaml` only triggers the download.

Platform-only scope:

- download the proof once so the app saves it, come back from the PDF viewer with XCUITest, make `/enrollment-proof/v1` answer 503 for that same session, and ask for the proof again
- verify the dialog (`enrollment_saved_copy_message`) asks before anything opens, that its positive button reaches `enrollmentproof.Enrollment.OpenSavedEnrollmentProof` and the saved PDF is handed to the system, and that cancelling leaves the record on screen

Covered elsewhere:

- `enrollmentproof/src/commonTest/.../presentation/route/EnrollmentProofRouteUiTest.kt` (`when_onlyTheSavedCopyIsLeft_then_asksBeforeOpeningIt`) verifies that the dialog is shown, that nothing opens until it is answered, and that the positive button hands the saved file to the opener
- `enrollmentproof/src/commonTest/.../ui/dialog/EnrollmentProofSavedCopyDialogUiTest.kt` verifies the dialog's copy and both callbacks
- `enrollmentproof/src/commonTest/.../data/repository/EnrollmentProofRepositoryContractTest.kt` verifies when a saved copy stands in for the download: only for transient failures (`getEnrollmentProof_savedCopyBacksOnlyTransientFailures`), never for a 409 or a 400, never for a 404
- `enrollmentproof/src/commonTest/.../domain/usecase/FetchEnrollmentProofUseCaseContractTest.kt` (`execute_tellsWhenTheFileIsTheSavedCopy`) and `EnrollmentProofStateMachineContractTest` verify the `ConfirmingSavedCopy` state and its `OpenSavedEnrollmentProof` row

Reason: the saved copy only exists after a successful download, and a successful download ends in `UIApplication.shared.open` on the file URL (`TuIndicePlatformBridge.openFile`), a host/system hand-off that leaves the app and that Maestro can neither assert nor return from stably. No debug seed state writes a proof into the app container (the host only knows the authenticated coachmark states), and the local backend picks its `/enrollment-proof/v1` answer by the account's password, so one session never sees a success followed by a 503.
