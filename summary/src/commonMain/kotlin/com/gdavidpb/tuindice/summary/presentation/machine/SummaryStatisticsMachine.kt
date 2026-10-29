package com.gdavidpb.tuindice.summary.presentation.machine

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveSummaryHubUseCase
import com.gdavidpb.tuindice.summary.presentation.contract.SummaryStatistics
import com.gdavidpb.tuindice.summary.presentation.mapper.summaryStatisticsUnavailableMessage
import com.gdavidpb.tuindice.summary.presentation.mapper.summaryStatisticsUnavailableTitle
import com.gdavidpb.tuindice.summary.presentation.mapper.toSummaryStatisticsItem
import com.gdavidpb.tuindice.summary.presentation.transition.statisticsAnyStateTransitions
import com.gdavidpb.tuindice.summary.presentation.transition.statisticsContentTransitions
import com.gdavidpb.tuindice.summary.presentation.transition.statisticsIdleTransitions
import com.gdavidpb.tuindice.summary.presentation.transition.statisticsLoadingTransitions
import com.gdavidpb.tuindice.summary.presentation.transition.statisticsUnavailableTransitions
import kotlinx.coroutines.Job

class SummaryStatisticsMachine(
	private val observeSummaryHubUseCase: ObserveSummaryHubUseCase
) : ScreenMachine<SummaryStatistics.State, SummaryStatistics.Effect> {
	// The hub's observation is restarted when the screen comes back, to read the clock again: the job is kept to cancel
	// it.
	private var observationJob: Job? = null

	// The first resumption is the screen's own opening, not a return: only the ones after it restart anything.
	private var hasSeenTheOpening = false

	override fun initialState(): SummaryStatistics.State = SummaryStatistics.State.Idle

	override fun define(host: MachineHost<SummaryStatistics.Effect>): MachineDefinition<SummaryStatistics.State> {
		return MachineDefinition.define {
			statisticsIdleTransitions(machine = this@SummaryStatisticsMachine, host = host)
			statisticsLoadingTransitions()
			statisticsContentTransitions(host = host)
			statisticsUnavailableTransitions()
			statisticsAnyStateTransitions(machine = this@SummaryStatisticsMachine, host = host)
		}
	}

	// What the hub says the statistics are, read as the hub reads them: the hub has none only when the record is not
	// ready. An observation that fails has none to give either, so the screen says so instead of loading forever.
	internal fun startObservation(host: MachineHost<SummaryStatistics.Effect>) {
		if (observationJob?.isActive == true) return

		observationJob = host.launchMachineJob {
			observeSummaryHubUseCase.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> {
						val statistics = useCaseState.value.toSummaryStatisticsItem()

						host.processInternalEvent(
							if (statistics == null) {
								SummaryStatisticsInternalEvent.StatisticsUnavailable
							} else {
								SummaryStatisticsInternalEvent.StatisticsObserved(statistics = statistics)
							}
						)
					}

					is UseCaseState.Error -> host.processInternalEvent(SummaryStatisticsInternalEvent.StatisticsUnavailable)
				}
			}
		}
	}

	internal fun resumed(host: MachineHost<SummaryStatistics.Effect>) {
		if (!hasSeenTheOpening) {
			hasSeenTheOpening = true
			return
		}

		restartObservation(host = host)
	}

	// Coming back from the background the clock's ticker sleeps with the process, so the observation is started again:
	// it reads the clock first thing and the statistics are laid out for the moment it is now.
	private fun restartObservation(host: MachineHost<SummaryStatistics.Effect>) {
		val running = observationJob ?: return

		running.cancel()
		observationJob = null
		startObservation(host = host)
	}
}

// The state of a screen whose record is not ready, with the words that say so.
internal fun unavailableState(): SummaryStatistics.State.Unavailable {
	return SummaryStatistics.State.Unavailable(
		title = summaryStatisticsUnavailableTitle(),
		message = summaryStatisticsUnavailableMessage()
	)
}
