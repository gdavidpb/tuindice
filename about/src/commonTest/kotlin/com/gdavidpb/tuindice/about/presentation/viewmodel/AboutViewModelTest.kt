package com.gdavidpb.tuindice.about.presentation.viewmodel

import com.gdavidpb.tuindice.about.domain.repository.AboutRepository
import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenExternalUrlUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenStorePageUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SendSupportEmailUseCase
import com.gdavidpb.tuindice.about.domain.usecase.ShareTextUseCase
import com.gdavidpb.tuindice.about.domain.repository.ExternalActionsRepository
import com.gdavidpb.tuindice.about.presentation.action.ContactDeveloperActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.LoadVersionActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.OpenPrivacyPolicyActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.OpenTermsAndConditionsActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.OpenUrlActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.RateOnPlayStoreActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.ReportBugActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.ShareAppActionProcessor
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
		val externalActionsRepository = ViewModelFakeExternalActionsGateway()
		val browserRepository = AboutViewModelFakeBrowserGateway()
		val viewModel = createViewModel(
			externalActionsRepository = externalActionsRepository,
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
			delay(100)

			assertEquals(1, effects.size)

			val terms = assertIs<About.Effect.NavigateToBrowser>(effects[0])
			assertEquals("TuIndice - Términos y condiciones", terms.title)
			assertEquals("https://tuindice.app/terms", terms.url)
			assertEquals("TuIndice", externalActionsRepository.lastSharedSubject)
			assertEquals(
				"TuIndice: Una nueva forma de administrar tus notas",
				externalActionsRepository.lastSharedText
			)
			assertEquals(2, externalActionsRepository.sendEmailCalls)
			assertEquals(1, externalActionsRepository.openStorePageCalls)
			assertEquals("https://tuindice.app/github", browserRepository.lastOpenedUrl)
		} finally {
			effectJob.cancel()
			stateJob.cancel()
		}
	}

	private fun createViewModel(
		externalActionsRepository: ExternalActionsRepository = ViewModelFakeExternalActionsGateway(),
		browserRepository: BrowserRepository = AboutViewModelFakeBrowserGateway(),
		configRepository: ConfigRepository = AboutViewModelFakeConfigGateway()
	): AboutViewModel {
		return AboutViewModel(
			loadVersionActionProcessor = LoadVersionActionProcessor(
				loadVersionUseCase = LoadVersionUseCase(
					aboutRepository = AboutViewModelFakeAboutRepository()
				)
			),
			contactDeveloperActionProcessor = ContactDeveloperActionProcessor(
				sendSupportEmailUseCase = SendSupportEmailUseCase(
					externalActionsRepository = externalActionsRepository,
					configRepository = configRepository
				)
			),
			openTermsAndConditionsActionProcessor = OpenTermsAndConditionsActionProcessor(
				appEnvironmentRepository = AboutViewModelFakeAppEnvironmentGateway()
			),
			openPrivacyPolicyActionProcessor = OpenPrivacyPolicyActionProcessor(
				appEnvironmentRepository = AboutViewModelFakeAppEnvironmentGateway()
			),
			shareAppActionProcessor = ShareAppActionProcessor(
				shareTextUseCase = ShareTextUseCase(
					externalActionsRepository = externalActionsRepository
				)
			),
			rateOnPlayStoreActionProcessor = RateOnPlayStoreActionProcessor(
				openStorePageUseCase = OpenStorePageUseCase(
					externalActionsRepository = externalActionsRepository
				)
			),
			reportBugActionProcessor = ReportBugActionProcessor(
				sendSupportEmailUseCase = SendSupportEmailUseCase(
					externalActionsRepository = externalActionsRepository,
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

private class ViewModelFakeExternalActionsGateway : ExternalActionsRepository {
	var sendEmailCalls: Int = 0
	var openStorePageCalls: Int = 0
	var lastSharedSubject: String? = null
	var lastSharedText: String? = null

	override fun sendEmail(email: String, subject: String, text: String) {
		sendEmailCalls++
	}

	override fun shareText(subject: String, text: String) {
		lastSharedSubject = subject
		lastSharedText = text
	}

	override fun openStorePage() {
		openStorePageCalls++
	}
}
