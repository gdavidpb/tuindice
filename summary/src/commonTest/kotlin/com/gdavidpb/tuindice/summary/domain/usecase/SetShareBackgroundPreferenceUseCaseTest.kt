package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.summary.testing.RecordingShareBackgroundRepository
import com.gdavidpb.tuindice.summary.testing.otherShareBackground
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SetShareBackgroundPreferenceUseCaseTest {
	@Test
	fun when_aBackgroundIsChosen_then_itIsKeptAsThePreference() = runTest {
		val repository = RecordingShareBackgroundRepository()
		val useCase = SetShareBackgroundPreferenceUseCase(
			shareBackgroundRepository = repository,
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(otherShareBackground.id).toList()

		assertEquals(listOf(otherShareBackground.id), repository.saved)
	}
}
