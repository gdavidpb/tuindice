package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.summary.testing.WithLargeLetter
import com.gdavidpb.tuindice.summary.testing.assertTextIsWhole
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SummarySectionHeaderViewUiTest {
	@Test
	fun when_headerIsShown_then_titleAndLinkAreNamedAfterTheSection() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummarySectionHeaderView(
				title = "Esta semana",
				linkText = "Horario",
				onLinkClick = {},
				sectionTag = SummaryUiTags.WeekSection
			)
		}

		onNodeWithTag(SummaryUiTags.sectionTitle(SummaryUiTags.WeekSection)).assertIsDisplayed()
		onNodeWithText("Esta semana").assertIsDisplayed()
		onNodeWithTag(SummaryUiTags.sectionLink(SummaryUiTags.WeekSection)).assertIsDisplayed()
		onNodeWithText("Horario").assertIsDisplayed()
	}

	@Test
	fun when_linkIsTapped_then_itAsksToLeadToTheScreenThatOwnsTheSection() = runTuIndiceUiTest {
		var taps = 0

		setTuIndiceTestContent {
			SummarySectionHeaderView(
				title = "Trimestre",
				linkText = "Sep - Dic 2026",
				onLinkClick = { taps++ },
				sectionTag = SummaryUiTags.TermSection
			)
		}

		onNodeWithTag(SummaryUiTags.sectionLink(SummaryUiTags.TermSection)).assertHasClickAction()
		onNodeWithTag(SummaryUiTags.sectionLink(SummaryUiTags.TermSection)).performClick()

		assertEquals(1, taps)
	}

	@Test
	fun when_linkIsShown_then_itsTouchTargetIsAtLeastTheMinimumSize() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummarySectionHeaderView(
				title = "Carrera",
				linkText = "Pensum",
				onLinkClick = {},
				sectionTag = SummaryUiTags.CareerSection
			)
		}

		onNodeWithTag(SummaryUiTags.sectionLink(SummaryUiTags.CareerSection)).assertHeightIsAtLeast(48.dp)
	}

	@Test
	fun when_theLetterIsLargeOnANarrowScreen_then_titleAndLinkAreWhole() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WithLargeLetter {
				SummarySectionHeaderView(
					title = "Esta semana",
					linkText = "Sep - Dic 2026",
					onLinkClick = {},
					sectionTag = SummaryUiTags.TermSection
				)
			}
		}

		assertTextIsWhole("Esta semana", singleLine = true)
		assertTextIsWhole("Sep - Dic 2026", singleLine = true)
	}

	@Test
	fun when_theSectionHasNoLinkText_then_noLinkIsShown_andTheHeadKeepsItsHeight() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Column {
				Box(modifier = Modifier.testTag("head")) {
					SummarySectionHeaderView(
						title = "Esta semana",
						linkText = null,
						onLinkClick = {},
						sectionTag = SummaryUiTags.WeekSection
					)
				}
			}
		}

		onNodeWithText("Esta semana").assertIsDisplayed()
		onNodeWithTag(SummaryUiTags.sectionLink(SummaryUiTags.WeekSection)).assertDoesNotExist()
		onNodeWithTag("head").assertHeightIsAtLeast(48.dp)
	}
}
