package com.gdavidpb.tuindice.about.presentation.viewmodel

import com.gdavidpb.tuindice.about.data.repository.StoreUrlDataSource
import com.gdavidpb.tuindice.about.domain.repository.AboutRepository
import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenExternalUrlUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenStoreUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SendSupportEmailUseCase
import com.gdavidpb.tuindice.about.presentation.action.*
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.base.domain.model.AppEnvironment
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.BrowserRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

class AboutViewModelTest {
	@Test
	fun initialLoadVersionAction_setsContentState() = runBlocking {
		val viewModel = createViewModel()
		val stateJob = launch { viewModel.state.collect() }

		try {
			waitUntil { viewModel.state.value is About.State.Content }

			val content = assertIs<About.State.Content>(viewModel.state.value)
			assertEquals("TuIndice v3.5.1", content.versionText)
		} finally {
			stateJob.cancel()
		}
	}

	@Test
	fun actionDispatch_emitsExpectedEffects() = runBlocking {
		val browserRepository = AboutViewModelFakeBrowserGateway()
		val viewModel = createViewModel(
			browserRepository = browserRepository
		)
		val effects = mutableListOf<About.Effect>()
		val effectJob = launch(start = CoroutineStart.UNDISPATCHED) {
			viewModel.effect.collect { effects += it }
		}
		val stateJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.state.collect() }

		try {
			viewModel.openTermsAndConditionsAction()
			waitUntil { effects.size == 1 }

			viewModel.shareAppAction()
			viewModel.reportBugAction()
			viewModel.contactDeveloperAction()
			viewModel.rateOnPlayStoreAction()
			viewModel.openUrlAction("https://tuindice.app/github")
			waitUntil { effects.size == 5 }
			waitUntil { browserRepository.lastOpenedUrl == "https://tuindice.app/github" }

			val terms = assertIs<About.Effect.NavigateToBrowser>(
				effects.single { effect -> effect is About.Effect.NavigateToBrowser }
			)
			assertEquals("TuIndice - Términos y condiciones", terms.title)
			assertEquals("https://tuindice.app/terms", terms.url)

			val share = assertIs<About.Effect.ShareText>(
				effects.single { effect -> effect is About.Effect.ShareText }
			)
			assertEquals("TuIndice", share.subject)
			assertEquals(
				"TuIndice: Una nueva forma de administrar tus notas",
				share.text
			)

			val uriEffects = effects.filterIsInstance<About.Effect.OpenUri>()
			assertEquals(3, uriEffects.size)
			assertEquals(
				2,
				uriEffects.count { effect ->
					effect.uri == "mailto:support@tuindice.app?subject=Support%20TuIndice&body="
				}
			)
			assertEquals(
				1,
				uriEffects.count { effect ->
					effect.uri == "market://details?id=com.gdavidpb.tuindice"
				}
			)
			assertEquals("https://tuindice.app/github", browserRepository.lastOpenedUrl)
		} finally {
			effectJob.cancel()
			stateJob.cancel()
		}
	}

	private fun createViewModel(
		browserRepository: BrowserRepository = AboutViewModelFakeBrowserGateway(),
		configRepository: ConfigRepository = AboutViewModelFakeConfigGateway(),
		storeUrlDataSource: StoreUrlDataSource = AboutViewModelFakeStoreUrlDataSource()
	): AboutViewModel {
		return AboutViewModel(
			loadVersionActionProcessor = LoadVersionActionProcessor(
				loadVersionUseCase = LoadVersionUseCase(
					aboutRepository = AboutViewModelFakeAboutRepository()
				)
			),
			contactDeveloperActionProcessor = ContactDeveloperActionProcessor(
				sendSupportEmailUseCase = SendSupportEmailUseCase(
					configRepository = configRepository
				)
			),
			openTermsAndConditionsActionProcessor = OpenTermsAndConditionsActionProcessor(
				appEnvironmentRepository = AboutViewModelFakeAppEnvironmentGateway()
			),
			openPrivacyPolicyActionProcessor = OpenPrivacyPolicyActionProcessor(
				appEnvironmentRepository = AboutViewModelFakeAppEnvironmentGateway()
			),
			shareAppActionProcessor = ShareAppActionProcessor(),
			rateOnStoreActionProcessor = RateOnStoreActionProcessor(
				openStoreUseCase = OpenStoreUseCase(
					storeUrlDataSource = storeUrlDataSource
				)
			),
			reportBugActionProcessor = ReportBugActionProcessor(
				sendSupportEmailUseCase = SendSupportEmailUseCase(
					configRepository = configRepository
				)
			),
			openUrlActionProcessor = OpenUrlActionProcessor(
				openExternalUrlUseCase = OpenExternalUrlUseCase(
					browserRepository = browserRepository
				)
			)
		)
	}

	private suspend fun waitUntil(
		timeoutMs: Long = 2_000L,
		condition: () -> Boolean
	) {
		val mark = TimeSource.Monotonic.markNow()

		while (!condition() && mark.elapsedNow() < timeoutMs.milliseconds) {
			delay(20)
		}

		assertTrue(condition(), "Condition not reached within timeout.")
	}
}

private class AboutViewModelFakeAboutRepository : AboutRepository {
	override suspend fun getVersionDescription(): String = "TuIndice v3.5.1"
}

private class AboutViewModelFakeAppEnvironmentGateway : AppEnvironmentRepository {
	override fun getEnvironment(): AppEnvironment {
		return AppEnvironment(
			apiBaseUrl = "https://api.tuindice.app/",
			privacyPolicyUrl = "https://tuindice.app/privacy",
			termsAndConditionsUrl = "https://tuindice.app/terms",
			debug = false
		)
	}
}

private class AboutViewModelFakeBrowserGateway : BrowserRepository {
	var lastOpenedUrl: String? = null

	override fun open(url: String) {
		lastOpenedUrl = url
	}
}

private class AboutViewModelFakeConfigGateway : ConfigRepository {
	override suspend fun tryFetch() = Unit

	override fun getTimeout(): Long = 5_000L

	override fun getContactEmail(): String = "support@tuindice.app"

	override fun getContactSubject(): String = "Support TuIndice"

	override fun getLoadingMessages(): List<String> = listOf("Loading")

	override fun getTimeUpdateStalenessDays(): Int = 7

	override fun getSyncsToSuggestReview(): Int = 3
}

private class AboutViewModelFakeStoreUrlDataSource : StoreUrlDataSource {
	override fun getStoreUrl(): String {
		return "market://details?id=com.gdavidpb.tuindice"
	}
}
