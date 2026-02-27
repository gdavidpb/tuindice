package com.gdavidpb.tuindice.about.presentation.action

import com.gdavidpb.tuindice.about.domain.usecase.OpenStorePageUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenExternalUrlUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SendSupportEmailUseCase
import com.gdavidpb.tuindice.about.domain.usecase.ShareTextUseCase
import com.gdavidpb.tuindice.about.domain.repository.ExternalActionsRepository
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.base.domain.model.AppEnvironment
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.BrowserRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
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
	fun shareAppActionProcessor_sharesContentWithoutEffect() = runBlocking {
		val externalActionsRepository = FakeExternalActionsGateway()
		val processor = ShareAppActionProcessor(
			shareTextUseCase = ShareTextUseCase(
				externalActionsRepository = externalActionsRepository
			)
		)
		val effects = mutableListOf<About.Effect>()

		processor.process(
			action = About.Action.ShareApp,
			sideEffect = effects::add
		).toList()

		assertTrue(effects.isEmpty())
		assertEquals("TuIndice", externalActionsRepository.lastSharedSubject)
		assertEquals(
			"TuIndice: Una nueva forma de administrar tus notas",
			externalActionsRepository.lastSharedText
		)
	}

	@Test
	fun reportBugActionProcessor_sendsSupportEmailWithoutEffect() = runBlocking {
		val externalActionsRepository = FakeExternalActionsGateway()
		val processor = ReportBugActionProcessor(
			sendSupportEmailUseCase = SendSupportEmailUseCase(
				externalActionsRepository = externalActionsRepository,
				configRepository = FakeConfigGateway()
			)
		)
		val effects = mutableListOf<About.Effect>()

		processor.process(
			action = About.Action.ReportBug,
			sideEffect = effects::add
		).toList()

		assertTrue(effects.isEmpty())
		assertEquals("support@tuindice.app", externalActionsRepository.lastEmail)
		assertEquals("Support TuIndice", externalActionsRepository.lastEmailSubject)
	}

	@Test
	fun contactDeveloperActionProcessor_sendsSupportEmailWithoutEffect() = runBlocking {
		val externalActionsRepository = FakeExternalActionsGateway()
		val processor = ContactDeveloperActionProcessor(
			sendSupportEmailUseCase = SendSupportEmailUseCase(
				externalActionsRepository = externalActionsRepository,
				configRepository = FakeConfigGateway()
			)
		)
		val effects = mutableListOf<About.Effect>()

		processor.process(
			action = About.Action.ContactDeveloper,
			sideEffect = effects::add
		).toList()

		assertTrue(effects.isEmpty())
		assertEquals("support@tuindice.app", externalActionsRepository.lastEmail)
		assertEquals("Support TuIndice", externalActionsRepository.lastEmailSubject)
	}

	@Test
	fun rateOnPlayStoreActionProcessor_opensStoreWithoutEffect() = runBlocking {
		val externalActionsRepository = FakeExternalActionsGateway()
		val processor = RateOnPlayStoreActionProcessor(
			openStorePageUseCase = OpenStorePageUseCase(
				externalActionsRepository = externalActionsRepository
			)
		)
		val effects = mutableListOf<About.Effect>()

		processor.process(
			action = About.Action.RateOnPlayStore,
			sideEffect = effects::add
		).toList()

		assertTrue(effects.isEmpty())
		assertEquals(1, externalActionsRepository.openStorePageCalls)
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

private class FakeConfigGateway : ConfigRepository {
	override suspend fun tryFetch() = Unit

	override fun getTimeout(): Long = 5_000L

	override fun getContactEmail(): String = "support@tuindice.app"

	override fun getContactSubject(): String = "Support TuIndice"

	override fun getLoadingMessages(): List<String> = listOf("Loading")

	override fun getTimeUpdateStalenessDays(): Int = 7

	override fun getSyncsToSuggestReview(): Int = 3
}

private class FakeExternalActionsGateway : ExternalActionsRepository {
	var openStorePageCalls: Int = 0
	var lastEmail: String? = null
	var lastEmailSubject: String? = null
	var lastEmailText: String? = null
	var lastSharedSubject: String? = null
	var lastSharedText: String? = null

	override fun sendEmail(email: String, subject: String, text: String) {
		lastEmail = email
		lastEmailSubject = subject
		lastEmailText = text
	}

	override fun shareText(subject: String, text: String) {
		lastSharedSubject = subject
		lastSharedText = text
	}

	override fun openStore() {
		openStorePageCalls++
	}
}
