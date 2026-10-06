package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
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
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SyncStatusInfoContentDialogUiTest {
	@Test
	fun when_syncIsHealthyAndEnrollmentIsNotEnrolled_then_showsNothing() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SyncStatusInfoContentDialog(
				syncStatus = SyncStatus.Healthy,
				syncReport = report(SyncSourceStatus.NotEnrolled),
				onUpdatePasswordClick = {},
				onDismissRequest = {}
			)
		}

		// Evaluations says it; the sync itself had no problem to explain.
		assertNodeHidden(SummaryUiTags.SyncStatusMessage)
		onNodeWithText("No estás inscrito en este trimestre").assertDoesNotExist()
	}

	@Test
	fun when_syncIsHealthyAndEnrollmentIsAnnulled_then_showsNothing() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SyncStatusInfoContentDialog(
				syncStatus = SyncStatus.Healthy,
				syncReport = report(SyncSourceStatus.Success, EnrollmentSituation(code = "01")),
				onUpdatePasswordClick = {},
				onDismissRequest = {}
			)
		}

		// Record and Evaluations carry the annulment notice.
		assertNodeHidden(SummaryUiTags.SyncStatusMessage)
		onNodeWithText("Tu inscripción aparece anulada").assertDoesNotExist()
		onNodeWithText("Tu inscripción fue anulada").assertDoesNotExist()
	}

	@Test
	fun when_newStudentHasNoRecord_then_showsNothing() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SyncStatusInfoContentDialog(
				syncStatus = SyncStatus.NewStudentNoRecord,
				// What the server sends with that failure: it must not read as an outage either.
				syncReport = SyncReport.failedRecordUnavailable(),
				onUpdatePasswordClick = {},
				onDismissRequest = {}
			)
		}

		assertNodeHidden(SummaryUiTags.SyncStatusMessage)
		onNodeWithText("Aún no tienes expediente en la universidad").assertDoesNotExist()
		onNodeWithText("Servicios de la universidad no disponibles").assertDoesNotExist()
	}

	@Test
	fun when_enrollmentIsUnavailableAndAnAnnulmentIsCarriedOver_then_explainsTheUnavailableSource() =
		runTuIndiceUiTest {
			setTuIndiceTestContent {
				SyncStatusInfoContentDialog(
					syncStatus = SyncStatus.Healthy,
					syncReport = SyncReport.partialEnrollmentUnavailable().carryingEnrollmentFrom(
						report(SyncSourceStatus.Success, EnrollmentSituation(code = "01"))
					),
					onUpdatePasswordClick = {},
					onDismissRequest = {}
				)
			}

			// The problem is what the dialog explains, not the annulment the report still carries.
			onNodeWithText("Servicios de la universidad no disponibles").assertExists()
			onNodeWithText("No pudimos actualizar tu inscripción", substring = true).assertExists()
			onNodeWithText("Tu inscripción aparece anulada").assertDoesNotExist()
		}

	@Test
	fun when_recordAccessIsDenied_then_keepsThePreviousDataMessage() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SyncStatusInfoContentDialog(
				syncStatus = SyncStatus.RecordAccessDenied,
				// What the server sends with that failure: the generic "sources" copy must not win.
				syncReport = SyncReport.failedRecordUnavailable(),
				onUpdatePasswordClick = {},
				onDismissRequest = {}
			)
		}

		onNodeWithText("Mantenemos tus datos anteriores.", substring = true).assertExists()
	}

	@Test
	fun when_syncFailed_then_explainsTheFailureAndDismisses() = runTuIndiceUiTest {
		var dismissals = 0

		setTuIndiceTestContent {
			SyncStatusInfoContentDialog(
				syncStatus = SyncStatus.Failed,
				onUpdatePasswordClick = {},
				onDismissRequest = { dismissals++ }
			)
		}

		onNodeWithText("No pudimos sincronizar con la universidad").assertExists()
		assertNodeVisible(BaseUiTags.ConfirmationDialogPositiveButton)
		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).performClick()
		waitUntil(timeoutMillis = 2_000) { dismissals > 0 }
		assertEquals(true, dismissals > 0)
	}

	private fun report(enrollment: SyncSourceStatus, situation: EnrollmentSituation? = null): SyncReport {
		return SyncReport(
			status = SyncReportStatus.Success,
			sources = SyncReportSources(
				record = SyncSourceReport(SyncSourceStatus.Success),
				enrollment = SyncSourceReport(enrollment, situation)
			)
		)
	}
}
