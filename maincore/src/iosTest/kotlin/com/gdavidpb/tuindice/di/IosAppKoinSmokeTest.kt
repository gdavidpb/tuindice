package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.domain.model.AppEnvironment
import com.gdavidpb.tuindice.base.domain.model.AttestationProvider
import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.presentation.viewmodel.BrowserViewModel
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.testkit.koin.assertResolves
import com.gdavidpb.tuindice.testkit.koin.withStartedKoin
import kotlin.test.Test

class IosAppKoinSmokeTest {
	@Test
	fun resolvesIosAppEntryPoints() = withStartedKoin(
		start = {
			startAppKoin(
				AppKoinBootstrapRequest(
					platformBootstrap = IosKoinBootstrap(
						iOSContext = IOSContext(
							hostCapabilities = IosHostCapabilities(
								remoteConfig = object : IosRemoteConfigCapability {
									override fun remoteConfigString(key: String): String? = null
								},
								attestation = object : IosAttestationCapability {
									override suspend fun requestAttestation(attestationInput: String): IosPlatformAttestation? {
										return IosPlatformAttestation(
											token = "token",
											keyId = "key",
											provider = AttestationProvider.APP_ATTEST
										)
									}
								},
								push = object : IosPushCapability {
									override suspend fun pushToken(): String? = "push-token"
								},
								review = object : IosReviewCapability {
									override suspend fun launchReview() = Unit
								},
								update = object : IosUpdateCapability {
									override suspend fun checkForUpdate(stalenessDays: Int): UpdateAction? = null
									override suspend fun launchUpdate(action: UpdateAction) = Unit
								},
								externalActions = object : IosExternalActionsCapability {
									override fun openUrl(url: String) = Unit
									override fun openFile(path: String): Boolean = true
									override fun canOpen(path: String): Boolean = true
								},
								device = object : IosDeviceCapability {
									override fun appVersionName(): String = "1.0"
									override fun appVersionCode(): Long = 1L
									override fun hasCamera(): Boolean = true
									override fun isNetworkAvailable(): Boolean = true
								},
								observability = object : IosObservabilityCapability {
									override fun setUserIdentifier(identifier: String) = Unit
									override fun logMessage(message: String) = Unit
									override fun logException(throwable: Throwable) = Unit
									override fun setCustomKey(key: String, value: String) = Unit
								}
							),
							appEnvironment = AppEnvironment(
								apiBaseUrl = "http://localhost:8080/",
								privacyPolicyUrl = "https://tuindice.app/privacy_policy.html",
								termsAndConditionsUrl = "https://tuindice.app/terms_and_conditions.html",
								debug = true
							),
							configValues = iosDefaultConfigValues(IosBuildVariant.DEBUG)
						),
						platformVariantModules = emptyList()
					)
				)
			)
		}
	) {
		assertResolves(
			MainViewModel::class,
			BrowserViewModel::class
		)
	}
}
