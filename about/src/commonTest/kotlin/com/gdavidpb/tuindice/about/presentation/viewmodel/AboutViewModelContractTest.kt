package com.gdavidpb.tuindice.about.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenExternalUrlUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenStoreUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SendSupportEmailUseCase
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.presentation.machine.AboutMachine
import com.gdavidpb.tuindice.base.data.source.usage.InMemoryUsageDataConsentRepository
import com.gdavidpb.tuindice.about.testing.CURRENT_PRODUCTION_VERSION_TEXT
import com.gdavidpb.tuindice.about.testing.FakeAboutRepository
import com.gdavidpb.tuindice.about.testing.FakeStoreUrlDataSource
import com.gdavidpb.tuindice.testkit.base.repository.FakeAppEnvironmentRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingBrowserRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AboutViewModelContractTest {
	@Test
	fun initialAction_loadsVersionIntoContentState() = runTest {
		createViewModel().state.test {
			val initial = awaitItem()
			assertEquals(About.State.Idle, initial)

			val content = assertIs<About.State.Content>(awaitItem())
			assertEquals(CURRENT_PRODUCTION_VERSION_TEXT, content.versionText)

			cancelAndIgnoreRemainingEvents()
		}
	}

	private fun createViewModel(
		browserRepository: RecordingBrowserRepository = RecordingBrowserRepository()
	): AboutViewModel {
		return AboutViewModel(
			screenMachine = AboutMachine(
				loadVersionUseCase = LoadVersionUseCase(
					aboutRepository = FakeAboutRepository(),
					reportingRepository = RecordingReportingRepository()
				),
				sendSupportEmailUseCase = SendSupportEmailUseCase(
					configRepository = FakeConfigRepository(),
					reportingRepository = RecordingReportingRepository()
				),
				openStoreUseCase = OpenStoreUseCase(
					storeUrlRepository = FakeStoreUrlDataSource(),
					reportingRepository = RecordingReportingRepository()
				),
				openExternalUrlUseCase = OpenExternalUrlUseCase(
					browserRepository = browserRepository,
					reportingRepository = RecordingReportingRepository()
				),
				appEnvironmentRepository = FakeAppEnvironmentRepository(),
				usageDataConsentRepository = InMemoryUsageDataConsentRepository()
			),
			eventPublisher = NoOpEventPublisher
		)
	}
}
