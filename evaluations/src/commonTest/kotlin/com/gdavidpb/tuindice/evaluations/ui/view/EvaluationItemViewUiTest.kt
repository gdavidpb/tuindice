package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import com.gdavidpb.tuindice.base.presentation.mapper.classPlaceItem
import com.gdavidpb.tuindice.base.presentation.model.ClassPlaceItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.evaluations.testing.FIXTURE_OVERDUE_DATE_TEXT
import com.gdavidpb.tuindice.evaluations.testing.evaluationItemFixture
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class EvaluationItemViewUiTest {
	@Test
	fun when_rendered_then_displaysEvaluationData() = runTuIndiceUiTest {
		val item = evaluationItemFixture(
			evaluationId = "evaluation-item-42",
			isOverdue = true
		)

		setTuIndiceTestContent {
			EvaluationItemView(item = item)
		}

		assertNodeVisible(EvaluationsUiTags.evaluationItemCard(item.evaluationId))
		assertTrue(onAllNodesWithText(item.subjectNameText).fetchSemanticsNodes().isEmpty())
		onNodeWithText(item.subjectCodeText).assertIsDisplayed()
		onNodeWithText(FIXTURE_OVERDUE_DATE_TEXT).assertIsDisplayed()
		onNodeWithText(item.typeNameText).assertIsDisplayed()
		onNodeWithText(item.statusText).assertIsDisplayed()
		onNodeWithText(item.gradeText).assertIsDisplayed()
		onNodeWithTag(
			testTag = EvaluationsUiTags.EvaluationStatusChip,
			useUnmergedTree = true
		).assertIsDisplayed()
		onNodeWithTag(
			testTag = EvaluationsUiTags.EvaluationTypeInlineIcon,
			useUnmergedTree = true
		).assertIsDisplayed()
		assertNodeHidden(EvaluationsUiTags.EvaluationTypeLeadingIcon, useUnmergedTree = true)
		assertNodeVisible(EvaluationsUiTags.evaluationGradeActionButton(item.evaluationId))
	}

	@Test
	fun when_gradeActionHidden_then_doesNotDisplayGradeButton() = runTuIndiceUiTest {
		val item = evaluationItemFixture(showsGradeAction = false)

		setTuIndiceTestContent {
			EvaluationItemView(item = item)
		}

		assertNodeHidden(EvaluationsUiTags.evaluationGradeActionButton(item.evaluationId))
	}

	@Test
	fun when_gradeButtonTapped_then_invokesGradeCallback() = runTuIndiceUiTest {
		val item = evaluationItemFixture()
		var gradeClicks = 0

		setTuIndiceTestContent {
			EvaluationItemView(
				item = item,
				onGradeClick = { gradeClicks++ }
			)
		}

		onNodeWithTag(EvaluationsUiTags.evaluationGradeActionButton(item.evaluationId)).performClick()

		assertEquals(1, gradeClicks)
	}

	@Test
	fun when_cardTapped_then_invokesCardCallback() = runTuIndiceUiTest {
		val item = evaluationItemFixture()
		var cardClicks = 0

		setTuIndiceTestContent {
			EvaluationItemView(
				item = item,
				onCardClick = { cardClicks++ }
			)
		}

		onNodeWithTag(EvaluationsUiTags.evaluationItemCard(item.evaluationId)).performClick()

		assertEquals(1, cardClicks)
	}

	@Test
	fun when_theEvaluationInheritsAPlace_then_showsTheBlocksAndTheHourUnderTheDate() = runTuIndiceUiTest {
		val item = evaluationItemFixture().copy(
			place = ClassPlaceItem(
				placeText = UiText.Raw("Bloques 1-2 · MYS-116"),
				timeText = UiText.Raw("7:30\u20139:30 a. m.")
			)
		)

		setTuIndiceTestContent {
			EvaluationItemView(item = item)
		}

		// The card merges its children: the line is read from the unmerged tree, like the status chip.
		onNodeWithTag(
			testTag = EvaluationsUiTags.evaluationPlace(item.evaluationId),
			useUnmergedTree = true
		).assertIsDisplayed()
		onNodeWithText("Bloques 1-2 · MYS-116").assertIsDisplayed()
		onNodeWithText("7:30\u20139:30 a. m.").assertIsDisplayed()
	}

	@Test
	fun when_theEvaluationInheritsNoPlace_then_hasNoPlaceLine() = runTuIndiceUiTest {
		val item = evaluationItemFixture()

		setTuIndiceTestContent {
			EvaluationItemView(item = item)
		}

		assertNodeHidden(EvaluationsUiTags.evaluationPlace(item.evaluationId), useUnmergedTree = true)
	}

	@Test
	fun when_theRowHasAPlace_then_theGradeButtonKeepsTheSizeItHasWithoutOne() = runTuIndiceUiTest {
		val plain = evaluationItemFixture(evaluationId = "plain")
		val placed = evaluationItemFixture(evaluationId = "placed").copy(
			place = classPlaceItem(startBlock = 1, endBlock = 2, classroom = "MYS-116", clockRange = 450..570)
		)

		setTuIndiceTestContent {
			Column(modifier = Modifier.width(360.dp)) {
				EvaluationItemView(item = plain)
				EvaluationItemView(item = placed)
			}
		}

		// The place line wraps inside the space the button leaves, it never squeezes the button.
		assertEquals(gradeButtonBounds("plain").size(), gradeButtonBounds("placed").size())
	}

	@Test
	fun when_theRoomIsLong_then_thePlaceIsShownWhole_andTheGradeButtonKeepsItsSize() = runTuIndiceUiTest {
		val plain = evaluationItemFixture(evaluationId = "plain")
		val placed = evaluationItemFixture(evaluationId = "placed").copy(
			place = classPlaceItem(startBlock = 10, endBlock = 11, classroom = "LAB-FIS-201", clockRange = 990..1110)
		)

		setTuIndiceTestContent {
			Column(modifier = Modifier.width(360.dp)) {
				EvaluationItemView(item = plain)
				EvaluationItemView(item = placed)
			}
		}

		assertEquals(gradeButtonBounds("plain").size(), gradeButtonBounds("placed").size())

		val place = onNodeWithText("Bloques 10-11 · LAB-FIS-201", useUnmergedTree = true)
		val layout = mutableListOf<TextLayoutResult>()

		place.assertIsDisplayed()
		place.fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action?.invoke(layout)

		// Whole: nothing cut with an ellipsis, however many lines it takes.
		assertFalse(layout.single().hasVisualOverflow, "the place must not be cut")
		onNodeWithText("4:30–6:30 p. m.", useUnmergedTree = true).assertIsDisplayed()
	}

	@Test
	fun when_theLetterIsLargeOnANarrowScreen_then_theDateIsWhole_andTheGradeButtonIsNotSqueezed() = runTuIndiceUiTest {
		val long = evaluationItemFixture(evaluationId = "long").copy(dateText = UiText.Raw(LONG_DATE))
		val short = evaluationItemFixture(evaluationId = "short")

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalDensity provides Density(1f, fontScale = 1.3f)) {
				Column(modifier = Modifier.width(360.dp)) {
					EvaluationItemView(item = long)
					EvaluationItemView(item = short)
				}
			}
		}

		assertDateIsWhole("long")
		// The button takes what it needs first; a long date goes to the next line instead of narrowing it.
		assertEquals(gradeButtonBounds("short").size(), gradeButtonBounds("long").size())
	}

	@Test
	fun when_theScreenIsAsNarrowAs320dp_then_theDateIsShownWhole() = runTuIndiceUiTest {
		val item = evaluationItemFixture(evaluationId = "long").copy(dateText = UiText.Raw(LONG_DATE))

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalDensity provides Density(1f)) {
				Box(modifier = Modifier.width(320.dp)) {
					EvaluationItemView(item = item)
				}
			}
		}

		assertDateIsWhole("long")
	}

	// The date laid out whole and inside the card that holds it: every character of it is in a line (a
	// text held to one line that does not fit drops the rest), no line is cut with an ellipsis or runs
	// past the box the row gave it, and the box lies inside the card.
	private fun ComposeUiTest.assertDateIsWhole(evaluationId: String) {
		val date = onNodeWithText(LONG_DATE, useUnmergedTree = true)
		val layout = mutableListOf<TextLayoutResult>()

		date.assertIsDisplayed()
		date.fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action?.invoke(layout)

		val result = layout.single()
		val boxWidth = date.fetchSemanticsNode().size.width
		val lines = (0 until result.lineCount)

		assertFalse(result.multiParagraph.didExceedMaxLines, "the date must not lose its last lines")
		assertEquals(LONG_DATE.length, result.getLineEnd(result.lineCount - 1), "the lines hold the whole date")
		assertTrue(lines.none { line -> result.isLineEllipsized(line) }, "the date must not be cut with an ellipsis")
		assertTrue(
			lines.all { line -> result.getLineRight(line) <= boxWidth + 0.5f },
			"every line of the date fits the box it was given: ${lines.map { line -> result.getLineRight(line) }} in $boxWidth"
		)

		val card = onNodeWithTag(EvaluationsUiTags.evaluationItemCard(evaluationId)).getUnclippedBoundsInRoot()
		val bounds = date.getUnclippedBoundsInRoot()

		assertTrue(
			bounds.left >= card.left && bounds.right <= card.right && bounds.top >= card.top && bounds.bottom <= card.bottom,
			"the date at $bounds lies inside the card at $card"
		)
	}

	private fun ComposeUiTest.gradeButtonBounds(evaluationId: String): DpRect {
		return onNodeWithTag(EvaluationsUiTags.evaluationGradeActionButton(evaluationId), useUnmergedTree = true)
			.getUnclippedBoundsInRoot()
	}

	private fun DpRect.size() = DpSize(width = width, height = height)

	private companion object {
		const val LONG_DATE = "Miércoles — 25/11/26"
	}
}
