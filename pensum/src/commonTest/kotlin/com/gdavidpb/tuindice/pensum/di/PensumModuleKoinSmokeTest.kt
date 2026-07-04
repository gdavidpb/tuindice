package com.gdavidpb.tuindice.pensum.di

import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.model.RecordDataPrerequisiteState
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.RecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.pensum.domain.model.PensumObservation
import com.gdavidpb.tuindice.pensum.domain.repository.PensumRepository
import com.gdavidpb.tuindice.pensum.domain.repository.PensumSelectionRepository
import com.gdavidpb.tuindice.pensum.presentation.viewmodel.PensumViewModel
import com.gdavidpb.tuindice.pensum.testing.FakeSettings
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.koin.assertResolves
import com.gdavidpb.tuindice.testkit.koin.withKoinSmokeTest
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.koin.dsl.module
import kotlin.test.Test

class PensumModuleKoinSmokeTest {
	@Test
	fun resolvesPensumViewModel() = withKoinSmokeTest(
		pensumModule,
		module {
			single<PensumRepository> { FakePensumRepository() }
			single<ReportingRepository> { RecordingReportingRepository() }
			single<NetworkRepository> { FakeNetworkRepository() }
			single<RecordDataPrerequisiteRepository> { FakeRecordDataPrerequisiteRepository() }
			single<EventPublisher> { NoOpEventPublisher }
			single<TuIndiceDispatchers> { DefaultTuIndiceDispatchers }
			single<Settings> { FakeSettings() }
		}
	) {
		assertResolves(
			PensumViewModel::class,
			PensumSelectionRepository::class
		)
	}
}

private class FakePensumRepository : PensumRepository {
	override fun observePensumFlow(): Flow<PensumObservation> = emptyFlow()
	override suspend fun hasSelectedPensumResponse(): Boolean = false
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
