package com.gdavidpb.tuindice.presentation.route

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.gdavidpb.tuindice.auth.di.authModule
import com.gdavidpb.tuindice.auth.domain.model.BootstrapTokens
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.data.source.usage.InMemoryUsageDataConsentRepository
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.model.AppAvailabilityNotice
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState
import com.gdavidpb.tuindice.base.domain.model.PendingChanges
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.base.domain.model.UpdateLaunchResult
import com.gdavidpb.tuindice.base.domain.model.event.AppEvent
import com.gdavidpb.tuindice.base.domain.model.event.EventNames
import com.gdavidpb.tuindice.base.domain.model.event.EventParameterKeys
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.PendingChangesRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.base.domain.repository.UpdateRepository
import com.gdavidpb.tuindice.base.domain.repository.UsageDataConsentRepository
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.data.source.network.OutdatedAppEventDataSource
import com.gdavidpb.tuindice.domain.repository.OutdatedAppEventRepository
import com.gdavidpb.tuindice.pensum.presentation.model.PensumTopBarActionBus
import com.gdavidpb.tuindice.presentation.navigation.NavEntryStoresViewModel
import com.gdavidpb.tuindice.security.domain.model.Attestation
import com.gdavidpb.tuindice.security.domain.model.AttestationRequest
import com.gdavidpb.tuindice.security.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testing.FakeDeviceInfoRepository
import com.gdavidpb.tuindice.testing.createMainViewModel
import com.gdavidpb.tuindice.testing.createSummaryViewModel
import com.gdavidpb.tuindice.testkit.base.repository.FakeAppEnvironmentRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakePendingChangesRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionInvalidationRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeUpdateRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingApplicationRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingBrowserRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReviewRepository
import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import com.gdavidpb.tuindice.ui.MaincoreUiTags
import com.gdavidpb.tuindice.wizard.domain.usecase.MarkCoachmarkSeenUseCase
import com.gdavidpb.tuindice.wizard.domain.usecase.ResolveCoachmarkUseCase
import com.gdavidpb.tuindice.wizard.presentation.machine.CoachmarkOverlayMachine
import com.gdavidpb.tuindice.wizard.presentation.model.contextualCoachmarks
import com.gdavidpb.tuindice.wizard.presentation.model.persistedId
import com.gdavidpb.tuindice.wizard.presentation.viewmodel.CoachmarkOverlayViewModel
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
			waitUntil(timeoutMillis = 2_000) {
				onAllNodesWithTag(MaincoreUiTags.TuIndiceNavHost)
					.fetchSemanticsNodes()
					.isNotEmpty()
			}

			assertNodeVisible(MaincoreUiTags.TuIndiceNavHost)
			assertTrue(reviewRepository.launchCalls > 0)
		} finally {
			stopKoin()
		}
	}

	@Test
	fun when_appAvailabilityNoticeIsEnabled_then_hostRouteBlocksAppSideEffects() = runTuIndiceUiTest {
		val reviewRepository = RecordingReviewRepository()
		val updateRepository = FakeUpdateRepository(updateAction = UpdateAction.Immediate)
		val syncRepository = FakeSyncRepository()
		val syncStatusRepository = FakeSyncStatusRepository()

		stopKoin()
		startKoin {
			modules(hostRouteNavigationModule(syncStatusRepository))
		}

		try {
			setTuIndiceTestContent {
				TuIndiceAppHostRoute(
					onConfirmExitClick = {},
					browserRepository = RecordingBrowserRepository(),
					deviceInfoRepository = FakeDeviceInfoRepository(hasCamera = false),
					sessionInvalidationRepository = FakeSessionInvalidationRepository(),
					syncStatusRepository = syncStatusRepository,
					reviewRepository = reviewRepository,
					updateRepository = updateRepository,
					viewModel = createMainViewModel(
						configRepository = FakeConfigRepository(
							appAvailabilityNotice = AppAvailabilityNotice(
								enabled = true,
								title = "Servicio pausado",
								message = "Estamos en mantenimiento."
							)
						),
						credentialsRepository = FakeCredentialsRepository(password = "secret123"),
						syncRepository = syncRepository
					)
				)
			}

			waitUntil(timeoutMillis = 2_000) {
				onAllNodesWithTag(MaincoreUiTags.AppAvailabilityNoticeScreen)
					.fetchSemanticsNodes()
					.isNotEmpty()
			}

			assertNodeVisible(MaincoreUiTags.AppAvailabilityNoticeScreen)
			assertNodeHidden(MaincoreUiTags.TuIndiceNavHost)
			assertEquals(0, reviewRepository.launchCalls)
			assertEquals(emptyList(), updateRepository.checkCalls)
			assertEquals(emptyList(), updateRepository.launchedActions)
			assertEquals(emptyList(), syncRepository.scheduledSyncCalls)
		} finally {
			stopKoin()
		}
	}

	@Test
	fun when_signInReceivesUpgradeRequired_then_hostRouteShowsGlobalOutdatedGate() = runTuIndiceUiTest {
		val settingsRepository = FakeSettingsRepository(
			reviewSuggested = true,
			lastMainSection = MainSection.SUMMARY
		)
		val sessionRepository = FakeSessionRepository(
			sessionId = "",
			usbId = "",
			accessToken = "",
			refreshToken = ""
		)
		val syncStatusRepository = FakeSyncStatusRepository()
		val eventPublisher = RecordingEventPublisher()
		val outdatedAppEventRepository = OutdatedAppEventDataSource()

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
							): BootstrapTokens {
								val outdatedAppState =
									OutdatedAppState(minimumVersionCode = 52)
								settingsRepository.setOutdatedAppState(outdatedAppState)
								outdatedAppEventRepository.notifyOutdatedApp(outdatedAppState)

								throw clientRequestException(
									statusCode = HttpStatusCode.UpgradeRequired,
									path = "/auth/v1/token"
								)
							}

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
								sessionId: String,
								refreshToken: String,
								attestation: Attestation
							): RefreshTokens = error("refreshTokens should not be called in this test")

							override suspend fun revokeTokens(
								sessionId: String,
								refreshToken: String,
								attestation: Attestation
							) = Unit
						}
					}
					single<SessionRepository> { sessionRepository }
					single<ApplicationRepository> { RecordingApplicationRepository() }
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
					browserRepository = RecordingBrowserRepository(),
					deviceInfoRepository = FakeDeviceInfoRepository(hasCamera = false),
					sessionInvalidationRepository = FakeSessionInvalidationRepository(),
					syncStatusRepository = syncStatusRepository,
					reviewRepository = RecordingReviewRepository(),
					updateRepository = FakeUpdateRepository(),
					outdatedAppEventRepository = outdatedAppEventRepository,
					viewModel = createMainViewModel(
						sessionRepository = sessionRepository,
						settingsRepository = settingsRepository,
						deviceInfoRepository = FakeDeviceInfoRepository(versionCode = 1),
						eventPublisher = eventPublisher
					)
				)
			}

			waitUntil(timeoutMillis = 5_000) {
				onAllNodesWithTag(AuthUiTags.PasswordTextField).fetchSemanticsNodes().isNotEmpty()
			}

			val eventCountBeforeSignIn = eventPublisher.events.size
			onNodeWithTag(AuthUiTags.UsbIdTextField).performTextInput("1234567")
			onNodeWithTag(AuthUiTags.PasswordTextField).performTextInput("version-vieja")
			onNodeWithTag(AuthUiTags.SignInButton).performClick()

			waitUntil(timeoutMillis = 5_000) {
				onAllNodesWithTag(BaseUiTags.OutdatedAppScreen).fetchSemanticsNodes().isNotEmpty()
			}

			assertNodeVisible(BaseUiTags.OutdatedAppScreen)
			assertNodeHidden(MaincoreUiTags.TuIndiceNavHost)
			assertNodeHidden(AuthUiTags.AnimatedPatternBackground)

			val gateEvents = eventPublisher.events.drop(eventCountBeforeSignIn)
			assertTrue(
				gateEvents.any { event ->
					event.isMainTransition(event = "show_outdated_app", to = "outdated_app")
				},
				"Expected sign-in 426 to show the global outdated screen directly."
			)
			assertTrue(
				gateEvents.none { event ->
					event.isMainTransition(to = "starting")
				},
				"Sign-in 426 should not transition main back to loading."
			)
		} finally {
			stopKoin()
		}
	}

	@Test
	fun when_hostRouteResumes_withStoredPassword_then_requestsSync() = runTuIndiceUiTest {
		val syncRepository = FakeSyncRepository()
		val syncStatusRepository = FakeSyncStatusRepository()

		stopKoin()
		startKoin {
			modules(hostRouteNavigationModule(syncStatusRepository))
		}

		try {
			setTuIndiceTestContent {
				TuIndiceAppHostRoute(
					onConfirmExitClick = {},
					browserRepository = RecordingBrowserRepository(),
					deviceInfoRepository = FakeDeviceInfoRepository(hasCamera = false),
					sessionInvalidationRepository = FakeSessionInvalidationRepository(),
					syncStatusRepository = syncStatusRepository,
					reviewRepository = RecordingReviewRepository(),
					updateRepository = FakeUpdateRepository(),
					viewModel = createMainViewModel(
						credentialsRepository = FakeCredentialsRepository(password = "secret123"),
						syncRepository = syncRepository
					)
				)
			}

			waitUntil(timeoutMillis = 2_000) {
				syncRepository.scheduledSyncCalls.isNotEmpty()
			}

			assertEquals(listOf("secret123"), syncRepository.scheduledSyncCalls)
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
	fun when_updateLaunchRequestsStoreFallback_then_hostRouteOpensStoreUrl() = runTuIndiceUiTest {
		val fallbackResult = UpdateLaunchResult.OpenStoreFallback(
			primaryUrl = "market://details?id=com.gdavidpb.tuindice",
			fallbackUrl = "https://play.google.com/store/apps/details?id=com.gdavidpb.tuindice"
		)
		val updateRepository = FakeUpdateRepository(
			updateAction = UpdateAction.Immediate,
			launchResult = fallbackResult
		)
		val browserRepository = RecordingBrowserRepository()
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
					browserRepository = browserRepository,
					deviceInfoRepository = FakeDeviceInfoRepository(hasCamera = false),
					sessionInvalidationRepository = FakeSessionInvalidationRepository(),
					syncStatusRepository = syncStatusRepository,
					reviewRepository = RecordingReviewRepository(),
					updateRepository = updateRepository,
					viewModel = viewModel
				)
			}

			waitUntil(timeoutMillis = 2_000) {
				browserRepository.openedUrls.value.isNotEmpty()
			}

			assertEquals(listOf(fallbackResult.primaryUrl), browserRepository.openedUrls.value)
		} finally {
			stopKoin()
		}
	}

	@Test
	fun when_signOutTapped_withResolvedPendingChanges_then_dialogShowsPendingMessage() = runTuIndiceUiTest {
		val pendingChanges = PendingChanges(
			totalCount = 4,
			recordCount = 4,
			evaluationsCount = 0,
			hasFailedMutations = true
		)
		val syncStatusRepository = FakeSyncStatusRepository()

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
								sessionId: String,
								refreshToken: String,
								attestation: Attestation
							): RefreshTokens = error("refreshTokens should not be called in this test")

							override suspend fun revokeTokens(
								sessionId: String,
								refreshToken: String,
								attestation: Attestation
							) = Unit
						}
					}
					single<SessionRepository> { FakeSessionRepository() }
					single<ApplicationRepository> { RecordingApplicationRepository() }
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
					browserRepository = RecordingBrowserRepository(),
					deviceInfoRepository = FakeDeviceInfoRepository(hasCamera = false),
					pendingChangesRepository = FakePendingChangesRepository(pendingChanges = pendingChanges),
					sessionInvalidationRepository = FakeSessionInvalidationRepository(),
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

			waitUntil(timeoutMillis = 5_000) {
				onAllNodesWithTag(
					BaseUiTags.topBarActionButton(TopBarAction.SignOutAction),
					useUnmergedTree = true
				).fetchSemanticsNodes().isNotEmpty()
			}

			onNodeWithTag(
				BaseUiTags.topBarActionButton(TopBarAction.SignOutAction),
				useUnmergedTree = true
			).performClick()

			waitUntil(timeoutMillis = 5_000) {
				onAllNodesWithTag(AuthUiTags.SignOutMessageText).fetchSemanticsNodes().isNotEmpty()
			}

			onNodeWithText("Tienes 4 cambios pendientes", substring = true)
				.assertExists()
		} finally {
			stopKoin()
		}
	}

	@Test
	fun when_signOutPendingChangesLookupFails_then_showsSnackBarWithoutOpeningDialog() = runTuIndiceUiTest {
		val syncStatusRepository = FakeSyncStatusRepository()
		val pendingChangesRepository = FakePendingChangesRepository(
			getPendingChangesThrowable = IllegalStateException("boom")
		)

		stopKoin()
		startKoin {
			modules(hostRouteNavigationModule(syncStatusRepository))
		}

		try {
			setTuIndiceTestContent {
				TuIndiceAppHostRoute(
					onConfirmExitClick = {},
					browserRepository = RecordingBrowserRepository(),
					deviceInfoRepository = FakeDeviceInfoRepository(hasCamera = false),
					pendingChangesRepository = pendingChangesRepository,
					sessionInvalidationRepository = FakeSessionInvalidationRepository(),
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

			waitUntil(timeoutMillis = 5_000) {
				onAllNodesWithTag(
					BaseUiTags.topBarActionButton(TopBarAction.SignOutAction),
					useUnmergedTree = true
				).fetchSemanticsNodes().isNotEmpty()
			}

			onNodeWithTag(
				BaseUiTags.topBarActionButton(TopBarAction.SignOutAction),
				useUnmergedTree = true
			).performClick()

			waitUntil(timeoutMillis = 5_000) {
				onAllNodesWithTag(AuthUiTags.SignOutMessageText).fetchSemanticsNodes().isEmpty()
			}

			waitUntil(timeoutMillis = 5_000) {
				onAllNodesWithText(
					"No pudimos revisar tus cambios pendientes",
					substring = true
				).fetchSemanticsNodes().isNotEmpty()
			}

			onNodeWithText(
				"No pudimos revisar tus cambios pendientes",
				substring = true
			).assertExists()
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
				hostRouteNavigationModule(
					syncStatusRepository = syncStatusRepository,
					sessionInvalidationRepository = sessionInvalidationRepository
				),
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
								sessionId: String,
								refreshToken: String,
								attestation: Attestation
							): RefreshTokens = error("refreshTokens should not be called in this test")

							override suspend fun revokeTokens(
								sessionId: String,
								refreshToken: String,
								attestation: Attestation
							) = Unit
						}
					}
					single<SessionRepository> { FakeSessionRepository() }
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

			waitUntil(timeoutMillis = 5_000) {
				onAllNodesWithTag(AuthUiTags.PasswordTextField).fetchSemanticsNodes().isNotEmpty()
			}

			assertNodeVisible(AuthUiTags.PasswordTextField)
			onNodeWithTag(BaseUiTags.ConfirmationDialogNegativeButton).performClick()

			waitUntil(timeoutMillis = 5_000) {
				onAllNodesWithTag(AuthUiTags.UpdatePasswordIdleContainer)
					.fetchSemanticsNodes()
					.isEmpty()
			}

			assertNodeHidden(AuthUiTags.UpdatePasswordIdleContainer)
		} finally {
			stopKoin()
		}
	}

	@Test
	fun when_syncStatusBecomesOutdatedCredentialsAfterSignIn_then_hostRouteNavigatesToUpdatePasswordDialog() =
		runTuIndiceUiTest {
			val sessionRepository = FakeSessionRepository(
				sessionId = "",
				usbId = "",
				accessToken = "",
				refreshToken = ""
			)
			val syncStatusRepository = FakeSyncStatusRepository()
			val sessionInvalidationRepository = FakeSessionInvalidationRepository()

			stopKoin()

			startKoin {
				modules(
					hostRouteNavigationModule(
						syncStatusRepository = syncStatusRepository,
						sessionInvalidationRepository = sessionInvalidationRepository
					),
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
									sessionId: String,
									refreshToken: String,
									attestation: Attestation
								): RefreshTokens = error("refreshTokens should not be called in this test")

								override suspend fun revokeTokens(
									sessionId: String,
									refreshToken: String,
									attestation: Attestation
								) = Unit
							}
						}
						single<SessionRepository> { sessionRepository }
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
							browserRepository = RecordingBrowserRepository(),
						deviceInfoRepository = FakeDeviceInfoRepository(hasCamera = false),
						sessionInvalidationRepository = sessionInvalidationRepository,
						syncStatusRepository = syncStatusRepository,
						reviewRepository = RecordingReviewRepository(),
						updateRepository = FakeUpdateRepository(),
						viewModel = createMainViewModel(
							sessionRepository = sessionRepository,
							settingsRepository = FakeSettingsRepository(
								reviewSuggested = true,
								lastMainSection = MainSection.SUMMARY
							)
						)
					)
				}

				waitUntil(timeoutMillis = 5_000) {
					onAllNodesWithTag(AuthUiTags.PasswordTextField).fetchSemanticsNodes().isNotEmpty()
				}

				onNodeWithTag(AuthUiTags.UsbIdTextField).performTextInput("1234567")
				onNodeWithTag(AuthUiTags.PasswordTextField).performTextInput("123456")
				onNodeWithTag(AuthUiTags.SignInButton).performClick()

				runOnIdle {
					syncStatusRepository.emitSyncStatus(SyncStatus.OutdatedCredentials)
				}

				waitUntil(timeoutMillis = 5_000) {
					onAllNodesWithTag(SummaryUiTags.ContentContainer).fetchSemanticsNodes().isNotEmpty()
				}

				waitUntil(timeoutMillis = 5_000) {
					onAllNodesWithTag(AuthUiTags.UpdatePasswordIdleContainer)
						.fetchSemanticsNodes()
						.isNotEmpty()
				}

				assertNodeVisible(AuthUiTags.UpdatePasswordIdleContainer)
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
				hostRouteNavigationModule(
					syncStatusRepository = syncStatusRepository,
					sessionInvalidationRepository = sessionInvalidationRepository
				),
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
								sessionId: String,
								refreshToken: String,
								attestation: Attestation
							): RefreshTokens = error("refreshTokens should not be called in this test")

							override suspend fun revokeTokens(
								sessionId: String,
								refreshToken: String,
								attestation: Attestation
							) = Unit
						}
					}
					single<SessionRepository> { FakeSessionRepository() }
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

			waitUntil(timeoutMillis = 5_000) {
				onAllNodesWithTag(
					BaseUiTags.topBarActionButton(TopBarAction.SignOutAction),
					useUnmergedTree = true
				).fetchSemanticsNodes().isNotEmpty()
			}

			runOnIdle {
				sessionInvalidationRepository.notifySessionInvalidated()
			}

			waitUntil(timeoutMillis = 5_000) {
				onAllNodesWithTag(AuthUiTags.PasswordTextField).fetchSemanticsNodes().isNotEmpty()
			}

			assertNodeVisible(AuthUiTags.PasswordTextField)
			waitUntil(timeoutMillis = 5_000) {
				onAllNodesWithText("Inicia sesión nuevamente").fetchSemanticsNodes().isNotEmpty()
			}
			onNodeWithText("Inicia sesión nuevamente").assertExists()
		} finally {
			stopKoin()
		}
	}

	private fun hostRouteNavigationModule(
		syncStatusRepository: SyncStatusRepository,
		sessionInvalidationRepository: SessionInvalidationRepository = FakeSessionInvalidationRepository()
	) = module {
		factory { createSummaryViewModel() }
		factory { NavEntryStoresViewModel() }
		single<TuIndiceDispatchers> { DefaultTuIndiceDispatchers }
		single { testSessionCoroutineScope() }
		single<EventPublisher> { NoOpEventPublisher }
		single<OutdatedAppEventRepository> { OutdatedAppEventDataSource() }
		single<PendingChangesRepository> { FakePendingChangesRepository() }
		single<SessionInvalidationRepository> { sessionInvalidationRepository }
		single<SyncRepository> { FakeSyncRepository() }
		single<SyncStatusRepository> { syncStatusRepository }
		single<UpdateRepository> { FakeUpdateRepository() }
		single<UsageDataConsentRepository> { InMemoryUsageDataConsentRepository() }
		factory { createTestCoachmarkOverlayViewModel() }
		single { PensumTopBarActionBus() }
	}
}

