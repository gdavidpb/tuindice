package com.gdavidpb.tuindice.about.presentation.action

import com.gdavidpb.tuindice.about.domain.usecase.OpenStoreUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenExternalUrlUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SendSupportEmailUseCase
import com.gdavidpb.tuindice.about.data.repository.StoreUrlDataSource
import com.gdavidpb.tuindice.base.presentation.Mutation
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
	fun shareAppActionProcessor_emitsShareTextEffect() = runBlocking {
		val processor = ShareAppActionProcessor()
		val effects = mutableListOf<About.Effect>()

		processor.process(
			action = About.Action.ShareApp,
			sideEffect = effects::add
		).toList().applyToState()

		val effect = assertIs<About.Effect.ShareText>(effects.single())
		assertEquals("TuIndice", effect.subject)
		assertEquals(
			"TuIndice: Una nueva forma de administrar tus notas",
			effect.text
		)
	}

	@Test
	fun reportBugActionProcessor_emitsMailtoUriEffect() = runBlocking {
		val processor = ReportBugActionProcessor(
			sendSupportEmailUseCase = SendSupportEmailUseCase(
				configRepository = FakeConfigGateway()
			)
		)
		val effects = mutableListOf<About.Effect>()

		processor.process(
			action = About.Action.ReportBug,
			sideEffect = effects::add
		).toList().applyToState()

		val effect = assertIs<About.Effect.OpenUri>(effects.single())
		assertEquals(
			"mailto:support@tuindice.app?subject=Support%20TuIndice&body=",
			effect.uri
		)
	}

	@Test
	fun contactDeveloperActionProcessor_emitsMailtoUriEffect() = runBlocking {
		val processor = ContactDeveloperActionProcessor(
			sendSupportEmailUseCase = SendSupportEmailUseCase(
				configRepository = FakeConfigGateway()
			)
		)
		val effects = mutableListOf<About.Effect>()

		processor.process(
			action = About.Action.ContactDeveloper,
			sideEffect = effects::add
		).toList().applyToState()

		val effect = assertIs<About.Effect.OpenUri>(effects.single())
		assertEquals(
			"mailto:support@tuindice.app?subject=Support%20TuIndice&body=",
			effect.uri
		)
	}

	@Test
	fun rateOnPlayStoreActionProcessor_emitsStoreUriEffect() = runBlocking {
		val processor = RateOnStoreActionProcessor(
			openStoreUseCase = OpenStoreUseCase(
				storeUrlDataSource = FakeStoreUrlDataSource()
			)
		)
		val effects = mutableListOf<About.Effect>()

		processor.process(
			action = About.Action.RateOnStore,
			sideEffect = effects::add
		).toList().applyToState()

		val effect = assertIs<About.Effect.OpenUri>(effects.single())
		assertEquals("market://details?id=com.gdavidpb.tuindice", effect.uri)
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

private fun List<Mutation<About.State>>.applyToState(
	initialState: About.State = About.State.Idle
): About.State {
	return fold(initialState) { state, mutation -> mutation(state) }
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

private class FakeStoreUrlDataSource : StoreUrlDataSource {
	override fun getStoreUrl(): String {
		return "market://details?id=com.gdavidpb.tuindice"
	}
}
