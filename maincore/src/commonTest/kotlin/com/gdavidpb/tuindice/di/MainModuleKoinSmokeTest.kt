package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.BrowserRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.ReviewRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.repository.UpdateRepository
import com.gdavidpb.tuindice.presentation.viewmodel.BrowserViewModel
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeUpdateRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingApplicationRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingBrowserRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReviewRepository
import com.gdavidpb.tuindice.testkit.koin.assertResolves
import com.gdavidpb.tuindice.testkit.koin.withKoinSmokeTest
import kotlin.test.Test
import org.koin.dsl.module

class MainModuleKoinSmokeTest {
	@Test
	fun resolvesMainViewModels() = withKoinSmokeTest(
		mainModule,
		module {
			single<SessionRepository> { FakeSessionRepository() }
			single<SettingsRepository> { FakeSettingsRepository() }
			single<ConfigRepository> { FakeConfigRepository() }
			single<ApplicationRepository> { RecordingApplicationRepository() }
			single<ReportingRepository> { RecordingReportingRepository() }
			single<NetworkRepository> { FakeNetworkRepository() }
			single<ReviewRepository> { RecordingReviewRepository() }
			single<UpdateRepository> { FakeUpdateRepository() }
			single<BrowserRepository> { RecordingBrowserRepository() }
		}
	) {
		assertResolves(
			MainViewModel::class,
			BrowserViewModel::class
		)
	}
}
