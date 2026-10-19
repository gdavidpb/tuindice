package com.gdavidpb.tuindice.summary.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.summary.presentation.contract.TermReport
import com.gdavidpb.tuindice.summary.presentation.machine.TermReportInternalEvent
import com.gdavidpb.tuindice.summary.presentation.machine.TermReportMachine
import com.gdavidpb.tuindice.summary.presentation.machine.termReportMissingState

internal fun MachineDefinitionBuilder<TermReport.State>.termReportContentTransitions(
	machine: TermReportMachine,
	host: MachineHost<TermReport.Effect>
) {
	from<TermReport.State.Content> {
		// The report is read again when the service or the record says something new. The thumb on screen is kept while a
		// vote of the student is pending; with none pending, the thumb follows the vote the service confirmed.
		on<TermReportInternalEvent.ReportObserved> { state, event ->
			val isVotePending = state.feedback != state.report.confirmedFeedback

			state.copy(
				report = event.report,
				feedback = if (isVotePending) state.feedback else event.report.confirmedFeedback
			)
		}

		onTo<TermReportInternalEvent.ReportMissing, TermReport.State.NoReport> { _, _ ->
			termReportMissingState()
		}

		// A report on screen is not taken away by a read that failed: it stays until the student leaves it.
		on<TermReportInternalEvent.ReportUnavailable> { state, _ -> state }

		// The service refused the thumb the screen shows: the one confirmed comes back. A refusal of a thumb the student has
		// already moved away from changes nothing on screen.
		on<TermReportInternalEvent.FeedbackRejected> { state, event ->
			if (state.feedback == event.rejected) {
				state.copy(feedback = state.report.confirmedFeedback)
			} else {
				state
			}
		}

		// The thumb is shown as chosen at once, and the service is asked. A tap on the thumb already shown asks nothing.
		on<TermReport.Action.Rate> { state, action ->
			if (state.feedback == action.useful) {
				state
			} else {
				machine.rate(host = host, insightId = state.report.insightId, useful = action.useful)

				state.copy(feedback = action.useful)
			}
		}

		on<TermReport.Action.Share>(emits = setOf(TermReport.Effect.NavigateToShareTerm::class)) { state, _ ->
			// The bar offers the share only for a term that can be shared; a share asked for another one changes nothing.
			if (state.report.isShareable) {
				host.sendEffect(TermReport.Effect.NavigateToShareTerm(termKey = state.report.termKey))
			}

			state
		}

		on<TermReport.Action.OpenNextAction>(
			emits = setOf(TermReport.Effect.NavigateToAssistantTarget::class)
		) { state, action ->
			host.sendEffect(
				TermReport.Effect.NavigateToAssistantTarget(
					target = action.action.target,
					entityRefs = action.action.entityRefs
				)
			)

			state
		}
	}
}
