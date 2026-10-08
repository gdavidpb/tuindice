package com.gdavidpb.tuindice.scenarios.catalog

/**
 * The actions no scenario has to cover, each with its reason: internal, platform edge or pending. Every other
 * action of every `presentation/contract` must be listed in some scenario's `covers`
 * (`ActionCoverageTest`).
 */
object ActionDispositions {
	val all: List<ActionDisposition> = listOf(
		ActionDisposition.Internal(
			action = "about.About.LoadVersion",
			reason = "screen bootstrap"
		),
		ActionDisposition.Internal(
			action = "auth.SignOut.Initialize",
			reason = "route state bootstrap from pending changes"
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
		ActionDisposition.Pending(
			action = "evaluations.Evaluations.RefreshEvaluations",
			reason =
				"its only trigger is the retry button of the failed list, and no scenario drives the " +
				"evaluations list into that state"
		),
		ActionDisposition.Pending(
			action = "evaluations.Evaluations.SelectWeek",
			reason =
				"no scenario taps a week of the strip; the scenarios that scroll the list only do it to " +
				"reach a card, and none checks which week that leaves selected"
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
			action = "maincore.Browser.ClickRetry",
			reason =
				"forcing a web view load failure needs host-level network manipulation; the retry UI is " +
				"covered by BrowserScreenUiTest"
		),
		ActionDisposition.Internal(
			action = "maincore.Main.RequestSync",
			reason = "the host dispatches it each time the app resumes with content; no user performs it"
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
		ActionDisposition.Pending(
			action = "record.CreateSyntheticTerm.RemoveSubject",
			reason =
				"the lifecycle scenario only checks that the remove button shows; no scenario presses it"
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
		ActionDisposition.Pending(
			action = "summary.Summary.TakeProfilePicture",
			reason =
				"no scenario taps the camera option: it opens the system camera, which the suite can reach " +
				"as system UI but nobody has written a scenario for"
		),
		ActionDisposition.Pending(
			action = "summary.Summary.PickProfilePicture",
			reason =
				"no scenario taps the gallery option: it opens the system photo picker, which the suite " +
				"can reach as system UI but nobody has written a scenario for"
		),
		ActionDisposition.PlatformEdge(
			action = "summary.Summary.UploadProfilePicture",
			reason =
				"it runs once the system picker or camera hands a file back, and no scenario can put a " +
				"file in them; the upload path needs a debug-only source of that file"
		),
		ActionDisposition.Internal(
			action = "wizard.CoachmarkOverlay.SurfaceChanged",
			reason = "host bridge from active real ViewState to contextual onboarding eligibility"
		)
	)
}
