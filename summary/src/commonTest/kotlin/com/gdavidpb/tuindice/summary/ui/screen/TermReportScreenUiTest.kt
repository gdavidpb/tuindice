package com.gdavidpb.tuindice.summary.ui.screen

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityKind
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityRef
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantNavTarget
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.presentation.navigation.AssistantLauncher
import com.gdavidpb.tuindice.base.presentation.navigation.LocalAssistantLauncher
import com.gdavidpb.tuindice.summary.presentation.contract.TermReport
import com.gdavidpb.tuindice.summary.presentation.model.TermReportActionItem
import com.gdavidpb.tuindice.summary.testing.termReportItemFixture
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
			CompositionLocalProvider(LocalAssistantLauncher provides AssistantLauncher { context -> launched += context }) {
				TermReportScreen(
					state = TermReport.State.Content(report = termReportItemFixture()),
					actions = TermReportActions()
				)
			}
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
