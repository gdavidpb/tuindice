package com.gdavidpb.tuindice.summary.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.testing.summaryContentState
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SummaryScreenUiTest {
	@Test
	fun when_stateIsLoading_then_displaysLoadingView() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryScreen(
				state = Summary.State.Loading(),
				syncStatus = SyncStatus.Healthy,
				syncReport = SyncReport.success(),
				onRetryClick = {},
				onEditProfilePictureClick = {},
				onUpdatePasswordClick = {}
			)
		}

		assertNodeVisible(SummaryUiTags.LoadingIndicator)
	}

	@Test
	fun when_stateIsFailedAndRetryTapped_then_invokesRetryCallback() = runTuIndiceUiTest {
		var retryClicks = 0

		setTuIndiceTestContent {
			SummaryScreen(
				state = Summary.State.Failed(),
				syncStatus = SyncStatus.Healthy,
				syncReport = SyncReport.success(),
				onRetryClick = { retryClicks++ },
				onEditProfilePictureClick = {},
				onUpdatePasswordClick = {}
			)
		}

		assertNodeVisible(BaseUiTags.ErrorViewContainer)
		assertNodeVisible(BaseUiTags.ErrorViewRetryButton)

		onNodeWithTag(BaseUiTags.ErrorViewRetryButton).performClick()

		assertEquals(1, retryClicks)
	}

	@Test
	fun when_stateIsContent_then_displaysSummaryContent() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryScreen(
				state = summaryContentState(),
				syncStatus = SyncStatus.Healthy,
				syncReport = SyncReport.success(),
				onRetryClick = {},
				onEditProfilePictureClick = {},
				onUpdatePasswordClick = {}
			)
		}

		assertNodeVisible(SummaryUiTags.ContentContainer)
	}

	@Test
	fun when_profilePictureEditTappedFromContent_then_invokesEditCallback() = runTuIndiceUiTest {
		var editClicks = 0

		setTuIndiceTestContent {
			SummaryScreen(
				state = summaryContentState(),
				syncStatus = SyncStatus.Healthy,
				syncReport = SyncReport.success(),
				onRetryClick = {},
				onEditProfilePictureClick = { editClicks++ },
				onUpdatePasswordClick = {}
			)
		}

		assertNodeVisible(SummaryUiTags.ProfilePictureEditButton)
		onNodeWithTag(SummaryUiTags.ProfilePictureEditButton).performClick()

		assertEquals(1, editClicks)
	}

	@Test
	fun when_failedStatusIconTapped_then_showsFailedSyncDialog() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryScreen(
				state = summaryContentState(),
				syncStatus = SyncStatus.Failed,
				syncReport = SyncReport.success(),
				onRetryClick = {},
				onEditProfilePictureClick = {},
				onUpdatePasswordClick = {}
			)
		}

		onNodeWithTag(SummaryUiTags.StatusIconButton).assertIsEnabled()
		onNodeWithTag(SummaryUiTags.StatusIconButton).performClick()

		onNodeWithText("No pudimos sincronizar con la universidad").assertExists()
		assertNodeVisible(SummaryUiTags.SyncStatusMessage)
		assertNodeVisible(BaseUiTags.ConfirmationDialogPositiveButton)
	}

	@Test
	fun when_unavailableStatusIconTapped_then_showsUnavailableSyncDialog() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryScreen(
				state = summaryContentState(),
				syncStatus = SyncStatus.Unavailable,
				syncReport = SyncReport.success(),
				onRetryClick = {},
				onEditProfilePictureClick = {},
				onUpdatePasswordClick = {}
			)
		}

		onNodeWithTag(SummaryUiTags.StatusIconButton).assertIsEnabled()
		onNodeWithTag(SummaryUiTags.StatusIconButton).performClick()

		onNodeWithText("Servicios de la universidad no disponibles").assertExists()
		assertNodeVisible(SummaryUiTags.SyncStatusMessage)
		assertNodeVisible(BaseUiTags.ConfirmationDialogPositiveButton)
	}

	@Test
	fun when_syncReportHasEnrollmentUnavailable_then_statusIconShowsSpecificDetailsAndAcknowledgesHalo() =
		runTuIndiceUiTest {
			setTuIndiceTestContent {
				SummaryScreen(
					state = summaryContentState(),
					syncStatus = SyncStatus.Healthy,
					syncReport = SyncReport.partialEnrollmentUnavailable(),
					onRetryClick = {},
					onEditProfilePictureClick = {},
					onUpdatePasswordClick = {}
				)
			}

			assertNodeVisible(SummaryUiTags.StatusIconHalo)
			onNodeWithTag(SummaryUiTags.StatusIconButton).assertIsEnabled()
			onNodeWithTag(SummaryUiTags.StatusIconButton).performClick()

			onNodeWithText("Servicios de la universidad no disponibles").assertExists()
			onNodeWithText(
				"No pudimos actualizar tu inscripción porque el servicio de la universidad no está disponible. " +
					"Tus datos anteriores se mantienen y volveremos a intentar más tarde."
			).assertExists()
			assertNodeHidden(SummaryUiTags.StatusIconHalo)
		}

	@Test
	fun when_outdatedCredentialsBottomSheetConfirmed_then_invokesUpdatePasswordCallback() = runTuIndiceUiTest {
		var updatePasswordClicks = 0

		setTuIndiceTestContent {
			SummaryScreen(
				state = summaryContentState(),
				syncStatus = SyncStatus.OutdatedCredentials,
				syncReport = SyncReport.success(),
				onRetryClick = {},
				onEditProfilePictureClick = {},
				onUpdatePasswordClick = { updatePasswordClicks++ }
			)
		}

		onNodeWithTag(SummaryUiTags.StatusIconButton).performClick()
		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).performClick()

		waitUntil(timeoutMillis = 2_000) {
			updatePasswordClicks == 1
		}

		assertEquals(1, updatePasswordClicks)
	}
}
