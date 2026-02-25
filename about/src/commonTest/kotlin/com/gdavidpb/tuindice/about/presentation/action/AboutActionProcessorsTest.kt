package com.gdavidpb.tuindice.about.presentation.action

import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.presentation.resource.AboutTextProvider
import com.gdavidpb.tuindice.base.domain.model.AppEnvironment
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentGateway
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AboutActionProcessorsTest {
	@Test
	fun openTermsAndConditionsActionProcessor_emitsNavigateToBrowser() = runBlocking {
		val termsUrl = "https://tuindice.app/terms"
		val processor = OpenTermsAndConditionsActionProcessor(
			textProvider = FakeAboutTextProvider,
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
		assertEquals("Terms", effect.title)
		assertEquals(termsUrl, effect.url)
	}

	@Test
	fun openPrivacyPolicyActionProcessor_emitsNavigateToBrowser() = runBlocking {
		val privacyUrl = "https://tuindice.app/privacy"
		val processor = OpenPrivacyPolicyActionProcessor(
			textProvider = FakeAboutTextProvider,
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
		assertEquals("Privacy", effect.title)
		assertEquals(privacyUrl, effect.url)
	}

	@Test
	fun shareAppActionProcessor_emitsShareEffect() = runBlocking {
		val processor = ShareAppActionProcessor(textProvider = FakeAboutTextProvider)
		val effects = mutableListOf<About.Effect>()

		processor.process(
			action = About.Action.ShareApp,
			sideEffect = effects::add
		).toList()

		val effect = assertIs<About.Effect.StartShare>(effects.single())
		assertEquals("TuIndice App", effect.subject)
		assertEquals("Try TuIndice", effect.text)
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
	fun openUrlActionProcessor_emitsStartBrowser() = runBlocking {
		val processor = OpenUrlActionProcessor()
		val effects = mutableListOf<About.Effect>()

		processor.process(
			action = About.Action.OpenUrl(url = "https://tuindice.app/github"),
			sideEffect = effects::add
		).toList()

		val effect = assertIs<About.Effect.StartBrowser>(effects.single())
		assertEquals("https://tuindice.app/github", effect.url)
	}
}

private object FakeAboutTextProvider : AboutTextProvider {
	override fun privacyPolicyTitle(): String = "Privacy"
	override fun termsAndConditionsTitle(): String = "Terms"
	override fun shareMessage(): String = "Try TuIndice"
	override fun shareSubject(): String = "TuIndice App"
}

private class FakeAppEnvironmentGateway(
	private val appEnvironment: AppEnvironment
) : AppEnvironmentGateway {
	override fun getEnvironment(): AppEnvironment = appEnvironment
}
