package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsSectionItem
import com.gdavidpb.tuindice.summary.testing.WithLargeLetter
import com.gdavidpb.tuindice.summary.testing.assertTextIsWhole
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions
import com.gdavidpb.tuindice.testkit.ui.advanceAnimationsBy
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

// The figures of the card are inside its button, so they are found in the tree that keeps each node apart.
@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsSectionViewUiTest {
	@Test
	fun when_noTermIsInProgressNorOver_then_itSaysWhatTheStatisticsWillShowAndHasNoLink() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsSectionView(
				item = SummaryStatisticsSectionItem.Empty,
				actions = SummaryHubActions()
			)
		}

		assertNodeVisible(SummaryUiTags.StatisticsEmptyCard)
		onNodeWithText("Estadísticas").assertIsDisplayed()
		onNodeWithText("Aún no hay estadísticas").assertIsDisplayed()
		onNodeWithText("Tus estadísticas aparecerán cuando termines tu primer trimestre.").assertIsDisplayed()
		onNodeWithTag(SummaryUiTags.sectionLink(SummaryUiTags.StatisticsSection)).assertDoesNotExist()
		onNodeWithTag(SummaryUiTags.StatisticsEmptyCard).assertHasNoClickAction()
	}

	@Test
	fun when_aTermIsInProgressAndNoneIsOver_then_theInvitationAndItsLinkAskForTheStatistics() = runTuIndiceUiTest {
		val targets = mutableListOf<SummaryHubTarget>()

		setTuIndiceTestContent {
			SummaryStatisticsSectionView(
				item = SummaryStatisticsSectionItem.Invite,
				actions = SummaryHubActions(onTargetClick = { target -> targets += target })
			)
		}

		onNodeWithText("Tu primer trimestre está en curso").assertIsDisplayed()
		onNodeWithText("Cuando termine verás aquí tu índice, tus notas y tu ritmo.").assertIsDisplayed()
		onNodeWithTag(SummaryUiTags.StatisticsInviteCard).assertHasClickAction()
		onNodeWithTag(SummaryUiTags.StatisticsInviteCard).performClick()
		onNodeWithTag(SummaryUiTags.sectionLink(SummaryUiTags.StatisticsSection)).performClick()

		assertEquals(listOf<SummaryHubTarget>(SummaryHubTarget.Statistics, SummaryHubTarget.Statistics), targets)
	}

	@Test
	fun when_termsAreOver_then_theCardShowsTheCumulativeAverageAndItsPhrase() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsSectionView(item = figuresWithLine(), actions = SummaryHubActions())
		}
		advanceAnimationsBy(1_000)

		assertNodeVisible(SummaryUiTags.StatisticsCard)
		assertNodeVisible(SummaryUiTags.StatisticsSparkline, useUnmergedTree = true)
		onNodeWithTag(SummaryUiTags.StatisticsFigureText, useUnmergedTree = true)
			.assertTextEquals("4.1250 / 5.0")
		onNodeWithText("acumulado", useUnmergedTree = true).assertIsDisplayed()
		onNodeWithTag(SummaryUiTags.StatisticsPhrase, useUnmergedTree = true)
			.assertTextEquals(FIGURES_PHRASE)
		onNodeWithTag(SummaryUiTags.StatisticsEmptyCard).assertDoesNotExist()
		onNodeWithTag(SummaryUiTags.StatisticsInviteCard).assertDoesNotExist()
	}

	@Test
	fun when_theWholeCardIsTapped_then_theStatisticsAreAskedOnce() = runTuIndiceUiTest {
		val targets = mutableListOf<SummaryHubTarget>()

		setTuIndiceTestContent {
			SummaryStatisticsSectionView(
				item = figuresWithLine(),
				actions = SummaryHubActions(onTargetClick = { target -> targets += target })
			)
		}

		onNodeWithTag(SummaryUiTags.StatisticsCard).assertHasClickAction()
		onNodeWithTag(SummaryUiTags.StatisticsCard).performClick()

		assertEquals(listOf<SummaryHubTarget>(SummaryHubTarget.Statistics), targets)
	}

	@Test
	fun when_theLineIsTapped_then_itIsNoTargetOfItsOwnAndTheCardAsksOnce() = runTuIndiceUiTest {
		val targets = mutableListOf<SummaryHubTarget>()

		setTuIndiceTestContent {
			SummaryStatisticsSectionView(
				item = figuresWithLine(),
				actions = SummaryHubActions(onTargetClick = { target -> targets += target })
			)
		}

		onNodeWithTag(SummaryUiTags.StatisticsSparkline, useUnmergedTree = true).assertHasNoClickAction()
		onNodeWithTag(SummaryUiTags.StatisticsSparkline, useUnmergedTree = true).performClick()

		assertEquals(listOf<SummaryHubTarget>(SummaryHubTarget.Statistics), targets)
	}

	@Test
	fun when_theTermsOverHaveNoAverageYet_then_onlyThePhraseIsShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsSectionView(
				item = SummaryStatisticsSectionItem.Figures(
					grade = null,
					line = emptyList(),
					phrase = UiText.Raw("2 aprobadas")
				),
				actions = SummaryHubActions()
			)
		}

		assertNodeVisible(SummaryUiTags.StatisticsCard)
		onNodeWithTag(SummaryUiTags.StatisticsPhrase, useUnmergedTree = true).assertTextEquals("2 aprobadas")
		onAllNodesWithTag(SummaryUiTags.StatisticsFigureText, useUnmergedTree = true).assertCountEquals(0)
		onNodeWithTag(SummaryUiTags.StatisticsSparkline, useUnmergedTree = true).assertDoesNotExist()
	}

	@Test
	fun when_theLetterIsLargeOnANarrowScreen_then_theFigureAndThePhraseAreWhole() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WithLargeLetter {
				Box(modifier = Modifier.width(360.dp)) {
					SummaryStatisticsSectionView(item = figuresWithLine(), actions = SummaryHubActions())
				}
			}
		}
		advanceAnimationsBy(1_000)

		assertTextIsWhole("4.1250 / 5.0")
		assertTextIsWhole("acumulado")
		assertTextIsWhole(FIGURES_PHRASE)
	}

	private fun figuresWithLine(): SummaryStatisticsSectionItem.Figures {
		return SummaryStatisticsSectionItem.Figures(
			grade = 4.125f,
			line = listOf(3.5f, 4.0f, 4.25f),
			phrase = UiText.Raw(FIGURES_PHRASE)
		)
	}

	private companion object {
		const val FIGURES_PHRASE = "44 aprobadas · índice más alto en Ene - Mar 2024"
	}
}
