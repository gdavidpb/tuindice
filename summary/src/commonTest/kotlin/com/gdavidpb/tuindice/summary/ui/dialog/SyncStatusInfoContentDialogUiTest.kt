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
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SyncStatusInfoContentDialogUiTest {
	@Test
	fun when_enrollmentIsNotEnrolled_then_showsTheInformativeDialogAndUnderstoodButton() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SyncStatusInfoContentDialog(
				syncStatus = SyncStatus.Healthy,
				syncReport = report(SyncSourceStatus.NotEnrolled),
				onUpdatePasswordClick = {},
				onDismissRequest = {}
			)
		}

		onNodeWithText("No estás inscrito en este trimestre").assertExists()
		onNodeWithText("Tus notas anteriores siguen disponibles.", substring = true).assertExists()
		onNodeWithText("Entendido").assertExists()
	}

	@Test
	fun when_annulledWithACurrentTerm_then_showsTheProvisionalCopyWithItsCause() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SyncStatusInfoContentDialog(
				syncStatus = SyncStatus.Healthy,
				syncReport = report(SyncSourceStatus.Success, EnrollmentSituation(code = "01")),
				hasCurrentTerm = true,
				onUpdatePasswordClick = {},
				onDismissRequest = {}
			)
		}

		onNodeWithText("Tu inscripción aparece anulada").assertExists()
		onNodeWithText("por el límite de créditos", substring = true).assertExists()
		onNodeWithText("todavía puedes regularizarla", substring = true).assertExists()
	}

	@Test
	fun when_annulledWithoutACurrentTerm_then_showsTheFinalCopyAndTheGenericOneForAnUnknownCode() =
		runTuIndiceUiTest {
			setTuIndiceTestContent {
				SyncStatusInfoContentDialog(
					syncStatus = SyncStatus.Healthy,
					syncReport = report(SyncSourceStatus.Success, EnrollmentSituation(code = "99")),
					hasCurrentTerm = false,
					onUpdatePasswordClick = {},
					onDismissRequest = {}
				)
			}

			onNodeWithText("Tu inscripción fue anulada").assertExists()
			onNodeWithText("de este trimestre. Consulta con DACE", substring = true).assertExists()
		}

	@Test
	fun when_recordAccessIsDenied_then_keepsThePreviousDataMessage() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SyncStatusInfoContentDialog(
				syncStatus = SyncStatus.RecordAccessDenied,
				onUpdatePasswordClick = {},
				onDismissRequest = {}
			)
		}

		onNodeWithText("Mantenemos tus datos anteriores.", substring = true).assertExists()
	}

	@Test
	fun when_newStudentHasNoRecord_then_usesTheNewStudentCopyAndDismisses() = runTuIndiceUiTest {
		var dismissals = 0

		setTuIndiceTestContent {
			SyncStatusInfoContentDialog(
				syncStatus = SyncStatus.NewStudentNoRecord,
				onUpdatePasswordClick = {},
				onDismissRequest = { dismissals++ }
			)
		}

		onNodeWithText("Aún no tienes expediente en la universidad").assertExists()
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
