package com.gdavidpb.tuindice.presentation.route

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import com.gdavidpb.tuindice.auth.di.authModule
import com.gdavidpb.tuindice.auth.domain.model.BootstrapTokens
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.base.domain.model.Attestation
import com.gdavidpb.tuindice.base.domain.model.AttestationRequest
import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.testing.createSummaryViewModel
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeAppEnvironmentRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionInvalidationRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeUpdateRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingBrowserRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReviewRepository
import com.gdavidpb.tuindice.testing.FakeDeviceInfoRepository
import com.gdavidpb.tuindice.testing.createMainViewModel
import com.gdavidpb.tuindice.ui.MaincoreUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertTrue
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@OptIn(ExperimentalTestApi::class)
class TuIndiceAppHostRouteUiTest {
	@Test
	fun when_hostRouteStarts_then_rendersNavHostAndTriggersReviewRequest() = runTuIndiceUiTest {
		val reviewRepository = RecordingReviewRepository()
		val syncStatusRepository = FakeSyncStatusRepository()

		stopKoin()
		startKoin {
			modules(hostRouteNavigationModule(syncStatusRepository))
		}

		try {
			setTuIndiceTestContent {
				TuIndiceAppHostRoute(
					onConfirmExitClick = {},
					isSwipeBackNavigationEnabled = false,
					browserRepository = RecordingBrowserRepository(),
					deviceInfoRepository = FakeDeviceInfoRepository(hasCamera = false),
					sessionInvalidationRepository = FakeSessionInvalidationRepository(),
					syncStatusRepository = syncStatusRepository,
					reviewRepository = reviewRepository,
					updateRepository = FakeUpdateRepository(),
					viewModel = createMainViewModel()
				)
			}

			waitUntil(timeoutMillis = 2_000) {
				reviewRepository.launchCalls > 0
			}

			assertNodeVisible(MaincoreUiTags.TuIndiceNavHost)
			assertTrue(reviewRepository.launchCalls > 0)
		} finally {
			stopKoin()
		}
	}

	@Test
	fun when_updateIsAvailable_then_hostRouteLaunchesUpdateFlow() = runTuIndiceUiTest {
		val updateRepository = FakeUpdateRepository(updateAction = UpdateAction.Immediate)
		val viewModel = createMainViewModel(updateRepository = updateRepository)
		val syncStatusRepository = FakeSyncStatusRepository()

		stopKoin()
		startKoin {
			modules(hostRouteNavigationModule(syncStatusRepository))
		}

		try {
			setTuIndiceTestContent {
				TuIndiceAppHostRoute(
					onConfirmExitClick = {},
					isSwipeBackNavigationEnabled = false,
					browserRepository = RecordingBrowserRepository(),
					deviceInfoRepository = FakeDeviceInfoRepository(hasCamera = false),
					sessionInvalidationRepository = FakeSessionInvalidationRepository(),
					syncStatusRepository = syncStatusRepository,
					reviewRepository = RecordingReviewRepository(),
					updateRepository = updateRepository,
					viewModel = viewModel
				)
			}

			waitUntil(timeoutMillis = 2_000) {
				updateRepository.launchedActions.isNotEmpty()
			}

			assertTrue(updateRepository.checkCalls.isNotEmpty())
			assertTrue(updateRepository.launchedActions.contains(UpdateAction.Immediate))
		} finally {
			stopKoin()
		}
	}

	@Test
	fun when_syncStatusIsOutdatedCredentials_then_hostRouteNavigatesToUpdatePasswordDialog() = runTuIndiceUiTest {
		val syncStatusRepository = FakeSyncStatusRepository(initialValue = SyncStatus.OutdatedCredentials)
		val sessionInvalidationRepository = FakeSessionInvalidationRepository()

		stopKoin()

		startKoin {
			modules(
				hostRouteNavigationModule(syncStatusRepository),
				authModule,
				module {
					single<AuthRepository> {
						object : AuthRepository {
							override suspend fun bootstrapSignIn(
								usbId: String,
								password: String
							): BootstrapTokens = BootstrapTokens(
								uid = "uid",
								usbId = usbId,
								accessToken = "bootstrap-token",
								expiresIn = 300
							)

							override suspend fun exchangeSignIn(
								bootstrapAccessToken: String,
								attestation: Attestation
							) = Unit

							override suspend fun reissueTokens(
								usbId: String,
								password: String,
								attestation: Attestation
							) = Unit

							override suspend fun refreshTokens(
								accessToken: String,
								refreshToken: String,
								attestation: Attestation
							): RefreshTokens = error("refreshTokens should not be called in this test")

							override suspend fun revokeTokens(accessToken: String) = Unit
						}
					}
					single<SessionInvalidationRepository> { sessionInvalidationRepository }
					single<SessionRepository> { FakeSessionRepository() }
					single<SyncRepository> { FakeSyncRepository() }
					single<MessagingRepository> {
						object : MessagingRepository {
							override suspend fun subscribe() = Unit

							override suspend fun unsubscribe() = Unit
						}
					}
					single<ConfigRepository> { FakeConfigRepository() }
					single<AppEnvironmentRepository> { FakeAppEnvironmentRepository() }
					single<CredentialsRepository> { FakeCredentialsRepository() }
					single<AttestationRepository> {
						object : AttestationRepository {
							override suspend fun attest(request: AttestationRequest): Attestation {
								return Attestation(token = "token")
							}
						}
					}
					single<NetworkRepository> { FakeNetworkRepository(isAvailable = true) }
					single<ReportingRepository> { RecordingReportingRepository() }
				}
			)
		}

		try {
			setTuIndiceTestContent {
				TuIndiceAppHostRoute(
					onConfirmExitClick = {},
					isSwipeBackNavigationEnabled = false,
					browserRepository = RecordingBrowserRepository(),
					deviceInfoRepository = FakeDeviceInfoRepository(hasCamera = false),
					sessionInvalidationRepository = sessionInvalidationRepository,
					syncStatusRepository = syncStatusRepository,
					reviewRepository = RecordingReviewRepository(),
					updateRepository = FakeUpdateRepository(),
					viewModel = createMainViewModel(
						settingsRepository = FakeSettingsRepository(
							reviewSuggested = true,
							lastMainSection = MainSection.SUMMARY
						)
					)
				)
			}

			waitUntil(timeoutMillis = 2_000) {
				onAllNodesWithTag(AuthUiTags.PasswordTextField).fetchSemanticsNodes().isNotEmpty()
			}

			assertNodeVisible(AuthUiTags.PasswordTextField)
		} finally {
			stopKoin()
		}
	}

