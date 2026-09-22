package com.gdavidpb.tuindice.pensum.di

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicPensumStatusEngine
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.model.RecordDataPrerequisiteState
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.RecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.pensum.data.repository.PensumLocalDataRepository
import com.gdavidpb.tuindice.pensum.data.repository.PensumRemoteDataRepository
import com.gdavidpb.tuindice.pensum.data.source.FakePensumLocalDataRepository
import com.gdavidpb.tuindice.pensum.data.source.FakePensumRemoteDataRepository
import com.gdavidpb.tuindice.pensum.domain.model.PensumObservation
import com.gdavidpb.tuindice.pensum.domain.model.PensumSelectionParams
import com.gdavidpb.tuindice.pensum.domain.repository.PensumRepository
import com.gdavidpb.tuindice.pensum.domain.repository.PensumRevalidationRepository
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
import kotlin.test.assertSame

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

	// One instance behind both contracts: a revalidation and a manual selection must share its
	// remoteWriteMutex, or a late revalidation could write back the pensum the student just left.
	@Test
	fun bindsRevalidationToTheSameDataSourceAsSelection() = withKoinSmokeTest(
		pensumModule,
		module {
			single<PensumLocalDataRepository> { FakePensumLocalDataRepository(selection = PensumSelectionParams()) }
			single<PensumRemoteDataRepository> { FakePensumRemoteDataRepository() }
			single<RecordDataPrerequisiteRepository> { FakeRecordDataPrerequisiteRepository() }
			// subjectsModule provides it in the app.
			single { AcademicPensumStatusEngine() }
		}
	) {
		assertSame<Any>(get<PensumRepository>(), get<PensumRevalidationRepository>())
	}
}

private class FakePensumRepository : PensumRepository {
	override fun observePensumFlow(): Flow<PensumObservation> = emptyFlow()
	override suspend fun hasSelectedPensumResponse(): Boolean = false
	override suspend fun refreshPensum(forceRemote: Boolean) = Unit
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
