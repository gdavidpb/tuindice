package com.gdavidpb.tuindice.auth.presentation.route

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.intl.Locale
import com.gdavidpb.tuindice.auth.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.exceptionhandler.SignInExceptionHandler
import com.gdavidpb.tuindice.auth.domain.usecase.validator.SignInParamsValidator
import com.gdavidpb.tuindice.auth.presentation.machine.SignInMachine
import com.gdavidpb.tuindice.auth.presentation.viewmodel.SignInViewModel
import com.gdavidpb.tuindice.auth.testing.FakeAttestationRepository
import com.gdavidpb.tuindice.auth.testing.RecordingAuthRepository
import com.gdavidpb.tuindice.auth.testing.RecordingMessagingRepository
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.data.source.usage.InMemoryUsageDataConsentRepository
import com.gdavidpb.tuindice.base.domain.model.AppEnvironment
import com.gdavidpb.tuindice.testkit.base.repository.FakeAppEnvironmentRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.ui.TuIndiceTestSizeClass
import com.gdavidpb.tuindice.testkit.ui.assertNodeEnabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * RTL and accessibility-semantics matrix for the SignIn screen.
 *
 * Mirrors the fixture construction of [SignInRouteUiTest] and reuses the
 * auth/testkit testing doubles without modifying them.
 */
@OptIn(ExperimentalTestApi::class)
class SignInRtlA11yUiTest {
	@Test
	fun when_layoutIsRtl_then_signInCriticalNodesRemainVisibleAndEnabled() = runTuIndiceUiTest {
		val viewModel = createSignInViewModel()

		// Locale "ar" drives LocalLayoutDirection to Rtl through the test kit.
		setTuIndiceTestContent(locale = Locale("ar")) {
			SignInRoute(
				onNavigateToSummary = {},
				onNavigateToBrowser = { _, _ -> },
				showSnackBar = {},
				viewModel = viewModel
			)
		}

		// E2E-critical nodes must stay laid out and interactive under RTL mirroring.
		assertNodeVisible(AuthUiTags.UsbIdTextField)
		assertNodeVisible(AuthUiTags.PasswordTextField)
		assertNodeVisible(AuthUiTags.SignInButton)
		assertNodeVisible(AuthUiTags.TermsAndConditionsLink)
		assertNodeVisible(AuthUiTags.PrivacyPolicyLink)

		assertNodeEnabled(AuthUiTags.UsbIdTextField)
		assertNodeEnabled(AuthUiTags.PasswordTextField)
	}

	@Test
	fun when_layoutIsRtl_then_typedCredentialsEnableButtonAndSignInNavigates() = runTuIndiceUiTest {
		val viewModel = createSignInViewModel()
		var summaryNavigations = 0

		setTuIndiceTestContent(locale = Locale("ar")) {
			SignInRoute(
				onNavigateToSummary = { summaryNavigations++ },
				onNavigateToBrowser = { _, _ -> },
				showSnackBar = {},
				viewModel = viewModel
			)
		}

		onNodeWithTag(AuthUiTags.UsbIdTextField).performTextInput("1234567")
		onNodeWithTag(AuthUiTags.PasswordTextField).performTextInput("1234")

		// The primary button only enables with a valid usbId and non-empty password;
		// RTL must not break that interaction chain.
		onNodeWithTag(AuthUiTags.SignInButton).assertIsEnabled()
		onNodeWithTag(AuthUiTags.SignInButton).performClick()

		waitUntil(timeoutMillis = 2_000) {
			summaryNavigations > 0
		}

		assertEquals(1, summaryNavigations)
	}

