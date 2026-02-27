package com.gdavidpb.tuindice.about.presentation.action

import com.gdavidpb.tuindice.about.domain.usecase.OpenExternalUrlUseCase
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.base.domain.model.AppEnvironment
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.BrowserRepository
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AboutActionProcessorsTest {
	@Test
	fun openTermsAndConditionsActionProcessor_emitsNavigateToBrowser() = runBlocking {
		val termsUrl = "https://tuindice.app/terms"
		val processor = OpenTermsAndConditionsActionProcessor(
			appEnvironmentRepository = FakeAppEnvironmentGateway(
				AppEnvironment(
					apiBaseUrl = "https://api.tuindice.app/",
					privacyPolicyUrl = "https://tuindice.app/privacy",
					termsAndConditionsUrl = termsUrl,
					debug = false
				)
			)
		)
		val effects = mutableListOf<About.Effect>()

		processor.process(
			action = About.Action.OpenTermsAndConditions,
			sideEffect = effects::add
		).toList()

		val effect = assertIs<About.Effect.NavigateToBrowser>(effects.single())
		assertEquals("TuIndice - Términos y condiciones", effect.title)
		assertEquals(termsUrl, effect.url)
	}

	@Test
	fun openPrivacyPolicyActionProcessor_emitsNavigateToBrowser() = runBlocking {
		val privacyUrl = "https://tuindice.app/privacy"
		val processor = OpenPrivacyPolicyActionProcessor(
			appEnvironmentRepository = FakeAppEnvironmentGateway(
				AppEnvironment(
					apiBaseUrl = "https://api.tuindice.app/",
					privacyPolicyUrl = privacyUrl,
					termsAndConditionsUrl = "https://tuindice.app/terms",
					debug = false
				)
			)
		)
		val effects = mutableListOf<About.Effect>()

		processor.process(
			action = About.Action.OpenPrivacyPolicy,
			sideEffect = effects::add
		).toList()

		val effect = assertIs<About.Effect.NavigateToBrowser>(effects.single())
		assertEquals("TuIndice - Política de privacidad", effect.title)
		assertEquals(privacyUrl, effect.url)
	}

	@Test
	fun shareAppActionProcessor_emitsShareEffect() = runBlocking {
		val processor = ShareAppActionProcessor()
		val effects = mutableListOf<About.Effect>()

		processor.process(
			action = About.Action.ShareApp,
			sideEffect = effects::add
		).toList()

		val effect = assertIs<About.Effect.StartShare>(effects.single())
		assertEquals("TuIndice", effect.subject)
		assertEquals("TuIndice: Una nueva forma de administrar tus notas", effect.text)
	}

	@Test
	fun reportBugActionProcessor_emitsShowReportBugDialog() = runBlocking {
		val processor = ReportBugActionProcessor()
		val effects = mutableListOf<About.Effect>()

		processor.process(
			action = About.Action.ReportBug,
			sideEffect = effects::add
		).toList()

		assertEquals(About.Effect.ShowReportBugDialog, effects.single())
	}

	@Test
	fun contactDeveloperActionProcessor_emitsStartEmail() = runBlocking {
		val processor = ContactDeveloperActionProcessor()
		val effects = mutableListOf<About.Effect>()

		processor.process(
			action = About.Action.ContactDeveloper,
			sideEffect = effects::add
		).toList()

		assertEquals(About.Effect.StartEmail, effects.single())
	}

	@Test
	fun rateOnPlayStoreActionProcessor_emitsStartPlayStore() = runBlocking {
		val processor = RateOnPlayStoreActionProcessor()
		val effects = mutableListOf<About.Effect>()

		processor.process(
			action = About.Action.RateOnPlayStore,
			sideEffect = effects::add
		).toList()

		assertEquals(About.Effect.StartPlayStore, effects.single())
	}

	@Test
	fun openUrlActionProcessor_opensExternalUrlWithoutEffect() = runBlocking {
		val browserRepository = FakeBrowserGateway()
		val processor = OpenUrlActionProcessor(
			openExternalUrlUseCase = OpenExternalUrlUseCase(browserRepository)
		)
		val effects = mutableListOf<About.Effect>()
		val targetUrl = "https://tuindice.app/github"

		processor.process(
			action = About.Action.OpenUrl(url = targetUrl),
			sideEffect = effects::add
		).toList()

		assertTrue(effects.isEmpty())
		assertEquals(targetUrl, browserRepository.lastOpenedUrl)
	}
}

private class FakeAppEnvironmentGateway(
	private val appEnvironment: AppEnvironment
) : AppEnvironmentRepository {
	override fun getEnvironment(): AppEnvironment = appEnvironment
}

private class FakeBrowserGateway : BrowserRepository {
	var lastOpenedUrl: String? = null

	override fun open(url: String) {
		lastOpenedUrl = url
	}
}
