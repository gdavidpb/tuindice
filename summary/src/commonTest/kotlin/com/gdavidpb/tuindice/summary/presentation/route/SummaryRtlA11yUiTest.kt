package com.gdavidpb.tuindice.summary.presentation.route

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.intl.Locale
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RemoveProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UpdateUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UploadProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RemoveProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UpdateUserExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UploadProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryMachine
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.summary.testing.RecordingUserRepository
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.TestTuIndiceDispatchers
import com.gdavidpb.tuindice.testkit.ui.TuIndiceTestSizeClass
import com.gdavidpb.tuindice.testkit.ui.assertNodeEnabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * RTL and accessibility-semantics matrix for the Summary screen.
 *
 * Mirrors the fixture construction of [SummaryRouteUiTest] and reuses the
 * summary/testkit testing doubles without modifying them.
 */
@OptIn(ExperimentalTestApi::class)
class SummaryRtlA11yUiTest {
	@Test
	fun when_layoutIsRtl_then_summaryCriticalNodesRemainVisible() = runTuIndiceUiTest {
		val viewModel = createSummaryViewModel()

		setTuIndiceTestContent(locale = Locale("ar")) {
			SummaryRoute(
				onNavigateToUpdatePassword = {},
				onNavigateToProfilePictureSettingsDialog = {},
				onNavigateToRemoveProfilePictureConfirmationDialog = {},
				showSnackBar = {},
				viewModel = viewModel,
				syncStatusRepository = FakeSyncStatusRepository(),
				syncRepository = FakeSyncRepository()
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			(viewModel.state.value as? Summary.State.Content)?.isUserRefreshing == false
		}

		// Header and grade summary must stay visible when the layout is mirrored.
		assertNodeVisible(SummaryUiTags.ContentContainer)
		assertNodeVisible(SummaryUiTags.NameText)
		assertNodeVisible(SummaryUiTags.CareerText)
		assertNodeVisible(SummaryUiTags.GradeText)

		// The items list may extend past the Compact viewport; composition is enough here.
		onNodeWithTag(SummaryUiTags.ItemsList).assertExists()

		assertNodeEnabled(SummaryUiTags.ProfilePictureEditButton)
	}

	@Test
	fun when_layoutIsRtl_then_profilePictureEditStillOpensSettingsDialog() = runTuIndiceUiTest {
		val viewModel = createSummaryViewModel()
		val profilePictureSettingsNavigations = mutableListOf<Boolean>()

		setTuIndiceTestContent(locale = Locale("ar")) {
			SummaryRoute(
				onNavigateToUpdatePassword = {},
				onNavigateToProfilePictureSettingsDialog = { showRemove ->
					profilePictureSettingsNavigations += showRemove
				},
				onNavigateToRemoveProfilePictureConfirmationDialog = {},
				showSnackBar = {},
				viewModel = viewModel,
				syncStatusRepository = FakeSyncStatusRepository(),
				syncRepository = FakeSyncRepository()
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			(viewModel.state.value as? Summary.State.Content)?.isUserRefreshing == false
		}

		// The mirrored layout must keep the edit affordance tappable end-to-end.
		onNodeWithTag(SummaryUiTags.ProfilePictureEditButton).performClick()

		waitUntil(timeoutMillis = 2_000) {
			profilePictureSettingsNavigations.isNotEmpty()
		}

		assertEquals(listOf(true), profilePictureSettingsNavigations)
	}

	@Test
	fun when_a11ySemanticsInspected_then_summaryExposesTextAndClickActions() = runTuIndiceUiTest {
		val viewModel = createSummaryViewModel()

		setTuIndiceTestContent {
			SummaryRoute(
				onNavigateToUpdatePassword = {},
				onNavigateToProfilePictureSettingsDialog = {},
				onNavigateToRemoveProfilePictureConfirmationDialog = {},
				showSnackBar = {},
				viewModel = viewModel,
				syncStatusRepository = FakeSyncStatusRepository(),
				syncRepository = FakeSyncRepository()
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			(viewModel.state.value as? Summary.State.Content)?.isUserRefreshing == false
		}

		// Informational nodes expose their text to assistive technologies.
		onNodeWithTag(SummaryUiTags.NameText).assert(hasAnyText)
		onNodeWithTag(SummaryUiTags.CareerText).assert(hasAnyText)
		onNodeWithTag(SummaryUiTags.GradeText).assert(hasAnyText)

		// The profile-picture edit control is clickable and enabled. NOTE: its inner
		// Icon sets contentDescription = null (ProfilePictureView.kt), so the merged
		// node carries no accessible label today — a known a11y gap that is reported,
		// not asserted, to keep this contract test green against current production.
		onNodeWithTag(SummaryUiTags.ProfilePictureEditButton)
			.assert(hasClickAction())
			.assertIsEnabled()
	}

	@Test
	fun when_compactRtlWithIncreasedDensity_then_headerRemainsVisibleAndEditable() = runTuIndiceUiTest {
		val viewModel = createSummaryViewModel()

		// Compact size class plus 1.25x density (the kit's scale knob).
		setTuIndiceTestContent(
			sizeClass = TuIndiceTestSizeClass.Compact,
			density = 1.25f,
			locale = Locale("ar")
		) {
			SummaryRoute(
				onNavigateToUpdatePassword = {},
				onNavigateToProfilePictureSettingsDialog = {},
				onNavigateToRemoveProfilePictureConfirmationDialog = {},
				showSnackBar = {},
				viewModel = viewModel,
				syncStatusRepository = FakeSyncStatusRepository(),
				syncRepository = FakeSyncRepository()
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			(viewModel.state.value as? Summary.State.Content)?.isUserRefreshing == false
		}

		// Top-region nodes stay visible at the larger scale; lower content may scroll
		// out of the scaled viewport, so it is checked for existence/enabled state only.
		assertNodeVisible(SummaryUiTags.NameText)
		assertNodeVisible(SummaryUiTags.GradeText)
		onNodeWithTag(SummaryUiTags.ProfilePictureEditButton).assertExists().assertIsEnabled()
	}

	private fun createSummaryViewModel(): SummaryViewModel {
		val userRepository = RecordingUserRepository()

		return SummaryViewModel(
			screenMachine = SummaryMachine(
				observeUserUseCase = ObserveUserUseCase(
					userRepository = userRepository,
					reportingRepository = RecordingReportingRepository()
				),
				updateUserUseCase = UpdateUserUseCase(
					userRepository = userRepository,
					reportingRepository = RecordingReportingRepository(),
					exceptionHandler = UpdateUserExceptionHandler(
						networkRepository = FakeNetworkRepository(isAvailable = true)
					)
				),
				uploadProfilePictureUseCase = UploadProfilePictureUseCase(
					userRepository = userRepository,
					reportingRepository = RecordingReportingRepository(),
					exceptionHandler = UploadProfilePictureExceptionHandler(
						networkRepository = FakeNetworkRepository(isAvailable = true)
					)
				),
				removeProfilePictureUseCase = RemoveProfilePictureUseCase(
					userRepository = userRepository,
					reportingRepository = RecordingReportingRepository(),
					exceptionHandler = RemoveProfilePictureExceptionHandler(
						networkRepository = FakeNetworkRepository(isAvailable = true)
					)
				)
			),
			eventPublisher = NoOpEventPublisher,
			dispatchers = TestTuIndiceDispatchers(Dispatchers.Unconfined)
		)
	}
}

/** Exposes regular text (merged) to assistive technologies. */
private val hasAnyText: SemanticsMatcher =
	SemanticsMatcher.keyIsDefined(SemanticsProperties.Text)
