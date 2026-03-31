package com.gdavidpb.tuindice.record.di

import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import com.gdavidpb.tuindice.record.domain.repository.QuarterSelectionRepository
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
import com.gdavidpb.tuindice.record.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.record.testing.RecordingQuarterRepository
import com.gdavidpb.tuindice.record.testing.RecordingQuarterSelectionRepository
import com.gdavidpb.tuindice.record.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.koin.assertResolves
import com.gdavidpb.tuindice.testkit.koin.withKoinSmokeTest
import kotlin.test.Test
import org.koin.dsl.module

class RecordModuleKoinSmokeTest {
	@Test
	fun resolvesRecordViewModel() = withKoinSmokeTest(
		recordModule,
		module {
			single<QuarterRepository> { RecordingQuarterRepository() }
			single<QuarterSelectionRepository> { RecordingQuarterSelectionRepository() }
			single<NetworkRepository> { FakeNetworkRepository(isAvailable = true) }
			single<ReportingRepository> { RecordingReportingRepository() }
		}
	) {
		assertResolves(RecordViewModel::class)
	}
}
