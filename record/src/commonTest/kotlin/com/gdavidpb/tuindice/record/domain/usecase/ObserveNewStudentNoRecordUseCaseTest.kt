package com.gdavidpb.tuindice.record.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ObserveNewStudentNoRecordUseCaseTest {
	// The account has no record stored, so nothing but the sync status can say why.
	@Test
	fun execute_whenTheUniversityHasNoRecordYet_saysSoWithoutAnyRecordStored() = runTest {
		val syncStatusRepository = FakeSyncStatusRepository(initialValue = SyncStatus.NewStudentNoRecord)
		val useCase = ObserveNewStudentNoRecordUseCase(
			syncStatusRepository = syncStatusRepository,
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			assertEquals(true, awaitLoadingThenData(this))

			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun execute_followsTheSyncStatusAndSkipsRepeats() = runTest {
		val syncStatusRepository = FakeSyncStatusRepository(initialValue = SyncStatus.Unavailable)
		val useCase = ObserveNewStudentNoRecordUseCase(
			syncStatusRepository = syncStatusRepository,
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			assertEquals(false, awaitLoadingThenData(this))

			// Another status that is not "new student" changes nothing for the screen.
			syncStatusRepository.setSyncStatus(SyncStatus.Failed)
			syncStatusRepository.setSyncStatus(SyncStatus.NewStudentNoRecord)
			assertEquals(true, assertIs<UseCaseState.Data<Boolean>>(awaitItem()).value)

			syncStatusRepository.setSyncStatus(SyncStatus.Healthy)
			assertEquals(false, assertIs<UseCaseState.Data<Boolean>>(awaitItem()).value)

			cancelAndIgnoreRemainingEvents()
		}
	}
}