	@Test
	fun when_sessionIsInvalidated_then_hostRouteNavigatesToSignIn() = runTuIndiceUiTest {
		val syncStatusRepository = FakeSyncStatusRepository()
		val sessionInvalidationRepository = FakeSessionInvalidationRepository()

		stopKoin()

		startKoin {
			modules(
				hostRouteNavigationModule(syncStatusRepository),
				authModule,
				module {
					single<AuthRepository> {
						object : AuthRepository {
							override suspend fun bootstrapSignIn(
								usbId: String,
								password: String
							): BootstrapTokens = BootstrapTokens(
								uid = "uid",
								usbId = usbId,
								accessToken = "bootstrap-token",
								expiresIn = 300
							)

							override suspend fun exchangeSignIn(
								bootstrapAccessToken: String,
								attestation: Attestation
							) = Unit

							override suspend fun reissueTokens(
								usbId: String,
								password: String,
								attestation: Attestation
							) = Unit

							override suspend fun refreshTokens(
								accessToken: String,
								refreshToken: String,
								attestation: Attestation
							): RefreshTokens = error("refreshTokens should not be called in this test")

							override suspend fun revokeTokens(accessToken: String) = Unit
						}
					}
					single<SessionInvalidationRepository> { sessionInvalidationRepository }
					single<SessionRepository> { FakeSessionRepository() }
					single<SyncRepository> { FakeSyncRepository() }
					single<MessagingRepository> {
						object : MessagingRepository {
							override suspend fun subscribe() = Unit

							override suspend fun unsubscribe() = Unit
						}
					}
					single<ConfigRepository> { FakeConfigRepository() }
					single<AppEnvironmentRepository> { FakeAppEnvironmentRepository() }
					single<CredentialsRepository> { FakeCredentialsRepository() }
					single<AttestationRepository> {
						object : AttestationRepository {
							override suspend fun attest(request: AttestationRequest): Attestation {
								return Attestation(token = "token")
							}
						}
					}
					single<NetworkRepository> { FakeNetworkRepository(isAvailable = true) }
					single<ReportingRepository> { RecordingReportingRepository() }
				}
			)
		}

		try {
			setTuIndiceTestContent {
				TuIndiceAppHostRoute(
					onConfirmExitClick = {},
					isSwipeBackNavigationEnabled = false,
					browserRepository = RecordingBrowserRepository(),
					deviceInfoRepository = FakeDeviceInfoRepository(hasCamera = false),
					sessionInvalidationRepository = sessionInvalidationRepository,
					syncStatusRepository = syncStatusRepository,
					reviewRepository = RecordingReviewRepository(),
					updateRepository = FakeUpdateRepository(),
					viewModel = createMainViewModel(
						settingsRepository = FakeSettingsRepository(
							reviewSuggested = true,
							lastMainSection = MainSection.SUMMARY
						)
					)
				)
			}

			runOnIdle {
				sessionInvalidationRepository.notifySessionInvalidated()
			}

			waitUntil(timeoutMillis = 2_000) {
				onAllNodesWithTag(AuthUiTags.PasswordTextField).fetchSemanticsNodes().isNotEmpty()
			}

			assertNodeVisible(AuthUiTags.PasswordTextField)
		} finally {
			stopKoin()
		}
	}

	private fun hostRouteNavigationModule(
		syncStatusRepository: SyncStatusRepository
	) = module {
		factory { createSummaryViewModel() }
		single<SyncStatusRepository> { syncStatusRepository }
	}
}
