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
		ActionDisposition.Pending(
			action = "about.About.OpenAssistantMemory",
			reason = "E2E del asistente pendiente (A11)"
		),
		ActionDisposition.PlatformEdge(
			action = "about.About.ReportBug",
			reason =
				"the trigger opens the mail app and nothing the scenario can assert follows: on Android Gmail, " +
				"with no account in the emulator, opens its compose screen and closes it by itself within about " +
				"100 ms, so the app was seen leaving the foreground in 0 of 10 runs; on iOS the app stayed in " +
				"front in 3 of 3 runs (docs/e2e-mediciones.md, section 3). `about-platform-edge-triggers` taps it " +
				"and asserts that About is usable when the app is back"
		),
		ActionDisposition.Pending(
			action = "about.About.SetAssistantConsent",
			reason = "E2E del asistente pendiente (A11)"
		),
		ActionDisposition.Pending(
			action = "assistant.AssistantChat.AcceptConsent",
			reason = "E2E del asistente pendiente (A11)"
		),
		ActionDisposition.Pending(
			action = "assistant.AssistantChat.AnswerStillApplies",
			reason = "E2E del asistente pendiente (A11)"
		),
		ActionDisposition.Pending(
			action = "assistant.AssistantChat.Appeal",
			reason = "E2E del asistente pendiente (A11)"
		),
		ActionDisposition.Pending(
			action = "assistant.AssistantChat.ChangeText",
			reason = "E2E del asistente pendiente (A11)"
		),
		ActionDisposition.Pending(
			action = "assistant.AssistantChat.ConfirmIntent",
			reason = "E2E del asistente pendiente (A11)"
		),
		ActionDisposition.Pending(
			action = "assistant.AssistantChat.CopyMessage",
			reason = "E2E del asistente pendiente (A11)"
		),
		ActionDisposition.Pending(
			action = "assistant.AssistantChat.DeclineConsent",
			reason = "E2E del asistente pendiente (A11)"
		),
		ActionDisposition.Pending(
			action = "assistant.AssistantChat.Dismiss",
			reason = "E2E del asistente pendiente (A11)"
		),
		ActionDisposition.Pending(
			action = "assistant.AssistantChat.DismissIntent",
			reason = "E2E del asistente pendiente (A11)"
		),
		ActionDisposition.Pending(
			action = "assistant.AssistantChat.Open",
			reason = "E2E del asistente pendiente (A11)"
		),
		ActionDisposition.Pending(
			action = "assistant.AssistantChat.OpenComponentSheet",
			reason = "E2E del asistente pendiente (A11)"
		),
		ActionDisposition.Pending(
			action = "assistant.AssistantChat.OpenNavTarget",
			reason = "E2E del asistente pendiente (A11)"
		),
		ActionDisposition.Pending(
			action = "assistant.AssistantChat.PickSuggestion",
			reason = "E2E del asistente pendiente (A11)"
		),
		ActionDisposition.Pending(
			action = "assistant.AssistantChat.RateMessage",
			reason = "E2E del asistente pendiente (A11)"
		),
		ActionDisposition.Pending(
			action = "assistant.AssistantChat.RemoveContext",
			reason = "E2E del asistente pendiente (A11)"
		),
		ActionDisposition.Pending(
			action = "assistant.AssistantChat.ReportMessage",
			reason = "E2E del asistente pendiente (A11)"
		),
		ActionDisposition.Pending(
			action = "assistant.AssistantChat.RetryTurn",
			reason = "E2E del asistente pendiente (A11)"
		),
		ActionDisposition.Pending(
			action = "assistant.AssistantChat.Send",
			reason = "E2E del asistente pendiente (A11)"
		),
		ActionDisposition.Pending(
			action = "assistant.AssistantChat.ToggleIntentDraft",
			reason = "E2E del asistente pendiente (A11)"
		),
		ActionDisposition.Internal(
			action = "assistant.AssistantComponentSheet.Open",
			reason = "route state bootstrap: the route dispatches it when the sheet opens"
		),
		ActionDisposition.Pending(
			action = "assistant.AssistantComponentSheet.OpenNavTarget",
			reason = "E2E del asistente pendiente (A11)"
		),
		ActionDisposition.Pending(
			action = "assistant.AssistantMemory.AnswerStillApplies",
			reason = "E2E del asistente pendiente (A11)"
		),
		ActionDisposition.Pending(
			action = "assistant.AssistantMemory.ClearAll",
			reason = "E2E del asistente pendiente (A11)"
		),
		ActionDisposition.Pending(
			action = "assistant.AssistantMemory.ConfirmClearAll",
			reason = "E2E del asistente pendiente (A11)"
		),
		ActionDisposition.Pending(
			action = "assistant.AssistantMemory.DeleteEntry",
			reason = "E2E del asistente pendiente (A11)"
		),
		ActionDisposition.Pending(
			action = "assistant.AssistantMemory.Load",
			reason = "E2E del asistente pendiente (A11)"
		),
		ActionDisposition.Pending(
			action = "assistant.AssistantMemory.Retry",
			reason = "E2E del asistente pendiente (A11)"
		),
		ActionDisposition.Internal(
			action = "auth.SignOut.Initialize",
			reason = "route state bootstrap from pending changes"
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
			action = "maincore.Browser.SetLoading",
			reason = "renderer callback"
		),
		ActionDisposition.Internal(
			action = "maincore.Browser.SetLoadFailed",
			reason = "renderer callback"
		),
		ActionDisposition.Internal(
			action = "maincore.Main.RequestSync",
			reason = "the host dispatches it each time the app resumes with content; no user performs it"
		),
		ActionDisposition.Internal(
			action = "maincore.Main.SessionEndHandled",
			reason = "the host route reports that it attended the end of a session that ended behind the user's back"
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
			action = "record.Schedule.Resumed",
			reason = "the sheet dispatches it each time it comes back in front, to read the clock again; no user performs it"
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
			action = "summary.ShareTerm.ObserveShareTerm",
			reason = "the sheet asks for its terms when it opens, on the term its opener names; no user performs it"
		),
		ActionDisposition.Internal(
			action = "summary.Summary.ObserveSummary",
			reason = "screen bootstrap"
		),
		ActionDisposition.Internal(
			action = "summary.Summary.Resumed",
			reason = "the screen dispatches it each time it comes back in front, to read the clock again; no user performs it"
		),
		ActionDisposition.PlatformEdge(
			action = "summary.Summary.UploadProfilePicture",
			reason =
				"it runs once the system picker or camera hands a file back, and no scenario can put a " +
				"file in them; the upload path needs a debug-only source of that file"
		),
		ActionDisposition.Internal(
			action = "summary.SummaryStatistics.ObserveStatistics",
			reason = "screen bootstrap"
		),
		ActionDisposition.Internal(
			action = "summary.SummaryStatistics.Resumed",
			reason =
				"the screen dispatches it each time it comes back in front, to read the clock again; no user " +
				"performs it"
		),
		ActionDisposition.Internal(
			action = "wizard.CoachmarkOverlay.SurfaceChanged",
			reason = "host bridge from active real ViewState to contextual onboarding eligibility"
		)
	)
}
