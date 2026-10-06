package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class QualitativeStatusSelectorUiTest {
	@Test
	fun when_nothingIsSelected_then_thePlaceholderInvitesToChoose_andTheOptionsStayFolded() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Selector(selectedItem = null)
		}

		assertNodeVisible(RecordUiTags.attemptStatusSelector(AttemptId))
		onNodeWithTag(RecordUiTags.attemptStatusSelector(AttemptId)).assertTextContains("Seleccionar")
		Options.forEach { option ->
			assertNodeHidden(optionTag(option.outcome))
			// No status is selected, so no value answers to any of them either.
			assertNodeHidden(valueTag(option.outcome), useUnmergedTree = true)
		}
	}

	@Test
	fun when_aStatusIsSelected_then_itsLabelTakesThePlaceholdersPlace() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Selector(selectedItem = Options.first())
		}

		onNodeWithTag(RecordUiTags.attemptStatusSelector(AttemptId)).assertTextContains("Aprobada")
		onNodeWithTag(valueTag(AttemptOutcome.APPROVED), useUnmergedTree = true)
			.assertIsDisplayed()
			.assertTextEquals("Aprobada")
		onAllNodesWithText("Seleccionar").assertCountEquals(0)
	}

	@Test
	fun when_theSelectorIsTapped_then_everyOptionIsOffered_andTappingItAgainFoldsThem() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Selector(selectedItem = null)
		}

		onNodeWithTag(RecordUiTags.attemptStatusSelector(AttemptId)).performClick()

		onNodeWithTag(optionTag(AttemptOutcome.APPROVED)).assertIsDisplayed().assertTextEquals("Aprobada")
		onNodeWithTag(optionTag(AttemptOutcome.FAILED)).assertIsDisplayed().assertTextEquals("Reprobada")
		onNodeWithTag(optionTag(AttemptOutcome.RETIRED)).assertIsDisplayed().assertTextEquals("Retirada")

		onNodeWithTag(RecordUiTags.attemptStatusSelector(AttemptId)).performClick()
		waitForIdle()

		Options.forEach { option -> assertNodeHidden(optionTag(option.outcome)) }
	}

	@Test
	fun when_anOptionIsChosen_then_itsOutcomeIsReported_andTheOptionsFold() = runTuIndiceUiTest {
		val reported = mutableListOf<AttemptOutcome>()

		setTuIndiceTestContent {
			Selector(selectedItem = Options.first(), onSelected = { outcome -> reported += outcome })
		}

		onNodeWithTag(RecordUiTags.attemptStatusSelector(AttemptId)).performClick()
		onNodeWithTag(optionTag(AttemptOutcome.RETIRED)).performClick()
		waitForIdle()

		assertEquals(listOf(AttemptOutcome.RETIRED), reported)
		assertNodeHidden(optionTag(AttemptOutcome.RETIRED))
		// The selector shows the status it is handed: applying the choice is the caller's.
		onNodeWithTag(RecordUiTags.attemptStatusSelector(AttemptId)).assertTextContains("Aprobada")
	}

	@Test
	fun when_theSubjectCarriesMetadata_then_itIsShownUnderTheSelector() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Selector(selectedItem = Options.first(), metadataText = "Sin efecto")
		}

		val selector = onNodeWithTag(RecordUiTags.attemptStatusSelector(AttemptId)).getUnclippedBoundsInRoot()
		val metadata = onNodeWithText("Sin efecto").assertIsDisplayed().getUnclippedBoundsInRoot()

		assertTrue(selector.bottom <= metadata.top, "what qualifies the status goes under it")
	}

	@Composable
	private fun Selector(
		selectedItem: QualitativeStatusDropdownItem?,
		metadataText: String? = null,
		onSelected: (AttemptOutcome) -> Unit = {}
	) {
		QualitativeStatusSelector(
			attemptId = AttemptId,
			selectedItem = selectedItem,
			placeholderText = "Seleccionar",
			metadataText = metadataText,
			items = Options,
			onSelected = onSelected
		)
	}

	private fun optionTag(outcome: AttemptOutcome) = RecordUiTags.attemptStatusOption(
		attemptId = AttemptId,
		status = outcome.name.lowercase()
	)

	private fun valueTag(outcome: AttemptOutcome) = RecordUiTags.attemptStatusValue(
		attemptId = AttemptId,
		status = outcome.name.lowercase()
	)

	private companion object {
		const val AttemptId = "attempt-1"

		val Options = listOf(
			QualitativeStatusDropdownItem(outcome = AttemptOutcome.APPROVED, label = "Aprobada"),
			QualitativeStatusDropdownItem(outcome = AttemptOutcome.FAILED, label = "Reprobada"),
			QualitativeStatusDropdownItem(outcome = AttemptOutcome.RETIRED, label = "Retirada")
		)
	}
}
