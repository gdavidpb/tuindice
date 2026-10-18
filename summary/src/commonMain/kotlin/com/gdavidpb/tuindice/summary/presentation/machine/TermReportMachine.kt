package com.gdavidpb.tuindice.summary.presentation.machine

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import com.gdavidpb.tuindice.summary.domain.usecase.MarkTermReportSeenUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveTermReportUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RateTermReportUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UpdateTermReportsUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.param.ObserveTermReportParams
import com.gdavidpb.tuindice.summary.domain.usecase.param.RateTermReportParams
import com.gdavidpb.tuindice.summary.presentation.contract.TermReport
import com.gdavidpb.tuindice.summary.presentation.mapper.toTermReportItem
import com.gdavidpb.tuindice.summary.presentation.transition.termReportContentTransitions
import com.gdavidpb.tuindice.summary.presentation.transition.termReportFailedTransitions
import com.gdavidpb.tuindice.summary.presentation.transition.termReportIdleTransitions
import com.gdavidpb.tuindice.summary.presentation.transition.termReportLoadingTransitions
import com.gdavidpb.tuindice.summary.presentation.transition.termReportNoReportTransitions
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.lastOrNull
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.term_report_failed_message
import tuindice.summary.generated.resources.term_report_failed_title
import tuindice.summary.generated.resources.term_report_no_report_message
import tuindice.summary.generated.resources.term_report_no_report_title

class TermReportMachine(
	private val observeTermReportUseCase: ObserveTermReportUseCase,
	private val markTermReportSeenUseCase: MarkTermReportSeenUseCase,
	private val rateTermReportUseCase: RateTermReportUseCase,
	private val updateTermReportsUseCase: UpdateTermReportsUseCase
) : ScreenMachine<TermReport.State, TermReport.Effect> {
	// The report the screen was opened on: a retry reads it again.
	private var insightId: String? = null

	// The observation of the report is kept to cancel it when it is started again.
	private var observationJob: Job? = null

	override fun initialState(): TermReport.State = TermReport.State.Idle

	override fun define(host: MachineHost<TermReport.Effect>): MachineDefinition<TermReport.State> {
		return MachineDefinition.define {
			termReportIdleTransitions(machine = this@TermReportMachine, host = host)
			termReportLoadingTransitions()
			termReportContentTransitions(machine = this@TermReportMachine, host = host)
			termReportFailedTransitions(machine = this@TermReportMachine, host = host)
			termReportNoReportTransitions()
		}
	}

	// Opening the report reads it and marks it as seen: the summary stops offering the line once the student has looked.
	internal fun open(host: MachineHost<TermReport.Effect>, insightId: String) {
		this.insightId = insightId

		host.launchMachineJob {
			markTermReportSeenUseCase.execute(insightId).collect()
		}

		startObservation(host = host)
	}

	internal fun retry(host: MachineHost<TermReport.Effect>) {
		startObservation(host = host)
	}

	// The report as the service and the record say it is now. A report the observation cannot read says so, and the screen
	// offers to try again; a report that is not there says that it is not.
	private fun startObservation(host: MachineHost<TermReport.Effect>) {
		val reportId = insightId ?: return

		observationJob?.cancel()
		observationJob = host.launchMachineJob {
			val refresh = updateTermReportsUseCase.execute(Unit).lastOrNull()
			val isRefreshed = refresh is UseCaseState.Data && refresh.value
			val params = ObserveTermReportParams(insightId = reportId, isRefreshed = isRefreshed)

			observeTermReportUseCase.execute(params).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> {
						val report = useCaseState.value

						host.processInternalEvent(
							if (report == null) {
								TermReportInternalEvent.ReportMissing
							} else {
								TermReportInternalEvent.ReportObserved(report = report.toTermReportItem())
							}
						)
					}

					is UseCaseState.Error -> host.processInternalEvent(TermReportInternalEvent.ReportUnavailable)
				}
			}
		}
	}

	// The thumb goes to the service. A refusal gives the thumb back to the screen, which had shown the other one selected.
	internal fun rate(host: MachineHost<TermReport.Effect>, insightId: String, useful: Boolean, previous: Boolean?) {
		host.launchMachineJob {
			rateTermReportUseCase.execute(RateTermReportParams(insightId = insightId, useful = useful)).collect { state ->
				if (state is UseCaseState.Error) {
					host.processInternalEvent(TermReportInternalEvent.FeedbackRejected(previous = previous))
				}
			}
		}
	}
}

// The state of a report the service does not have, with the words that say so.
internal fun termReportMissingState(): TermReport.State.NoReport {
	return TermReport.State.NoReport(
		title = UiText.Resource(Res.string.term_report_no_report_title),
		message = UiText.Resource(Res.string.term_report_no_report_message)
	)
}

// The state of a report that cannot be read at the moment, with the words that offer to try again.
internal fun termReportUnavailableState(): TermReport.State.Failed {
	return TermReport.State.Failed(
		title = UiText.Resource(Res.string.term_report_failed_title),
		message = UiText.Resource(Res.string.term_report_failed_message)
	)
}
