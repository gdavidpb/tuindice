package com.gdavidpb.tuindice.about.presentation.route

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionUseCase
import com.gdavidpb.tuindice.about.domain.usecase.ObserveAssistantAvailabilityUseCase
import com.gdavidpb.tuindice.about.domain.usecase.ObserveAssistantConsentUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenExternalUrlUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenStoreUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SendSupportEmailUseCase
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.presentation.machine.AboutAssistantUseCases
import com.gdavidpb.tuindice.about.presentation.machine.AboutExternalUseCases
import com.gdavidpb.tuindice.about.presentation.machine.AboutMachine
import com.gdavidpb.tuindice.about.presentation.utils.LocalShareTextHandler
import com.gdavidpb.tuindice.about.presentation.viewmodel.AboutViewModel
import com.gdavidpb.tuindice.about.testing.FakeAboutRepository
import com.gdavidpb.tuindice.about.testing.FakeAssistantAvailabilityRepository
import com.gdavidpb.tuindice.about.testing.FakeAssistantConsentRepository
import com.gdavidpb.tuindice.about.testing.FakeAssistantConversationsRepository
import com.gdavidpb.tuindice.about.testing.FakeStoreUrlDataSource
import com.gdavidpb.tuindice.about.testing.fakeDeleteAssistantConversationsUseCase
import com.gdavidpb.tuindice.about.testing.fakeRefreshAssistantConsentUseCase
import com.gdavidpb.tuindice.about.testing.fakeSetAssistantConsentUseCase
import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.data.source.usage.InMemoryUsageDataConsentRepository
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
class AboutAssistantRouteUiTest {
	@Test
	fun when_consentSwitchTapped_then_storesTheChoice() = runTuIndiceUiTest {
		val assistantConsent = FakeAssistantConsentRepository(granted = false)
		val viewModel = createAboutViewModel(assistantConsent = assistantConsent)

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalShareTextHandler provides { _, _ -> }
			) {
				AboutRoute(
					onNavigateToBrowser = { _, _ -> },
					showSnackBar = { },
					viewModel = viewModel
				)
			}
		}

		waitUntilNodeExists(AboutUiTags.AssistantConsentSwitch)
		scrollToRow(AboutUiTags.AssistantConsentSwitch)
		onNodeWithTag(AboutUiTags.AssistantConsentSwitch).performClick()

		waitUntil(timeoutMillis = 2_000) {
			assistantConsent.isAssistantConsentGranted()
		}

		assertTrue(assistantConsent.isAssistantConsentGranted())
	}

	@Test
	fun when_memoryTapped_then_navigatesToTheMemory() = runTuIndiceUiTest {
		var memoryOpenings = 0
		val viewModel = createAboutViewModel()

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalShareTextHandler provides { _, _ -> }
			) {
				AboutRoute(
					onNavigateToBrowser = { _, _ -> },
					onNavigateToAssistantMemory = { memoryOpenings++ },
					showSnackBar = { },
					viewModel = viewModel
				)
			}
		}

		waitUntilNodeExists(AboutUiTags.AssistantMemoryItem)
		scrollToRow(AboutUiTags.AssistantMemoryItem)
		onNodeWithTag(AboutUiTags.AssistantMemoryItem).performClick()

		waitUntil(timeoutMillis = 2_000) {
			memoryOpenings > 0
		}

		assertEquals(1, memoryOpenings)
	}

	@Test
	fun when_consentWriteFails_then_showsTheSnackBarAndKeepsTheSwitchOff() = runTuIndiceUiTest {
		val messages = mutableListOf<String>()
		val viewModel = createAboutViewModel(
			assistantConsent = FakeAssistantConsentRepository(granted = false, writesFail = true)
		)

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalShareTextHandler provides { _, _ -> }
			) {
				AboutRoute(
					onNavigateToBrowser = { _, _ -> },
					showSnackBar = { message -> messages += message.message },
					viewModel = viewModel
				)
			}
		}

		waitUntilNodeExists(AboutUiTags.AssistantConsentSwitch)
		scrollToRow(AboutUiTags.AssistantConsentSwitch)
		onNodeWithTag(AboutUiTags.AssistantConsentSwitch).performClick()

		waitUntil(timeoutMillis = 2_000) {
			messages.isNotEmpty()
		}

		assertEquals(listOf(EXPECTED_CONSENT_FAILED_MESSAGE), messages)
		onNodeWithTag(AboutUiTags.AssistantConsentSwitch).assertIsOff()
	}

	@Test
	fun when_assistantConversationsTapped_then_opensTheConfirmation() = runTuIndiceUiTest {
		var confirmationOpenings = 0
		val viewModel = createAboutViewModel()

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalShareTextHandler provides { _, _ -> }
			) {
				AboutRoute(
					onNavigateToBrowser = { _, _ -> },
					onNavigateToDeleteConfirmation = { confirmationOpenings++ },
					showSnackBar = { },
					viewModel = viewModel
				)
			}
		}

		waitUntilNodeExists(AboutUiTags.AssistantConversationsItem)
		scrollToRow(AboutUiTags.AssistantConversationsItem)
		onNodeWithTag(AboutUiTags.AssistantConversationsItem).performClick()

		waitUntil(timeoutMillis = 2_000) {
			confirmationOpenings > 0
		}

		assertEquals(1, confirmationOpenings)
	}

	@Test
	fun when_conversationsAreDeleted_then_showsTheSuccessSnackBar() = runTuIndiceUiTest {
		val messages = mutableListOf<String>()
		val assistantConversations = FakeAssistantConversationsRepository()
		val viewModel = createAboutViewModel(assistantConversations = assistantConversations)

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalShareTextHandler provides { _, _ -> }
			) {
				AboutRoute(
					onNavigateToBrowser = { _, _ -> },
					showSnackBar = { message -> messages += message.message },
					viewModel = viewModel
				)
			}
		}

		waitUntilNodeExists(AboutUiTags.AssistantConversationsItem)
		viewModel.dispatchAction(About.Action.ConfirmDeleteAssistantConversations)

		waitUntil(timeoutMillis = 2_000) {
			messages.isNotEmpty()
		}

		assertEquals(1, assistantConversations.deletions)
		assertEquals(listOf(DELETED_MESSAGE), messages)
	}

	@Test
	fun when_conversationsDeleteFails_then_showsTheFailureSnackBar() = runTuIndiceUiTest {
		val messages = mutableListOf<String>()
		val assistantConversations = FakeAssistantConversationsRepository(deletesFail = true)
		val viewModel = createAboutViewModel(assistantConversations = assistantConversations)

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalShareTextHandler provides { _, _ -> }
			) {
				AboutRoute(
					onNavigateToBrowser = { _, _ -> },
					showSnackBar = { message -> messages += message.message },
					viewModel = viewModel
				)
			}
		}

		waitUntilNodeExists(AboutUiTags.AssistantConversationsItem)
		viewModel.dispatchAction(About.Action.ConfirmDeleteAssistantConversations)

		waitUntil(timeoutMillis = 2_000) {
			messages.isNotEmpty()
		}

		assertEquals(0, assistantConversations.deletions)
		assertEquals(listOf(DELETE_FAILED_MESSAGE), messages)
	}

	private fun ComposeUiTest.waitUntilNodeExists(
		tag: String,
		timeoutMillis: Long = 2_000
	) {
		waitUntil(timeoutMillis = timeoutMillis) {
			onAllNodesWithTag(testTag = tag).fetchSemanticsNodes().isNotEmpty()
		}
	}

	private fun ComposeUiTest.scrollToRow(tag: String) {
		onNodeWithTag(AboutUiTags.ContentContainer)
			.performScrollToNode(hasTestTag(tag))
	}

	private fun createAboutViewModel(
		assistantAvailable: Boolean = true,
		assistantConsent: FakeAssistantConsentRepository = FakeAssistantConsentRepository(),
		assistantConversations: FakeAssistantConversationsRepository = FakeAssistantConversationsRepository()
	): AboutViewModel {
		val assistantAvailability = FakeAssistantAvailabilityRepository(visible = assistantAvailable)

		return AboutViewModel(
			screenMachine = AboutMachine(
				loadVersionUseCase = LoadVersionUseCase(
					aboutRepository = FakeAboutRepository(),
					reportingRepository = RecordingReportingRepository()
				),
				externalUseCases = AboutExternalUseCases(
					sendSupportEmail = SendSupportEmailUseCase(
						configRepository = FakeConfigRepository(),
						deviceInfoRepository = FakeDeviceInfoRepository(),
						reportingRepository = RecordingReportingRepository()
					),
					openStore = OpenStoreUseCase(
						storeUrlRepository = FakeStoreUrlDataSource(),
						reportingRepository = RecordingReportingRepository()
					),
					openExternalUrl = OpenExternalUrlUseCase(
						browserRepository = RecordingBrowserRepository(),
						reportingRepository = RecordingReportingRepository()
					)
				),
				appEnvironmentRepository = FakeAppEnvironmentRepository(),
				usageDataConsentRepository = InMemoryUsageDataConsentRepository(),
				assistantUseCases = AboutAssistantUseCases(
					observeAvailability = ObserveAssistantAvailabilityUseCase(
						assistantAvailabilityRepository = assistantAvailability,
						reportingRepository = RecordingReportingRepository()
					),
					observeConsent = ObserveAssistantConsentUseCase(
						assistantConsentRepository = assistantConsent,
						reportingRepository = RecordingReportingRepository()
					),
					setConsent = fakeSetAssistantConsentUseCase(assistantConsent),
					refreshConsent = fakeRefreshAssistantConsentUseCase(assistantConsent),
					deleteConversations = fakeDeleteAssistantConversationsUseCase(assistantConversations)
				)
			),
			eventPublisher = NoOpEventPublisher
		)
	}

	private companion object {
		const val EXPECTED_CONSENT_FAILED_MESSAGE = "No pudimos cambiar el asistente. Inténtalo de nuevo."
		const val DELETED_MESSAGE = "Se borraron tus conversaciones con el asistente"
		const val DELETE_FAILED_MESSAGE = "No pudimos borrar tus conversaciones. Inténtalo de nuevo."
	}
}
