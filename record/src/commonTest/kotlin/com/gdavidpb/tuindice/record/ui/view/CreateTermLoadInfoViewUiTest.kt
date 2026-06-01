package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadBand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadBasis
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadConfidence
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadDetail
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadPreview
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermPeriodOption
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class CreateTermLoadInfoViewUiTest {
	@Test
	fun when_availableLoadInfoClicked_then_showsConfidenceTooltip() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateTermPeriodRow(
				selectedPeriod = periodOption(),
				periodOptions = listOf(periodOption()),
				loadPreview = SyntheticTermLoadPreview(
					available = true,
					band = SyntheticTermLoadBand.NORMAL,
					effectiveTerms = 4,
					basis = SyntheticTermLoadBasis.PERSONAL,
					confidence = SyntheticTermLoadConfidence.HIGH,
					detail = SyntheticTermLoadDetail.PERSONAL_HISTORY_STRONG
				),
				hasSelectedSubjects = true,
				isLoadingLoadPreview = false,
				hasLoadPreviewError = false,
				onPeriodSelected = {}
			)
		}

		onNodeWithTag(RecordUiTags.CreateSyntheticTermLoadInfoButton).performClick()

		onNodeWithText("Estimación basada en tu historial académico. Confianza alta. Se usaron 4 trimestres.")
			.assertIsDisplayed()
	}

	@Test
	fun when_unavailableLoadInfoClicked_then_showsReasonTooltip() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateTermPeriodRow(
				selectedPeriod = periodOption(),
				periodOptions = listOf(periodOption()),
				loadPreview = SyntheticTermLoadPreview(
					available = false,
					reason = SyntheticTermLoadDetail.MISSING_SUBJECT_DIFFICULTY.name,
					detail = SyntheticTermLoadDetail.MISSING_SUBJECT_DIFFICULTY
				),
				hasSelectedSubjects = true,
				isLoadingLoadPreview = false,
				hasLoadPreviewError = false,
				onPeriodSelected = {}
			)
		}

		onNodeWithTag(RecordUiTags.CreateSyntheticTermLoadInfoButton).performClick()

		onNodeWithText("No pudimos estimar la carga porque faltan datos de dificultad para una materia seleccionada.")
			.assertIsDisplayed()
	}

	private fun periodOption(): SyntheticTermPeriodOption {
		return SyntheticTermPeriodOption(
			periodYear = 2027,
			periodCode = AcademicTermPeriod.JAN_MAR
		)
	}
}
