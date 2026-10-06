package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadBand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadBasis
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadConfidence
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadDetail
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadPreview
import com.gdavidpb.tuindice.record.testing.isProgressIndicator
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class CreateTermLoadChipViewUiTest {
	@Test
	fun when_noSubjectIsSelected_then_theChipSaysSo_andItsInfoAsksToAddSome() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			// A preview left over from an earlier selection does not count without subjects.
			LoadChip(loadPreview = available(SyntheticTermLoadBand.NORMAL), hasSelectedSubjects = false)
		}

		onNodeWithText("Sin materias").assertIsDisplayed()
		onAllNodesWithText("Normal").assertCountEquals(0)
		onNodeWithTag(RecordUiTags.CreateSyntheticTermLoadInfoButton)
			.assertContentDescriptionEquals("Información sobre la carga estimada")

		assertInfoSays("Agrega materias para estimar la carga.")
	}

	@Test
	fun when_theLoadIsBeingEstimated_then_theChipSpins_whateverThePreviousEstimateWas() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			LoadChip(loadPreview = available(SyntheticTermLoadBand.DEMANDING), isLoading = true, hasError = true)
		}

		onNodeWithText("Calculando").assertIsDisplayed()
		onNode(isProgressIndicator()).assertIsDisplayed()
		onAllNodesWithText("Exigente").assertCountEquals(0)

		assertInfoSays("Estamos calculando la carga estimada.")
	}

	@Test
	fun when_theEstimateFailed_then_theChipHasNoData_andItsInfoAsksToRetry() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			LoadChip(loadPreview = available(SyntheticTermLoadBand.LIGHT), hasError = true)
		}

		onNodeWithText("Sin datos").assertIsDisplayed()
		onAllNodes(isProgressIndicator()).assertCountEquals(0)

		assertInfoSays("No pudimos actualizar la estimación. Intenta de nuevo en unos segundos.")
	}

	@Test
	fun when_eachLoadBandIsEstimated_then_theChipNamesIt() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Column {
				SyntheticTermLoadBand.entries.forEach { band ->
					LoadChip(loadPreview = available(band))
				}
			}
		}

		listOf("Ligera", "Manejable", "Normal", "Exigente", "Muy exigente").forEach { label ->
			onNodeWithText(label).assertIsDisplayed()
		}
	}

	@Test
	fun when_theEstimateRestsOnOneTermOfOwnHistory_then_theInfoSaysHowLittleItHad() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			LoadChip(
				loadPreview = available(SyntheticTermLoadBand.MANAGEABLE).copy(
					effectiveTerms = 1,
					confidence = SyntheticTermLoadConfidence.LOW,
					detail = SyntheticTermLoadDetail.PERSONAL_HISTORY_LIMITED
				)
			)
		}

		onNodeWithText("Manejable").assertIsDisplayed()

		assertInfoSays("Estimación basada en pocos trimestres de tu historial. Confianza baja. Se usó 1 trimestre.")
	}

	@Test
	fun when_theEstimateBorrowsFromTheCareer_then_theInfoSaysItIsAReference_withNoTermsToCount() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			// No detail: the basis alone says where the estimate comes from.
			LoadChip(
				loadPreview = available(SyntheticTermLoadBand.NORMAL).copy(basis = SyntheticTermLoadBasis.CAREER)
			)
		}

		assertInfoSays("Estimación referencial basada en estudiantes de tu carrera. Confianza media.")
	}

	@Test
	fun when_aSelectedSubjectIsUnknown_then_theChipHasNoData_andTheInfoNamesTheReason() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			// An older server names the reason only as text: it is read the same.
			LoadChip(loadPreview = SyntheticTermLoadPreview(available = false, reason = "UNKNOWN_SUBJECT"))
		}

		onNodeWithText("Sin datos").assertIsDisplayed()

		assertInfoSays("No pudimos estimar la carga porque no encontramos una materia seleccionada.")
	}

	@Test
	fun when_theInfoIsTappedAgain_then_itsMessageIsPutAway() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			LoadChip(loadPreview = null)
		}

		// Subjects selected and nothing estimated yet: there is no data to show.
		onNodeWithText("Sin datos").assertIsDisplayed()
		assertInfoSays("No pudimos estimar la carga con los datos disponibles.")

		onNodeWithTag(RecordUiTags.CreateSyntheticTermLoadInfoButton).performClick()

		waitUntil(timeoutMillis = 5_000) {
			onAllNodesWithText("No pudimos estimar la carga con los datos disponibles.")
				.fetchSemanticsNodes()
				.isEmpty()
		}
	}

	private fun ComposeUiTest.assertInfoSays(message: String) {
		onNodeWithTag(RecordUiTags.CreateSyntheticTermLoadInfoButton).performClick()

		assertNodeVisible(RecordUiTags.CreateSyntheticTermLoadInfoMessage)
		onNodeWithTag(RecordUiTags.CreateSyntheticTermLoadInfoMessage).assertTextEquals(message)
	}

	@Composable
	private fun LoadChip(
		loadPreview: SyntheticTermLoadPreview?,
		hasSelectedSubjects: Boolean = true,
		isLoading: Boolean = false,
		hasError: Boolean = false
	) {
		CreateTermLoadChip(
			loadPreview = loadPreview,
			hasSelectedSubjects = hasSelectedSubjects,
			isLoading = isLoading,
			hasError = hasError
		)
	}

	private fun available(band: SyntheticTermLoadBand) = SyntheticTermLoadPreview(available = true, band = band)
}
