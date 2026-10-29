package com.gdavidpb.tuindice.summary.presentation.machine

import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.summary.domain.usecase.MarkTermReportSeenUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveTermReportUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RateTermReportUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.TermReportVoteQueue
import com.gdavidpb.tuindice.summary.domain.usecase.UpdateTermReportsUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.ObserveTermReportExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RateTermReportExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UpdateTermReportsExceptionHandler
import com.gdavidpb.tuindice.summary.presentation.contract.TermReport
import com.gdavidpb.tuindice.summary.presentation.viewmodel.TermReportViewModel
import com.gdavidpb.tuindice.summary.testing.ControllableAssistantInsightsRepository
import com.gdavidpb.tuindice.summary.testing.ControllableRecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.summary.testing.ControllableTermReportFeedbackRepository
import com.gdavidpb.tuindice.summary.testing.ControllableTermReportSeenRepository
import com.gdavidpb.tuindice.summary.testing.ControllableVisibleAcademicRecordRepository
import com.gdavidpb.tuindice.summary.testing.termReportItemFixture
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversAlphabet
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversEffects
import com.gdavidpb.tuindice.testkit.mvi.assertMachineHasNoShadowedRows
import com.gdavidpb.tuindice.testkit.mvi.assertMachineRandomWalk
import com.gdavidpb.tuindice.testkit.mvi.assertMachineStatesReachable
import com.gdavidpb.tuindice.testkit.mvi.exportToMermaid
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

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

	@Test
	fun machine_exportsDeclaredTransitionsToMermaid() {
		val diagram = createViewModel().machine.exportToMermaid(
			machineName = "summary_term_report",
			initialState = TermReport.State.Idle::class
		)

		val expectedFragments = listOf(
			"idle",
			"loading",
			"content",
			"Open",
			"ReportObserved",
			"ReportMissing",
			"ReportUnavailable",
			"FeedbackRejected"
		)

		for (fragment in expectedFragments) {
			assertTrue(
				diagram.contains(fragment),
				"Expected Mermaid export to mention '$fragment':\n$diagram"
			)
		}
	}

	@Test
	fun machine_survivesSeededRandomWalk() = runTest {
		val report = termReportItemFixture()

		assertMachineRandomWalk(
			screenMachine = createMachine(),
			sampleEvents = listOf(
				TermReport.Action.Open(insightId = report.insightId),
				TermReport.Action.Retry,
				TermReport.Action.Rate(useful = true),
				TermReport.Action.Rate(useful = false),
				TermReport.Action.Share,
				TermReport.Action.OpenNextAction(action = report.nextActions.first()),
				TermReportInternalEvent.ReportObserved(report = report),
				TermReportInternalEvent.ReportMissing,
				TermReportInternalEvent.ReportUnavailable(cause = null),
				TermReportInternalEvent.FeedbackRejected(thumb = 1, rejected = false),
				TermReportInternalEvent.ThumbSettled(thumb = 1)
			),
			coroutineScope = backgroundScope
		)
	}

	private fun createViewModel(): TermReportViewModel {
		return TermReportViewModel(
			screenMachine = createMachine(),
			eventPublisher = NoOpEventPublisher
		)
	}

	private fun createMachine(): TermReportMachine {
		val reporting = RecordingReportingRepository()
		val insights = ControllableAssistantInsightsRepository()

		return TermReportMachine(
			observeTermReportUseCase = ObserveTermReportUseCase(
				insightsRepository = insights,
				visibleAcademicRecordRepository = ControllableVisibleAcademicRecordRepository(),
				recordDataPrerequisiteRepository = ControllableRecordDataPrerequisiteRepository(),
				feedbackRepository = ControllableTermReportFeedbackRepository(),
				reportingRepository = reporting,
				exceptionHandler = ObserveTermReportExceptionHandler()
			),
			markTermReportSeenUseCase = MarkTermReportSeenUseCase(ControllableTermReportSeenRepository(), reporting),
			sessionCoroutineScope = testSessionCoroutineScope(),
			rateTermReportUseCase = RateTermReportUseCase(
				insightsRepository = insights,
				feedbackRepository = ControllableTermReportFeedbackRepository(),
				sessionCoroutineScope = testSessionCoroutineScope(),
				termReportVoteQueue = TermReportVoteQueue(),
				reportingRepository = reporting,
				exceptionHandler = RateTermReportExceptionHandler()
			),
			updateTermReportsUseCase = UpdateTermReportsUseCase(
				insightsRepository = insights,
				reportingRepository = reporting,
				exceptionHandler = UpdateTermReportsExceptionHandler()
			)
		)
	}
}
