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
			action = "assistant.AssistantChat.Appeal",
			reason = "detrás de la sanción: solo aparece con una restricción vigente y la apelación activada; " +
				"en la build de E2E la apelación está apagada en sombra"
		),
		ActionDisposition.PlatformEdge(
			action = "assistant.AssistantChat.AttachPhoto",
			reason =
				"Abre la cámara o la galería del sistema; se prueba a mano " +
				"(ver e2e/platform/*/assistant-photo-picker.md)"
		),
		ActionDisposition.Pending(
			action = "assistant.AssistantChat.Dismiss",
			reason = "la hoja se cierra con el gesto del sistema (back en Android; el harness no lo traduce en iOS)"
		),
		ActionDisposition.Internal(
			action = "assistant.AssistantChat.Open",
			reason = "the sheet opens from a screen with the context that screen declares; the route dispatches it, " +
				"no user performs it"
		),
		ActionDisposition.PlatformEdge(
			action = "assistant.AssistantChat.RequestPhoto",
			reason =
				"Abre la cámara o la galería del sistema; se prueba a mano " +
				"(ver e2e/platform/*/assistant-photo-picker.md)"
		),
		ActionDisposition.Internal(
			action = "assistant.AssistantComponentSheet.Open",
			reason = "route state bootstrap: the route dispatches it when the sheet opens"
		),
		ActionDisposition.Internal(
			action = "assistant.AssistantMemory.Load",
			reason = "screen bootstrap: the memory screen dispatches it as its initial action"
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
