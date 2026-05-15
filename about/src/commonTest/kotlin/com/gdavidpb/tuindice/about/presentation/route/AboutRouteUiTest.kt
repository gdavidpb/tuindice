package com.gdavidpb.tuindice.about.presentation.route

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.gdavidpb.tuindice.about.domain.repository.StoreUrlRepository
import com.gdavidpb.tuindice.about.domain.model.AboutLinks
import com.gdavidpb.tuindice.about.domain.repository.AboutRepository
import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenExternalUrlUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenStoreUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SendSupportEmailUseCase
import com.gdavidpb.tuindice.about.presentation.action.ContactDeveloperActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.LoadVersionActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.OpenPrivacyPolicyActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.OpenTermsAndConditionsActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.OpenUrlActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.RateOnStoreActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.ReportBugActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.ShareAppActionProcessor
import com.gdavidpb.tuindice.about.presentation.utils.LocalShareTextHandler
import com.gdavidpb.tuindice.about.presentation.viewmodel.AboutViewModel
import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.base.domain.model.AppEnvironment
import com.gdavidpb.tuindice.testkit.base.repository.FakeAppEnvironmentRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingBrowserRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AboutRouteUiTest {
	private data class AboutRouteFixture(
		val viewModel: AboutViewModel,
		val browserRepository: RecordingBrowserRepository
	)

	@Test
	fun when_termsActionTriggered_then_navigatesToBrowser() = runTuIndiceUiTest {
		val expectedUrl = "https://tuindice.test/terms"
		val fixture = createAboutViewModel(termsAndConditionsUrl = expectedUrl)
		var navigatedUrl = ""

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalShareTextHandler provides { _, _ -> }
			) {
				AboutRoute(
					onNavigateToBrowser = { _, url ->
						navigatedUrl = url
					},
					viewModel = fixture.viewModel
				)
			}
		}

		runOnIdle {
			fixture.viewModel.openTermsAndConditionsAction()
		}

		waitUntil(timeoutMillis = 2_000) {
			navigatedUrl.isNotEmpty()
		}

		assertEquals(expectedUrl, navigatedUrl)
	}

	@Test
	fun when_termsItemTapped_then_navigatesToBrowser() = runTuIndiceUiTest {
		val expectedUrl = "https://tuindice.test/terms"
		val fixture = createAboutViewModel(termsAndConditionsUrl = expectedUrl)
		var navigatedUrl = ""

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalShareTextHandler provides { _, _ -> }
			) {
				AboutRoute(
					onNavigateToBrowser = { _, url ->
						navigatedUrl = url
					},
					viewModel = fixture.viewModel
				)
			}
		}

		waitUntilNodeExists(AboutUiTags.OpenTerms)
		onNodeWithTag(AboutUiTags.OpenTerms).performClick()

		waitUntil(timeoutMillis = 2_000) {
			navigatedUrl.isNotEmpty()
		}

		assertEquals(expectedUrl, navigatedUrl)
	}

	@Test
	fun when_contactDeveloperActionTriggered_then_opensMailUri() = runTuIndiceUiTest {
		val fixture = createAboutViewModel(termsAndConditionsUrl = "https://tuindice.test/terms")
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
				AboutRoute(
					onNavigateToBrowser = { _, _ -> },
					viewModel = fixture.viewModel
				)
			}
		}

		runOnIdle {
			fixture.viewModel.contactDeveloperAction()
		}

		waitUntil(timeoutMillis = 2_000) {
			openedUri.isNotEmpty()
		}

		assertTrue(openedUri.startsWith("mailto:"))
	}

	@Test
	fun when_shareAppActionTriggered_then_delegatesToShareTextHandler() = runTuIndiceUiTest {
		val fixture = createAboutViewModel(termsAndConditionsUrl = "https://tuindice.test/terms")
		var subject = ""
		var text = ""

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalShareTextHandler provides { inputSubject, inputText ->
					subject = inputSubject
					text = inputText
				}
			) {
				AboutRoute(
					onNavigateToBrowser = { _, _ -> },
					viewModel = fixture.viewModel
				)
			}
		}

		runOnIdle {
			fixture.viewModel.shareAppAction()
		}

		waitUntil(timeoutMillis = 2_000) {
			subject.isNotEmpty() && text.isNotEmpty()
		}

		assertTrue(subject.isNotBlank())
		assertTrue(text.isNotBlank())
	}

	@Test
	fun when_shareAppItemTapped_then_delegatesToShareTextHandler() = runTuIndiceUiTest {
		val fixture = createAboutViewModel(termsAndConditionsUrl = "https://tuindice.test/terms")
		var subject = ""
		var text = ""

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalShareTextHandler provides { inputSubject, inputText ->
					subject = inputSubject
					text = inputText
				}
			) {
				AboutRoute(
					onNavigateToBrowser = { _, _ -> },
					viewModel = fixture.viewModel
				)
			}
		}

		waitUntilNodeExists(AboutUiTags.ShareApp)
		onNodeWithTag(AboutUiTags.ShareApp).performClick()

		waitUntil(timeoutMillis = 2_000) {
			subject.isNotEmpty() && text.isNotEmpty()
		}

		assertTrue(subject.isNotBlank())
		assertTrue(text.isNotBlank())
	}

	@Test
	fun when_privacyPolicyActionTriggered_then_navigatesToBrowserPrivacyUrl() = runTuIndiceUiTest {
		val expectedPrivacyUrl = "https://tuindice.test/privacy"
		val fixture = createAboutViewModel(termsAndConditionsUrl = "https://tuindice.test/terms")
		var navigatedUrl = ""

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalShareTextHandler provides { _, _ -> }
			) {
				AboutRoute(
					onNavigateToBrowser = { _, url ->
						navigatedUrl = url
					},
					viewModel = fixture.viewModel
				)
			}
		}

		runOnIdle {
			fixture.viewModel.openPrivacyPolicyAction()
		}

		waitUntil(timeoutMillis = 2_000) {
			navigatedUrl.isNotEmpty()
		}

		assertEquals(expectedPrivacyUrl, navigatedUrl)
	}

	@Test
	fun when_privacyPolicyItemTapped_then_navigatesToBrowserPrivacyUrl() = runTuIndiceUiTest {
		val expectedPrivacyUrl = "https://tuindice.test/privacy"
		val fixture = createAboutViewModel(termsAndConditionsUrl = "https://tuindice.test/terms")
		var navigatedUrl = ""

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalShareTextHandler provides { _, _ -> }
			) {
				AboutRoute(
					onNavigateToBrowser = { _, url ->
						navigatedUrl = url
					},
					viewModel = fixture.viewModel
				)
			}
		}

		waitUntilNodeExists(AboutUiTags.OpenPrivacy)
		onNodeWithTag(AboutUiTags.OpenPrivacy).performClick()

		waitUntil(timeoutMillis = 2_000) {
			navigatedUrl.isNotEmpty()
		}

		assertEquals(expectedPrivacyUrl, navigatedUrl)
	}

	@Test
	fun when_rateOnStoreActionTriggered_then_opensStoreUri() = runTuIndiceUiTest {
		val fixture = createAboutViewModel(termsAndConditionsUrl = "https://tuindice.test/terms")
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
				AboutRoute(
					onNavigateToBrowser = { _, _ -> },
					viewModel = fixture.viewModel
				)
			}
		}

		runOnIdle {
			fixture.viewModel.rateOnPlayStoreAction()
		}

		waitUntil(timeoutMillis = 2_000) {
			openedUri.isNotEmpty()
		}

		assertEquals(expectedStoreUrl, openedUri)
	}

	@Test
	fun when_rateOnStoreItemTapped_then_opensStoreUri() = runTuIndiceUiTest {
		val fixture = createAboutViewModel(termsAndConditionsUrl = "https://tuindice.test/terms")
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
				AboutRoute(
					onNavigateToBrowser = { _, _ -> },
					viewModel = fixture.viewModel
				)
			}
		}

		waitUntilNodeExists(AboutUiTags.RateOnStore)
		onNodeWithTag(AboutUiTags.RateOnStore).performClick()

		waitUntil(timeoutMillis = 2_000) {
			openedUri.isNotEmpty()
		}

		assertEquals(expectedStoreUrl, openedUri)
	}

	@Test
	fun when_openUrlActionTriggered_then_usesBrowserRepositoryWithoutRouteLevelEffects() = runTuIndiceUiTest {
		val fixture = createAboutViewModel(termsAndConditionsUrl = "https://tuindice.test/terms")
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
				AboutRoute(
					onNavigateToBrowser = { _, _ ->
						navigateCalls++
					},
					viewModel = fixture.viewModel
				)
			}
		}

		runOnIdle {
			fixture.viewModel.openUrlAction(AboutLinks.KOIN)
		}

		waitUntil(timeoutMillis = 2_000) {
			fixture.browserRepository.lastOpenedUrl != null
		}

		assertEquals(AboutLinks.KOIN, fixture.browserRepository.lastOpenedUrl)
		assertEquals(0, navigateCalls)
		assertEquals(0, openUriCalls)
		assertEquals(0, shareCalls)
	}

	@Test
	fun when_openKoinItemTapped_then_usesBrowserRepositoryWithoutRouteLevelEffects() = runTuIndiceUiTest {
		val fixture = createAboutViewModel(termsAndConditionsUrl = "https://tuindice.test/terms")
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
				AboutRoute(
					onNavigateToBrowser = { _, _ ->
						navigateCalls++
					},
					viewModel = fixture.viewModel
				)
			}
		}

		waitUntilNodeExists(AboutUiTags.ContentContainer)
		onNodeWithTag(AboutUiTags.ContentContainer)
			.performScrollToNode(hasTestTag(AboutUiTags.OpenKoin))
		onNodeWithTag(AboutUiTags.OpenKoin).performClick()

		waitUntil(timeoutMillis = 2_000) {
			fixture.browserRepository.lastOpenedUrl != null
		}

		assertEquals(AboutLinks.KOIN, fixture.browserRepository.lastOpenedUrl)
		assertEquals(0, navigateCalls)
		assertEquals(0, openUriCalls)
		assertEquals(0, shareCalls)
	}

	@Test
	fun when_contactDeveloperItemTapped_then_opensMailUri() = runTuIndiceUiTest {
		val fixture = createAboutViewModel(termsAndConditionsUrl = "https://tuindice.test/terms")
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
				AboutRoute(
					onNavigateToBrowser = { _, _ -> },
					viewModel = fixture.viewModel
				)
			}
		}

		waitUntilNodeExists(AboutUiTags.ContentContainer)
		onNodeWithTag(AboutUiTags.ContentContainer)
			.performScrollToNode(hasTestTag(AboutUiTags.ContactDeveloper))
		onNodeWithTag(AboutUiTags.ContactDeveloper).performClick()

		waitUntil(timeoutMillis = 2_000) {
			openedUri.isNotEmpty()
		}

		assertTrue(openedUri.startsWith("mailto:"))
	}

	@Test
	fun when_reportBugActionTriggered_then_opensMailUri() = runTuIndiceUiTest {
		val fixture = createAboutViewModel(termsAndConditionsUrl = "https://tuindice.test/terms")
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
				AboutRoute(
					onNavigateToBrowser = { _, _ -> },
					viewModel = fixture.viewModel
				)
			}
		}

		runOnIdle {
			fixture.viewModel.reportBugAction()
		}

		waitUntil(timeoutMillis = 2_000) {
			openedUri.isNotEmpty()
		}

		assertTrue(openedUri.startsWith("mailto:"))
	}

	@Test
	fun when_reportBugItemTapped_then_opensMailUri() = runTuIndiceUiTest {
		val fixture = createAboutViewModel(termsAndConditionsUrl = "https://tuindice.test/terms")
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
				AboutRoute(
					onNavigateToBrowser = { _, _ -> },
					viewModel = fixture.viewModel
				)
			}
		}

		waitUntilNodeExists(AboutUiTags.ContentContainer)
		onNodeWithTag(AboutUiTags.ContentContainer)
			.performScrollToNode(hasTestTag(AboutUiTags.ReportBug))
		onNodeWithTag(AboutUiTags.ReportBug).performClick()

		waitUntil(timeoutMillis = 2_000) {
			openedUri.isNotEmpty()
		}

		assertTrue(openedUri.startsWith("mailto:"))
	}

	@Test
	fun when_openUrlItemsTapped_then_eachItemUsesBrowserRepositoryWithoutRouteLevelEffects() = runTuIndiceUiTest {
		val fixture = createAboutViewModel(termsAndConditionsUrl = "https://tuindice.test/terms")
		var navigateCalls = 0
		var openUriCalls = 0
		var shareCalls = 0

		val expectedLinksByTag = listOf(
			AboutUiTags.OpenCreativeCommons to AboutLinks.CREATIVE_COMMONS,
			AboutUiTags.OpenX to AboutLinks.X,
			AboutUiTags.OpenGithub to AboutLinks.GITHUB,
			AboutUiTags.OpenKotlin to AboutLinks.KOTLIN,
			AboutUiTags.OpenCompose to AboutLinks.COMPOSE,
			AboutUiTags.OpenFirebase to AboutLinks.FIREBASE,
			AboutUiTags.OpenKoin to AboutLinks.KOIN,
			AboutUiTags.OpenKtor to AboutLinks.KTOR,
			AboutUiTags.OpenDst to AboutLinks.DST
		)

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
				AboutRoute(
					onNavigateToBrowser = { _, _ ->
						navigateCalls++
					},
					viewModel = fixture.viewModel
				)
			}
		}

		expectedLinksByTag.forEach { (tag, expectedLink) ->
			onNodeWithTag(AboutUiTags.ContentContainer)
				.performScrollToNode(hasTestTag(tag))
			waitUntilNodeExists(tag = tag)
			onNodeWithTag(tag).performClick()

			waitUntil(timeoutMillis = 2_000) {
				fixture.browserRepository.lastOpenedUrl == expectedLink
			}

			assertEquals(expectedLink, fixture.browserRepository.lastOpenedUrl)
		}

		assertEquals(0, navigateCalls)
		assertEquals(0, openUriCalls)
		assertEquals(0, shareCalls)
	}

	private fun ComposeUiTest.waitUntilNodeExists(
		tag: String,
		timeoutMillis: Long = 2_000
	) {
		waitUntil(timeoutMillis = timeoutMillis) {
			onAllNodesWithTag(testTag = tag).fetchSemanticsNodes().isNotEmpty()
		}
	}

	private fun createAboutViewModel(
		termsAndConditionsUrl: String
	): AboutRouteFixture {
		val appEnvironmentRepository = FakeAppEnvironmentRepository(
			appEnvironment = AppEnvironment(
				apiBaseUrl = "https://api.tuindice.test",
				privacyPolicyUrl = "https://tuindice.test/privacy",
				termsAndConditionsUrl = termsAndConditionsUrl,
				debug = true
			)
		)
		val aboutRepository = object : AboutRepository {
			override suspend fun getVersionDescription(): String = "1.2.3"
		}
		val storeUrlRepository = object : StoreUrlRepository {
			override fun getStoreUrl(): String = "https://store.tuindice.test"
		}

		val loadVersionUseCase = LoadVersionUseCase(
			aboutRepository = aboutRepository,
			reportingRepository = RecordingReportingRepository()
		)
		val sendSupportEmailUseCase = SendSupportEmailUseCase(
			configRepository = FakeConfigRepository(),
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

		return AboutRouteFixture(
			viewModel = AboutViewModel(
				loadVersionActionProcessor = LoadVersionActionProcessor(loadVersionUseCase),
				contactDeveloperActionProcessor = ContactDeveloperActionProcessor(sendSupportEmailUseCase),
				openTermsAndConditionsActionProcessor = OpenTermsAndConditionsActionProcessor(
					appEnvironmentRepository = appEnvironmentRepository
				),
				openPrivacyPolicyActionProcessor = OpenPrivacyPolicyActionProcessor(
					appEnvironmentRepository = appEnvironmentRepository
				),
				shareAppActionProcessor = ShareAppActionProcessor(),
				rateOnStoreActionProcessor = RateOnStoreActionProcessor(openStoreUseCase),
				reportBugActionProcessor = ReportBugActionProcessor(sendSupportEmailUseCase),
				openUrlActionProcessor = OpenUrlActionProcessor(openExternalUrlUseCase)
			),
			browserRepository = browserRepository
		)
	}
}
