package com.gdavidpb.tuindice.summary.di

import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.summary.domain.repository.UserRepository
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.summary.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.summary.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.summary.testing.RecordingUserRepository
import com.gdavidpb.tuindice.testkit.koin.assertResolves
import com.gdavidpb.tuindice.testkit.koin.withKoinSmokeTest
import kotlin.test.Test
import org.koin.dsl.module

class SummaryModuleKoinSmokeTest {
	@Test
	fun resolvesSummaryViewModel() = withKoinSmokeTest(
		summaryModule,
		module {
			single<UserRepository> { RecordingUserRepository() }
			single<NetworkRepository> { FakeNetworkRepository(isAvailable = true) }
			single<ReportingRepository> { RecordingReportingRepository() }
		}
	) {
		assertResolves(SummaryViewModel::class)
	}
}
