package com.gdavidpb.tuindice.auth.presentation.machine

import com.gdavidpb.tuindice.auth.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.exceptionhandler.SignInExceptionHandler
import com.gdavidpb.tuindice.auth.domain.usecase.validator.SignInParamsValidator
import com.gdavidpb.tuindice.auth.presentation.contract.SignIn
import com.gdavidpb.tuindice.auth.testing.FakeAttestationRepository
import com.gdavidpb.tuindice.auth.testing.RecordingAuthRepository
import com.gdavidpb.tuindice.auth.testing.RecordingMessagingRepository
import com.gdavidpb.tuindice.base.data.source.usage.InMemoryUsageDataConsentRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeAppEnvironmentRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.mvi.assertMachineRandomWalk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * Seeded random walk for the SignIn machine. UiTest-suffixed on purpose: some rows
 * resolve composeResources strings, which the android host JVM stub cannot serve, so
 * the walk runs on the iOS simulator gate while the static validators stay on the host
 * in SignInStateMachineContractTest.
 */
class SignInStateMachineWalkUiTest {
	@Test
	fun machine_survivesSeededRandomWalk() = runTest {
		val screenMachine = SignInMachine(
			signInUseCase = SignInUseCase(
				authRepository = RecordingAuthRepository(),
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
			),
			configRepository = FakeConfigRepository(),
			appEnvironmentRepository = FakeAppEnvironmentRepository(),
			usageDataConsentRepository = InMemoryUsageDataConsentRepository()
		)

		assertMachineRandomWalk(
			screenMachine = screenMachine,
			sampleEvents = listOf(
				SignIn.Action.SetUsbId(usbId = "20-26123"),
				SignIn.Action.SetPassword(password = "secret123"),
				SignIn.Action.TogglePasswordVisibility,
				SignIn.Action.ToggleIdentifierMode,
				SignIn.Action.SetUsageDataCollectionEnabled(enabled = true),
				SignIn.Action.ClickSignIn,
				SignIn.Action.ClickTermsAndConditions,
				SignIn.Action.ClickPrivacyPolicy,
				SignInInternalEvent.SignInSucceeded,
				SignInInternalEvent.OutdatedAppDetected,
				SignInInternalEvent.SignInFailed(error = null)
			),
			coroutineScope = backgroundScope,
			// Conservative floor: every row is reachable from these samples; raise to the
			// observed coverage once the walk has run on CI.
			minRowCoverage = 0.5
		)
	}
}
