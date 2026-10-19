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
import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionNameUseCase
import com.gdavidpb.tuindice.about.domain.usecase.ObserveAssistantAvailabilityUseCase
import com.gdavidpb.tuindice.about.domain.usecase.ObserveAssistantConsentUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenExternalUrlUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenStoreUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SendSupportEmailUseCase
import com.gdavidpb.tuindice.about.presentation.contract.Settings
import com.gdavidpb.tuindice.about.presentation.machine.SettingsAssistantUseCases
import com.gdavidpb.tuindice.about.presentation.machine.SettingsContentUseCases
import com.gdavidpb.tuindice.about.presentation.machine.SettingsExternalUseCases
import com.gdavidpb.tuindice.about.presentation.machine.SettingsMachine
import com.gdavidpb.tuindice.about.presentation.utils.LocalShareTextHandler
import com.gdavidpb.tuindice.about.presentation.viewmodel.SettingsViewModel
import com.gdavidpb.tuindice.about.testing.FakeAssistantAvailabilityRepository
import com.gdavidpb.tuindice.about.testing.FakeAssistantConsentRepository
import com.gdavidpb.tuindice.about.testing.FakeAssistantConversationsRepository
import com.gdavidpb.tuindice.about.testing.FakeStoreUrlDataSource
import com.gdavidpb.tuindice.about.testing.SettingsRouteUnderTest
import com.gdavidpb.tuindice.about.testing.fakeDeleteAssistantConversationsUseCase
import com.gdavidpb.tuindice.about.testing.fakeObserveProfileIdentityUseCase
import com.gdavidpb.tuindice.about.testing.fakeRefreshAssistantConsentUseCase
import com.gdavidpb.tuindice.about.testing.fakeSetAssistantConsentUseCase
import com.gdavidpb.tuindice.about.testing.noSettingsNavDependencies
import com.gdavidpb.tuindice.about.ui.SettingsUiTags
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
class SettingsAssistantRouteUiTest {
	@Test
	fun when_consentSwitchTapped_then_storesTheChoice() = runTuIndiceUiTest {
		val assistantConsent = FakeAssistantConsentRepository(granted = false)
		val viewModel = createSettingsViewModel(assistantConsent = assistantConsent)

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalShareTextHandler provides { _, _ -> }
			) {
				SettingsRouteUnderTest(
					onNavigateToBrowser = { _, _ -> },
					showSnackBar = { },
					viewModel = viewModel
				)
			}
		}

		waitUntilNodeExists(SettingsUiTags.AssistantConsentSwitch)
		scrollToRow(SettingsUiTags.AssistantConsentSwitch)
		onNodeWithTag(SettingsUiTags.AssistantConsentSwitch).performClick()

		waitUntil(timeoutMillis = 2_000) {
			assistantConsent.isAssistantConsentGranted()
		}

		assertTrue(assistantConsent.isAssistantConsentGranted())
	}

	@Test
	fun when_memoryTapped_then_navigatesToTheMemory() = runTuIndiceUiTest {
		var memoryOpenings = 0
		val viewModel = createSettingsViewModel()

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalShareTextHandler provides { _, _ -> }
			) {
				SettingsRoute(
					dependencies = noSettingsNavDependencies().copy(
						onNavigateToAssistantMemory = { memoryOpenings++ }
					),
					showSnackBar = { },
					viewModel = viewModel
				)
			}
		}

		waitUntilNodeExists(SettingsUiTags.AssistantMemoryItem)
		scrollToRow(SettingsUiTags.AssistantMemoryItem)
		onNodeWithTag(SettingsUiTags.AssistantMemoryItem).performClick()

		waitUntil(timeoutMillis = 2_000) {
			memoryOpenings > 0
		}

		assertEquals(1, memoryOpenings)
	}

	@Test
	fun when_consentWriteFails_then_showsTheSnackBarAndKeepsTheSwitchOff() = runTuIndiceUiTest {
		val messages = mutableListOf<String>()
		val viewModel = createSettingsViewModel(
			assistantConsent = FakeAssistantConsentRepository(granted = false, writesFail = true)
		)

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalShareTextHandler provides { _, _ -> }
			) {
				SettingsRouteUnderTest(
					onNavigateToBrowser = { _, _ -> },
					showSnackBar = { message -> messages += message.message },
					viewModel = viewModel
				)
			}
		}

		waitUntilNodeExists(SettingsUiTags.AssistantConsentSwitch)
		scrollToRow(SettingsUiTags.AssistantConsentSwitch)
		onNodeWithTag(SettingsUiTags.AssistantConsentSwitch).performClick()

		waitUntil(timeoutMillis = 2_000) {
			messages.isNotEmpty()
		}

		assertEquals(listOf(EXPECTED_CONSENT_FAILED_MESSAGE), messages)
		onNodeWithTag(SettingsUiTags.AssistantConsentSwitch).assertIsOff()
	}

	@Test
	fun when_assistantConversationsTapped_then_opensTheConfirmation() = runTuIndiceUiTest {
		var confirmationOpenings = 0
		val viewModel = createSettingsViewModel()

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalShareTextHandler provides { _, _ -> }
			) {
				SettingsRoute(
					dependencies = noSettingsNavDependencies().copy(
						onNavigateToDeleteConfirmation = { confirmationOpenings++ }
					),
					showSnackBar = { },
					viewModel = viewModel
				)
			}
		}

		waitUntilNodeExists(SettingsUiTags.AssistantConversationsItem)
		scrollToRow(SettingsUiTags.AssistantConversationsItem)
		onNodeWithTag(SettingsUiTags.AssistantConversationsItem).performClick()

		waitUntil(timeoutMillis = 2_000) {
			confirmationOpenings > 0
		}

		assertEquals(1, confirmationOpenings)
	}

	@Test
	fun when_conversationsAreDeleted_then_showsTheSuccessSnackBar() = runTuIndiceUiTest {
		val messages = mutableListOf<String>()
		val assistantConversations = FakeAssistantConversationsRepository()
		val viewModel = createSettingsViewModel(assistantConversations = assistantConversations)

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalShareTextHandler provides { _, _ -> }
			) {
				SettingsRouteUnderTest(
					onNavigateToBrowser = { _, _ -> },
					showSnackBar = { message -> messages += message.message },
					viewModel = viewModel
				)
			}
		}

		waitUntilNodeExists(SettingsUiTags.AssistantConversationsItem)
		viewModel.dispatchAction(Settings.Action.ConfirmDeleteAssistantConversations)

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
		val viewModel = createSettingsViewModel(assistantConversations = assistantConversations)

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalShareTextHandler provides { _, _ -> }
			) {
				SettingsRouteUnderTest(
					onNavigateToBrowser = { _, _ -> },
					showSnackBar = { message -> messages += message.message },
					viewModel = viewModel
				)
			}
		}

		waitUntilNodeExists(SettingsUiTags.AssistantConversationsItem)
		viewModel.dispatchAction(Settings.Action.ConfirmDeleteAssistantConversations)

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
		onNodeWithTag(SettingsUiTags.ContentContainer)
			.performScrollToNode(hasTestTag(tag))
	}

	private fun createSettingsViewModel(
		assistantAvailable: Boolean = true,
		assistantConsent: FakeAssistantConsentRepository = FakeAssistantConsentRepository(),
		assistantConversations: FakeAssistantConversationsRepository = FakeAssistantConversationsRepository()
	): SettingsViewModel {
		val assistantAvailability = FakeAssistantAvailabilityRepository(visible = assistantAvailable)

		return SettingsViewModel(
			screenMachine = SettingsMachine(
				contentUseCases = SettingsContentUseCases(
					loadVersionName = LoadVersionNameUseCase(
						deviceInfoRepository = FakeDeviceInfoRepository(versionName = "1.2.3"),
						reportingRepository = RecordingReportingRepository()
					),
					observeProfileIdentity = fakeObserveProfileIdentityUseCase()
				),
				externalUseCases = SettingsExternalUseCases(
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
				assistantUseCases = SettingsAssistantUseCases(
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
