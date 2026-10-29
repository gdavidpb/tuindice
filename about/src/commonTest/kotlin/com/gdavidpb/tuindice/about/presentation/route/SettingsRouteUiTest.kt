package com.gdavidpb.tuindice.about.presentation.route

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.gdavidpb.tuindice.about.domain.model.AboutLinks
import com.gdavidpb.tuindice.about.domain.repository.StoreUrlRepository
import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionNameUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenExternalUrlUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenStoreUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SendSupportEmailUseCase
import com.gdavidpb.tuindice.about.presentation.contract.Settings
import com.gdavidpb.tuindice.about.presentation.machine.SettingsContentUseCases
import com.gdavidpb.tuindice.about.presentation.machine.SettingsExternalUseCases
import com.gdavidpb.tuindice.about.presentation.machine.SettingsMachine
import com.gdavidpb.tuindice.about.presentation.utils.LocalShareTextHandler
import com.gdavidpb.tuindice.about.presentation.viewmodel.SettingsViewModel
import com.gdavidpb.tuindice.about.testing.FakeProfileIdentityRepository
import com.gdavidpb.tuindice.about.testing.SettingsRouteUnderTest
import com.gdavidpb.tuindice.about.testing.fakeObserveProfileIdentityUseCase
import com.gdavidpb.tuindice.about.testing.fakeSettingsAssistantUseCases
import com.gdavidpb.tuindice.about.ui.SettingsUiTags
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.data.source.usage.InMemoryUsageDataConsentRepository
import com.gdavidpb.tuindice.base.domain.model.AppEnvironment
import com.gdavidpb.tuindice.base.domain.repository.ProfileIdentityRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeAppEnvironmentRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeDeviceInfoRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingBrowserRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SettingsRouteUiTest {
	private data class SettingsRouteFixture(
		val viewModel: SettingsViewModel,
		val browserRepository: RecordingBrowserRepository
	)

	@Test
	fun when_termsActionTriggered_then_navigatesToBrowser() = runTuIndiceUiTest {
		val expectedUrl = "https://tuindice.test/terms"
		val fixture = createSettingsViewModel(termsAndConditionsUrl = expectedUrl)
		var navigatedUrl = ""

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalShareTextHandler provides { _, _ -> }
			) {
				SettingsRouteUnderTest(
					onNavigateToBrowser = { _, url ->
						navigatedUrl = url
					},
					showSnackBar = { },
					viewModel = fixture.viewModel
				)
			}
		}

		runOnIdle {
			fixture.viewModel.dispatchAction(Settings.Action.OpenTermsAndConditions)
		}

		waitUntil(timeoutMillis = 10_000) {
			navigatedUrl.isNotEmpty()
		}

		assertEquals(expectedUrl, navigatedUrl)
	}

	@Test
	fun when_termsItemTapped_then_navigatesToBrowser() = runTuIndiceUiTest {
		val expectedUrl = "https://tuindice.test/terms"
		val fixture = createSettingsViewModel(termsAndConditionsUrl = expectedUrl)
		var navigatedUrl = ""

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalShareTextHandler provides { _, _ -> }
			) {
				SettingsRouteUnderTest(
					onNavigateToBrowser = { _, url ->
						navigatedUrl = url
					},
					showSnackBar = { },
					viewModel = fixture.viewModel
				)
			}
		}

		clickRow(SettingsUiTags.OpenTerms)

		waitUntil(timeoutMillis = 2_000) {
			navigatedUrl.isNotEmpty()
		}

		assertEquals(expectedUrl, navigatedUrl)
	}

	@Test
	fun when_mailUriCannotOpen_then_showsSnackBar() = runTuIndiceUiTest {
		val fixture = createSettingsViewModel(termsAndConditionsUrl = "https://tuindice.test/terms")
		var snackBarMessage = ""

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalShareTextHandler provides { _, _ -> },
				LocalUriHandler provides object : UriHandler {
					override fun openUri(uri: String) {
						error("No mail app installed")
					}
				}
			) {
				SettingsRouteUnderTest(
					onNavigateToBrowser = { _, _ -> },
					showSnackBar = { message -> snackBarMessage = message.message },
					viewModel = fixture.viewModel
				)
			}
		}

		runOnIdle {
			fixture.viewModel.dispatchAction(Settings.Action.ReportBug)
		}

		waitUntil(timeoutMillis = 2_000) {
			snackBarMessage.isNotEmpty()
		}
	}

	@Test
	fun when_shareAppActionTriggered_then_delegatesToShareTextHandler() = runTuIndiceUiTest {
		val fixture = createSettingsViewModel(termsAndConditionsUrl = "https://tuindice.test/terms")
		var subject = ""
		var text = ""

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalShareTextHandler provides { inputSubject, inputText ->
					subject = inputSubject
					text = inputText
				}
			) {
				SettingsRouteUnderTest(
					onNavigateToBrowser = { _, _ -> },
					showSnackBar = { },
					viewModel = fixture.viewModel
				)
			}
		}

		runOnIdle {
			fixture.viewModel.dispatchAction(Settings.Action.ShareApp)
		}

		waitUntil(timeoutMillis = 2_000) {
			subject.isNotEmpty() && text.isNotEmpty()
		}

		assertTrue(subject.isNotBlank())
		assertTrue(text.isNotBlank())
	}

	@Test
	fun when_shareAppItemTapped_then_delegatesToShareTextHandler() = runTuIndiceUiTest {
		val fixture = createSettingsViewModel(termsAndConditionsUrl = "https://tuindice.test/terms")
		var subject = ""
		var text = ""

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalShareTextHandler provides { inputSubject, inputText ->
					subject = inputSubject
					text = inputText
				}
			) {
				SettingsRouteUnderTest(
					onNavigateToBrowser = { _, _ -> },
					showSnackBar = { },
					viewModel = fixture.viewModel
				)
			}
		}

		clickRow(SettingsUiTags.ShareApp)

		waitUntil(timeoutMillis = 2_000) {
			subject.isNotEmpty() && text.isNotEmpty()
		}

		assertTrue(subject.isNotBlank())
		assertTrue(text.isNotBlank())
	}

	@Test
	fun when_privacyPolicyActionTriggered_then_navigatesToBrowserPrivacyUrl() = runTuIndiceUiTest {
		val expectedPrivacyUrl = "https://tuindice.test/privacy"
		val fixture = createSettingsViewModel(termsAndConditionsUrl = "https://tuindice.test/terms")
		var navigatedUrl = ""

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalShareTextHandler provides { _, _ -> }
			) {
				SettingsRouteUnderTest(
					onNavigateToBrowser = { _, url ->
						navigatedUrl = url
					},
					showSnackBar = { },
					viewModel = fixture.viewModel
				)
			}
		}

		runOnIdle {
			fixture.viewModel.dispatchAction(Settings.Action.OpenPrivacyPolicy)
		}

		waitUntil(timeoutMillis = 2_000) {
			navigatedUrl.isNotEmpty()
		}

		assertEquals(expectedPrivacyUrl, navigatedUrl)
	}

	@Test
	fun when_privacyPolicyItemTapped_then_navigatesToBrowserPrivacyUrl() = runTuIndiceUiTest {
		val expectedPrivacyUrl = "https://tuindice.test/privacy"
		val fixture = createSettingsViewModel(termsAndConditionsUrl = "https://tuindice.test/terms")
		var navigatedUrl = ""

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalShareTextHandler provides { _, _ -> }
			) {
				SettingsRouteUnderTest(
					onNavigateToBrowser = { _, url ->
						navigatedUrl = url
					},
					showSnackBar = { },
					viewModel = fixture.viewModel
				)
			}
		}

		clickRow(SettingsUiTags.OpenPrivacy)

		waitUntil(timeoutMillis = 2_000) {
			navigatedUrl.isNotEmpty()
		}

		assertEquals(expectedPrivacyUrl, navigatedUrl)
	}

	@Test
	fun when_supportActionTriggered_then_navigatesToBrowserSupportUrl() = runTuIndiceUiTest {
		val expectedSupportUrl = "https://tuindice.test/support"
		val fixture = createSettingsViewModel(
			termsAndConditionsUrl = "https://tuindice.test/terms",
			supportUrl = expectedSupportUrl
		)
		var navigatedUrl = ""

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalShareTextHandler provides { _, _ -> }
			) {
				SettingsRouteUnderTest(
					onNavigateToBrowser = { _, url ->
						navigatedUrl = url
					},
					showSnackBar = { },
					viewModel = fixture.viewModel
				)
			}
		}

		runOnIdle {
			fixture.viewModel.dispatchAction(Settings.Action.OpenSupport)
		}

		waitUntil(timeoutMillis = 2_000) {
			navigatedUrl.isNotEmpty()
		}

		assertEquals(expectedSupportUrl, navigatedUrl)
	}

	@Test
	fun when_supportItemTapped_then_navigatesToBrowserSupportUrl() = runTuIndiceUiTest {
		val expectedSupportUrl = "https://tuindice.test/support"
		val fixture = createSettingsViewModel(
			termsAndConditionsUrl = "https://tuindice.test/terms",
			supportUrl = expectedSupportUrl
		)
		var navigatedUrl = ""

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalShareTextHandler provides { _, _ -> }
			) {
				SettingsRouteUnderTest(
					onNavigateToBrowser = { _, url ->
						navigatedUrl = url
					},
					showSnackBar = { },
					viewModel = fixture.viewModel
				)
			}
		}

		clickRow(SettingsUiTags.OpenSupport)

		waitUntil(timeoutMillis = 2_000) {
			navigatedUrl.isNotEmpty()
		}

		assertEquals(expectedSupportUrl, navigatedUrl)
	}

	@Test
	fun when_rateOnStoreActionTriggered_then_opensStoreUri() = runTuIndiceUiTest {
		val fixture = createSettingsViewModel(termsAndConditionsUrl = "https://tuindice.test/terms")
		val expectedStoreUrl = "https://store.tuindice.test"
		var openedUri = ""

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalShareTextHandler provides { _, _ -> },
				LocalUriHandler provides object : UriHandler {
					override fun openUri(uri: String) {
						openedUri = uri
					}
				}
			) {
				SettingsRouteUnderTest(
					onNavigateToBrowser = { _, _ -> },
					showSnackBar = { },
					viewModel = fixture.viewModel
				)
			}
		}

		runOnIdle {
			fixture.viewModel.dispatchAction(Settings.Action.RateOnStore)
		}

		waitUntil(timeoutMillis = 2_000) {
			openedUri.isNotEmpty()
		}

		assertEquals(expectedStoreUrl, openedUri)
	}

	@Test
	fun when_rateOnStoreItemTapped_then_opensStoreUri() = runTuIndiceUiTest {
		val fixture = createSettingsViewModel(termsAndConditionsUrl = "https://tuindice.test/terms")
		val expectedStoreUrl = "https://store.tuindice.test"
		var openedUri = ""

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalShareTextHandler provides { _, _ -> },
				LocalUriHandler provides object : UriHandler {
					override fun openUri(uri: String) {
						openedUri = uri
					}
				}
			) {
				SettingsRouteUnderTest(
					onNavigateToBrowser = { _, _ -> },
					showSnackBar = { },
					viewModel = fixture.viewModel
				)
			}
		}

		clickRow(SettingsUiTags.RateOnStore)

		waitUntil(timeoutMillis = 2_000) {
			openedUri.isNotEmpty()
		}

		assertEquals(expectedStoreUrl, openedUri)
	}

	@Test
	fun when_openUrlActionTriggered_then_usesBrowserRepositoryWithoutRouteLevelEffects() = runTuIndiceUiTest {
		val fixture = createSettingsViewModel(termsAndConditionsUrl = "https://tuindice.test/terms")
		var navigateCalls = 0
		var openUriCalls = 0
		var shareCalls = 0

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalShareTextHandler provides { _, _ ->
					shareCalls++
				},
				LocalUriHandler provides object : UriHandler {
					override fun openUri(uri: String) {
						openUriCalls++
					}
				}
			) {
				SettingsRouteUnderTest(
					onNavigateToBrowser = { _, _ ->
						navigateCalls++
					},
					showSnackBar = { },
					viewModel = fixture.viewModel
				)
			}
		}

		runOnIdle {
			fixture.viewModel.dispatchAction(Settings.Action.OpenUrl(AboutLinks.X))
		}

		waitUntil(timeoutMillis = 2_000) {
			fixture.browserRepository.lastOpenedUrl != null
		}

		assertEquals(AboutLinks.X, fixture.browserRepository.lastOpenedUrl)
		assertEquals(0, navigateCalls)
		assertEquals(0, openUriCalls)
		assertEquals(0, shareCalls)
	}

	@Test
	fun when_reportBugActionTriggered_then_opensMailUri() = runTuIndiceUiTest {
		val fixture = createSettingsViewModel(termsAndConditionsUrl = "https://tuindice.test/terms")
		var openedUri = ""

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalShareTextHandler provides { _, _ -> },
				LocalUriHandler provides object : UriHandler {
					override fun openUri(uri: String) {
						openedUri = uri
					}
				}
			) {
				SettingsRouteUnderTest(
					onNavigateToBrowser = { _, _ -> },
					showSnackBar = { },
					viewModel = fixture.viewModel
				)
			}
		}

		runOnIdle {
			fixture.viewModel.dispatchAction(Settings.Action.ReportBug)
		}

		waitUntil(timeoutMillis = 2_000) {
			openedUri.isNotEmpty()
		}

		assertTrue(openedUri.startsWith("mailto:"))
	}

	@Test
	fun when_reportBugItemTapped_then_opensMailUri() = runTuIndiceUiTest {
		val fixture = createSettingsViewModel(termsAndConditionsUrl = "https://tuindice.test/terms")
		var openedUri = ""

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalShareTextHandler provides { _, _ -> },
				LocalUriHandler provides object : UriHandler {
					override fun openUri(uri: String) {
						openedUri = uri
					}
				}
			) {
				SettingsRouteUnderTest(
					onNavigateToBrowser = { _, _ -> },
					showSnackBar = { },
					viewModel = fixture.viewModel
				)
			}
		}

		waitUntilNodeExists(SettingsUiTags.ContentContainer)
		onNodeWithTag(SettingsUiTags.ContentContainer)
			.performScrollToNode(hasTestTag(SettingsUiTags.ReportBug))
		onNodeWithTag(SettingsUiTags.ReportBug).performClick()

		waitUntil(timeoutMillis = 2_000) {
			openedUri.isNotEmpty()
		}

		assertTrue(openedUri.startsWith("mailto:"))
	}

	@Test
	fun when_xItemTapped_then_usesBrowserRepositoryWithoutRouteLevelEffects() = runTuIndiceUiTest {
		val fixture = createSettingsViewModel(termsAndConditionsUrl = "https://tuindice.test/terms")
		var navigateCalls = 0

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalShareTextHandler provides { _, _ -> }) {
				SettingsRouteUnderTest(
					onNavigateToBrowser = { _, _ -> navigateCalls++ },
					viewModel = fixture.viewModel
				)
			}
		}

		clickRow(SettingsUiTags.OpenX)

		waitUntil(timeoutMillis = 2_000) {
			fixture.browserRepository.lastOpenedUrl != null
		}

		assertEquals(AboutLinks.X, fixture.browserRepository.lastOpenedUrl)
		assertEquals(0, navigateCalls)
	}

	@Test
	fun when_aboutItemTapped_then_opensTheAboutScreen() = runTuIndiceUiTest {
		val fixture = createSettingsViewModel(termsAndConditionsUrl = "https://tuindice.test/terms")
		var aboutCalls = 0

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalShareTextHandler provides { _, _ -> }) {
				SettingsRouteUnderTest(
					onNavigateToAbout = { aboutCalls++ },
					viewModel = fixture.viewModel
				)
			}
		}

		clickRow(SettingsUiTags.OpenAbout)

		waitUntil(timeoutMillis = 2_000) { aboutCalls > 0 }

		assertEquals(1, aboutCalls)
	}

	@Test
	fun when_signOutItemTapped_then_asksTheHostForTheSignOutFlow() = runTuIndiceUiTest {
		val fixture = createSettingsViewModel(termsAndConditionsUrl = "https://tuindice.test/terms")
		var signOutRequests = 0

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalShareTextHandler provides { _, _ -> }) {
				SettingsRouteUnderTest(
					onSignOutRequest = { signOutRequests++ },
					viewModel = fixture.viewModel
				)
			}
		}

		clickRow(SettingsUiTags.SignOut)

		waitUntil(timeoutMillis = 2_000) { signOutRequests > 0 }

		assertEquals(1, signOutRequests)
	}

	@Test
	fun when_theStudentIsStored_then_theIdentityCardNamesThem() = runTuIndiceUiTest {
		val fixture = createSettingsViewModel(termsAndConditionsUrl = "https://tuindice.test/terms")

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalShareTextHandler provides { _, _ -> }) {
				SettingsRouteUnderTest(viewModel = fixture.viewModel)
			}
		}

		waitUntilNodeExists(SettingsUiTags.IdentityCard)

		onNodeWithTag(SettingsUiTags.IdentityName).assertTextEquals("Ana Diaz")
		onNodeWithTag(SettingsUiTags.IdentityCareer).assertTextEquals("Ingenieria Civil Informatica")
		onNodeWithTag(SettingsUiTags.IdentityCarnet).assertTextEquals("Carnet 20261234")
	}

	@Test
	fun when_noUserIsStored_then_theScreenHasNoIdentityCardButKeepsItsRows() = runTuIndiceUiTest {
		val fixture = createSettingsViewModel(
			termsAndConditionsUrl = "https://tuindice.test/terms",
			profileIdentityRepository = FakeProfileIdentityRepository(identity = null)
		)

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalShareTextHandler provides { _, _ -> }) {
				SettingsRouteUnderTest(viewModel = fixture.viewModel)
			}
		}

		waitUntilNodeExists(SettingsUiTags.ContentContainer)

		assertEquals(0, onAllNodesWithTag(SettingsUiTags.IdentityCard).fetchSemanticsNodes().size)
		waitUntilNodeExists(SettingsUiTags.OpenPrivacy)
	}

	// The rows below the fold are laid out but not on screen: the list scrolls to the row before it is tapped.
	private fun ComposeUiTest.clickRow(tag: String) {
		waitUntilNodeExists(SettingsUiTags.ContentContainer)
		onNodeWithTag(SettingsUiTags.ContentContainer).performScrollToNode(hasTestTag(tag))
		onNodeWithTag(tag).performClick()
	}

	private fun ComposeUiTest.waitUntilNodeExists(
		tag: String,
		timeoutMillis: Long = 2_000
	) {
		waitUntil(timeoutMillis = timeoutMillis) {
			onAllNodesWithTag(testTag = tag).fetchSemanticsNodes().isNotEmpty()
		}
	}

	private fun createSettingsViewModel(
		termsAndConditionsUrl: String,
		supportUrl: String = "https://tuindice.test/support",
		profileIdentityRepository: ProfileIdentityRepository = FakeProfileIdentityRepository()
	): SettingsRouteFixture {
		val appEnvironmentRepository = FakeAppEnvironmentRepository(
			appEnvironment = AppEnvironment(
				apiBaseUrl = "https://api.tuindice.test",
				privacyPolicyUrl = "https://tuindice.test/privacy",
				termsAndConditionsUrl = termsAndConditionsUrl,
				supportUrl = supportUrl,
				debug = true
			)
		)
		val storeUrlRepository = object : StoreUrlRepository {
			override fun getStoreUrl(): String = "https://store.tuindice.test"
		}

		val loadVersionNameUseCase = LoadVersionNameUseCase(
			deviceInfoRepository = FakeDeviceInfoRepository(versionName = "1.2.3"),
			reportingRepository = RecordingReportingRepository()
		)
		val sendSupportEmailUseCase = SendSupportEmailUseCase(
			configRepository = FakeConfigRepository(),
			deviceInfoRepository = FakeDeviceInfoRepository(),
			reportingRepository = RecordingReportingRepository()
		)
		val openStoreUseCase = OpenStoreUseCase(
			storeUrlRepository = storeUrlRepository,
			reportingRepository = RecordingReportingRepository()
		)
		val browserRepository = RecordingBrowserRepository()
		val openExternalUrlUseCase = OpenExternalUrlUseCase(
			browserRepository = browserRepository,
			reportingRepository = RecordingReportingRepository()
		)

		return SettingsRouteFixture(
			viewModel = SettingsViewModel(
				screenMachine = SettingsMachine(
					contentUseCases = SettingsContentUseCases(
						loadVersionName = loadVersionNameUseCase,
						observeProfileIdentity = fakeObserveProfileIdentityUseCase(profileIdentityRepository)
					),
					externalUseCases = SettingsExternalUseCases(
						sendSupportEmail = sendSupportEmailUseCase,
						openStore = openStoreUseCase,
						openExternalUrl = openExternalUrlUseCase
					),
					appEnvironmentRepository = appEnvironmentRepository,
					usageDataConsentRepository = InMemoryUsageDataConsentRepository(),
					assistantUseCases = fakeSettingsAssistantUseCases()
				),
				eventPublisher = NoOpEventPublisher
			),
			browserRepository = browserRepository
		)
	}
}
