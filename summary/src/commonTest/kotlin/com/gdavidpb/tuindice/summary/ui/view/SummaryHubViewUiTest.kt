package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryAttentionItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCareerItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.testing.WithLargeLetter
import com.gdavidpb.tuindice.summary.testing.assertTextIsWhole
import com.gdavidpb.tuindice.summary.testing.summaryCareerProgressItem
import com.gdavidpb.tuindice.summary.testing.summaryEvaluationRowItem
import com.gdavidpb.tuindice.summary.testing.summaryHubItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SummaryHubViewUiTest {
	@Test
	fun when_thereIsEverythingToSay_then_theBlocksAreInTheOrderOfTheMock() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Box(modifier = Modifier.verticalScroll(rememberScrollState())) {
				SummaryHubView(hub = summaryHubItem(), actions = SummaryHubActions())
			}
		}

		val tops = listOf(
			SummaryUiTags.AttentionContainer,
			SummaryUiTags.WeekSection,
			SummaryUiTags.TermSection,
			SummaryUiTags.CareerSection
		).map { tag -> onNodeWithTag(tag).fetchSemanticsNode().positionInRoot.y }

		assertNodeVisible(SummaryUiTags.ItemsList)
		check(tops == tops.sorted() && tops.distinct().size == 4) { "blocks out of order: $tops" }
	}

	@Test
	fun when_thereIsNothingToAttendTo_then_theAttentionBlockDoesNotExist() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryHubView(hub = summaryHubItem(attention = emptyList()), actions = SummaryHubActions())
		}

		onNodeWithTag(SummaryUiTags.AttentionContainer).assertDoesNotExist()
		assertNodeVisible(SummaryUiTags.WeekSection)
	}

	@Test
	fun when_thereIsNoTermNoWeekAndNoAttention_then_onlyTheCareerRemains() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryHubView(
				hub = SummaryHubItem(career = SummaryCareerItem.Invite),
				actions = SummaryHubActions()
			)
		}

		onNodeWithTag(SummaryUiTags.AttentionContainer).assertDoesNotExist()
		onNodeWithTag(SummaryUiTags.WeekSection).assertDoesNotExist()
		onNodeWithTag(SummaryUiTags.TermSection).assertDoesNotExist()
		assertNodeVisible(SummaryUiTags.CareerSection)
	}

	@Test
	fun when_theHubHasNotBeenObserved_then_nothingIsShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryHubView(hub = SummaryHubItem(), actions = SummaryHubActions())
		}

		onNodeWithTag(SummaryUiTags.CareerSection).assertDoesNotExist()
	}

	@Test
	fun when_theLetterIsLargeOnA360dpScreen_then_noRowCutsATextOrSqueezesAChipOrAPill() = runTuIndiceUiTest {
		val hub = summaryHubItem(
			attention = listOf(
				SummaryAttentionItem(
					key = "enrollment-CI5311",
					text = UiText.Raw("Choque de horario en CI5311"),
					target = SummaryHubTarget.Record
				)
			),
			career = summaryCareerProgressItem()
		)
		val withLongTitle = hub.copy(
			week = hub.week!!.copy(
				evaluations = listOf(
					summaryEvaluationRowItem(
						evaluationId = "long",
						leadText = "Mié 21",
						titleText = "Trabajo escrito 12",
						subjectCode = "CI5311"
					)
				)
			)
		)

		setTuIndiceTestContent {
			WithLargeLetter {
				Box(modifier = Modifier.width(360.dp)) {
					SummaryHubView(hub = withLongTitle, actions = SummaryHubActions())
				}
			}
		}

		assertTextIsWhole("Choque de horario en CI5311")
		assertTextIsWhole("En curso", singleLine = true)
		assertTextIsWhole("Mié 21", singleLine = true)
		assertTextIsWhole("Trabajo escrito 12")
		assertTextIsWhole("CI5437", singleLine = true)
		assertTextIsWhole("CI5311", singleLine = true)
		assertTextIsWhole("3-4 · MYS-210")
		assertTextIsWhole("Semana 3 de 12")
		assertTextIsWhole("41 de 60 puntos")
		assertTextIsWhole("156 de 240 créditos")
	}
}
