package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCardItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryFinishedTermItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.presentation.model.SummaryTermItem
import com.gdavidpb.tuindice.summary.testing.summaryHubItem
import com.gdavidpb.tuindice.summary.testing.summaryPointsItem
import com.gdavidpb.tuindice.summary.testing.summaryTermWeekItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SummaryTermViewUiTest {
	private val term = summaryHubItem().term!!

	@Test
	fun when_termHasAWeek_then_itShowsTheWeekTheTrackAndEachSubjectsPoints() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryTermView(term = term, actions = SummaryHubActions())
		}

		assertNodeVisible(SummaryUiTags.TermSection)
		onNodeWithText("Trimestre").assertIsDisplayed()
		onNodeWithText("Sep - Dic 2026").assertIsDisplayed()
		onNodeWithText("Semana 3 de 12").assertIsDisplayed()
		onNodeWithText("faltan 9 semanas").assertIsDisplayed()
		onNodeWithTag(SummaryUiTags.TermWeekTrack).assertContentDescriptionEquals("Semana 3 de 12")
		onNodeWithText("3 materias · 11 UC").assertIsDisplayed()
		onNodeWithText("41 de 60 puntos").assertIsDisplayed()
		onNodeWithText("18 de 25 puntos").assertIsDisplayed()
		onNodeWithText("Sin notas aún").assertIsDisplayed()
		assertNodeVisible(SummaryUiTags.termPointsRow("EG1114"))
	}

	@Test
	fun when_termHasNoWeek_then_onlyThePointsAndTheirLineRemain() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryTermView(term = term.copy(week = null), actions = SummaryHubActions())
		}

		onNodeWithTag(SummaryUiTags.TermWeekText).assertDoesNotExist()
		onNodeWithTag(SummaryUiTags.TermWeekRemaining).assertDoesNotExist()
		onNodeWithTag(SummaryUiTags.TermWeekTrack).assertDoesNotExist()
		onNodeWithText("3 materias · 11 UC").assertIsDisplayed()
		onNodeWithText("41 de 60 puntos").assertIsDisplayed()
	}

	@Test
	fun when_termHasNoPointsYet_then_itSaysNothingOfTheSubjects() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryTermView(term = term.copy(subjects = emptyList(), summaryText = null), actions = SummaryHubActions())
		}

		onNodeWithText("Semana 3 de 12").assertIsDisplayed()
		onNodeWithTag(SummaryUiTags.TermSummary).assertDoesNotExist()
		onNodeWithText("Sin notas aún").assertDoesNotExist()
	}

	@Test
	fun when_termHasAWeek_then_theWeeksLeftSitOnTheBaselineOfTheFigure() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryTermView(term = term, actions = SummaryHubActions())
		}

		val figure = baselineOf(SummaryUiTags.TermWeekText)
		val remaining = baselineOf(SummaryUiTags.TermWeekRemaining)

		assertTrue(abs(figure - remaining) <= 1f, "the weeks left sit at $remaining, the figure at $figure")
	}

	@Test
	fun when_termIsInItsEleventhWeek_then_itSaysOneWeekIsLeft() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryTermView(term = term.copy(week = summaryTermWeekItem(week = 11)), actions = SummaryHubActions())
		}

		onNodeWithText("falta 1 semana").assertIsDisplayed()
	}

	@Test
	fun when_termIsInItsLastWeek_then_itSaysThatItIsTheLastOne() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryTermView(term = term.copy(week = summaryTermWeekItem(week = 12)), actions = SummaryHubActions())
		}

		onNodeWithText("Semana 12 de 12").assertIsDisplayed()
		onNodeWithText("última semana").assertIsDisplayed()
	}

	@Test
	fun when_theLinkIsTapped_then_itAsksForTheRecord() = runTuIndiceUiTest {
		val targets = mutableListOf<SummaryHubTarget>()

		setTuIndiceTestContent {
			SummaryTermView(term = term, actions = SummaryHubActions(onTargetClick = { target -> targets += target }))
		}

		onNodeWithTag(SummaryUiTags.sectionLink(SummaryUiTags.TermSection)).performClick()

		assertEquals(listOf<SummaryHubTarget>(SummaryHubTarget.Record), targets)
	}

	@Test
	fun when_aSubjectRowIsTapped_then_itsAttemptIsAskedForOnRecord() = runTuIndiceUiTest {
		val targets = mutableListOf<SummaryHubTarget>()

		setTuIndiceTestContent {
			SummaryTermView(term = term, actions = SummaryHubActions(onTargetClick = { target -> targets += target }))
		}

		onNodeWithTag(SummaryUiTags.termPointsRow("CI5437")).performClick()

		assertEquals(
			listOf<SummaryHubTarget>(SummaryHubTarget.RecordAttempt(attemptId = "attempt-CI5437")),
			targets
		)
	}

	@Test
	fun when_termHasNoWeek_then_theCardKeepsTheSameSpaceAboveItsFirstLineAndBelowItsLastRow() = runTuIndiceUiTest {
		val pointsOnly = term.copy(
			week = null,
			summaryText = UiText.Raw("2 materias · 7 UC"),
			subjects = listOf(
				summaryPointsItem(subjectCode = "CI5311", earned = "41"),
				summaryPointsItem(subjectCode = "CI5437", earned = "18", evaluated = "25")
			)
		)
		lateinit var density: Density

		setTuIndiceTestContent {
			density = LocalDensity.current
			SummaryTermView(term = pointsOnly, actions = SummaryHubActions())
		}

		val spaces = cardSpaces(
			density = density,
			firstTag = SummaryUiTags.TermSummary,
			lastTag = SummaryUiTags.termPointsBar("CI5437")
		)

		assertSameSpace(spaces, density)
	}

	@Test
	fun when_termHasNoPointsYet_then_theCardKeepsTheSameSpaceAboveItsWeekAndBelowItsTrack() = runTuIndiceUiTest {
		lateinit var density: Density

		setTuIndiceTestContent {
			density = LocalDensity.current
			SummaryTermView(term = term.copy(subjects = emptyList(), summaryText = null), actions = SummaryHubActions())
		}

		val track = onNodeWithTag(SummaryUiTags.TermWeekTrack).fetchSemanticsNode().boundsInRoot
		val week = onNodeWithText("Semana 3 de 12").fetchSemanticsNode().boundsInRoot
		val spaces = Pair(week.top - cardTop(density), sectionBottom() - track.bottom)

		assertSameSpace(spaces, density)
	}

	// The card starts one spacing under the header, which is as tall as its link.
	@Test
	fun when_noTermIsInProgress_then_theLinkAsksForRecord_theLastFinishedOneLeadsThere_andThePlanAsksForItsForm() =
		runTuIndiceUiTest {
			val targets = mutableListOf<SummaryHubTarget>()

			setTuIndiceTestContent {
				SummaryTermView(
					term = SummaryTermItem(
						linkText = UiText.Raw("Registro"),
						week = null,
						summaryText = null,
						subjects = emptyList(),
						finished = SummaryFinishedTermItem(
							nameText = UiText.Raw("Abr - Jul 2026"),
							stateText = UiText.Raw("terminado"),
							indexLabel = UiText.Raw("Índice del trimestre"),
							indexText = "14.5000",
							creditsLabel = UiText.Raw("Créditos aprobados"),
							creditsText = UiText.Raw("16 UC"),
							termKey = "2026-APR_JUL",
							isShareable = true
						),
						card = SummaryCardItem(
							icon = Icons.AutoMirrored.Outlined.NoteAdd,
							titleText = UiText.Raw("Planifica tu próximo trimestre"),
							messageText = UiText.Raw("Arma un trimestre con las materias que puedes inscribir."),
							action = SummaryCardItem.Action(
								text = UiText.Raw("Planificar"),
								target = SummaryHubTarget.CreateTerm
							)
						)
					),
					actions = SummaryHubActions(onTargetClick = { target -> targets += target })
				)
			}

			assertNodeVisible(SummaryUiTags.TermFinishedCard)
			assertNodeVisible(SummaryUiTags.TermCard)
			onNodeWithTag(SummaryUiTags.sectionLink(SummaryUiTags.TermSection)).performClick()
			onNodeWithTag(SummaryUiTags.TermFinishedCard).performClick()
			onNodeWithTag(SummaryUiTags.CardAction).performClick()
			assertEquals(
				listOf(SummaryHubTarget.Record, SummaryHubTarget.Record, SummaryHubTarget.CreateTerm),
				targets
			)
		}

	// The share button of the last term that is over asks for the sheet on that term; the card still leads to Registro.
	@Test
	fun when_theFinishedTermCanBeShared_then_itsButtonAsksForTheSheetOnThatTerm() = runTuIndiceUiTest {
		val targets = mutableListOf<SummaryHubTarget>()

		setTuIndiceTestContent {
			SummaryTermView(
				term = SummaryTermItem(
					linkText = UiText.Raw("Registro"),
					week = null,
					summaryText = null,
					subjects = emptyList(),
					finished = SummaryFinishedTermItem(
						nameText = UiText.Raw("Abr - Jul 2026"),
						stateText = UiText.Raw("terminado"),
						indexLabel = UiText.Raw("Índice del trimestre"),
						indexText = "14.5000",
						creditsLabel = UiText.Raw("Créditos aprobados"),
						creditsText = UiText.Raw("16 UC"),
						termKey = "2026-APR_JUL",
						isShareable = true
					),
					card = null
				),
				actions = SummaryHubActions(onTargetClick = { target -> targets += target })
			)
		}

		onNodeWithTag(SummaryUiTags.TermFinishedShare).performClick()

		assertEquals(listOf<SummaryHubTarget>(SummaryHubTarget.ShareTerm(termKey = "2026-APR_JUL")), targets)
	}

	@Test
	fun when_theTermHasNothingToShowYet_then_itsCardSaysWhatIsStillToCome() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryTermView(
				term = term.copy(
					week = null,
					subjects = emptyList(),
					summaryText = null,
					card = SummaryCardItem(
						icon = Icons.AutoMirrored.Outlined.Assignment,
						titleText = UiText.Raw("Tus evaluaciones"),
						messageText = UiText.Raw("Ábrelas en Evaluaciones para verlas aquí.")
					)
				),
				actions = SummaryHubActions()
			)
		}

		assertNodeVisible(SummaryUiTags.TermCard)
		onNodeWithTag(SummaryUiTags.TermWeekText).assertDoesNotExist()
		onNodeWithText("Ábrelas en Evaluaciones para verlas aquí.").assertIsDisplayed()
	}

	private fun ComposeUiTest.cardTop(density: Density): Float {
		val link = onNodeWithTag(SummaryUiTags.sectionLink(SummaryUiTags.TermSection)).fetchSemanticsNode()

		return link.boundsInRoot.bottom + with(density) { TuIndiceSpacing.Medium.toPx() }
	}

	private fun ComposeUiTest.sectionBottom(): Float {
		return onNodeWithTag(SummaryUiTags.TermSection).fetchSemanticsNode().boundsInRoot.bottom
	}

	// The spaces are measured to the content itself, the last one being the bar of the last row: a row's box keeps the
	// padding it has inside it.
	private fun ComposeUiTest.cardSpaces(
		density: Density,
		firstTag: String,
		lastTag: String
	): Pair<Float, Float> {
		val first = onNodeWithTag(firstTag).fetchSemanticsNode().boundsInRoot
		val last = onNodeWithTag(lastTag, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

		return Pair(first.top - cardTop(density), sectionBottom() - last.bottom)
	}

	// The baseline of the first line of a text, measured from the top of the screen.
	private fun ComposeUiTest.baselineOf(tag: String): Float {
		val node = onNodeWithTag(tag, useUnmergedTree = true).fetchSemanticsNode()
		val layouts = mutableListOf<TextLayoutResult>()

		node.config[SemanticsActions.GetTextLayoutResult].action?.invoke(layouts)

		return node.boundsInRoot.top + layouts.single().getLineBaseline(0)
	}

	private fun assertSameSpace(spaces: Pair<Float, Float>, density: Density) {
		val (above, below) = spaces

		assertTrue(
			abs(above - below) <= with(density) { 1.dp.toPx() },
			"the card leaves $above px above its content and $below px below it"
		)
	}
}
