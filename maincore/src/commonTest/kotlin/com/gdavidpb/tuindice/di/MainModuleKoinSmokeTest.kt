package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.BrowserRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.DeviceInfoRepository
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.ReviewRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncRepository
import com.gdavidpb.tuindice.base.domain.repository.UpdateRepository
import com.gdavidpb.tuindice.domain.repository.CoreCacheStateRepository
import com.gdavidpb.tuindice.presentation.viewmodel.BrowserViewModel
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.testing.FakeCoreCacheStateRepository
import com.gdavidpb.tuindice.testing.FakeDeviceInfoRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeUpdateRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingApplicationRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingBrowserRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReviewRepository
import com.gdavidpb.tuindice.testkit.koin.assertResolves
import com.gdavidpb.tuindice.testkit.koin.withKoinSmokeTest
import com.gdavidpb.tuindice.wizard.di.wizardModule
import kotlin.test.Test
import org.koin.dsl.module

class MainModuleKoinSmokeTest {
	@Test
	fun resolvesMainViewModels() = withKoinSmokeTest(
		mainModule,
		wizardModule,
		module {
			single<SessionRepository> { FakeSessionRepository() }
			single<SettingsRepository> { FakeSettingsRepository() }
			single<ConfigRepository> { FakeConfigRepository() }
			single<CredentialsRepository> { FakeCredentialsRepository() }
			single<DeviceInfoRepository> { FakeDeviceInfoRepository() }
			single<ApplicationRepository> { RecordingApplicationRepository() }
			single<ReportingRepository> { RecordingReportingRepository() }
			single<NetworkRepository> { FakeNetworkRepository() }
			single<ReviewRepository> { RecordingReviewRepository() }
			single<SyncRepository> { FakeSyncRepository() }
			single<CoreCacheStateRepository> { FakeCoreCacheStateRepository() }
			single<UpdateRepository> { FakeUpdateRepository() }
			single<BrowserRepository> { RecordingBrowserRepository() }
			single<EventPublisher> { NoOpEventPublisher }
			single<TuIndiceDispatchers> { DefaultTuIndiceDispatchers }
		}
	) {
		assertResolves(
			MainViewModel::class,
			BrowserViewModel::class
		)
	}
}
