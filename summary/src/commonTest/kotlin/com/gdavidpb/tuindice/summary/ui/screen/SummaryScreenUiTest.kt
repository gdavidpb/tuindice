package com.gdavidpb.tuindice.summary.ui.screen

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.domain.model.EnrollmentSituation
import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.domain.model.SyncReportSources
import com.gdavidpb.tuindice.base.domain.model.SyncReportStatus
import com.gdavidpb.tuindice.base.domain.model.SyncSourceReport
import com.gdavidpb.tuindice.base.domain.model.SyncSourceStatus
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
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

	@Test
	fun when_enrollmentIsAnnulled_then_statusIconShowsNoHaloAndOpensNoDialog() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryScreen(
				state = summaryContentState(),
				syncStatus = SyncStatus.Healthy,
				syncReport = enrollmentReport(SyncSourceStatus.Success, EnrollmentSituation(code = "01")),
				onRetryClick = {},
				onEditProfilePictureClick = {},
				onUpdatePasswordClick = {}
			)
		}

		// Record and Evaluations carry the annulment notice; the sync row only speaks of sync problems.
		assertNodeHidden(SummaryUiTags.StatusIconHalo)
		onNodeWithTag(SummaryUiTags.StatusIconButton).assertIsNotEnabled()
		onNodeWithTag(SummaryUiTags.StatusIconButton).performClick()

		assertNodeHidden(SummaryUiTags.SyncStatusMessage)
		onNodeWithText("Tu inscripción aparece anulada").assertDoesNotExist()
		onNodeWithText("Tu inscripción fue anulada").assertDoesNotExist()
	}

	@Test
	fun when_enrollmentIsNotEnrolled_then_statusIconShowsNoHaloAndOpensNoDialog() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryScreen(
				state = summaryContentState(),
				syncStatus = SyncStatus.Healthy,
				syncReport = enrollmentReport(SyncSourceStatus.NotEnrolled),
				onRetryClick = {},
				onEditProfilePictureClick = {},
				onUpdatePasswordClick = {}
			)
		}

		assertNodeHidden(SummaryUiTags.StatusIconHalo)
		onNodeWithTag(SummaryUiTags.StatusIconButton).assertIsNotEnabled()
		onNodeWithTag(SummaryUiTags.StatusIconButton).performClick()

		assertNodeHidden(SummaryUiTags.SyncStatusMessage)
		onNodeWithText("No estás inscrito en este trimestre").assertDoesNotExist()
	}

	@Test
	fun when_contentBelongsToANewStudent_then_statusIconShowsNoHaloAndOpensNoDialog() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryScreen(
				// Signing in leaves a profile, so Summary has content while the record is still missing.
				state = summaryContentState(),
				syncStatus = SyncStatus.NewStudentNoRecord,
				syncReport = SyncReport.failedRecordUnavailable(),
				onRetryClick = {},
				onEditProfilePictureClick = {},
				onUpdatePasswordClick = {}
			)
		}

		assertNodeVisible(SummaryUiTags.ContentContainer)
		assertNodeHidden(SummaryUiTags.StatusIconHalo)
		onNodeWithTag(SummaryUiTags.StatusIconButton).assertIsNotEnabled()
		onNodeWithTag(SummaryUiTags.StatusIconButton).performClick()

		assertNodeHidden(SummaryUiTags.SyncStatusMessage)
		onNodeWithText("Aún no tienes expediente en la universidad").assertDoesNotExist()
	}

	@Test
	fun when_enrollmentIsUnavailableAndAnAnnulmentIsCarriedOver_then_statusDialogExplainsTheProblem() =
		runTuIndiceUiTest {
			setTuIndiceTestContent {
				SummaryScreen(
					state = summaryContentState(),
					syncStatus = SyncStatus.Healthy,
					syncReport = SyncReport.partialEnrollmentUnavailable().carryingEnrollmentFrom(
						enrollmentReport(SyncSourceStatus.Success, EnrollmentSituation(code = "01"))
					),
					onRetryClick = {},
					onEditProfilePictureClick = {},
					onUpdatePasswordClick = {}
				)
			}

			assertNodeVisible(SummaryUiTags.StatusIconHalo)
			onNodeWithTag(SummaryUiTags.StatusIconButton).assertIsEnabled()
			onNodeWithTag(SummaryUiTags.StatusIconButton).performClick()

			onNodeWithText("Servicios de la universidad no disponibles").assertExists()
			onNodeWithText("Tu inscripción aparece anulada").assertDoesNotExist()
			assertNodeHidden(SummaryUiTags.StatusIconHalo)
		}

	@Test
	fun when_recordAccessIsDenied_then_statusIconOpensTheDeniedRecordDialog() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryScreen(
				state = summaryContentState(),
				syncStatus = SyncStatus.RecordAccessDenied,
				syncReport = SyncReport.failedRecordUnavailable(),
				onRetryClick = {},
				onEditProfilePictureClick = {},
				onUpdatePasswordClick = {}
			)
		}

		assertNodeVisible(SummaryUiTags.StatusIconHalo)
		onNodeWithTag(SummaryUiTags.StatusIconButton).performClick()

		onNodeWithText("No pudimos consultar tu expediente").assertExists()
		assertNodeHidden(SummaryUiTags.StatusIconHalo)
	}

	@Test
	fun when_problemIsSyncing_then_haloWaitsForTheSyncToEnd() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			// The sync icon spins for as long as the sync runs; with animations on the test never idles.
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
				SummaryScreen(
					state = summaryContentState(),
					syncStatus = SyncStatus.Healthy,
					syncReport = SyncReport.partialEnrollmentUnavailable(),
					isSyncing = true,
					onRetryClick = {},
					onEditProfilePictureClick = {},
					onUpdatePasswordClick = {}
				)
			}
		}

		assertNodeHidden(SummaryUiTags.StatusIconHalo)
		onNodeWithTag(SummaryUiTags.StatusIconButton).assertIsNotEnabled()
	}

	@Test
	fun when_failedForANewStudent_then_showsTheNewStudentViewWithAnEnabledRetry() = runTuIndiceUiTest {
		var retryClicks = 0

		setTuIndiceTestContent {
			SummaryScreen(
				state = Summary.State.Failed(),
				syncStatus = SyncStatus.NewStudentNoRecord,
				syncReport = SyncReport.success(),
				onRetryClick = { retryClicks++ },
				onEditProfilePictureClick = {},
				onUpdatePasswordClick = {}
			)
		}

		assertNodeVisible(SummaryUiTags.NewStudentContainer)
		assertNodeHidden(BaseUiTags.ErrorViewContainer)
		onNodeWithText("Aún no tienes expediente en la universidad").assertExists()
		onNodeWithTag(SummaryUiTags.NewStudentRetryButton).assertIsEnabled()
		onNodeWithTag(SummaryUiTags.NewStudentRetryButton).performClick()
		assertEquals(1, retryClicks)
	}

	@Test
	fun when_newStudentIsSyncing_then_retryIsDisabled() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryScreen(
				state = Summary.State.Failed(),
				syncStatus = SyncStatus.NewStudentNoRecord,
				syncReport = SyncReport.success(),
				isSyncing = true,
				onRetryClick = {},
				onEditProfilePictureClick = {},
				onUpdatePasswordClick = {}
			)
		}

		onNodeWithTag(SummaryUiTags.NewStudentRetryButton).assertIsNotEnabled()
	}

	private fun enrollmentReport(
		enrollment: SyncSourceStatus,
		situation: EnrollmentSituation? = null
	): SyncReport {
		return SyncReport(
			status = SyncReportStatus.Success,
			sources = SyncReportSources(
				record = SyncSourceReport(SyncSourceStatus.Success),
				enrollment = SyncSourceReport(enrollment, situation)
			)
		)
	}
}
