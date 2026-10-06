package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import com.gdavidpb.tuindice.pensum.testing.SAMPLE_PENSUM_CAREER_NAME
import com.gdavidpb.tuindice.pensum.testing.samplePensumScreenModelWithSelectableYears
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class PensumCurrentSelectionSummaryUiTest {
	@Test
	fun when_careerNameIsPresent_then_showsCareerPensumModalityAndProgress() = runTuIndiceUiTest {
		val model = samplePensumScreenModelWithSelectableYears().copy(
			progressPercent = 40,
			approvedCredits = 32,
			totalCredits = 80
		)

		setTuIndiceTestContent {
			PensumCurrentSelectionSummary(
				model = model,
				currentPensum = model.pensumOptions.last(),
				currentModality = model.modalityOptions.first()
			)
		}

		assertNodeVisible(PensumUiTags.PensumCurrentSelectionSummary)
		onNodeWithText("Pensum actual").assertExists()
		onNodeWithText(SAMPLE_PENSUM_CAREER_NAME).assertExists()
		onNodeWithText("Pensum 2019 · Proyecto de Grado").assertExists()
		onNodeWithText("40% avance · 32 / 80 UC aprobadas").assertExists()
	}

	@Test
	fun when_careerNameIsBlank_then_titleFallsBackToPensumYear() = runTuIndiceUiTest {
		val model = samplePensumScreenModelWithSelectableYears().copy(careerName = " ")

		setTuIndiceTestContent {
			PensumCurrentSelectionSummary(
				model = model,
				currentPensum = model.pensumOptions.first(),
				currentModality = model.modalityOptions.last()
			)
		}

		onAllNodesWithText(SAMPLE_PENSUM_CAREER_NAME).assertCountEquals(0)
		onNodeWithText("Pensum 2018").assertExists()
		onNodeWithText("Pensum 2018 · Pasantía Larga").assertExists()
	}

	@Test
	fun when_modalityNameIsBlank_then_subtitleShowsOnlyThePensumYear() = runTuIndiceUiTest {
		val model = samplePensumScreenModelWithSelectableYears()

		setTuIndiceTestContent {
			PensumCurrentSelectionSummary(
				model = model,
				currentPensum = model.currentPensumOption(),
				currentModality = model.modalityOptions.first().copy(name = "")
			)
		}

		onNodeWithText(SAMPLE_PENSUM_CAREER_NAME).assertExists()
		onNodeWithText("Pensum 2019").assertExists()
		onAllNodesWithText("Pensum 2019 · Proyecto de Grado").assertCountEquals(0)
	}
}

private fun PensumScreenModel.currentPensumOption() = pensumOptions.first { option ->
	option.year == selection.year
}
