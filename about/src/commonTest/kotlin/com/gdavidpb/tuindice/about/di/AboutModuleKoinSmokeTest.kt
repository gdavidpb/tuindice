package com.gdavidpb.tuindice.about.di

import com.gdavidpb.tuindice.about.domain.repository.AboutRepository
import com.gdavidpb.tuindice.about.domain.repository.StoreUrlRepository
import com.gdavidpb.tuindice.about.presentation.viewmodel.AboutViewModel
import com.gdavidpb.tuindice.about.presentation.viewmodel.SettingsViewModel
import com.gdavidpb.tuindice.about.testing.FakeAboutRepository
import com.gdavidpb.tuindice.about.testing.FakeAssistantAvailabilityRepository
import com.gdavidpb.tuindice.about.testing.FakeAssistantConsentRepository
import com.gdavidpb.tuindice.about.testing.FakeAssistantConversationsRepository
import com.gdavidpb.tuindice.about.testing.FakeProfileIdentityRepository
import com.gdavidpb.tuindice.about.testing.FakeStoreUrlDataSource
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.data.source.usage.InMemoryUsageDataConsentRepository
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.AssistantAvailabilityRepository
import com.gdavidpb.tuindice.base.domain.repository.AssistantConsentRepository
import com.gdavidpb.tuindice.base.domain.repository.AssistantConversationsRepository
import com.gdavidpb.tuindice.base.domain.repository.BrowserRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.DeviceInfoRepository
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.domain.repository.ProfileIdentityRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.UsageDataConsentRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeAppEnvironmentRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeDeviceInfoRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingBrowserRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.koin.assertResolves
import com.gdavidpb.tuindice.testkit.koin.withKoinSmokeTest
import org.koin.dsl.module
import kotlin.test.Test

class AboutModuleKoinSmokeTest {
	@Test
	fun resolvesAboutAndSettingsViewModels() = withKoinSmokeTest(
		aboutModule,
		module {
			single<AboutRepository> { FakeAboutRepository() }
			single<AppEnvironmentRepository> { FakeAppEnvironmentRepository() }
			single<BrowserRepository> { RecordingBrowserRepository() }
			single<ConfigRepository> { FakeConfigRepository() }
			single<DeviceInfoRepository> { FakeDeviceInfoRepository() }
			single<StoreUrlRepository> { FakeStoreUrlDataSource() }
			single<ReportingRepository> { RecordingReportingRepository() }
			single<UsageDataConsentRepository> { InMemoryUsageDataConsentRepository() }
			single<ProfileIdentityRepository> { FakeProfileIdentityRepository() }
			single<AssistantAvailabilityRepository> { FakeAssistantAvailabilityRepository() }
			single<AssistantConsentRepository> { FakeAssistantConsentRepository() }
			single<AssistantConversationsRepository> { FakeAssistantConversationsRepository() }
			single<EventPublisher> { NoOpEventPublisher }
			single<TuIndiceDispatchers> { DefaultTuIndiceDispatchers }
		}
	) {
		assertResolves(SettingsViewModel::class)
		assertResolves(AboutViewModel::class)
	}
}
