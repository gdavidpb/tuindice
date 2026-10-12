package com.gdavidpb.tuindice.pensum.data.source

import com.gdavidpb.tuindice.pensum.domain.model.PensumObservation
import com.gdavidpb.tuindice.pensum.testing.RecordingPensumRepository
import com.gdavidpb.tuindice.pensum.testing.sampleObservedPensum
import com.gdavidpb.tuindice.pensum.testing.samplePensumGraph
import com.gdavidpb.tuindice.persistence.domain.model.PensumCreditsProgress
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PensumProgressDataSourceTest {
	@Test
	fun observePensumProgressFlow_withACachedPensum_reportsApprovedAndTotalCredits() = runTest {
		val observed = sampleObservedPensum().copy(
			pensum = samplePensumGraph().copy(totalCredits = 192),
			approvedCredits = 87
		)
		val repository = RecordingPensumRepository(observations = listOf(PensumObservation.Content(observed)))

		val progress = PensumProgressDataSource(repository).observePensumProgressFlow().toList()

		assertEquals(listOf(PensumCreditsProgress(approvedCredits = 87, totalCredits = 192)), progress)
		// A map over the flow the screen already observes: it fetches nothing of its own.
		assertEquals(0, repository.refreshCalls)
	}

	@Test
	fun observePensumProgressFlow_withoutACachedPensum_emitsNull() = runTest {
		val repository = RecordingPensumRepository(observations = listOf(PensumObservation.Missing))

		val progress = PensumProgressDataSource(repository).observePensumProgressFlow().toList()

		assertEquals(listOf<PensumCreditsProgress?>(null), progress)
		assertEquals(0, repository.refreshCalls)
	}

	@Test
	fun observePensumProgressFlow_whileTheRecordIsNotReadyOrAvailable_emitsNullOnce() = runTest {
		val repository = RecordingPensumRepository(
			observations = listOf(
				PensumObservation.WaitingForRecordData,
				PensumObservation.RecordDataUnavailable
			)
		)

		val progress = PensumProgressDataSource(repository).observePensumProgressFlow().toList()

		// Two causes, one value: the contract only emits when the progress changes.
		assertEquals(listOf<PensumCreditsProgress?>(null), progress)
	}

	@Test
	fun observePensumProgressFlow_followsTheObservation_fromMissingToContent() = runTest {
		val content = PensumObservation.Content(sampleObservedPensum().copy(approvedCredits = 12))
		val repository = RecordingPensumRepository(observations = listOf(PensumObservation.Missing, content))

		val progress = PensumProgressDataSource(repository).observePensumProgressFlow().toList()

		assertEquals(
			listOf(null, PensumCreditsProgress(approvedCredits = 12, totalCredits = samplePensumGraph().totalCredits)),
			progress
		)
	}

	@Test
	fun observePensumProgressFlow_doesNotRepeatAProgressThatDidNotChange() = runTest {
		val content = PensumObservation.Content(sampleObservedPensum().copy(approvedCredits = 12))
		val sameCreditsRebuilt = PensumObservation.Content(sampleObservedPensum().copy(approvedCredits = 12))
		val moreCredits = PensumObservation.Content(sampleObservedPensum().copy(approvedCredits = 15))
		val repository = RecordingPensumRepository(
			observations = listOf(content, sameCreditsRebuilt, moreCredits, moreCredits)
		)

		val progress = PensumProgressDataSource(repository).observePensumProgressFlow().toList()

		assertEquals(listOf(12, 15), progress.map { credits -> credits?.approvedCredits })
	}
}
