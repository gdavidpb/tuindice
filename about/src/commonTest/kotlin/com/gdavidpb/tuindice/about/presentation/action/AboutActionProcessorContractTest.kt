package com.gdavidpb.tuindice.about.presentation.action

import app.cash.turbine.test
import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenExternalUrlUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenStoreUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SendSupportEmailUseCase
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.testing.FakeAboutRepository
import com.gdavidpb.tuindice.about.testing.FakeStoreUrlDataSource
import com.gdavidpb.tuindice.testkit.base.repository.FakeAppEnvironmentRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingBrowserRepository
import com.gdavidpb.tuindice.testkit.mvi.reduceMutations
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AboutActionProcessorContractTest {
	@Test
	fun loadVersionActionProcessor_reducesStateToContent() = runTest {
		val processor = LoadVersionActionProcessor(
			loadVersionUseCase = LoadVersionUseCase(
				aboutRepository = FakeAboutRepository("Producción v3.5.1 (351)")
			)
		)
		val effects = mutableListOf<About.Effect>()

		processor.process(
			action = About.Action.LoadVersion,
			sideEffect = effects::add
		).test {
			val idleMutation = awaitItem()
			assertEquals(About.State.Idle, idleMutation(About.State.Idle))

			val contentMutation = awaitItem()
			val content = assertIs<About.State.Content>(contentMutation(About.State.Idle))
			assertEquals("Producción v3.5.1 (351)", content.versionText)

			awaitComplete()
		}

		assertTrue(effects.isEmpty())
	}

	@Test
	fun openTermsAndConditionsActionProcessor_emitsNavigateToBrowserEffect() = runTest {
		val processor = OpenTermsAndConditionsActionProcessor(
			appEnvironmentRepository = FakeAppEnvironmentRepository()
		)
		val effects = mutableListOf<About.Effect>()
		val initialState = About.State.Content(versionText = "Producción v3.5.1 (351)")

		val finalState = processor.process(
			action = About.Action.OpenTermsAndConditions,
			sideEffect = effects::add
		).toList().reduceMutations(initialState)

		assertEquals(initialState, finalState)

		val effect = assertIs<About.Effect.NavigateToBrowser>(effects.single())
		assertEquals("TuIndice - Términos y condiciones", effect.title)
		assertEquals("https://tuindice.app/terms", effect.url)
	}

	@Test
	fun openPrivacyPolicyActionProcessor_emitsNavigateToBrowserEffect() = runTest {
		val processor = OpenPrivacyPolicyActionProcessor(
			appEnvironmentRepository = FakeAppEnvironmentRepository()
		)
		val effects = mutableListOf<About.Effect>()
		val initialState = About.State.Content(versionText = "Producción v3.5.1 (351)")

		val finalState = processor.process(
			action = About.Action.OpenPrivacyPolicy,
			sideEffect = effects::add
		).toList().reduceMutations(initialState)

		assertEquals(initialState, finalState)

		val effect = assertIs<About.Effect.NavigateToBrowser>(effects.single())
		assertEquals("TuIndice - Política de privacidad", effect.title)
		assertEquals("https://tuindice.app/privacy", effect.url)
	}

	@Test
	fun shareAppActionProcessor_emitsShareTextEffect() = runTest {
		val processor = ShareAppActionProcessor()
		val effects = mutableListOf<About.Effect>()
		val initialState = About.State.Content(versionText = "Producción v3.5.1 (351)")

		val finalState = processor.process(
			action = About.Action.ShareApp,
			sideEffect = effects::add
		).toList().reduceMutations(initialState)

		assertEquals(initialState, finalState)

		val effect = assertIs<About.Effect.ShareText>(effects.single())
		assertEquals("TuIndice", effect.subject)
		assertEquals("TuIndice: Una nueva forma de administrar tus notas", effect.text)
	}

	@Test
	fun rateOnStoreActionProcessor_emitsOpenUriEffect() = runTest {
		val processor = RateOnStoreActionProcessor(
			openStoreUseCase = OpenStoreUseCase(
				storeUrlDataSource = FakeStoreUrlDataSource(
					storeUrl = "market://details?id=com.gdavidpb.tuindice"
				)
			)
		)
		val effects = mutableListOf<About.Effect>()
		val initialState = About.State.Content(versionText = "Producción v3.5.1 (351)")

		val finalState = processor.process(
			action = About.Action.RateOnStore,
			sideEffect = effects::add
		).toList().reduceMutations(initialState)

		assertEquals(initialState, finalState)

		val effect = assertIs<About.Effect.OpenUri>(effects.single())
		assertEquals("market://details?id=com.gdavidpb.tuindice", effect.uri)
	}

	@Test
	fun reportBugActionProcessor_emitsMailtoEffect() = runTest {
		val processor = ReportBugActionProcessor(
			sendSupportEmailUseCase = SendSupportEmailUseCase(
				configRepository = FakeConfigRepository()
			)
		)
		val effects = mutableListOf<About.Effect>()
		val initialState = About.State.Content(versionText = "Producción v3.5.1 (351)")

		val finalState = processor.process(
			action = About.Action.ReportBug,
			sideEffect = effects::add
		).toList().reduceMutations(initialState)

		assertEquals(initialState, finalState)

		val effect = assertIs<About.Effect.OpenUri>(effects.single())
		assertEquals("mailto:support@tuindice.app?subject=Support%20TuIndice&body=", effect.uri)
	}

	@Test
	fun contactDeveloperActionProcessor_emitsMailtoEffect() = runTest {
		val processor = ContactDeveloperActionProcessor(
			sendSupportEmailUseCase = SendSupportEmailUseCase(
				configRepository = FakeConfigRepository()
			)
		)
		val effects = mutableListOf<About.Effect>()
		val initialState = About.State.Content(versionText = "Producción v3.5.1 (351)")

		val finalState = processor.process(
			action = About.Action.ContactDeveloper,
			sideEffect = effects::add
		).toList().reduceMutations(initialState)

		assertEquals(initialState, finalState)

		val effect = assertIs<About.Effect.OpenUri>(effects.single())
		assertEquals("mailto:support@tuindice.app?subject=Support%20TuIndice&body=", effect.uri)
	}

	@Test
	fun openUrlActionProcessor_opensBrowserWithoutEmittingEffect() = runTest {
		val browserRepository = RecordingBrowserRepository()
		val processor = OpenUrlActionProcessor(
			openExternalUrlUseCase = OpenExternalUrlUseCase(
				browserRepository = browserRepository
			)
		)
		val effects = mutableListOf<About.Effect>()
		val initialState = About.State.Content(versionText = "Producción v3.5.1 (351)")

		val finalState = processor.process(
			action = About.Action.OpenUrl(url = "https://tuindice.app/github"),
			sideEffect = effects::add
		).toList().reduceMutations(initialState)

		assertEquals(initialState, finalState)
		assertNull(effects.singleOrNull())
		assertEquals("https://tuindice.app/github", browserRepository.lastOpenedUrl)
	}
}
