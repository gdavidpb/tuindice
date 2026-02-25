package com.gdavidpb.tuindice.login.presentation.action

import com.gdavidpb.tuindice.base.domain.model.AppEnvironment
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentGateway
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import com.gdavidpb.tuindice.login.presentation.resource.LoginTextProvider
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SignInActionProcessorsTest {
	@Test
	fun setUsbIdActionProcessor_updatesIdleState() = runBlocking {
		val processor = SetUsbIdActionProcessor()

		val mutations = processor.process(
			action = SignIn.Action.SetUsbId(usbId = "USB-123"),
			sideEffect = {}
		).toList()
		val finalState = applyMutations(
			initialState = SignIn.State.Idle(password = "secret"),
			mutations = mutations
		)

		val idle = assertIs<SignIn.State.Idle>(finalState)
		assertEquals("USB-123", idle.usbId)
		assertEquals("secret", idle.password)
	}

	@Test
	fun setPasswordActionProcessor_updatesIdleState() = runBlocking {
		val processor = SetPasswordActionProcessor()

		val mutations = processor.process(
			action = SignIn.Action.SetPassword(password = "new-secret"),
			sideEffect = {}
		).toList()
		val finalState = applyMutations(
			initialState = SignIn.State.Idle(usbId = "USB-123"),
			mutations = mutations
		)

		val idle = assertIs<SignIn.State.Idle>(finalState)
		assertEquals("USB-123", idle.usbId)
		assertEquals("new-secret", idle.password)
	}

	@Test
	fun setUsbIdActionProcessor_whenLoggingIn_keepsState() = runBlocking {
		val processor = SetUsbIdActionProcessor()
		val initialState = SignIn.State.LoggingIn(
			usbId = "USB-OLD",
			password = "secret",
			messages = listOf("Cargando")
		)

		val mutations = processor.process(
			action = SignIn.Action.SetUsbId(usbId = "USB-NEW"),
			sideEffect = {}
		).toList()
		val finalState = applyMutations(initialState, mutations)

		assertEquals(initialState, finalState)
	}

	@Test
	fun openTermsAndConditionsActionProcessor_emitsNavigateToBrowserEffect() = runBlocking {
		val termsUrl = "https://tuindice.app/terms"
		val processor = OpenTermsAndConditionsActionProcessor(
			textProvider = FakeLoginTextProvider,
			appEnvironmentRepository = FakeAppEnvironmentGateway(
				AppEnvironment(
					apiBaseUrl = "https://api.tuindice.app/",
					privacyPolicyUrl = "https://tuindice.app/privacy",
					termsAndConditionsUrl = termsUrl,
					debug = false
				)
			)
		)
		val effects = mutableListOf<SignIn.Effect>()

		val mutations = processor.process(
			action = SignIn.Action.ClickTermsAndConditions,
			sideEffect = effects::add
		).toList()
		val finalState = applyMutations(SignIn.State.Idle(), mutations)

		val effect = assertIs<SignIn.Effect.NavigateToBrowser>(effects.single())
		assertEquals("Terms", effect.title)
		assertEquals(termsUrl, effect.url)
		assertEquals(SignIn.State.Idle(), finalState)
	}

	@Test
	fun openPrivacyPolicyActionProcessor_emitsNavigateToBrowserEffect() = runBlocking {
		val privacyUrl = "https://tuindice.app/privacy"
		val processor = OpenPrivacyPolicyActionProcessor(
			textProvider = FakeLoginTextProvider,
			appEnvironmentRepository = FakeAppEnvironmentGateway(
				AppEnvironment(
					apiBaseUrl = "https://api.tuindice.app/",
					privacyPolicyUrl = privacyUrl,
					termsAndConditionsUrl = "https://tuindice.app/terms",
					debug = false
				)
			)
		)
		val effects = mutableListOf<SignIn.Effect>()

		val mutations = processor.process(
			action = SignIn.Action.ClickPrivacyPolicy,
			sideEffect = effects::add
		).toList()
		val finalState = applyMutations(SignIn.State.Idle(), mutations)

		val effect = assertIs<SignIn.Effect.NavigateToBrowser>(effects.single())
		assertEquals("Privacy", effect.title)
		assertEquals(privacyUrl, effect.url)
		assertEquals(SignIn.State.Idle(), finalState)
	}

	private fun applyMutations(
		initialState: SignIn.State,
		mutations: List<(SignIn.State) -> SignIn.State>
	): SignIn.State {
		return mutations.fold(initialState) { state, mutation -> mutation(state) }
	}
}

private object FakeLoginTextProvider : LoginTextProvider {
	override fun privacyPolicyTitle(): String = "Privacy"
	override fun termsAndConditionsTitle(): String = "Terms"
	override fun invalidCredentials(): String = "Invalid credentials"
	override fun userDisabled(): String = "User disabled"
	override fun serviceUnavailable(): String = "Service unavailable"
	override fun networkUnavailable(): String = "Network unavailable"
	override fun retry(): String = "Retry"
	override fun timeout(): String = "Timeout"
	override fun passwordUpdated(): String = "Password updated"
	override fun invalidPassword(): String = "Invalid password"
	override fun defaultError(): String = "Default error"
}

private class FakeAppEnvironmentGateway(
	private val appEnvironment: AppEnvironment
) : AppEnvironmentGateway {
	override fun getEnvironment(): AppEnvironment = appEnvironment
}
