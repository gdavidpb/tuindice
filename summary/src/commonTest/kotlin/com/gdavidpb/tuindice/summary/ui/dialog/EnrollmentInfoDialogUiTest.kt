package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.base.domain.model.EnrollmentSituation
import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.domain.model.SyncReportSources
import com.gdavidpb.tuindice.base.domain.model.SyncReportStatus
import com.gdavidpb.tuindice.base.domain.model.SyncSourceReport
import com.gdavidpb.tuindice.base.domain.model.SyncSourceStatus
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class EnrollmentInfoDialogUiTest {
	@Test
	fun when_annulledWithACurrentTerm_then_showsTheProvisionalCopy() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EnrollmentInfoDialog(
				syncReport = report(SyncSourceStatus.Success, EnrollmentSituation(code = "01")),
				hasCurrentTerm = true,
				onDismissRequest = {}
			)
		}

		onNodeWithText("Tu inscripción aparece anulada").assertExists()
		onNodeWithText("Anulada por el límite de créditos. Consulta en DACE.").assertExists()
		assertNodeVisible(SummaryUiTags.SyncStatusMessage)
	}

	@Test
	fun when_annulledWithoutACurrentTerm_then_showsTheFinalCopy() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EnrollmentInfoDialog(
				syncReport = report(SyncSourceStatus.Success, EnrollmentSituation(code = "01")),
				hasCurrentTerm = false,
				onDismissRequest = {}
			)
		}

		onNodeWithText("Tu inscripción fue anulada").assertExists()
		assertNodeVisible(SummaryUiTags.SyncStatusMessage)
	}

	@Test
	fun when_annulledAndNotEnrolled_then_theAnnulmentWinsOverNotEnrolled() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EnrollmentInfoDialog(
				syncReport = report(SyncSourceStatus.NotEnrolled, EnrollmentSituation(code = "01")),
				hasCurrentTerm = false,
				onDismissRequest = {}
			)
		}

		onNodeWithText("Tu inscripción fue anulada").assertExists()
		onNodeWithText("No estás inscrito en este trimestre").assertDoesNotExist()
	}

	@Test
	fun when_notEnrolled_then_showsTheNotEnrolledCopy() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EnrollmentInfoDialog(
				syncReport = report(SyncSourceStatus.NotEnrolled),
				hasCurrentTerm = false,
				onDismissRequest = {}
			)
		}

		onNodeWithText("No estás inscrito en este trimestre").assertExists()
		onNodeWithText("Entendido").assertExists()
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
