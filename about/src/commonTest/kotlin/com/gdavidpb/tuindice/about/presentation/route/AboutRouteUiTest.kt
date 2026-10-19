package com.gdavidpb.tuindice.about.presentation.route

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.gdavidpb.tuindice.about.domain.model.AboutLinks
import com.gdavidpb.tuindice.about.domain.repository.AboutRepository
import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenExternalUrlUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SendSupportEmailUseCase
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.presentation.machine.AboutExternalUseCases
import com.gdavidpb.tuindice.about.presentation.machine.AboutMachine
import com.gdavidpb.tuindice.about.presentation.viewmodel.AboutViewModel
import com.gdavidpb.tuindice.about.testing.AboutRouteUnderTest
import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
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
class AboutRouteUiTest {
	private data class AboutRouteFixture(
		val viewModel: AboutViewModel,
		val browserRepository: RecordingBrowserRepository
	)

	@Test
	fun when_contactDeveloperActionTriggered_then_opensMailUri() = runTuIndiceUiTest {
		val fixture = createAboutViewModel()
		var openedUri = ""

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalUriHandler provides object : UriHandler {
					override fun openUri(uri: String) {
						openedUri = uri
					}
				}
			) {
				AboutRouteUnderTest(viewModel = fixture.viewModel)
			}
		}

		runOnIdle {
			fixture.viewModel.dispatchAction(About.Action.ContactDeveloper)
		}

		waitUntil(timeoutMillis = 2_000) {
			openedUri.isNotEmpty()
		}

		assertTrue(openedUri.startsWith("mailto:"))
	}

	@Test
	fun when_contactDeveloperItemTapped_then_opensMailUri() = runTuIndiceUiTest {
		val fixture = createAboutViewModel()
		var openedUri = ""

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalUriHandler provides object : UriHandler {
					override fun openUri(uri: String) {
						openedUri = uri
					}
				}
			) {
				AboutRouteUnderTest(viewModel = fixture.viewModel)
			}
		}

		waitUntilNodeExists(AboutUiTags.ContentContainer)
		onNodeWithTag(AboutUiTags.ContentContainer).performScrollToNode(hasTestTag(AboutUiTags.ContactDeveloper))
		onNodeWithTag(AboutUiTags.ContactDeveloper).performClick()

		waitUntil(timeoutMillis = 2_000) {
			openedUri.isNotEmpty()
		}

		assertTrue(openedUri.startsWith("mailto:"))
	}

	@Test
	fun when_mailUriCannotOpen_then_showsSnackBar() = runTuIndiceUiTest {
		val fixture = createAboutViewModel()
		var snackBarMessage = ""

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalUriHandler provides object : UriHandler {
					override fun openUri(uri: String) {
						error("No mail app installed")
					}
				}
			) {
				AboutRouteUnderTest(
					viewModel = fixture.viewModel,
					showSnackBar = { message -> snackBarMessage = message.message }
				)
			}
		}

		runOnIdle {
			fixture.viewModel.dispatchAction(About.Action.ContactDeveloper)
		}

		waitUntil(timeoutMillis = 2_000) {
			snackBarMessage.isNotEmpty()
		}
	}

	@Test
	fun when_openUrlActionTriggered_then_usesBrowserRepositoryWithoutRouteLevelEffects() = runTuIndiceUiTest {
		val fixture = createAboutViewModel()
		var openUriCalls = 0

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalUriHandler provides object : UriHandler {
					override fun openUri(uri: String) {
						openUriCalls++
					}
				}
			) {
				AboutRouteUnderTest(viewModel = fixture.viewModel)
			}
		}

		runOnIdle {
			fixture.viewModel.dispatchAction(About.Action.OpenUrl(AboutLinks.KOIN))
		}

		waitUntil(timeoutMillis = 2_000) {
			fixture.browserRepository.lastOpenedUrl != null
		}

		assertEquals(AboutLinks.KOIN, fixture.browserRepository.lastOpenedUrl)
		assertEquals(0, openUriCalls)
	}

	@Test
	fun when_openUrlItemsTapped_then_eachItemUsesBrowserRepositoryWithoutRouteLevelEffects() = runTuIndiceUiTest {
		val fixture = createAboutViewModel()
		var openUriCalls = 0

		val expectedLinksByTag = listOf(
			AboutUiTags.OpenCreativeCommons to AboutLinks.CREATIVE_COMMONS,
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
				LocalUriHandler provides object : UriHandler {
					override fun openUri(uri: String) {
						openUriCalls++
					}
				}
			) {
				AboutRouteUnderTest(viewModel = fixture.viewModel)
			}
		}

		waitUntilNodeExists(AboutUiTags.ContentContainer)

		expectedLinksByTag.forEach { (tag, expectedLink) ->
			onNodeWithTag(AboutUiTags.ContentContainer).performScrollToNode(hasTestTag(tag))
			waitForIdle()
			waitUntilNodeExists(tag = tag)
			onNodeWithTag(tag).performClick()

			waitUntil(timeoutMillis = 2_000) {
				fixture.browserRepository.openedUrls.value.lastOrNull() == expectedLink
			}

			assertEquals(expectedLink, fixture.browserRepository.openedUrls.value.lastOrNull())
		}

		assertEquals(expectedLinksByTag.map { (_, link) -> link }, fixture.browserRepository.openedUrls.value)
		assertEquals(0, openUriCalls)
	}

	private fun ComposeUiTest.waitUntilNodeExists(
		tag: String,
		timeoutMillis: Long = 2_000
	) {
		waitUntil(timeoutMillis = timeoutMillis) {
			onAllNodesWithTag(testTag = tag).fetchSemanticsNodes().isNotEmpty()
		}
	}

	private fun createAboutViewModel(): AboutRouteFixture {
		val browserRepository = RecordingBrowserRepository()

		return AboutRouteFixture(
			viewModel = AboutViewModel(
				screenMachine = AboutMachine(
					loadVersionUseCase = LoadVersionUseCase(
						aboutRepository = object : AboutRepository {
							override suspend fun getVersionDescription(): String = "1.2.3"
						},
						reportingRepository = RecordingReportingRepository()
					),
					externalUseCases = AboutExternalUseCases(
						sendSupportEmail = SendSupportEmailUseCase(
							configRepository = FakeConfigRepository(),
							deviceInfoRepository = FakeDeviceInfoRepository(),
							reportingRepository = RecordingReportingRepository()
						),
						openExternalUrl = OpenExternalUrlUseCase(
							browserRepository = browserRepository,
							reportingRepository = RecordingReportingRepository()
						)
					)
				),
				eventPublisher = NoOpEventPublisher
			),
			browserRepository = browserRepository
		)
	}
}
