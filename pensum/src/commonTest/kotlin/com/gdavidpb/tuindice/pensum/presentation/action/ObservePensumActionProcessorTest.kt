package com.gdavidpb.tuindice.pensum.presentation.action

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.pensum.domain.model.PensumObservation
import com.gdavidpb.tuindice.pensum.domain.repository.PensumRepository
import com.gdavidpb.tuindice.pensum.domain.usecase.ObservePensumUseCase
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.mvi.reduceMutations
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_failed_record_data_unavailable

class ObservePensumActionProcessorTest {
	@Test
	fun when_recordDataIsNotReady_then_stateStaysLoading() = runTest {
		val processor = createProcessor(PensumObservation.WaitingForRecordData)

		val finalState = processor.process(
			action = Pensum.Action.ObservePensum,
			sideEffect = {}
		).toList().reduceMutations(Pensum.State.Loading)

		assertEquals(Pensum.State.Loading, finalState)
	}

	@Test
	fun when_recordDataFailed_then_stateBecomesFailed() = runTest {
		val processor = createProcessor(PensumObservation.RecordDataUnavailable)

		val finalState = processor.process(
			action = Pensum.Action.ObservePensum,
			sideEffect = {}
		).toList().reduceMutations(Pensum.State.Loading)

		assertEquals(
			Pensum.State.Failed(
				message = UiText.Resource(Res.string.pensum_failed_record_data_unavailable)
			),
			finalState
		)
	}

	private fun createProcessor(observation: PensumObservation): ObservePensumActionProcessor {
		return ObservePensumActionProcessor(
			observePensumUseCase = ObservePensumUseCase(
				pensumRepository = FakePensumRepository(observation),
				reportingRepository = RecordingReportingRepository()
			)
		)
	}
}

private class FakePensumRepository(
	private val observation: PensumObservation
) : PensumRepository {
	override fun observePensumFlow(): Flow<PensumObservation> = flowOf(observation)

	override suspend fun refreshPensum() = Unit

	override suspend fun selectPensum(year: Int) = Unit

	override suspend fun selectModality(modalityId: String) = Unit

	override suspend fun selectSelection(year: Int, modalityId: String) = Unit
}
