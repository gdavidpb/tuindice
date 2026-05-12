package com.gdavidpb.tuindice.pensum.presentation.action

import com.gdavidpb.tuindice.pensum.domain.model.PensumObservation
import com.gdavidpb.tuindice.pensum.domain.repository.PensumRepository
import com.gdavidpb.tuindice.pensum.domain.usecase.UpdatePensumUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.exceptionhandler.UpdatePensumExceptionHandler
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import com.gdavidpb.tuindice.testkit.mvi.reduceMutations
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest

class RefreshPensumActionProcessorTest {
	@Test
	fun when_refreshReturnsNotFound_then_stateBecomesEmptyWithoutSnackbar() = runTest {
		val processor = RefreshPensumActionProcessor(
			updatePensumUseCase = UpdatePensumUseCase(
				pensumRepository = ThrowingPensumRepository(
					throwable = clientRequestException(
						statusCode = HttpStatusCode.NotFound,
						path = "/pensums/v1"
					)
				),
				reportingRepository = RecordingReportingRepository(),
				exceptionHandler = UpdatePensumExceptionHandler(
					networkRepository = FakeNetworkRepository(isAvailable = true)
				)
			)
		)
		val effects = mutableListOf<Pensum.Effect>()

		val finalState = processor.process(
			action = Pensum.Action.RefreshPensum,
			sideEffect = effects::add
		).toList().reduceMutations(Pensum.State.Loading)

		assertEquals(Pensum.State.Empty, finalState)
		assertEquals(emptyList(), effects)
	}
}

private class ThrowingPensumRepository(
	private val throwable: Throwable
) : PensumRepository {
	override fun observePensumFlow(): Flow<PensumObservation> = emptyFlow()

	override suspend fun refreshPensum() {
		throw throwable
	}

	override suspend fun selectPensum(careerCode: Int, year: Int) = Unit

	override suspend fun selectModality(modalityId: String) = Unit

	override suspend fun selectSelection(careerCode: Int, year: Int, modalityId: String) = Unit
}
