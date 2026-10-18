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
		// The report is read again when the service or the record says something new: the thumb the student chose stays.
		on<TermReportInternalEvent.ReportObserved> { state, event ->
			state.copy(report = event.report)
		}

		onTo<TermReportInternalEvent.ReportMissing, TermReport.State.NoReport> { _, _ ->
			termReportMissingState()
		}

		// A report on screen is not taken away by a read that failed: it stays until the student leaves it.
		on<TermReportInternalEvent.ReportUnavailable> { state, _ -> state }

		on<TermReportInternalEvent.FeedbackRejected> { state, event ->
			state.copy(feedback = event.previous)
		}

		// The thumb is shown as chosen at once, and the service is asked; a refusal gives the previous one back.
		on<TermReport.Action.Rate> { state, action ->
			machine.rate(
				host = host,
				insightId = state.report.insightId,
				useful = action.useful,
				previous = state.feedback
			)

			state.copy(feedback = action.useful)
		}

		on<TermReport.Action.Share>(emits = setOf(TermReport.Effect.NavigateToShareTerm::class)) { state, _ ->
			host.sendEffect(TermReport.Effect.NavigateToShareTerm(termKey = state.report.termKey))

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
