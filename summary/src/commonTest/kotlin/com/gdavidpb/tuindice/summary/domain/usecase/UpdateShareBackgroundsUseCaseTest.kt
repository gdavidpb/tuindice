package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.summary.testing.RecordingShareBackgroundRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UpdateShareBackgroundsUseCaseTest {
	@Test
	fun when_theSheetOpens_then_theCatalogIsAskedFromTheServerOnce() = runTest {
		val repository = RecordingShareBackgroundRepository()
		val useCase = UpdateShareBackgroundsUseCase(
			shareBackgroundRepository = repository,
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).toList()

		assertEquals(1, repository.refreshes)
	}
}
