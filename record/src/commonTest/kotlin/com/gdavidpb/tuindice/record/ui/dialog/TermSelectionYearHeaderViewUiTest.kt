package com.gdavidpb.tuindice.record.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.record.testing.textLayout
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class TermSelectionYearHeaderViewUiTest {
	@Test
	fun when_aYearHeadsItsTerms_then_itShowsTheYearInBold_andIsNotTappable() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermSelectionYearHeaderView(year = 2026)
		}

		assertNodeVisible(RecordUiTags.termSelectionYear(2026))
		onNodeWithTag(RecordUiTags.termSelectionYear(2026))
			.assertTextEquals("2026")
			// It only titles the group: choosing is done on the rows under it.
			.assertHasNoClickAction()
		assertEquals(
			expected = FontWeight.Bold,
			actual = onNodeWithTag(RecordUiTags.termSelectionYear(2026)).textLayout().layoutInput.style.fontWeight
		)
	}

	@Test
	fun when_twoYearsAreListed_then_eachHeaderAnswersToItsOwnYear() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Column {
				TermSelectionYearHeaderView(year = 2026)
				TermSelectionYearHeaderView(year = 2024)
			}
		}

		onNodeWithTag(RecordUiTags.termSelectionYear(2026)).assertTextEquals("2026")
		onNodeWithTag(RecordUiTags.termSelectionYear(2024)).assertTextEquals("2024")
		assertNodeHidden(RecordUiTags.termSelectionYear(2025))

		val newer = onNodeWithTag(RecordUiTags.termSelectionYear(2026)).getUnclippedBoundsInRoot()
		val older = onNodeWithTag(RecordUiTags.termSelectionYear(2024)).getUnclippedBoundsInRoot()

		assertTrue(newer.bottom <= older.top, "the headers keep the order they were given")
	}
}
