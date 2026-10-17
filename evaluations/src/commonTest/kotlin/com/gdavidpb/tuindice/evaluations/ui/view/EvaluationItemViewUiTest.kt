package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
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
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.presentation.mapper.classPlaceItem
import com.gdavidpb.tuindice.base.presentation.model.ClassPlaceItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.presentation.navigation.AssistantLauncher
import com.gdavidpb.tuindice.base.presentation.navigation.LocalAssistantLauncher
import com.gdavidpb.tuindice.base.ui.view.arrivalHighlight
import com.gdavidpb.tuindice.evaluations.testing.FIXTURE_OVERDUE_DATE_TEXT
import com.gdavidpb.tuindice.evaluations.testing.evaluationItemFixture
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.advanceAnimationsBy
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.math.abs
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
	fun when_theSubjectIsGiven_then_itsChipIsBesideTheNameOnItsLine() = runTuIndiceUiTest {
		val item = evaluationItemFixture(evaluationId = "chip")

		setTuIndiceTestContent {
			EvaluationItemView(item = item)
		}

		val name = onNodeWithText(item.typeNameText, useUnmergedTree = true).getUnclippedBoundsInRoot()
		val chip = onNodeWithText(item.subjectCodeText, useUnmergedTree = true).getUnclippedBoundsInRoot()

		assertTrue(chip.left >= name.right, "the subject's chip is written after the name")
		assertTrue(abs(centerY(chip) - centerY(name)) <= 4f, "the chip shares the line of the name")
	}

	@Test
	fun when_theEvaluationInheritsAPlace_then_showsTheRoomAndTheScheduleUnderTheDate() = runTuIndiceUiTest {
		val item = evaluationItemFixture(isOverdue = true).copy(
			place = ClassPlaceItem(
				room = "MYS-116",
				scheduleText = UiText.Raw("1-2 · 7:30–9:30 a. m.")
			)
		)

		setTuIndiceTestContent {
			EvaluationItemView(item = item)
		}

		// The card merges its children: the lines are read from the unmerged tree, like the status chip.
		onNodeWithTag(
			testTag = EvaluationsUiTags.evaluationPlace(item.evaluationId),
			useUnmergedTree = true
		).assertIsDisplayed()
		onNodeWithText("MYS-116", useUnmergedTree = true).assertIsDisplayed()
		onNodeWithText("1-2 · 7:30–9:30 a. m.", useUnmergedTree = true).assertIsDisplayed()

		val date = onNodeWithText(FIXTURE_OVERDUE_DATE_TEXT, useUnmergedTree = true).getUnclippedBoundsInRoot()
		val name = onNodeWithText(item.typeNameText, useUnmergedTree = true).getUnclippedBoundsInRoot()
		val room = onNodeWithText("MYS-116", useUnmergedTree = true).getUnclippedBoundsInRoot()
		val schedule = onNodeWithText("1-2 · 7:30–9:30 a. m.", useUnmergedTree = true).getUnclippedBoundsInRoot()

		// The room goes under the date and the schedule under the room, and each one starts where the name starts.
		assertTrue(room.top >= date.bottom, "the room must be under the date")
		assertTrue(schedule.top >= room.bottom, "the schedule must be under the room")
		assertTrue(abs(room.left.value - name.left.value) <= 0.5f, "the room must start where the name starts")
		assertTrue(abs(schedule.left.value - name.left.value) <= 0.5f, "the schedule must start where the name starts")
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

		// The place lines wrap inside the space the button leaves, they never squeeze the button.
		assertEquals(gradeButtonBounds("plain").size(), gradeButtonBounds("placed").size())
	}

	@Test
	fun when_theRoomIsLong_then_thePlaceIsShownWhole_andTheGradeButtonKeepsItsSize() = runTuIndiceUiTest {
		val plain = evaluationItemFixture(evaluationId = "plain")
		val placed = evaluationItemFixture(evaluationId = "placed").copy(
			place = classPlaceItem(startBlock = 10, endBlock = 11, classroom = LONG_ROOM, clockRange = 990..1110)
		)

		setTuIndiceTestContent {
			Column(modifier = Modifier.width(360.dp)) {
				EvaluationItemView(item = plain)
				EvaluationItemView(item = placed)
			}
		}

		assertEquals(gradeButtonBounds("plain").size(), gradeButtonBounds("placed").size())

		// Whole: nothing cut with an ellipsis, however many lines it takes.
		assertTextIsWhole(text = LONG_ROOM, evaluationId = "placed")
		assertTextIsWhole(text = "10-11 · 4:30–6:30 p. m.", evaluationId = "placed")
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
	fun when_theNameIsLongAndTheLetterIsLarge_then_theNameWraps_andTheChipStaysWhole() = runTuIndiceUiTest {
		val item = evaluationItemFixture(evaluationId = "long-name").copy(typeNameText = LONG_NAME)

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalDensity provides Density(1f, fontScale = 1.3f)) {
				Box(modifier = Modifier.width(320.dp)) {
					EvaluationItemView(item = item)
				}
			}
		}

		assertTextIsWhole(text = LONG_NAME, evaluationId = "long-name")
		assertTextIsWhole(text = item.subjectCodeText, evaluationId = "long-name")

		// Beside the name there is no room at this width: the subject goes under it, and the name keeps its width.
		val name = onNodeWithText(LONG_NAME, useUnmergedTree = true).getUnclippedBoundsInRoot()
		val chip = onNodeWithText(item.subjectCodeText, useUnmergedTree = true).getUnclippedBoundsInRoot()

		assertTrue(chip.top >= name.bottom, "the subject goes under the name when it does not fit beside it")
		assertTrue(name.width >= NameMinWidth, "the name keeps the width the icon and the state leave it: ${name.width}")
	}

	@Test
	fun when_theNameWrapsAtA320dpScreen_then_theStateStaysOnTheFirstLineOfTheName() = runTuIndiceUiTest {
		val item = evaluationItemFixture(evaluationId = "state-first-line").copy(typeNameText = LONG_NAME)

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalDensity provides Density(1f, fontScale = 1.3f)) {
				Box(modifier = Modifier.width(320.dp)) {
					EvaluationItemView(item = item)
				}
			}
		}

		val name = onNodeWithText(LONG_NAME, useUnmergedTree = true)
		val layout = mutableListOf<TextLayoutResult>()

		name.fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action?.invoke(layout)

		val result = layout.single()
		val firstLineCenter = name.getUnclippedBoundsInRoot().top.value +
			(result.getLineTop(0) + result.getLineBottom(0)) / 2f
		val state = onNodeWithTag(EvaluationsUiTags.EvaluationStatusChip, useUnmergedTree = true)
			.getUnclippedBoundsInRoot()

		assertTrue(
			abs(centerY(state) - firstLineCenter) <= 6f,
			"the state is centered at ${centerY(state)}, not on the first line of the name at $firstLineCenter"
		)
	}

	@Test
	fun when_theSubjectIsBesideTheName_then_theStateIsLevelWithTheName() = runTuIndiceUiTest {
		val item = evaluationItemFixture(evaluationId = "state-level")

		setTuIndiceTestContent {
			Box(modifier = Modifier.width(360.dp)) {
				EvaluationItemView(item = item)
			}
		}

		val name = onNodeWithText(item.typeNameText, useUnmergedTree = true).getUnclippedBoundsInRoot()
		val state = onNodeWithTag(EvaluationsUiTags.EvaluationStatusChip, useUnmergedTree = true)
			.getUnclippedBoundsInRoot()

		assertTrue(abs(centerY(state) - centerY(name)) <= 1.5f, "the state and the name share the line")
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

	@Test
	fun when_anArrivalIsGiven_then_itsBorderLiesOnTheCardsEdge_andNotInTheMarginAroundIt() = runTuIndiceUiTest {
		val item = evaluationItemFixture(evaluationId = "arrived")

		setTuIndiceTestContent {
			MaterialTheme(colorScheme = lightColorScheme(primary = Accent, surfaceVariant = CardSurface)) {
				Box(modifier = Modifier.testTag(HostTag).background(Backdrop)) {
					EvaluationItemView(
						item = item,
						modifier = Modifier.arrivalHighlight(
							trigger = ARRIVAL,
							shape = MaterialTheme.shapes.medium,
							onFinished = {}
						)
					)
				}
			}
		}

		advanceAnimationsBy(FADE_IN_MILLIS)

		// The row keeps a 16dp margin on each side of the card; the border must not reach into it.
		assertEquals(Accent, pixelAtDp(xDp = MarginDp + BorderDp / 2), "the border is drawn on the card's edge")
		// Where a border drawn on the padded bounds would sit, at the row's outer edge.
		assertEquals(Backdrop, pixelAtDp(xDp = BorderDp / 2), "the outer edge of the margin stays clear")
	}

	// The pixel of the host's capture at [xDp] from its left edge, at the middle of its height. The test runs at
	// density 1, so a dp is a pixel: the root of the content is 360 x 640 pixels and a denser one would not fit.
	private fun ComposeUiTest.pixelAtDp(xDp: Float): Color {
		waitForIdle()

		val pixels = onNodeWithTag(HostTag).captureToImage().toPixelMap()

		return pixels[xDp.toInt(), pixels.height / 2]
	}

	private fun ComposeUiTest.assertDateIsWhole(evaluationId: String) {
		assertTextIsWhole(text = LONG_DATE, evaluationId = evaluationId)
	}

	// A text laid out whole and inside the card that holds it: every character of it is in a line (a
	// text held to one line that does not fit drops the rest), no line is cut with an ellipsis or runs
	// past the box the row gave it, and the box lies inside the card. It reads the lines and not
	// hasVisualOverflow, which on iOS is also true of a single line narrower than the width it was given.
	private fun ComposeUiTest.assertTextIsWhole(text: String, evaluationId: String) {
		val node = onNodeWithText(text, useUnmergedTree = true)
		val layout = mutableListOf<TextLayoutResult>()

		node.assertIsDisplayed()
		node.fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action?.invoke(layout)

		val result = layout.single()
		val boxWidth = node.fetchSemanticsNode().size.width
		val lines = (0 until result.lineCount)

		assertFalse(result.multiParagraph.didExceedMaxLines, "'$text' must not lose its last lines")
		assertEquals(text.length, result.getLineEnd(result.lineCount - 1), "the lines hold the whole '$text'")
		assertTrue(lines.none { line -> result.isLineEllipsized(line) }, "'$text' must not be cut with an ellipsis")
		assertTrue(
			lines.all { line -> result.getLineRight(line) <= boxWidth + 0.5f },
			"every line of '$text' fits the box it was given: ${lines.map { line -> result.getLineRight(line) }} in $boxWidth"
		)

		val card = onNodeWithTag(EvaluationsUiTags.evaluationItemCard(evaluationId)).getUnclippedBoundsInRoot()
		val bounds = node.getUnclippedBoundsInRoot()

		assertTrue(
			bounds.left >= card.left && bounds.right <= card.right && bounds.top >= card.top && bounds.bottom <= card.bottom,
			"'$text' at $bounds lies inside the card at $card"
		)
	}

	private fun ComposeUiTest.gradeButtonBounds(evaluationId: String): DpRect {
		return onNodeWithTag(EvaluationsUiTags.evaluationGradeActionButton(evaluationId), useUnmergedTree = true)
			.getUnclippedBoundsInRoot()
	}

	private fun centerY(bounds: DpRect): Float = (bounds.top.value + bounds.bottom.value) / 2f

	private fun DpRect.size() = DpSize(width = width, height = height)

	@Test
	fun when_assistantIsProvided_then_askLeadsTheAccessibilityActions() = runTuIndiceUiTest {
		val item = evaluationItemFixture(evaluationId = "evaluation-accessible")
		val asked = mutableListOf<AssistantScreenContext>()

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalAssistantLauncher provides AssistantLauncher { asked += it }) {
				EvaluationItemView(item = item, onEditAction = {}, onDeleteAction = {})
			}
		}

		val actions = accessibilityActionsOf(item.evaluationId)

		assertEquals(listOf("Preguntar", "Modificar", "Eliminar"), actions.map { action -> action.label })

		actions.first { action -> action.label == "Preguntar" }.action()

		assertEquals(listOf(item.assistantContext), asked)
	}

	@Test
	fun when_noAssistantIsProvided_then_theAccessibilityActionsAreEditAndDeleteOnly() = runTuIndiceUiTest {
		val item = evaluationItemFixture(evaluationId = "evaluation-accessible")

		setTuIndiceTestContent {
			EvaluationItemView(item = item, onEditAction = {}, onDeleteAction = {})
		}

		val labels = accessibilityActionsOf(item.evaluationId).map { action -> action.label }

		assertEquals(listOf("Modificar", "Eliminar"), labels)
	}

	private fun ComposeUiTest.accessibilityActionsOf(evaluationId: String): List<CustomAccessibilityAction> {
		return onNodeWithTag(EvaluationsUiTags.evaluationItemCard(evaluationId))
			.fetchSemanticsNode()
			.config[SemanticsActions.CustomActions]
	}

	private companion object {
		const val LONG_DATE = "Miércoles — 25/11/26"
		const val LONG_ROOM = "LAB-FIS-201 Edificio de Laboratorios"
		const val LONG_NAME = "Examen de recuperación del segundo parcial"
		const val HostTag = "evaluation_arrival_host"
		const val ARRIVAL = "arrival-1"
		const val FADE_IN_MILLIS = 300L
		const val MarginDp = 16f
		const val BorderDp = 2f
		val Accent = Color.Red
		val CardSurface = Color.Blue
		val Backdrop = Color.White
		val NameMinWidth = 40.dp
	}
}
