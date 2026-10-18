package com.gdavidpb.tuindice.summary.presentation.machine

import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.summary.domain.usecase.MarkTermReportSeenUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveTermReportUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RateTermReportUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UpdateTermReportsUseCase
import com.gdavidpb.tuindice.summary.presentation.contract.TermReport
import com.gdavidpb.tuindice.summary.presentation.viewmodel.TermReportViewModel
import com.gdavidpb.tuindice.summary.testing.ControllableAssistantInsightsRepository
import com.gdavidpb.tuindice.summary.testing.ControllableRecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.summary.testing.ControllableTermReportSeenRepository
import com.gdavidpb.tuindice.summary.testing.ControllableVisibleAcademicRecordRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversAlphabet
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversEffects
import com.gdavidpb.tuindice.testkit.mvi.assertMachineHasNoShadowedRows
import com.gdavidpb.tuindice.testkit.mvi.assertMachineStatesReachable
import kotlin.test.Test

class TermReportStateMachineContractTest {
	@Test
	fun machine_coversAlphabet_andStatesAreReachable() {
		val machine = createViewModel().machine

		assertMachineCoversAlphabet(
			machine,
			TermReport.Action::class,
			TermReportInternalEvent::class
		)

		assertMachineHasNoShadowedRows(
			machine,
			TermReport.Action::class,
			TermReportInternalEvent::class
		)

		assertMachineStatesReachable(
			machine = machine,
			initialState = TermReport.State.Idle::class
		)

		assertMachineCoversEffects(machine, TermReport.Effect::class)
	}

	private fun createViewModel(): TermReportViewModel {
		val reporting = RecordingReportingRepository()
		val insights = ControllableAssistantInsightsRepository()

		return TermReportViewModel(
			screenMachine = TermReportMachine(
				observeTermReportUseCase = ObserveTermReportUseCase(
					insightsRepository = insights,
					visibleAcademicRecordRepository = ControllableVisibleAcademicRecordRepository(),
					recordDataPrerequisiteRepository = ControllableRecordDataPrerequisiteRepository(),
					reportingRepository = reporting
				),
				markTermReportSeenUseCase = MarkTermReportSeenUseCase(ControllableTermReportSeenRepository(), reporting),
				rateTermReportUseCase = RateTermReportUseCase(insights, reporting),
				updateTermReportsUseCase = UpdateTermReportsUseCase(insights, reporting)
			),
			eventPublisher = NoOpEventPublisher
		)
	}
}
