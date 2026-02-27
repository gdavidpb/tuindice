package com.gdavidpb.tuindice.about.presentation.viewmodel

import com.gdavidpb.tuindice.about.domain.repository.AboutRepository
import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionUseCase
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
		val viewModel = createViewModel()
		val effects = mutableListOf<About.Effect>()
		val effectJob = launch(start = CoroutineStart.UNDISPATCHED) {
			viewModel.effect.collect { effects += it }
		}
		val stateJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.state.collect() }

		try {
			waitUntil { viewModel.action.subscriptionCount.value > 0 }
			waitUntil { viewModel.effect.subscriptionCount.value > 0 }

			viewModel.openTermsAndConditionsAction()
			waitUntil { effects.size == 1 }

			viewModel.shareAppAction()
			waitUntil { effects.size == 2 }

			viewModel.reportBugAction()
			waitUntil { effects.size == 3 }

			viewModel.contactDeveloperAction()
			waitUntil { effects.size == 4 }

			viewModel.rateOnPlayStoreAction()
			waitUntil { effects.size == 5 }

			viewModel.openUrlAction("https://tuindice.app/github")
			waitUntil { effects.size == 6 }

			val terms = assertIs<About.Effect.NavigateToBrowser>(effects[0])
			assertEquals("TuIndice - Términos y condiciones", terms.title)
			assertEquals("https://tuindice.app/terms", terms.url)

			val share = assertIs<About.Effect.StartShare>(effects[1])
			assertEquals("TuIndice", share.subject)
			assertEquals("TuIndice: Una nueva forma de administrar tus notas", share.text)

			assertEquals(About.Effect.ShowReportBugDialog, effects[2])
			assertEquals(About.Effect.StartEmail, effects[3])
			assertEquals(About.Effect.StartPlayStore, effects[4])

			val openUrl = assertIs<About.Effect.StartBrowser>(effects[5])
			assertEquals("https://tuindice.app/github", openUrl.url)
		} finally {
			effectJob.cancel()
			stateJob.cancel()
		}
	}

	private fun createViewModel(): AboutViewModel {
		return AboutViewModel(
			loadVersionActionProcessor = LoadVersionActionProcessor(
				loadVersionUseCase = LoadVersionUseCase(
					aboutRepository = AboutViewModelFakeAboutRepository()
				)
			),
			contactDeveloperActionProcessor = ContactDeveloperActionProcessor(),
			openTermsAndConditionsActionProcessor = OpenTermsAndConditionsActionProcessor(
				appEnvironmentRepository = AboutViewModelFakeAppEnvironmentGateway()
			),
			openPrivacyPolicyActionProcessor = OpenPrivacyPolicyActionProcessor(
				appEnvironmentRepository = AboutViewModelFakeAppEnvironmentGateway()
			),
			shareAppActionProcessor = ShareAppActionProcessor(),
			rateOnPlayStoreActionProcessor = RateOnPlayStoreActionProcessor(),
			reportBugActionProcessor = ReportBugActionProcessor(),
			openUrlActionProcessor = OpenUrlActionProcessor()
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
