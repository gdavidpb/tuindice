package com.gdavidpb.tuindice.about.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenExternalUrlUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SendSupportEmailUseCase
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.presentation.machine.AboutExternalUseCases
import com.gdavidpb.tuindice.about.presentation.machine.AboutMachine
import com.gdavidpb.tuindice.about.testing.CURRENT_PRODUCTION_VERSION_TEXT
import com.gdavidpb.tuindice.about.testing.FakeAboutRepository
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeDeviceInfoRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingBrowserRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.TestTuIndiceDispatchers
import com.gdavidpb.tuindice.testkit.mvi.awaitUntilState
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AboutViewModelContractTest {
	@Test
	fun initialAction_loadsVersionIntoContentState() = runTest {
		val viewModel = createViewModel(testScheduler = testScheduler)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				val content = awaitUntilState<About.State.Content>()
				assertEquals(CURRENT_PRODUCTION_VERSION_TEXT, content.versionText)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun contactDeveloper_resolvesTheMailUriIntoAnEffect() = runTest {
		val viewModel = createViewModel(testScheduler = testScheduler)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.effect.test {
				viewModel.dispatchAction(About.Action.ContactDeveloper)

				val effect = awaitItem()
				assertTrue(effect is About.Effect.OpenUri && effect.uri.startsWith("mailto:"))

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun openUrl_handsTheUrlToTheBrowserRepositoryAndKeepsTheState() = runTest {
		val browserRepository = RecordingBrowserRepository()
		val viewModel = createViewModel(testScheduler = testScheduler, browserRepository = browserRepository)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.dispatchAction(About.Action.OpenUrl(url = "https://tuindice.app"))

			assertEquals("https://tuindice.app", browserRepository.lastOpenedUrl)
			assertTrue(viewModel.state.value is About.State.Content)
		} finally {
			stateCollector.cancel()
		}
	}

	private fun createViewModel(
		testScheduler: TestCoroutineScheduler,
		browserRepository: RecordingBrowserRepository = RecordingBrowserRepository()
	): AboutViewModel {
		return AboutViewModel(
			screenMachine = AboutMachine(
				loadVersionUseCase = LoadVersionUseCase(
					aboutRepository = FakeAboutRepository(),
					reportingRepository = RecordingReportingRepository()
				),
				externalUseCases = AboutExternalUseCases(
					sendSupportEmail = SendSupportEmailUseCase(
						configRepository = FakeConfigRepository(),
						deviceInfoRepository = FakeDeviceInfoRepository(),
						reportingRepository = RecordingReportingRepository()
					),
					openExternalUrl = OpenExternalUrlUseCase(
						browserRepository = browserRepository,
						reportingRepository = RecordingReportingRepository()
					)
				)
			),
			eventPublisher = NoOpEventPublisher,
			dispatchers = TestTuIndiceDispatchers(UnconfinedTestDispatcher(testScheduler))
		)
	}
}
