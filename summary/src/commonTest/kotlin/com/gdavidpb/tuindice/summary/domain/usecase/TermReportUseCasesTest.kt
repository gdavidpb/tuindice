package com.gdavidpb.tuindice.summary.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.repository.AssistantInsightsRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.summary.domain.usecase.error.RateTermReportUseCaseError
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RateTermReportExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.param.RateTermReportParams
import com.gdavidpb.tuindice.summary.testing.ControllableAssistantInsightsRepository
import com.gdavidpb.tuindice.summary.testing.ControllableTermReportFeedbackRepository
import com.gdavidpb.tuindice.summary.testing.ControllableTermReportSeenRepository
import com.gdavidpb.tuindice.summary.testing.ManualRatingsInsightsRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class TermReportUseCasesTest {

	@Test
	fun rateSendsTheThumbsOfTheStudent_andKeepsTheConfirmedOne() = runTest {
		val insights = ControllableAssistantInsightsRepository()
		val feedback = ControllableTermReportFeedbackRepository()
		val useCase = createRateUseCase(insights = insights, feedback = feedback)

		useCase.execute(RateTermReportParams(insightId = "term-1:2", useful = false)).test {
			awaitItem()
			assertIs<UseCaseState.Data<Unit>>(awaitItem())
			awaitComplete()
		}

		assertEquals(listOf("term-1:2" to false), insights.ratings)
		assertEquals(mapOf("term-1:2" to false), feedback.feedbacksFlow.value)
	}

	@Test
	fun aRefusedRatingFailsTheUseCase_andKeepsNoVote() = runTest {
		val insights = ControllableAssistantInsightsRepository(ratingFailure = IllegalStateException("404"))
		val feedback = ControllableTermReportFeedbackRepository()
		val useCase = createRateUseCase(insights = insights, feedback = feedback)

		useCase.execute(RateTermReportParams(insightId = "term-1:2", useful = true)).test {
			awaitItem()
			assertIs<UseCaseState.Error<*>>(awaitItem())
			awaitComplete()
		}

		assertEquals(emptyMap<String, Boolean>(), feedback.feedbacksFlow.value)
	}

	@Test
	fun aRefusalOfTheServiceIsControlled_andAConnectionFailureToo() = runTest {
		val refused = createRateUseCase(
			insights = ControllableAssistantInsightsRepository(
				ratingFailure = clientRequestException(HttpStatusCode.Unauthorized, path = "/insights/v1")
			)
		)
		val offline = createRateUseCase(
			insights = ControllableAssistantInsightsRepository(ratingFailure = IllegalStateException("network is unreachable"))
		)

		refused.execute(RateTermReportParams(insightId = "term-1:2", useful = true)).test {
			awaitItem()
			assertEquals(
				RateTermReportUseCaseError.Refused,
				assertIs<UseCaseState.Error<RateTermReportUseCaseError>>(awaitItem()).error
			)
			awaitComplete()
		}

		offline.execute(RateTermReportParams(insightId = "term-1:2", useful = true)).test {
			awaitItem()
			assertEquals(
				RateTermReportUseCaseError.NoConnection,
				assertIs<UseCaseState.Error<RateTermReportUseCaseError>>(awaitItem()).error
			)
			awaitComplete()
		}
	}

	@Test
	fun markingAReportSeenKeepsItSeen() = runTest {
		val seen = ControllableTermReportSeenRepository()
		val useCase = MarkTermReportSeenUseCase(seen, RecordingReportingRepository())

		useCase.execute("term-1:2").test {
			awaitItem()
			awaitItem()
			awaitComplete()
		}

		assertEquals(setOf("term-1:2"), seen.seenIdsFlow.value)
	}

	// A refusal of a thumb whose flow still awaits it is reported by that flow, and only once: the use case adds nothing.
	@Test
	fun aRefusalOfAStillAwaitedRating_isReportedOnce() = runTest {
		val insights = ManualRatingsInsightsRepository()
		val reporting = RecordingReportingRepository()
		val useCase = createRateUseCase(insights = insights, reporting = reporting)

		useCase.execute(RateTermReportParams(insightId = "term-1:2", useful = true)).test {
			awaitItem()
			insights.requestCount.first { sent -> sent >= 1 }
			insights.requests.single().refuse()

			assertIs<UseCaseState.Error<*>>(awaitItem())
			awaitComplete()
		}

		assertEquals(1, reporting.loggedExceptions.size)
	}

	private fun createRateUseCase(
		insights: AssistantInsightsRepository,
		feedback: ControllableTermReportFeedbackRepository = ControllableTermReportFeedbackRepository(),
		reporting: RecordingReportingRepository = RecordingReportingRepository()
	) = RateTermReportUseCase(
		insightsRepository = insights,
		feedbackRepository = feedback,
		sessionCoroutineScope = testSessionCoroutineScope(),
		termReportVoteQueue = TermReportVoteQueue(),
		reportingRepository = reporting,
		exceptionHandler = RateTermReportExceptionHandler()
	)
}
