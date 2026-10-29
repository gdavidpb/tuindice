package com.gdavidpb.tuindice.summary.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.base.domain.model.RecordDataPrerequisiteState
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantTermInsight
import com.gdavidpb.tuindice.base.domain.repository.AssistantInsightsRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.summary.domain.model.TermReportObservation
import com.gdavidpb.tuindice.summary.domain.usecase.error.ObserveTermReportUseCaseError
import com.gdavidpb.tuindice.summary.domain.usecase.error.UpdateTermReportsUseCaseError
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.ObserveTermReportExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.param.ObserveTermReportParams
import com.gdavidpb.tuindice.summary.testing.ControllableAssistantInsightsRepository
import com.gdavidpb.tuindice.summary.testing.ControllableRecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.summary.testing.ControllableTermReportFeedbackRepository
import com.gdavidpb.tuindice.summary.testing.ControllableVisibleAcademicRecordRepository
import com.gdavidpb.tuindice.summary.testing.hubClosedTerm
import com.gdavidpb.tuindice.summary.testing.hubPassedAttempt
import com.gdavidpb.tuindice.summary.testing.hubRecord
import com.gdavidpb.tuindice.summary.testing.termReportInsight
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ObserveTermReportUseCaseTest {

	private val params = ObserveTermReportParams(insightId = "term-1:2", isRefreshed = true)

	private val record = hubRecord(
		hubClosedTerm(
			year = 2026,
			attempts = listOf(hubPassedAttempt("CI1")),
			index = 4.0,
			periodCode = AcademicTermPeriod.APR_JUL
		)
	)

	@Test
	fun theReportOfAnInsightIsReadWithItsFigures() = runTest {
		val useCase = createObserveTermReportUseCase(
			insights = ControllableAssistantInsightsRepository(listOf(termReportInsight(termKey = "2026-APR_JUL"))),
			record = ControllableVisibleAcademicRecordRepository(record)
		)

		useCase.execute(params).test {
			assertIs<UseCaseState.Loading>(awaitItem())

			val report = assertIs<TermReportObservation.Found>(
				assertIs<UseCaseState.Data<TermReportObservation>>(awaitItem()).value
			).report

			assertEquals("term-1:2", report.insightId)
			assertEquals("term-1", report.termId)
			assertEquals(2, report.sections.size)
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun noReportWhenTheInsightIsNotTheStudentsOrTheRecordHoldsNoTermOfIt() = runTest {
		val notHere = createObserveTermReportUseCase(
			insights = ControllableAssistantInsightsRepository(),
			record = ControllableVisibleAcademicRecordRepository(record)
		)
		val noTerm = createObserveTermReportUseCase(
			insights = ControllableAssistantInsightsRepository(listOf(termReportInsight(termKey = "2027-JAN_MAR"))),
			record = ControllableVisibleAcademicRecordRepository(record)
		)

		notHere.execute(params).test {
			awaitItem()
			assertEquals(TermReportObservation.Missing, assertIs<UseCaseState.Data<TermReportObservation>>(awaitItem()).value)
			cancelAndIgnoreRemainingEvents()
		}

		noTerm.execute(params).test {
			awaitItem()
			assertEquals(TermReportObservation.Missing, assertIs<UseCaseState.Data<TermReportObservation>>(awaitItem()).value)
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun aReportThatIsUnknownAndNotAskedForIsUnavailableWithItsCause_andArrivesOnceTheListHasIt() = runTest {
		val insights = ControllableAssistantInsightsRepository()
		val useCase = createObserveTermReportUseCase(
			insights = insights,
			record = ControllableVisibleAcademicRecordRepository(record)
		)

		useCase.execute(ObserveTermReportParams(insightId = "term-1:2", isRefreshed = false)).test {
			awaitItem()

			val unavailable = assertIs<TermReportObservation.Unavailable>(
				assertIs<UseCaseState.Data<TermReportObservation>>(awaitItem()).value
			)
			assertEquals(ObserveTermReportUseCaseError.NoConnection, unavailable.error)

			insights.insightsFlow.value = listOf(termReportInsight(termKey = "2026-APR_JUL"))

			val found = assertIs<TermReportObservation.Found>(
				assertIs<UseCaseState.Data<TermReportObservation>>(awaitItem()).value
			)
			assertEquals("term-1:2", found.report.insightId)
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun aReportMissingAfterASessionEndedUpdate_isUnavailableWithSessionEnded() = runTest {
		val useCase = createObserveTermReportUseCase(
			insights = ControllableAssistantInsightsRepository(),
			record = ControllableVisibleAcademicRecordRepository(record)
		)

		useCase.execute(
			ObserveTermReportParams(
				insightId = "term-1:2",
				isRefreshed = false,
				refreshError = UpdateTermReportsUseCaseError.SessionEnded
			)
		).test {
			awaitItem()

			val unavailable = assertIs<TermReportObservation.Unavailable>(
				assertIs<UseCaseState.Data<TermReportObservation>>(awaitItem()).value
			)
			assertEquals(ObserveTermReportUseCaseError.SessionEnded, unavailable.error)
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun aReportMissingAfterAConnectionFailedUpdate_isUnavailableWithNoConnection() = runTest {
		val useCase = createObserveTermReportUseCase(
			insights = ControllableAssistantInsightsRepository(),
			record = ControllableVisibleAcademicRecordRepository(record)
		)

		useCase.execute(
			ObserveTermReportParams(
				insightId = "term-1:2",
				isRefreshed = false,
				refreshError = UpdateTermReportsUseCaseError.NoConnection
			)
		).test {
			awaitItem()

			val unavailable = assertIs<TermReportObservation.Unavailable>(
				assertIs<UseCaseState.Data<TermReportObservation>>(awaitItem()).value
			)
			assertEquals(ObserveTermReportUseCaseError.NoConnection, unavailable.error)
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun aRecordNotReadyYetIsUnavailable_andTheFlowStaysAliveUntilItIsReady() = runTest {
		val academic = ControllableVisibleAcademicRecordRepository(null)
		val prerequisite = ControllableRecordDataPrerequisiteRepository(isReady = false)
		val useCase = createObserveTermReportUseCase(
			insights = ControllableAssistantInsightsRepository(listOf(termReportInsight())),
			record = academic,
			prerequisite = prerequisite
		)

		useCase.execute(params).test {
			awaitItem()

			val unavailable = assertIs<TermReportObservation.Unavailable>(
				assertIs<UseCaseState.Data<TermReportObservation>>(awaitItem()).value
			)
			assertEquals(ObserveTermReportUseCaseError.RecordNotReady, unavailable.error)

			academic.recordFlow.value = hubRecord(
				hubClosedTerm(
					year = 2026,
					attempts = listOf(hubPassedAttempt("CI1")),
					index = 4.0,
					periodCode = AcademicTermPeriod.APR_JUL
				)
			)
			prerequisite.stateFlow.value = RecordDataPrerequisiteState(
				isReady = true,
				hasFailed = false,
				isNewStudentWithoutRecord = false
			)

			assertIs<TermReportObservation.Found>(
				assertIs<UseCaseState.Data<TermReportObservation>>(awaitItem()).value
			)
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun theThumbOfTheStudentIsReadWithTheReport() = runTest {
		val useCase = createObserveTermReportUseCase(
			insights = ControllableAssistantInsightsRepository(listOf(termReportInsight(termKey = "2026-APR_JUL"))),
			record = ControllableVisibleAcademicRecordRepository(record),
			feedback = ControllableTermReportFeedbackRepository(mapOf("term-1:2" to false))
		)

		useCase.execute(params).test {
			awaitItem()

			val found = assertIs<TermReportObservation.Found>(
				assertIs<UseCaseState.Data<TermReportObservation>>(awaitItem()).value
			)
			assertEquals(false, found.report.feedback)
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun aConnectionFailureUpstreamIsAControlledError() = runTest {
		val insights = object : AssistantInsightsRepository by ControllableAssistantInsightsRepository() {
			override fun observeInsightsFlow(): Flow<List<AssistantTermInsight>> =
				flow { throw IllegalStateException("network is unreachable") }
		}
		val useCase = createObserveTermReportUseCase(
			insights = insights,
			record = ControllableVisibleAcademicRecordRepository(record)
		)

		useCase.execute(params).test {
			awaitItem()
			assertEquals(
				ObserveTermReportUseCaseError.NoConnection,
				assertIs<UseCaseState.Error<ObserveTermReportUseCaseError>>(awaitItem()).error
			)
			awaitComplete()
		}
	}
}

private fun createObserveTermReportUseCase(
	insights: AssistantInsightsRepository,
	record: ControllableVisibleAcademicRecordRepository,
	prerequisite: ControllableRecordDataPrerequisiteRepository = ControllableRecordDataPrerequisiteRepository(),
	feedback: ControllableTermReportFeedbackRepository = ControllableTermReportFeedbackRepository()
) = ObserveTermReportUseCase(
	insightsRepository = insights,
	visibleAcademicRecordRepository = record,
	recordDataPrerequisiteRepository = prerequisite,
	feedbackRepository = feedback,
	reportingRepository = RecordingReportingRepository(),
	exceptionHandler = ObserveTermReportExceptionHandler()
)
