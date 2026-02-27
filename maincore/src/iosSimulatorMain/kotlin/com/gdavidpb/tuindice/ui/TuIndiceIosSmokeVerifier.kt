package com.gdavidpb.tuindice.ui

import com.gdavidpb.tuindice.about.presentation.navigation.AboutDestination
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.presentation.viewmodel.AboutViewModel
import com.gdavidpb.tuindice.base.domain.model.AttestationProvider
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.DeviceInfoRepository
import com.gdavidpb.tuindice.base.domain.repository.FileRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SecureStoreRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.di.IosPlatformBridge
import com.gdavidpb.tuindice.di.createIosUserAgent
import com.gdavidpb.tuindice.di.requireIosKoin
import com.gdavidpb.tuindice.domain.usecase.GetUpdateInfoUseCase
import com.gdavidpb.tuindice.domain.usecase.RequestReviewUseCase
import com.gdavidpb.tuindice.domain.usecase.SetLastDestinationUseCase
import com.gdavidpb.tuindice.domain.usecase.StartUpUseCase
import com.gdavidpb.tuindice.domain.usecase.result.StartUpResult
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.FetchEnrollmentProofUseCase
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment
import com.gdavidpb.tuindice.enrollmentproof.presentation.viewmodel.EnrollmentProofViewModel
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationViewModel
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationsViewModel
import com.gdavidpb.tuindice.login.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.login.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.login.domain.usecase.param.SignInParams
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import com.gdavidpb.tuindice.login.presentation.viewmodel.SignInViewModel
import com.gdavidpb.tuindice.login.presentation.viewmodel.SignOutViewModel
import com.gdavidpb.tuindice.login.presentation.viewmodel.UpdatePasswordViewModel
import com.gdavidpb.tuindice.presentation.contract.Browser
import com.gdavidpb.tuindice.presentation.viewmodel.BrowserViewModel
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.record.domain.usecase.GetQuartersUseCase
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
import com.gdavidpb.tuindice.summary.domain.usecase.GetUserUseCase
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv

