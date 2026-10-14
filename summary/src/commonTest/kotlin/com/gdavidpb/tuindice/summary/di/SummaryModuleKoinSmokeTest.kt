package com.gdavidpb.tuindice.summary.di

import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.DeviceInfoRepository
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.RecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.ScheduleClockRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.persistence.domain.repository.PensumProgressRepository
import com.gdavidpb.tuindice.persistence.domain.repository.VisibleAcademicRecordRepository
import com.gdavidpb.tuindice.persistence.domain.repository.VisibleEvaluationsRepository
import com.gdavidpb.tuindice.summary.domain.repository.ImageShareRepository
import com.gdavidpb.tuindice.summary.domain.repository.ShareBackgroundRepository
import com.gdavidpb.tuindice.summary.domain.repository.UserRepository
import com.gdavidpb.tuindice.summary.presentation.viewmodel.ShareTermViewModel
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryStatisticsViewModel
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.summary.testing.ControllablePensumProgressRepository
import com.gdavidpb.tuindice.summary.testing.ControllableRecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.summary.testing.ControllableScheduleClockRepository
import com.gdavidpb.tuindice.summary.testing.ControllableVisibleAcademicRecordRepository
import com.gdavidpb.tuindice.summary.testing.ControllableVisibleEvaluationsRepository
import com.gdavidpb.tuindice.summary.testing.RecordingShareBackgroundRepository
import com.gdavidpb.tuindice.summary.testing.RecordingShareImageRepository
import com.gdavidpb.tuindice.summary.testing.RecordingUserRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeDeviceInfoRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.koin.assertResolves
import com.gdavidpb.tuindice.testkit.koin.withKoinSmokeTest
import org.koin.dsl.module
import kotlin.test.Test

class SummaryModuleKoinSmokeTest {
	@Test
	fun resolvesSummaryViewModel() = withKoinSmokeTest(
		summaryModule,
		summaryTestRepositories
	) {
		assertResolves(SummaryViewModel::class)
	}

	@Test
	fun resolvesSummaryStatisticsViewModel() = withKoinSmokeTest(
		summaryModule,
		summaryTestRepositories
	) {
		assertResolves(SummaryStatisticsViewModel::class)
	}

	@Test
	fun resolvesShareTermViewModel() = withKoinSmokeTest(
		summaryModule,
		summaryTestRepositories
	) {
		assertResolves(ShareTermViewModel::class)
	}
}

// What the summary module reads from the outside, replaced by the test doubles of the hub and of the profile.
private val summaryTestRepositories = module {
	single<UserRepository> { RecordingUserRepository() }
	single<ShareBackgroundRepository> { RecordingShareBackgroundRepository() }
	single<ImageShareRepository> { RecordingShareImageRepository() }
	single<NetworkRepository> { FakeNetworkRepository(isAvailable = true) }
	single<ReportingRepository> { RecordingReportingRepository() }
	single<SyncStatusRepository> { FakeSyncStatusRepository() }
	single<SyncRepository> { FakeSyncRepository() }
	single<DeviceInfoRepository> { FakeDeviceInfoRepository() }
	single<VisibleAcademicRecordRepository> { ControllableVisibleAcademicRecordRepository() }
	single<VisibleEvaluationsRepository> { ControllableVisibleEvaluationsRepository() }
	single<PensumProgressRepository> { ControllablePensumProgressRepository() }
	single<RecordDataPrerequisiteRepository> { ControllableRecordDataPrerequisiteRepository() }
	single<ScheduleClockRepository> { ControllableScheduleClockRepository() }
	single<EventPublisher> { NoOpEventPublisher }
	single<TuIndiceDispatchers> { DefaultTuIndiceDispatchers }
}