	@Test
	fun when_a11ySemanticsInspected_then_interactiveNodesExposeLabelAndClickAction() = runTuIndiceUiTest {
		val viewModel = createSignInViewModel()

		setTuIndiceTestContent {
			SignInRoute(
				onNavigateToSummary = {},
				onNavigateToBrowser = { _, _ -> },
				showSnackBar = {},
				viewModel = viewModel
			)
		}

		// Primary button: clickable and labeled (Button { Text(...) } merges the label).
		onNodeWithTag(AuthUiTags.SignInButton)
			.assert(hasClickAction())
			.assert(hasAccessibleLabel)

		// Text fields: expose editable text semantics (plus merged label text).
		onNodeWithTag(AuthUiTags.UsbIdTextField).assert(hasAccessibleLabel)
		onNodeWithTag(AuthUiTags.PasswordTextField).assert(hasAccessibleLabel)

		// Password visibility toggle: icon-only control, must carry a contentDescription.
		onNodeWithTag(AuthUiTags.PasswordToggle)
			.assert(hasClickAction())
			.assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.ContentDescription))

		// Legal links: clickable text exposing their own label.
		onNodeWithTag(AuthUiTags.TermsAndConditionsLink)
			.assert(hasClickAction())
			.assert(hasAccessibleLabel)
		onNodeWithTag(AuthUiTags.PrivacyPolicyLink)
			.assert(hasClickAction())
			.assert(hasAccessibleLabel)
	}

	@Test
	fun when_compactRtlWithIncreasedDensity_then_formRemainsUsable() = runTuIndiceUiTest {
		val viewModel = createSignInViewModel()

		// Compact size class plus a 1.25x density scale (the kit exposes density as its scale knob).
		setTuIndiceTestContent(
			sizeClass = TuIndiceTestSizeClass.Compact,
			density = 1.25f,
			locale = Locale("ar")
		) {
			SignInRoute(
				onNavigateToSummary = {},
				onNavigateToBrowser = { _, _ -> },
				showSnackBar = {},
				viewModel = viewModel
			)
		}

		// Upper-region fields must stay visible at the larger scale.
		assertNodeVisible(AuthUiTags.UsbIdTextField)
		assertNodeVisible(AuthUiTags.PasswordTextField)

		onNodeWithTag(AuthUiTags.UsbIdTextField).performTextInput("1234567")
		onNodeWithTag(AuthUiTags.PasswordTextField).performTextInput("1234")

		// The scaled layout may push the button towards the window edge, so assert
		// existence + enabled state instead of strict viewport visibility.
		onNodeWithTag(AuthUiTags.SignInButton).assertExists().assertIsEnabled()
	}

	private fun createSignInViewModel(): SignInViewModel {
		val signInUseCase = SignInUseCase(
			authRepository = RecordingAuthRepository(throwable = null),
			messagingRepository = RecordingMessagingRepository(),
			syncRepository = FakeSyncRepository(),
			credentialsRepository = FakeCredentialsRepository(),
			syncStatusRepository = FakeSyncStatusRepository(),
			attestationRepository = FakeAttestationRepository(),
			reportingRepository = RecordingReportingRepository(),
			paramsValidator = SignInParamsValidator(),
			exceptionHandler = SignInExceptionHandler(
				networkRepository = FakeNetworkRepository(isAvailable = true)
			)
		)

		return SignInViewModel(
			screenMachine = SignInMachine(
				signInUseCase = signInUseCase,
				configRepository = FakeConfigRepository(),
				appEnvironmentRepository = FakeAppEnvironmentRepository(
					appEnvironment = AppEnvironment(
						apiBaseUrl = "https://api.tuindice.test/",
						privacyPolicyUrl = "https://tuindice.test/privacy",
						termsAndConditionsUrl = "https://tuindice.test/terms",
						supportUrl = "https://tuindice.test/support",
						debug = true
					)
				),
				usageDataConsentRepository = InMemoryUsageDataConsentRepository()
			),
			eventPublisher = NoOpEventPublisher
		)
	}
}

/**
 * A node is considered accessible when it exposes a contentDescription,
 * regular text or editable text to assistive technologies.
 */
private val hasAccessibleLabel: SemanticsMatcher =
	SemanticsMatcher.keyIsDefined(SemanticsProperties.ContentDescription)
		.or(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text))
		.or(SemanticsMatcher.keyIsDefined(SemanticsProperties.EditableText))
