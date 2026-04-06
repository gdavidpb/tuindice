package com.gdavidpb.tuindice.about.di

import com.gdavidpb.tuindice.about.data.repository.StoreUrlDataRepository
import com.gdavidpb.tuindice.about.domain.repository.AboutRepository
import com.gdavidpb.tuindice.about.presentation.viewmodel.AboutViewModel
import com.gdavidpb.tuindice.about.testing.FakeAboutRepository
import com.gdavidpb.tuindice.about.testing.FakeStoreUrlDataSource
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.BrowserRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeAppEnvironmentRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingBrowserRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.koin.assertResolves
import com.gdavidpb.tuindice.testkit.koin.withKoinSmokeTest
import kotlin.test.Test
import org.koin.dsl.module

class AboutModuleKoinSmokeTest {
	@Test
	fun resolvesAboutViewModel() = withKoinSmokeTest(
		aboutModule,
		module {
			single<AboutRepository> { FakeAboutRepository() }
			single<AppEnvironmentRepository> { FakeAppEnvironmentRepository() }
			single<BrowserRepository> { RecordingBrowserRepository() }
			single<ConfigRepository> { FakeConfigRepository() }
			single<StoreUrlDataRepository> { FakeStoreUrlDataSource() }
			single<ReportingRepository> { RecordingReportingRepository() }
		}
	) {
		assertResolves(AboutViewModel::class)
	}
}
