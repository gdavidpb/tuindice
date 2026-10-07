package com.gdavidpb.tuindice.scenarios.catalog

/**
 * The actions no scenario has to cover, ported from the `internal` and `platform-edge` entries of the old
 * MVI action catalog. Every other action of every `presentation/contract` must be listed in some
 * scenario's `covers` (`ActionCoverageTest`).
 */
object ActionDispositions {
	val all: List<ActionDisposition> = listOf(
		ActionDisposition.Internal(
			action = "about.About.LoadVersion",
			reason = "screen bootstrap"
		),
		ActionDisposition.PlatformEdge(
			action = "about.About.RateOnStore",
			reason = "store intent requires host/system assertion"
		),
		ActionDisposition.PlatformEdge(
			action = "about.About.ReportBug",
			reason = "external composer/browser requires host/system assertion"
		),
		ActionDisposition.PlatformEdge(
			action = "about.About.ContactDeveloper",
			reason = "mail composer requires host/system assertion"
		),
		ActionDisposition.PlatformEdge(
			action = "about.About.ShareApp",
			reason = "share sheet requires UI Automator/XCUITest"
		),
		ActionDisposition.Internal(
			action = "auth.SignOut.Initialize",
			reason = "route state bootstrap from pending changes"
		),
		ActionDisposition.Internal(
			action = "enrollmentproof.Enrollment.FetchEnrollmentProof",
			reason = "enrollment proof dialog bootstrap after record route navigation"
		),
		ActionDisposition.PlatformEdge(
			action = "enrollmentproof.Enrollment.OpenSavedEnrollmentProof",
			reason =
				"hands the saved PDF to the device viewer after a transient download failure; seeding the " +
				"saved copy needs an earlier download that opens the system PDF viewer, so the dialog is " +
				"assigned to e2e/platform/android/enrollmentproof-saved-copy-dialog.md and " +
				"e2e/platform/ios/enrollmentproof-saved-copy-dialog.md; covered by " +
				"EnrollmentProofRouteUiTest and EnrollmentProofSavedCopyDialogUiTest"
		),
		ActionDisposition.Internal(
			action = "enrollmentproof.Enrollment.OpenEnrollmentProofCompleted",
			reason = "route re-enters the machine with whether the device viewer opened the proof"
		),
		ActionDisposition.Internal(
			action = "evaluations.Evaluation.LoadAvailableAttempts",
			reason = "form bootstrap"
		),
		ActionDisposition.Internal(
			action = "evaluations.Evaluation.LoadEvaluation",
			reason = "edit form bootstrap"
		),
		ActionDisposition.Internal(
			action = "evaluations.Evaluations.LoadEvaluations",
			reason = "list bootstrap"
		),
		ActionDisposition.Internal(
			action = "evaluations.Evaluations.EnsureEvaluationsLoaded",
			reason = "route entry confirms initial evaluations refresh before showing empty"
		),
		ActionDisposition.Internal(
			action = "maincore.Browser.NavigateTo",
			reason = "route effect from link actions"
		),
		ActionDisposition.Internal(
			action = "maincore.Browser.SetLoading",
			reason = "renderer callback"
		),
		ActionDisposition.Internal(
			action = "maincore.Browser.SetLoadFailed",
			reason = "renderer callback"
		),
		ActionDisposition.PlatformEdge(
			action = "maincore.Browser.OpenExternalResource",
			reason = "confirm/open handoff requires UI Automator/XCUITest host assertion"
		),
		ActionDisposition.Internal(
			action = "maincore.Main.StartUp",
			reason = "app bootstrap"
		),
		ActionDisposition.Internal(
			action = "maincore.Main.ShowOutdatedApp",
			reason = "host shows the global outdated app screen from the shared HTTP 426 event"
		),
		ActionDisposition.PlatformEdge(
			action = "maincore.Main.ClickUpdateApp",
			reason = "Store handoff opens Google Play or App Store and requires host/system assertion."
		),
		ActionDisposition.Internal(
			action = "maincore.Main.UpdateFlowCompleted",
			reason = "route re-enters the machine with the platform update launch result"
		),
		ActionDisposition.PlatformEdge(
			action = "maincore.Main.RequestReview",
			reason = "app store review prompt requires host/system assertion"
		),
		ActionDisposition.PlatformEdge(
			action = "maincore.Main.RequestUpdateCheck",
			reason = "system update prompt requires host/system assertion"
		),
		ActionDisposition.Internal(
			action = "maincore.Main.NoteSyncUnavailable",
			reason = "route-dispatched telemetry marker when the sync status transitions into Unavailable"
		),
		ActionDisposition.Internal(
			action = "maincore.Main.NoteSyncFailed",
			reason = "route-dispatched telemetry marker when the sync status transitions into Failed"
		),
		ActionDisposition.Internal(
			action = "pensum.Pensum.ObservePensum",
			reason = "screen bootstrap"
		),
		ActionDisposition.Internal(
			action = "pensum.Pensum.EnsurePensumLoaded",
			reason =
				"every entry into the pensum tab loads a missing pensum or silently revalidates a cached " +
				"one older than a day"
		),
		ActionDisposition.Internal(
			action = "pensum.Pensum.SelectPensum",
			reason =
				"current bottom sheet keeps version as local UI state and dispatches SelectSelection on " +
				"apply"
		),
		ActionDisposition.Internal(
			action = "pensum.Pensum.SelectModality",
			reason =
				"current bottom sheet keeps modality as local UI state and dispatches SelectSelection on " +
				"apply"
		),
		ActionDisposition.Internal(
			action = "record.CreateSyntheticTerm.Observe",
			reason = "screen bootstrap and local state observation"
		),
		ActionDisposition.Internal(
			action = "record.Record.ObserveRecord",
			reason = "screen bootstrap"
		),
		ActionDisposition.Internal(
			action = "record.Record.EnsureRecordLoaded",
			reason = "route entry preserves initial record refresh while local content wins"
		),
		ActionDisposition.Internal(
			action = "record.Schedule.ObserveSchedule",
			reason = "screen bootstrap"
		),
		ActionDisposition.Internal(
			action = "subjects.SubjectDetail.LoadSubjectDetail",
			reason = "detail route bootstrap after user selects a subject"
		),
		ActionDisposition.Internal(
			action = "subjects.SubjectSearch.ObserveSubjectSearch",
			reason = "search query observation"
		),
		ActionDisposition.Internal(
			action = "summary.Summary.ObserveSummary",
			reason = "screen bootstrap"
		),
		ActionDisposition.PlatformEdge(
			action = "summary.Summary.TakeProfilePicture",
			reason = "camera picker requires UI Automator/XCUITest"
		),
		ActionDisposition.PlatformEdge(
			action = "summary.Summary.PickProfilePicture",
			reason = "photo picker requires UI Automator/XCUITest"
		),
		ActionDisposition.PlatformEdge(
			action = "summary.Summary.UploadProfilePicture",
			reason =
				"Upload requires a native picker/camera file source; Maestro covers the visible triggers, " +
				"while file selection and upload variants belong in UI Automator/XCUITest or a debug-only " +
				"host adapter."
		),
		ActionDisposition.Internal(
			action = "wizard.CoachmarkOverlay.SurfaceChanged",
			reason = "host bridge from active real ViewState to contextual onboarding eligibility"
		)
	)
}
