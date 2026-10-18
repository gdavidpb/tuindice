package com.gdavidpb.tuindice.summary.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.summary.domain.model.SummaryTermReport
import com.gdavidpb.tuindice.summary.testing.ControllableAssistantInsightsRepository
import com.gdavidpb.tuindice.summary.testing.ControllableRecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.summary.testing.ControllableVisibleAcademicRecordRepository
import com.gdavidpb.tuindice.summary.testing.hubClosedTerm
import com.gdavidpb.tuindice.summary.testing.hubPassedAttempt
import com.gdavidpb.tuindice.summary.testing.hubRecord
import com.gdavidpb.tuindice.summary.testing.termReportInsight
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.test.runTest
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class ObserveTermReportUseCaseTest {

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

		useCase.execute("term-1:2").test {
			assertIs<UseCaseState.Loading>(awaitItem())

			val report = assertIs<UseCaseState.Data<SummaryTermReport?>>(awaitItem()).value

			assertEquals("term-1:2", report?.insightId)
			assertEquals("term-1", report?.termId)
			assertEquals(2, report?.sections?.size)
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

		notHere.execute("term-1:2").test {
			awaitItem()
			assertNull(assertIs<UseCaseState.Data<SummaryTermReport?>>(awaitItem()).value)
			cancelAndIgnoreRemainingEvents()
		}

		noTerm.execute("term-1:2").test {
			awaitItem()
			assertNull(assertIs<UseCaseState.Data<SummaryTermReport?>>(awaitItem()).value)
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun failsWhenTheInsightIsUnknownAndTheServiceCouldNotBeAsked() = runTest {
		val useCase = createObserveTermReportUseCase(
			insights = ControllableAssistantInsightsRepository(refreshFailure = IOException("offline")),
			record = ControllableVisibleAcademicRecordRepository(record)
		)

		useCase.execute("term-1:2").test {
			awaitItem()
			assertIs<UseCaseState.Error<*>>(awaitItem())
			awaitComplete()
		}
	}

	@Test
	fun failsWhenTheRecordCannotBeReadYet() = runTest {
		val useCase = createObserveTermReportUseCase(
			insights = ControllableAssistantInsightsRepository(listOf(termReportInsight())),
			record = ControllableVisibleAcademicRecordRepository(null),
			prerequisite = ControllableRecordDataPrerequisiteRepository(isReady = false)
		)

		useCase.execute("term-1:2").test {
			awaitItem()
			assertIs<UseCaseState.Error<*>>(awaitItem())
			awaitComplete()
		}
	}
}

private fun createObserveTermReportUseCase(
	insights: ControllableAssistantInsightsRepository,
	record: ControllableVisibleAcademicRecordRepository,
	prerequisite: ControllableRecordDataPrerequisiteRepository = ControllableRecordDataPrerequisiteRepository()
) = ObserveTermReportUseCase(
	insightsRepository = insights,
	visibleAcademicRecordRepository = record,
	recordDataPrerequisiteRepository = prerequisite,
	reportingRepository = RecordingReportingRepository()
)