class TuIndiceIosSmokeVerifier {
	fun runChecks(): String {
		return runCatching {
			runBlocking {
				val koin = requireIosKoin()
				val checks = mutableListOf<String>()
				val appEnvironment = koin.get<AppEnvironmentRepository>().getEnvironment()
				val networkStatusGateway = koin.get<NetworkRepository>()
				val deviceInfoGateway = koin.get<DeviceInfoRepository>()
				val fileGateway = koin.get<FileRepository>()
				val secureStore = koin.get<SecureStoreRepository>()
				val iosPlatformBridge = koin.get<IosPlatformBridge>()
				val configGateway = koin.get<ConfigRepository>()
				val reportingGateway = koin.get<ReportingRepository>()

				// App environment contract is resolved and exposes non-empty runtime endpoints.
				check(appEnvironment.apiBaseUrl.isNotBlank()) {
					"Smoke: appEnvironment.apiBaseUrl must not be blank."
				}
				check(appEnvironment.privacyPolicyUrl.isNotBlank()) {
					"Smoke: appEnvironment.privacyPolicyUrl must not be blank."
				}
				check(appEnvironment.termsAndConditionsUrl.isNotBlank()) {
					"Smoke: appEnvironment.termsAndConditionsUrl must not be blank."
				}
				checks += "appenv:ok"
				val isDebugVariant = appEnvironment.debug
				checks += if (isDebugVariant) "variant:debug" else "variant:production"

				// Device/network shared gateways are callable from iOS host runtime.
				val networkStatus = if (networkStatusGateway.isAvailable()) "online" else "offline"
				checks += "network:$networkStatus"

				check(deviceInfoGateway.appVersionName().isNotBlank()) {
					"Smoke: deviceInfo.appVersionName must not be blank."
				}
				check(deviceInfoGateway.appVersionCode() >= 0L) {
					"Smoke: deviceInfo.appVersionCode must be >= 0."
				}
				deviceInfoGateway.hasCamera()
				checks += "device:ok"

				// Shared iOS user-agent contract must stay aligned with backend parser.
				val userAgent = createIosUserAgent(iosPlatformBridge)
				val userAgentSegments = userAgent.split(";")
				check(userAgentSegments.size == 9) {
					"Smoke: iOS User-Agent must contain 9 segments."
				}
				check(userAgentSegments[0] == "TuIndice") {
					"Smoke: iOS User-Agent app name mismatch."
				}
				check(userAgentSegments[3] == "iOS") {
					"Smoke: iOS User-Agent OS segment mismatch."
				}
				check(userAgentSegments[7] == "Apple") {
					"Smoke: iOS User-Agent manufacturer segment mismatch."
				}
				checks += "user-agent:ok"

				// File gateway must create a platform file reference and be queryable.
				val smokeFileRef = fileGateway.createTemporaryFile(nameHint = "ios-smoke.tmp")
				check(smokeFileRef.value.isNotBlank()) {
					"Smoke: FileRepository returned an empty PlatformFileRef."
				}
				fileGateway.canOpen(smokeFileRef)
				checks += "file-gateway:ok"

				// Secure store bridge should support basic write/read/contains roundtrip.
				val smokeSecureStoreKey = "ios_smoke_secure_store_key"
				val smokeSecureStoreValue = "ok"
				secureStore.putString(smokeSecureStoreKey, smokeSecureStoreValue)
				check(secureStore.contains(smokeSecureStoreKey)) {
					"Smoke: SecureStoreRepository did not contain the key after write."
				}
				check(secureStore.getString(smokeSecureStoreKey) == smokeSecureStoreValue) {
					"Smoke: SecureStoreRepository roundtrip value mismatch."
				}
				checks += "secure-store:ok"

				// Push + attestation bridge contracts are available on iOS runtime.
				val pushToken = iosPlatformBridge.pushToken()
				check(!pushToken.isNullOrBlank()) {
					"Smoke: push token unavailable on iOS bridge."
				}
				val nonBlankPushToken = pushToken
				checks += "push:ok"
				if (!isDebugVariant) {
					check(!nonBlankPushToken.startsWith(IOS_DEBUG_TOKEN_PREFIX)) {
						"Smoke: production build must not use synthetic debug push token."
					}
					checks += "push:real"
				}

				val platformAttestation = iosPlatformBridge.requestAttestation("ios-smoke-attestation-input")
					?: error("Smoke: attestation token unavailable on iOS bridge.")
				check(platformAttestation.token.isNotBlank()) {
					"Smoke: attestation token must not be blank."
				}
				check(platformAttestation.provider == AttestationProvider.APP_ATTEST) {
					"Smoke: unexpected iOS attestation provider ${platformAttestation.provider}."
				}
				check(!platformAttestation.keyId.isNullOrBlank()) {
					"Smoke: attestation keyId unavailable for iOS APP_ATTEST."
				}
				checks += "attestation:APP_ATTEST"
				checks += "attestation-key-id:ok"
				if (!isDebugVariant) {
					check(!platformAttestation.token.startsWith(IOS_DEBUG_TOKEN_PREFIX)) {
						"Smoke: production build must not use synthetic debug attestation token."
					}
					check(!platformAttestation.keyId.startsWith(IOS_DEBUG_TOKEN_PREFIX)) {
						"Smoke: production build must not use synthetic debug attestation key id."
					}
					checks += "attestation:real"
				}

				// Startup flow executes and returns destination.
				val startupStates = koin.get<StartUpUseCase>().execute(Unit).toList()
				val startupData = startupStates
					.firstOrNull { state -> state is UseCaseState.Data<*, *> }
					?.let { state -> state as UseCaseState.Data<StartUpResult, *> }
					?.value
					?: error("Smoke: StartUpUseCase did not emit data.")
				checks += "startup:${startupData.startDestination::class.simpleName}"

				// Settings destination write/read is functional.
				koin.get<SetLastDestinationUseCase>().execute(AboutDestination.About).toList()
				val restoredDestination = koin.get<SettingsRepository>().getLastDestination()
				check(restoredDestination == AboutDestination.About) {
					"Smoke: destination roundtrip failed. Got=${restoredDestination::class.simpleName}"
				}
				checks += "destination:ok"

				// Review/update use cases execute without unexpected failure.
				val reviewStates = koin.get<RequestReviewUseCase>().execute(Unit).toList()
				val updateStates = koin.get<GetUpdateInfoUseCase>().execute(Unit).toList()
				check(reviewStates.isNotEmpty()) { "Smoke: RequestReviewUseCase emitted no states." }
				check(updateStates.isNotEmpty()) { "Smoke: GetUpdateInfoUseCase emitted no states." }
				checks += "review:${reviewStates.size}"
				checks += "update:${updateStates.size}"

				// Config gateway contract is wired and returns expected defaults/runtime values.
				configGateway.tryFetch()
				check(configGateway.getTimeout() > 0L) { "Smoke: Config timeout must be > 0." }
				check(configGateway.getLoadingMessages().isNotEmpty()) {
					"Smoke: Config loading messages must not be empty."
				}
				check(configGateway.getContactEmail().isNotBlank()) {
					"Smoke: Config contact email must not be blank."
				}
				check(configGateway.getContactSubject().isNotBlank()) {
					"Smoke: Config contact subject must not be blank."
				}
				checks += "config:ok"

				// Reporting gateway must be callable from shared runtime on iOS host.
				reportingGateway.setIdentifier("ios-smoke-user")
				reportingGateway.setCustomKey("ios_smoke_check", "ok")
				reportingGateway.logMessage("iOS smoke reporting check")
				reportingGateway.logException(IllegalStateException("iOS smoke reporting check"))
				checks += "reporting:ok"

				// Browser view model actions update state and emit effect.
				val browserViewModel = koin.get<BrowserViewModel>()
				withViewModelCollectors(browserViewModel.state, browserViewModel.effect) {
					val browserUrl = "https://tuindice.app/smoke/browser"
					browserViewModel.navigateToAction(title = "Smoke Browser", url = browserUrl)
					waitFor("Smoke: Browser navigate action did not update content state.") {
						val state = browserViewModel.state.value
						state is Browser.State.Content &&
							state.topBarTitle == "Smoke Browser" &&
							state.url == browserUrl &&
							state.isLoading
					}

					browserViewModel.hideLoadingAction()
					waitFor("Smoke: Browser loading flag did not update.") {
						val state = browserViewModel.state.value
						state is Browser.State.Content && !state.isLoading
					}

					val externalUrl = "https://tuindice.app/smoke/external"
					val externalResourceEffect = expectEffect(browserViewModel.effect) { effect ->
						effect is Browser.Effect.NavigateToExternalResourceDialog &&
							effect.url == externalUrl
					}
					browserViewModel.openExternalResourceAction(externalUrl)
					externalResourceEffect.await()
				}
				checks += "browser-flow:ok"

				// Sign in view model local state and browser effects are functional.
				val signInViewModel = koin.get<SignInViewModel>()
				withViewModelCollectors(signInViewModel.state, signInViewModel.effect) {
					signInViewModel.setUsbIdAction(usbId = "SMOKE-USB")
					signInViewModel.setPasswordAction(password = "SMOKE-PASSWORD")
					waitFor("Smoke: SignIn state did not reflect edited credentials.") {
						val state = signInViewModel.state.value
						state is SignIn.State.Idle &&
							state.usbId == "SMOKE-USB" &&
							state.password == "SMOKE-PASSWORD"
					}

					val termsEffect = expectEffect(signInViewModel.effect) { effect ->
						effect is SignIn.Effect.NavigateToBrowser &&
							effect.url == appEnvironment.termsAndConditionsUrl
					}
					signInViewModel.openTermsAndConditionsAction()
					termsEffect.await()

					val privacyEffect = expectEffect(signInViewModel.effect) { effect ->
						effect is SignIn.Effect.NavigateToBrowser &&
							effect.url == appEnvironment.privacyPolicyUrl
					}
					signInViewModel.openPrivacyPolicyAction()
					privacyEffect.await()
				}
				checks += "signin-flow:ok"

				// About actions keep browser navigation effects and execute external actions.
				val aboutViewModel = koin.get<AboutViewModel>()
				withViewModelCollectors(aboutViewModel.state, aboutViewModel.effect) {
					waitFor("Smoke: About version was not loaded.") {
						aboutViewModel.state.value is About.State.Content
					}

					val termsEffect = expectEffect(aboutViewModel.effect) { effect ->
						effect is About.Effect.NavigateToBrowser &&
							effect.url == appEnvironment.termsAndConditionsUrl
					}
					aboutViewModel.openTermsAndConditionsAction()
					termsEffect.await()

					val privacyEffect = expectEffect(aboutViewModel.effect) { effect ->
						effect is About.Effect.NavigateToBrowser &&
							effect.url == appEnvironment.privacyPolicyUrl
					}
					aboutViewModel.openPrivacyPolicyAction()
					privacyEffect.await()

					aboutViewModel.shareAppAction()
					aboutViewModel.reportBugAction()
					aboutViewModel.contactDeveloperAction()
					aboutViewModel.rateOnPlayStoreAction()
					aboutViewModel.openUrlAction("https://tuindice.app/smoke/about")
				}
				checks += "about-flow:ok"

				// Summary profile picture UI actions emit expected effects.
				val summaryViewModel = koin.get<SummaryViewModel>()
				withViewModelCollectors(summaryViewModel.state, summaryViewModel.effect) {
					val pickEffect = expectEffect(summaryViewModel.effect) { effect ->
						effect is Summary.Effect.OpenPicker
					}
					summaryViewModel.pickProfilePictureAction()
					pickEffect.await()

					val settingsEffect = expectEffect(summaryViewModel.effect) { effect ->
						effect is Summary.Effect.NavigateToProfilePictureSettingsDialog
					}
					summaryViewModel.openProfilePictureSettingsAction()
					settingsEffect.await()
				}
				checks += "summary-flow:ok"

				// Record flow validates deterministic invalid-grade feedback path.
				val recordViewModel = koin.get<RecordViewModel>()
				withViewModelCollectors(recordViewModel.state, recordViewModel.effect) {
					val invalidGradeEffect = expectEffect(recordViewModel.effect) { effect ->
						effect is Record.Effect.ShowSnackBar && effect.message.isNotBlank()
					}
					recordViewModel.updateSubjectAction(
						quarterId = "smoke-quarter",
						subjectId = "smoke-subject",
						grade = -1,
						commit = false
					)
					invalidGradeEffect.await()
				}
				checks += "record-flow:ok"

				// Enrollment flow is wired and starts from fetching state.
				val enrollmentProofViewModel = koin.get<EnrollmentProofViewModel>()
				check(enrollmentProofViewModel.state.value is Enrollment.State.Fetching) {
					"Smoke: EnrollmentProofViewModel initial state must be Fetching."
				}
				checks += "enrollment-flow:ok"

				// Evaluations list/editor deterministic navigation and picker effects.
				val evaluationsViewModel = koin.get<EvaluationsViewModel>()
				withViewModelCollectors(evaluationsViewModel.state, evaluationsViewModel.effect) {
					val addEffect = expectEffect(evaluationsViewModel.effect) { effect ->
						effect is Evaluations.Effect.NavigateToAddEvaluation
					}
					evaluationsViewModel.addEvaluationAction()
					addEffect.await()

					val evaluationId = "smoke-evaluation-id"
					val editEffect = expectEffect(evaluationsViewModel.effect) { effect ->
						effect is Evaluations.Effect.NavigateToEvaluation &&
							effect.evaluationId == evaluationId
					}
					evaluationsViewModel.editEvaluationAction(evaluationId)
					editEffect.await()
				}
				checks += "evaluations-flow:ok"

				val evaluationViewModel = koin.get<EvaluationViewModel>()
				withViewModelCollectors(evaluationViewModel.state, evaluationViewModel.effect) {
					val gradeEffect = expectEffect(evaluationViewModel.effect) { effect ->
						effect is Evaluation.Effect.NavigateToGradePickerDialog &&
							effect.grade == 4.0 &&
							effect.maxGrade == 5.0
					}
					evaluationViewModel.clickGradeAction(grade = 4.0, maxGrade = 5.0)
					gradeEffect.await()

					val maxGradeEffect = expectEffect(evaluationViewModel.effect) { effect ->
						effect is Evaluation.Effect.NavigateToMaxGradePickerDialog &&
							effect.maxGrade == 5.0
					}
					evaluationViewModel.clickMaxGradeAction(maxGrade = 5.0)
					maxGradeEffect.await()
				}
				checks += "evaluation-flow:ok"

				// Cross-feature use cases are wired and emit loading state without platform crash.
				check(koin.get<GetUserUseCase>().execute(Unit).first() is UseCaseState.Loading<*, *>) {
					"Smoke: GetUserUseCase did not emit loading first."
				}
				check(koin.get<GetQuartersUseCase>().execute(Unit).first() is UseCaseState.Loading<*, *>) {
					"Smoke: GetQuartersUseCase did not emit loading first."
				}
				check(
					koin.get<GetEvaluationsUseCase>()
						.execute(params = flowOf(emptyList<EvaluationFilter>()))
						.first() is UseCaseState.Loading<*, *>
				) {
					"Smoke: GetEvaluationsUseCase did not emit loading first."
				}
				check(
					koin.get<FetchEnrollmentProofUseCase>()
						.execute(Unit)
						.first() is UseCaseState.Loading<*, *>
				) {
					"Smoke: FetchEnrollmentProofUseCase did not emit loading first."
				}
				checks += "feature-usecases:loading"

				// Resolve all shared view models from Koin (required by shared nav on iOS).
				koin.get<MainViewModel>()
				koin.get<BrowserViewModel>()
				koin.get<SignInViewModel>()
				koin.get<SignOutViewModel>()
				koin.get<UpdatePasswordViewModel>()
				koin.get<AboutViewModel>()
				koin.get<SummaryViewModel>()
				koin.get<RecordViewModel>()
				koin.get<EvaluationsViewModel>()
				koin.get<EvaluationViewModel>()
				koin.get<EnrollmentProofViewModel>()
				checks += "viewmodels:ok"

				// Optional authenticated smoke for real-device evidence (credentials injected by script).
				val smokeUsbId = readEnvironmentValue(name = SMOKE_USBID_ENV_KEY)
				val smokePassword = readEnvironmentValue(name = SMOKE_PASSWORD_ENV_KEY)
				if (!smokeUsbId.isNullOrBlank() || !smokePassword.isNullOrBlank()) {
					check(!smokeUsbId.isNullOrBlank() && !smokePassword.isNullOrBlank()) {
						"Smoke: both $SMOKE_USBID_ENV_KEY and $SMOKE_PASSWORD_ENV_KEY are required when enabling authenticated smoke."
					}

					val signInStates = koin.get<SignInUseCase>()
						.execute(SignInParams(usbId = smokeUsbId, password = smokePassword))
						.toList()

					check(signInStates.any { state -> state is UseCaseState.Data<*, *> }) {
						"Smoke: authenticated sign-in did not complete successfully. States=${signInStates.describeForSmoke()}"
					}
					checks += "signin-auth:ok"

					val signOutStates = koin.get<SignOutUseCase>()
						.execute(Unit)
						.toList()

					check(signOutStates.any { state -> state is UseCaseState.Data<*, *> }) {
						"Smoke: authenticated sign-out did not complete successfully. States=${signOutStates.describeForSmoke()}"
					}
					checks += "signout-auth:ok"
				}

				"PASS:${checks.joinToString(separator = ",")}"
			}
		}.getOrElse { throwable ->
			val reason = throwable.message ?: throwable::class.simpleName ?: "unknown"
			"FAIL:$reason"
		}
	}

