package com.gdavidpb.tuindice.summary.presentation.machine

import com.gdavidpb.tuindice.base.domain.coroutine.SessionCoroutineScope
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import com.gdavidpb.tuindice.summary.domain.model.TermReportObservation
import com.gdavidpb.tuindice.summary.domain.usecase.MarkTermReportSeenUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveTermReportUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RateTermReportUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UpdateTermReportsUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.param.ObserveTermReportParams
import com.gdavidpb.tuindice.summary.domain.usecase.param.RateTermReportParams
import com.gdavidpb.tuindice.summary.presentation.contract.TermReport
import com.gdavidpb.tuindice.summary.presentation.mapper.toTermReportItem
import com.gdavidpb.tuindice.summary.presentation.model.TermReportItem
import com.gdavidpb.tuindice.summary.presentation.transition.termReportAnyStateTransitions
import com.gdavidpb.tuindice.summary.presentation.transition.termReportContentTransitions
import com.gdavidpb.tuindice.summary.presentation.transition.termReportFailedTransitions
import com.gdavidpb.tuindice.summary.presentation.transition.termReportIdleTransitions
import com.gdavidpb.tuindice.summary.presentation.transition.termReportLoadingTransitions
import com.gdavidpb.tuindice.summary.presentation.transition.termReportNoReportTransitions
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.launch

class TermReportMachine(
	private val observeTermReportUseCase: ObserveTermReportUseCase,
	private val markTermReportSeenUseCase: MarkTermReportSeenUseCase,
	private val rateTermReportUseCase: RateTermReportUseCase,
	private val updateTermReportsUseCase: UpdateTermReportsUseCase,
	private val sessionCoroutineScope: SessionCoroutineScope
) : ScreenMachine<TermReport.State, TermReport.Effect> {
	// The report the screen was opened on: a retry reads it again.
	private var insightId: String? = null

	// The observation of the report is kept to cancel it when it is started again.
	private var observationJob: Job? = null

	// The number of the last thumb sent: each vote is numbered, and the screen knows which one is the last from its state
	// (see TermReport.State.Content). A thumb a newer one replaces is not cancelled: its flow runs to its end, so the use
	// case reports its refusal like any other (see FlowUseCase).
	private var lastThumb = 0

	// The report is marked as seen once, the first time it reaches the screen.
	private var isMarkedSeen = false

	override fun initialState(): TermReport.State = TermReport.State.Idle

	override fun define(host: MachineHost<TermReport.Effect>): MachineDefinition<TermReport.State> {
		return MachineDefinition.define {
			termReportIdleTransitions(machine = this@TermReportMachine, host = host)
			termReportLoadingTransitions(machine = this@TermReportMachine, host = host)
			termReportContentTransitions(machine = this@TermReportMachine, host = host)
			termReportFailedTransitions(machine = this@TermReportMachine, host = host)
			termReportNoReportTransitions(machine = this@TermReportMachine, host = host)
			termReportAnyStateTransitions()
		}
	}

	// Opening the report reads it. It is marked as seen once it is on screen, not when it is only asked for: a report
	// that could not be read stays on the summary's line.
	internal fun open(host: MachineHost<TermReport.Effect>, insightId: String) {
		this.insightId = insightId

		startObservation(host = host)
	}

	internal fun retry(host: MachineHost<TermReport.Effect>) {
		startObservation(host = host)
	}

	// The state of a report on screen: the thumb the student's vote was confirmed with is the one shown. The first time
	// the report reaches the screen, it is marked as seen.
	internal fun contentOf(host: MachineHost<TermReport.Effect>, report: TermReportItem): TermReport.State.Content {
		markSeenOnce(host = host)

		return TermReport.State.Content(report = report, feedback = report.confirmedFeedback)
	}

	private fun markSeenOnce(host: MachineHost<TermReport.Effect>) {
		if (isMarkedSeen) return

		val reportId = insightId ?: return
		isMarkedSeen = true

		host.launchMachineJob {
			markTermReportSeenUseCase.execute(reportId).collect()
		}
	}

	// The report as the service and the record say it is now. A report the observation cannot read says why, and the
	// screen offers to try again; a report that is not there says that it is not.
	private fun startObservation(host: MachineHost<TermReport.Effect>) {
		val reportId = insightId ?: return

		observationJob?.cancel()
		observationJob = host.launchMachineJob {
			val refresh = updateTermReportsUseCase.execute(Unit).lastOrNull()
			val isRefreshed = refresh is UseCaseState.Data
			val refreshError = (refresh as? UseCaseState.Error)?.error
			val params = ObserveTermReportParams(
				insightId = reportId,
				isRefreshed = isRefreshed,
				refreshError = refreshError
			)

			observeTermReportUseCase.execute(params).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> when (val observation = useCaseState.value) {
						is TermReportObservation.Found -> host.processInternalEvent(
							TermReportInternalEvent.ReportObserved(report = observation.report.toTermReportItem())
						)

						TermReportObservation.Missing -> host.processInternalEvent(TermReportInternalEvent.ReportMissing)

						is TermReportObservation.Unavailable -> host.processInternalEvent(
							TermReportInternalEvent.ReportUnavailable(cause = observation.error)
						)
					}

					is UseCaseState.Error -> host.processInternalEvent(
						TermReportInternalEvent.ReportUnavailable(cause = useCaseState.error)
					)
				}
			}
		}
	}

	// The thumb goes to the service after the one before it, of the same report. The screen no longer acts on that
	// one: a vote the service takes is kept on the device all the same. A refusal gives the thumb back to the screen,
	// when the screen still shows the one refused.
	//
	// The vote starts here, in the session and at once. The thumbs then reach the queue in the order the student gave
	// them, whatever order the screen's own jobs happen to be scheduled in.
	// The number of the vote is returned: the screen keeps it as the thumb in flight (see TermReport.State.Content), and
	// the vote reports its refusal and its end with it.
	internal fun rate(host: MachineHost<TermReport.Effect>, insightId: String, useful: Boolean): Int {
		val thumb = ++lastThumb

		sessionCoroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
			rateTermReportUseCase.execute(RateTermReportParams(insightId = insightId, useful = useful)).collect { state ->
				if (state is UseCaseState.Error) {
					host.processInternalEvent(TermReportInternalEvent.FeedbackRejected(thumb = thumb, rejected = useful))
				}
			}

			host.processInternalEvent(TermReportInternalEvent.ThumbSettled(thumb = thumb))
		}

		return thumb
	}
}
