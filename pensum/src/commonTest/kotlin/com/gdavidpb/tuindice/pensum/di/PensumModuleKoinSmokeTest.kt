package com.gdavidpb.tuindice.pensum.di

import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.RecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.model.RecordDataPrerequisiteState
import com.gdavidpb.tuindice.pensum.domain.model.PensumObservation
import com.gdavidpb.tuindice.pensum.domain.repository.PensumRepository
import com.gdavidpb.tuindice.pensum.presentation.viewmodel.PensumViewModel
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.koin.assertResolves
import com.gdavidpb.tuindice.testkit.koin.withKoinSmokeTest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import org.koin.dsl.module

class PensumModuleKoinSmokeTest {
	@Test
	fun resolvesPensumViewModel() = withKoinSmokeTest(
		pensumModule,
		module {
			single<PensumRepository> { FakePensumRepository() }
			single<ReportingRepository> { RecordingReportingRepository() }
			single<NetworkRepository> { FakeNetworkRepository() }
			single<RecordDataPrerequisiteRepository> { FakeRecordDataPrerequisiteRepository() }
		}
	) {
		assertResolves(PensumViewModel::class)
	}
}

private class FakePensumRepository : PensumRepository {
	override fun observePensumFlow(): Flow<PensumObservation> = emptyFlow()
	override suspend fun refreshPensum() = Unit
	override suspend fun selectPensum(year: Int) = Unit
	override suspend fun selectModality(modalityId: String) = Unit
	override suspend fun selectSelection(year: Int, modalityId: String) = Unit
}

private class FakeRecordDataPrerequisiteRepository : RecordDataPrerequisiteRepository {
	override fun observeRecordDataPrerequisiteFlow(): Flow<RecordDataPrerequisiteState> {
		return flowOf(RecordDataPrerequisiteState(isReady = true, hasFailed = false))
	}

	override suspend fun isRecordDataReady(): Boolean = true
}

private class FakeNetworkRepository : NetworkRepository {
	override fun isAvailable(): Boolean = true
}