	private suspend fun <S, E> withViewModelCollectors(
		state: StateFlow<S>,
		effect: Flow<E>,
		block: suspend CoroutineScope.() -> Unit
	) {
		coroutineScope {
			val effectCollector = launch(start = CoroutineStart.UNDISPATCHED) {
				effect.collect { }
			}
			val stateCollector = launch(start = CoroutineStart.UNDISPATCHED) {
				state.collect { }
			}

			try {
				block()
			} finally {
				stateCollector.cancelAndJoin()
				effectCollector.cancelAndJoin()
			}
		}
	}

	private suspend fun waitFor(message: String, condition: () -> Boolean) {
		try {
			withTimeout(WAIT_TIMEOUT_MS) {
				while (!condition()) {
					delay(POLL_DELAY_MS)
				}
			}
		} catch (_: TimeoutCancellationException) {
			error(message)
		}
	}

	private fun <E> CoroutineScope.expectEffect(
		effect: Flow<E>,
		predicate: (E) -> Boolean
	) = async(start = CoroutineStart.UNDISPATCHED) {
		withTimeout(WAIT_TIMEOUT_MS) {
			effect.first { value -> predicate(value) }
		}
	}

	private fun List<UseCaseState<*, *>>.describeForSmoke(): String {
		return joinToString(separator = ",") { state ->
			when (state) {
				is UseCaseState.Loading<*, *> -> "loading"
				is UseCaseState.Data<*, *> -> "data"
				is UseCaseState.Error<*, *> -> "error:${state.error?.toString() ?: "unknown"}"
			}
		}
	}

	@OptIn(ExperimentalForeignApi::class)
	private fun readEnvironmentValue(name: String): String? {
		val pointer = getenv(name) ?: return null
		return pointer.toKString().takeIf { value -> value.isNotBlank() }
	}

	private companion object {
		const val WAIT_TIMEOUT_MS = 2500L
		const val POLL_DELAY_MS = 20L
		const val IOS_DEBUG_TOKEN_PREFIX = "ios-debug-"
		const val SMOKE_USBID_ENV_KEY = "TUINDICE_SMOKE_USBID"
		const val SMOKE_PASSWORD_ENV_KEY = "TUINDICE_SMOKE_PASSWORD"
	}
}
