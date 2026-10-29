package com.gdavidpb.tuindice.summary.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityKind
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityRef
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantNavTarget
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.contract.TermReport
import com.gdavidpb.tuindice.summary.presentation.model.TermReportActionItem
import com.gdavidpb.tuindice.summary.testing.termReportItemFixture
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.TermReportUiTags
import com.gdavidpb.tuindice.summary.ui.model.TermReportActions
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class TermReportScreenUiTest {

	@Test
	fun when_theReportIsShown_then_itsPartsAreInTheOrderTheScreenDrawsThem() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermReportScreen(
				state = TermReport.State.Content(report = termReportItemFixture()),
				actions = TermReportActions()
			)
		}
		waitForIdle()

		val parts = listOf(
			TermReportUiTags.TermReportHero,
			TermReportUiTags.TermReportSubjects,
			TermReportUiTags.TermReportPace,
			TermReportUiTags.TermReportAssistant,
			TermReportUiTags.TermReportNextActions
		)
		val tops = parts.map { tag -> onNodeWithTag(tag).fetchSemanticsNode().positionInRoot.y }
		assertEquals(tops.sorted(), tops, "the figures, the subjects, the pace, the assistant and the actions, in order")

		val sections = (0..1).map { index ->
			onNodeWithTag(TermReportUiTags.termReportSection(index)).fetchSemanticsNode().positionInRoot.y
		}
		assertTrue(sections[0] < sections[1], "the sections of the assistant keep their order")
	}

	@Test
	fun when_theIndexChanges_then_theChipHoldsTheFigure_andTheTermBeforeIsWrittenUnderTheIndex() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermReportScreen(
				state = TermReport.State.Content(report = termReportItemFixture()),
				actions = TermReportActions()
			)
		}
		waitForIdle()

		onNodeWithTag(TermReportUiTags.TermReportIndexReference)
			.assertTextEquals("frente a Ene\u00A0-\u00A0Mar\u00A02025")
		onNodeWithTag(TermReportUiTags.TermReportIndexDelta)
			.assertContentDescriptionEquals("Subió 1.5000 frente a Ene\u00A0-\u00A0Mar\u00A02025")
		val indexTop = onNodeWithTag(TermReportUiTags.TermReportIndex).fetchSemanticsNode().positionInRoot.y
		val referenceTop = onNodeWithTag(TermReportUiTags.TermReportIndexReference)
			.fetchSemanticsNode().positionInRoot.y
		assertTrue(referenceTop > indexTop, "the term before is written under the index")
	}

	@Test
	fun when_theSubjectsAreShown_then_theirHeadingComesBeforeTheRows() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermReportScreen(
				state = TermReport.State.Content(report = termReportItemFixture()),
				actions = TermReportActions()
			)
		}
		waitForIdle()

		val heading = onNodeWithText("Materias").fetchSemanticsNode().positionInRoot.y
		val firstRow = onNodeWithTag(TermReportUiTags.termReportSubject("CI5311"))
			.fetchSemanticsNode().positionInRoot.y
		assertTrue(heading < firstRow, "the heading of the subjects is above the card of its rows")
		onNodeWithTag(TermReportUiTags.TermReportCredits).assertTextEquals("11 de 14 créditos")
	}

	@Test
	fun when_theAverageIsDrawn_then_itsLineHasNoLabel_andTheLegendWritesIt() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermReportScreen(
				state = TermReport.State.Content(report = termReportItemFixture()),
				actions = TermReportActions()
			)
		}
		waitForIdle()

		onNodeWithTag(SummaryUiTags.StatisticsReferenceLabel).assertDoesNotExist()
		onNodeWithText("Promedio: 12,7").performScrollTo().assertIsDisplayed()
	}

	@Test
	fun when_aThumbIsTapped_then_itIsAskedAndTheChosenOneIsShown() = runTuIndiceUiTest {
		val rated = mutableListOf<Boolean>()

		setTuIndiceTestContent {
			TermReportScreen(
				state = TermReport.State.Content(report = termReportItemFixture(), feedback = true),
				actions = TermReportActions(onRateClick = { useful -> rated += useful })
			)
		}

		onNodeWithTag(TermReportUiTags.termReportThumb(useful = true)).assertIsSelected()
		onNodeWithTag(TermReportUiTags.termReportThumb(useful = false)).assertIsNotSelected()

		onNodeWithTag(TermReportUiTags.termReportThumb(useful = false)).performScrollTo().performClick()

		assertEquals(listOf(false), rated)
	}

	@Test
	fun when_aButtonOfWhatFollowsIsTapped_then_itsActionIsAskedWithItsTarget() = runTuIndiceUiTest {
		val asked = mutableListOf<TermReportActionItem>()

		setTuIndiceTestContent {
			TermReportScreen(
				state = TermReport.State.Content(report = termReportItemFixture()),
				actions = TermReportActions(onNextActionClick = { action -> asked += action })
			)
		}

		onNodeWithTag(TermReportUiTags.termReportNextAction(1)).performScrollTo().performClick()

		assertEquals(AssistantNavTarget.SHARE_TERM, asked.single().target)
	}

	@Test
	fun when_theAssistantIsThere_then_askingOpensTheChatAboutTheReport() = runTuIndiceUiTest {
		val launched = mutableListOf<AssistantScreenContext>()

		setTuIndiceTestContent {
			TermReportScreen(
				state = TermReport.State.Content(report = termReportItemFixture()),
				actions = TermReportActions(onAskAssistant = { context -> launched += context })
			)
		}

		onNodeWithTag(TermReportUiTags.TermReportAskButton).performScrollTo().performClick()

		val context = launched.single()
		assertEquals(AssistantScreen.TERM_REPORT, context.screen)
		assertEquals(listOf(AssistantEntityRef(kind = AssistantEntityKind.TERM, id = "term-1")), context.entities)
		assertEquals("Abr - Jul 2026", context.visibleFields["termLabel"])
	}

	@Test
	fun when_theAssistantIsNotThere_then_theQuestionIsNotOffered() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermReportScreen(
				state = TermReport.State.Content(report = termReportItemFixture()),
				actions = TermReportActions()
			)
		}

		onNodeWithTag(TermReportUiTags.TermReportAskButton).assertDoesNotExist()
	}

	@Test
	fun when_theAssistantIsThere_then_theAskButtonComesAboveTheThumbsInTheirOwnRow() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermReportScreen(
				state = TermReport.State.Content(report = termReportItemFixture()),
				actions = TermReportActions(onAskAssistant = { _ -> })
			)
		}
		waitForIdle()

		val ask = onNodeWithTag(TermReportUiTags.TermReportAskButton).performScrollTo().fetchSemanticsNode()
		val thumb = onNodeWithTag(TermReportUiTags.termReportThumb(useful = true)).fetchSemanticsNode()
		val row = onNodeWithTag(TermReportUiTags.TermReportFeedbackRow).fetchSemanticsNode()

		assertTrue(ask.positionInRoot.y < row.positionInRoot.y, "the question across the card comes first")
		assertTrue(row.positionInRoot.y <= thumb.positionInRoot.y, "the thumbs are in the row of the question")
		onNodeWithText("¿Te sirvió este informe?").performScrollTo().assertIsDisplayed()
	}

	@Test
	fun when_theAssistantIsNotThere_then_theThumbsAreStillOffered() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermReportScreen(
				state = TermReport.State.Content(report = termReportItemFixture()),
				actions = TermReportActions()
			)
		}
		waitForIdle()

		onNodeWithTag(TermReportUiTags.TermReportFeedbackRow).performScrollTo().assertIsDisplayed()
		onNodeWithTag(TermReportUiTags.termReportThumb(useful = true)).assertExists()
		onNodeWithTag(TermReportUiTags.termReportThumb(useful = false)).assertExists()
		onNodeWithText("¿Te sirvió este informe?").performScrollTo().assertIsDisplayed()
	}

	@Test
	fun when_theReportCannotBeRead_then_itSaysSoAndOffersToTryAgain() = runTuIndiceUiTest {
		var retries = 0

		setTuIndiceTestContent {
			TermReportScreen(
				state = TermReport.State.Failed(
					title = UiText.Raw("No pudimos abrir tu informe"),
					message = UiText.Raw("Revisa tu conexión y vuelve a intentarlo.")
				),
				actions = TermReportActions(onRetryClick = { retries++ })
			)
		}

		assertNodeVisible(TermReportUiTags.TermReportFailed)
		onNodeWithTag(TermReportUiTags.TermReportRetryButton).performClick()

		assertEquals(1, retries)
	}

	@Test
	fun when_theServiceHasNoReportOfTheTerm_then_itSaysSoAndOffersNothingToRetry() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermReportScreen(
				state = TermReport.State.NoReport(
					title = UiText.Raw("No hay informe para este trimestre"),
					message = UiText.Raw("El asistente aún no ha escrito el informe de este trimestre.")
				),
				actions = TermReportActions()
			)
		}

		assertNodeVisible(TermReportUiTags.TermReportNoReport)
		onNodeWithTag(TermReportUiTags.TermReportRetryButton).assertDoesNotExist()
	}
}
