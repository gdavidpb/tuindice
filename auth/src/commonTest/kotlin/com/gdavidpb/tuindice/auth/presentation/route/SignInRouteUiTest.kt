package com.gdavidpb.tuindice.auth.presentation.route

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.gdavidpb.tuindice.base.domain.model.AppEnvironment
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.auth.domain.model.IssueTokensFlow
import com.gdavidpb.tuindice.auth.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.exceptionhandler.SignInExceptionHandler
import com.gdavidpb.tuindice.auth.domain.usecase.validator.SignInParamsValidator
import com.gdavidpb.tuindice.auth.presentation.action.OpenPrivacyPolicyActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.OpenTermsAndConditionsActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.SetPasswordActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.SetUsbIdActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.SignInActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.TogglePasswordVisibilityActionProcessor
import com.gdavidpb.tuindice.auth.presentation.viewmodel.SignInViewModel
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.auth.testing.FakeAttestationRepository
import com.gdavidpb.tuindice.auth.testing.RecordingAuthRepository
import com.gdavidpb.tuindice.auth.testing.RecordingMessagingRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeAppEnvironmentRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SignInRouteUiTest {
	private data class SignInRouteFixture(
		val viewModel: SignInViewModel,
		val authRepository: RecordingAuthRepository
	)

	@Test
	fun when_termsActionTriggered_then_navigatesToBrowser() = runTuIndiceUiTest {
		val expectedUrl = "https://tuindice.test/terms"
		val fixture = createSignInViewModel(
			termsAndConditionsUrl = expectedUrl
		)
		var navigatedUrl = ""
		val shownSnackBars = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			SignInRoute(
				onNavigateToSummary = {},
				onNavigateToBrowser = { _, url -> navigatedUrl = url },
				showSnackBar = { message -> shownSnackBars += message },
				viewModel = fixture.viewModel
			)
		}

		runOnIdle {
			fixture.viewModel.openTermsAndConditionsAction()
		}

		waitUntil(timeoutMillis = 2_000) {
			navigatedUrl.isNotEmpty()
		}

		assertEquals(expectedUrl, navigatedUrl)
		assertTrue(shownSnackBars.isEmpty())
	}

	@Test
	fun when_signInActionTriggered_then_navigatesToSummary() = runTuIndiceUiTest {
		val fixture = createSignInViewModel(
			termsAndConditionsUrl = "https://tuindice.test/terms"
		)
		var summaryNavigations = 0
		val shownSnackBars = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			SignInRoute(
				onNavigateToSummary = { summaryNavigations++ },
				onNavigateToBrowser = { _, _ -> },
				showSnackBar = { message -> shownSnackBars += message },
				viewModel = fixture.viewModel
			)
		}

		runOnIdle {
			fixture.viewModel.signInAction(
				usbId = "12-34567",
				password = "1234"
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			summaryNavigations > 0
		}

		assertEquals(1, summaryNavigations)
		assertTrue(shownSnackBars.isEmpty())
	}

	@Test
	fun when_credentialsEnteredAndSignInButtonTapped_then_navigatesToSummaryAndUsesTypedCredentials() = runTuIndiceUiTest {
		val fixture = createSignInViewModel(
			termsAndConditionsUrl = "https://tuindice.test/terms"
		)
		var summaryNavigations = 0
		val shownSnackBars = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			SignInRoute(
				onNavigateToSummary = { summaryNavigations++ },
				onNavigateToBrowser = { _, _ -> },
				showSnackBar = { message -> shownSnackBars += message },
				viewModel = fixture.viewModel
			)
		}

		onNodeWithTag(AuthUiTags.UsbIdTextField).performTextInput("1234567")
		onNodeWithTag(AuthUiTags.PasswordTextField).performTextInput("1234")
		onNodeWithTag(AuthUiTags.SignInButton).performClick()

		waitUntil(timeoutMillis = 2_000) {
			summaryNavigations > 0 && fixture.authRepository.issueTokensCalls.isNotEmpty()
		}

		val call = fixture.authRepository.issueTokensCalls.first()
		assertEquals(1, summaryNavigations)
		assertEquals("12-34567", call.usbId)
		assertEquals("1234", call.password)
		assertEquals(IssueTokensFlow.IssueTokens, call.flow)
		assertTrue(shownSnackBars.isEmpty())
	}

	@Test
	fun when_privacyPolicyActionTriggered_then_navigatesToBrowserPrivacyUrl() = runTuIndiceUiTest {
		val expectedPrivacyUrl = "https://tuindice.test/privacy"
		val fixture = createSignInViewModel(
			termsAndConditionsUrl = "https://tuindice.test/terms",
			privacyPolicyUrl = expectedPrivacyUrl
		)
		var navigatedUrl = ""
		val shownSnackBars = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			SignInRoute(
				onNavigateToSummary = {},
				onNavigateToBrowser = { _, url -> navigatedUrl = url },
				showSnackBar = { message -> shownSnackBars += message },
				viewModel = fixture.viewModel
			)
		}

		runOnIdle {
			fixture.viewModel.openPrivacyPolicyAction()
		}

		waitUntil(timeoutMillis = 2_000) {
			navigatedUrl.isNotEmpty()
		}

		assertEquals(expectedPrivacyUrl, navigatedUrl)
		assertTrue(shownSnackBars.isEmpty())
	}

	@Test
	fun when_signInFailsWithInvalidCredentials_then_showsSnackBarWithoutSummaryNavigation() = runTuIndiceUiTest {
		val fixture = createSignInViewModel(
			termsAndConditionsUrl = "https://tuindice.test/terms",
			signInThrowable = clientRequestException(HttpStatusCode.Unauthorized, path = "/auth/v1/token")
		)
		var summaryNavigations = 0
		val shownSnackBars = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			SignInRoute(
				onNavigateToSummary = { summaryNavigations++ },
				onNavigateToBrowser = { _, _ -> },
				showSnackBar = { message -> shownSnackBars += message },
				viewModel = fixture.viewModel
			)
		}

		runOnIdle {
			fixture.viewModel.signInAction(
				usbId = "12-34567",
				password = "clave-invalida"
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			shownSnackBars.isNotEmpty()
		}

		val snackBar = shownSnackBars.first()
		assertEquals(0, summaryNavigations)
		assertTrue(snackBar.message.isNotBlank())
		assertEquals(null, snackBar.actionLabel)
		assertEquals(null, snackBar.onAction)
	}

	@Test
	fun when_signInFailsWithConnection_then_retrySnackBarActionRetriesSignIn() = runTuIndiceUiTest {
		val fixture = createSignInViewModel(
			termsAndConditionsUrl = "https://tuindice.test/terms",
			signInThrowable = IllegalStateException("network unreachable"),
			networkAvailable = false
		)
		val shownSnackBars = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			SignInRoute(
				onNavigateToSummary = {},
				onNavigateToBrowser = { _, _ -> },
				showSnackBar = { message -> shownSnackBars += message },
				viewModel = fixture.viewModel
			)
		}

		runOnIdle {
			fixture.viewModel.signInAction(
				usbId = "12-34567",
				password = "1234"
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			shownSnackBars.isNotEmpty()
		}

		val retrySnackBar = shownSnackBars.first()
		val retryAction = retrySnackBar.onAction
		assertNotNull(retryAction)
		assertTrue(retrySnackBar.actionLabel.isNullOrBlank().not())
		assertEquals(1, fixture.authRepository.issueTokensCalls.size)

		runOnIdle {
			retryAction()
		}

		waitUntil(timeoutMillis = 2_000) {
			fixture.authRepository.issueTokensCalls.size >= 2
		}

		assertEquals(2, fixture.authRepository.issueTokensCalls.size)
	}

	private fun createSignInViewModel(
		termsAndConditionsUrl: String,
		privacyPolicyUrl: String = "https://tuindice.test/privacy",
		signInThrowable: Throwable? = null,
		networkAvailable: Boolean = true
	): SignInRouteFixture {
		val authRepository = RecordingAuthRepository(throwable = signInThrowable)
		val signInUseCase = SignInUseCase(
			authRepository = authRepository,
			messagingRepository = RecordingMessagingRepository(),
			riskAttestationRepository = FakeAttestationRepository(),
			paramsValidator = SignInParamsValidator(),
			exceptionHandler = SignInExceptionHandler(
				networkRepository = FakeNetworkRepository(isAvailable = networkAvailable),
				reportingRepository = RecordingReportingRepository()
			)
		)

		return SignInRouteFixture(
			viewModel = SignInViewModel(
				signInActionProcessor = SignInActionProcessor(
					signInUseCase = signInUseCase,
					configRepository = FakeConfigRepository()
				),
				setUsbIdActionProcessor = SetUsbIdActionProcessor(),
				setPasswordActionProcessor = SetPasswordActionProcessor(),
				togglePasswordVisibilityActionProcessor = TogglePasswordVisibilityActionProcessor(),
				openTermsAndConditionsActionProcessor = OpenTermsAndConditionsActionProcessor(
					appEnvironmentRepository = FakeAppEnvironmentRepository(
						appEnvironment = AppEnvironment(
							apiBaseUrl = "https://api.tuindice.test/",
							privacyPolicyUrl = privacyPolicyUrl,
							termsAndConditionsUrl = termsAndConditionsUrl,
							debug = true
						)
					)
				),
				privacyPolicyActionProcessor = OpenPrivacyPolicyActionProcessor(
					appEnvironmentRepository = FakeAppEnvironmentRepository(
						appEnvironment = AppEnvironment(
							apiBaseUrl = "https://api.tuindice.test/",
							privacyPolicyUrl = privacyPolicyUrl,
							termsAndConditionsUrl = termsAndConditionsUrl,
							debug = true
						)
					)
				)
			),
			authRepository = authRepository
		)
	}
}
