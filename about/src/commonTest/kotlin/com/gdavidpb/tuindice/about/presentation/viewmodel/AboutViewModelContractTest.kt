package com.gdavidpb.tuindice.about.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenExternalUrlUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenStoreUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SendSupportEmailUseCase
import com.gdavidpb.tuindice.about.presentation.action.ContactDeveloperActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.LoadVersionActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.OpenPrivacyPolicyActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.OpenSupportActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.OpenTermsAndConditionsActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.OpenUrlActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.RateOnStoreActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.ReportBugActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.ShareAppActionProcessor
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.testing.CURRENT_PRODUCTION_VERSION_TEXT
import com.gdavidpb.tuindice.about.testing.FakeAboutRepository
import com.gdavidpb.tuindice.about.testing.FakeStoreUrlDataSource
import com.gdavidpb.tuindice.testkit.base.repository.FakeAppEnvironmentRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingBrowserRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
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

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun userActions_emitExpectedEffects() = runTest {
		val viewModel = createViewModel()
		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(About.State.Idle, awaitItem())
				assertIs<About.State.Content>(awaitItem())
				cancelAndIgnoreRemainingEvents()
			}

			viewModel.effect.test {
				viewModel.openTermsAndConditionsAction()
				val termsEffect = assertIs<About.Effect.NavigateToBrowser>(awaitItem())
				assertEquals("https://tuindice.app/terms_and_conditions_v6_0.html", termsEffect.url)
				cancelAndIgnoreRemainingEvents()
			}

			viewModel.effect.test {
				viewModel.openSupportAction()
				val supportEffect = assertIs<About.Effect.NavigateToBrowser>(awaitItem())
				assertEquals("https://tuindice.app/support_v6_0.html", supportEffect.url)
				cancelAndIgnoreRemainingEvents()
			}

			viewModel.effect.test {
				viewModel.shareAppAction()
				val shareEffect = assertIs<About.Effect.ShareText>(awaitItem())
				assertEquals("TuIndice", shareEffect.subject)
				assertEquals(
					"""
					Descarga TuIndice y administra tus notas de forma simple.
					Google Play: https://play.google.com/store/apps/details?id=com.gdavidpb.tuindice
					App Store: https://apps.apple.com/app/id6760307454
					""".trimIndent(),
					shareEffect.text
				)
				cancelAndIgnoreRemainingEvents()
			}

			viewModel.effect.test {
				viewModel.contactDeveloperAction()
				val contactEffect = assertIs<About.Effect.OpenUri>(awaitItem())
				assertEquals(
					"mailto:support@tuindice.app?subject=Support%20TuIndice&body=",
					contactEffect.uri
				)
				cancelAndIgnoreRemainingEvents()
			}

			viewModel.effect.test {
				viewModel.rateOnPlayStoreAction()
				val rateEffect = assertIs<About.Effect.OpenUri>(awaitItem())
				assertEquals("market://details?id=com.gdavidpb.tuindice", rateEffect.uri)
				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	private fun createViewModel(
		browserRepository: RecordingBrowserRepository = RecordingBrowserRepository()
	): AboutViewModel {
		return AboutViewModel(
			loadVersionActionProcessor = LoadVersionActionProcessor(
				loadVersionUseCase = LoadVersionUseCase(
					aboutRepository = FakeAboutRepository(),
					reportingRepository = RecordingReportingRepository()
				)
			),
			contactDeveloperActionProcessor = ContactDeveloperActionProcessor(
				sendSupportEmailUseCase = SendSupportEmailUseCase(
					configRepository = FakeConfigRepository(),
					reportingRepository = RecordingReportingRepository()
				)
			),
			openTermsAndConditionsActionProcessor = OpenTermsAndConditionsActionProcessor(
				appEnvironmentRepository = FakeAppEnvironmentRepository()
			),
			openPrivacyPolicyActionProcessor = OpenPrivacyPolicyActionProcessor(
				appEnvironmentRepository = FakeAppEnvironmentRepository()
			),
			openSupportActionProcessor = OpenSupportActionProcessor(
				appEnvironmentRepository = FakeAppEnvironmentRepository()
			),
			shareAppActionProcessor = ShareAppActionProcessor(),
			rateOnStoreActionProcessor = RateOnStoreActionProcessor(
				openStoreUseCase = OpenStoreUseCase(
					storeUrlRepository = FakeStoreUrlDataSource(),
					reportingRepository = RecordingReportingRepository()
				)
			),
			reportBugActionProcessor = ReportBugActionProcessor(
				sendSupportEmailUseCase = SendSupportEmailUseCase(
					configRepository = FakeConfigRepository(),
					reportingRepository = RecordingReportingRepository()
				)
			),
			openUrlActionProcessor = OpenUrlActionProcessor(
				openExternalUrlUseCase = OpenExternalUrlUseCase(
					browserRepository = browserRepository,
					reportingRepository = RecordingReportingRepository()
				)
			),
			eventPublisher = NoOpEventPublisher
		)
	}
}