private fun createTestCoachmarkOverlayViewModel(): CoachmarkOverlayViewModel {
	val settingsRepository = FakeSettingsRepository(
		seenCoachmarkIds = contextualCoachmarks()
			.map { coachmark -> coachmark.id.persistedId }
			.toMutableSet()
	)
	val reportingRepository = RecordingReportingRepository()

	return CoachmarkOverlayViewModel(
		screenMachine = CoachmarkOverlayMachine(
			resolveCoachmarkUseCase = ResolveCoachmarkUseCase(
				settingsRepository = settingsRepository,
				sessionRepository = FakeSessionRepository(),
				reportingRepository = reportingRepository
			),
			markCoachmarkSeenUseCase = MarkCoachmarkSeenUseCase(
				settingsRepository = settingsRepository,
				reportingRepository = reportingRepository
			)
		),
		eventPublisher = NoOpEventPublisher
	)
}

private class RecordingEventPublisher : EventPublisher {
	private val eventsFlow = MutableStateFlow<List<AppEvent>>(emptyList())
	val events: List<AppEvent>
		get() = eventsFlow.value

	override fun publish(event: AppEvent) {
		eventsFlow.value += event
	}
}

private fun AppEvent.isMainTransition(
	event: String? = null,
	to: String
): Boolean {
	return name == EventNames.APP_TRANSITION &&
			parameters[EventParameterKeys.SOURCE] == "main" &&
			(event == null || parameters[EventParameterKeys.EVENT] == event) &&
			parameters[EventParameterKeys.TO] == to
}
