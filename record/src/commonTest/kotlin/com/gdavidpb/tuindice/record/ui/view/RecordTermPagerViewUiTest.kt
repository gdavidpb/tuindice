package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.record.presentation.mapper.recordNotice
import com.gdavidpb.tuindice.record.presentation.model.AttemptItem
import com.gdavidpb.tuindice.record.presentation.model.RecordNotice
import com.gdavidpb.tuindice.record.presentation.model.RecordNoticeKind
import com.gdavidpb.tuindice.record.presentation.model.TermItem
import com.gdavidpb.tuindice.record.presentation.model.TermItemKind
import com.gdavidpb.tuindice.record.testing.recordMapperTexts
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.RecordActions
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class RecordTermPagerViewUiTest {
	@Test
	fun when_aNoticeIsSharedByEveryPage_then_itSitsBetweenTheTermSelectorAndThePages() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermPager(notice = finalAnnulment())
		}

		assertNodeVisible(BaseUiTags.NoticeView)
		onNodeWithTag(BaseUiTags.NoticeTitle).assertTextEquals("Tu inscripción aparece anulada")
		onNodeWithTag(BaseUiTags.NoticeMessage).assertTextEquals("La universidad la tiene anulada.")

		val selector = onNodeWithTag(RecordUiTags.TermSelectorRow).getUnclippedBoundsInRoot()
		val notice = onNodeWithTag(BaseUiTags.NoticeView).getUnclippedBoundsInRoot()
		val pager = onNodeWithTag(RecordUiTags.TermPager).getUnclippedBoundsInRoot()

		assertTrue(selector.bottom <= notice.top, "the notice starts under the term selector")
		assertTrue(notice.bottom <= pager.top, "the pages start under the notice")
		// The page under it is still the term's own.
		assertNodeVisible(RecordUiTags.SelectedTermSummary)
	}

	@Test
	fun when_theNoticeHasNoTitle_then_onlyItsMessageIsDrawn() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermPager(
				notice = recordNotice(
					title = null,
					message = UiText.Raw("La universidad la tiene anulada."),
					kind = RecordNoticeKind.AnnulledFinal
				)
			)
		}

		assertNodeVisible(BaseUiTags.NoticeView)
		assertNodeHidden(BaseUiTags.NoticeTitle)
		onNodeWithTag(BaseUiTags.NoticeMessage).assertTextEquals("La universidad la tiene anulada.")
	}

	@Test
	fun when_thereIsNoNotice_then_nothingStandsBetweenTheSelectorAndThePages() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermPager(notice = null)
		}

		assertNodeHidden(BaseUiTags.NoticeView)
		assertNodeVisible(RecordUiTags.TermSelectorRow)
		assertNodeVisible(RecordUiTags.TermPager)
		assertNodeVisible(RecordUiTags.SelectedTermSummary)
	}

	@Test
	fun when_theNoticeIsCleared_then_itLeavesTheScreen_andThePagesStay() = runTuIndiceUiTest {
		var notice by mutableStateOf<RecordNotice?>(finalAnnulment())

		setTuIndiceTestContent {
			TermPager(notice = notice)
		}

		assertNodeVisible(BaseUiTags.NoticeView)

		// A sync that clears the annulment: the notice animates out and nothing else is recreated.
		notice = null
		waitForIdle()

		assertNodeHidden(BaseUiTags.NoticeView)
		assertNodeVisible(RecordUiTags.TermPager)
		assertNodeVisible(RecordUiTags.SelectedTermSummary)
	}

	@Test
	fun when_anArrivalIsOnTheSelectedPage_then_thePageBringsItsCardIntoView_andTakesIt() = runTuIndiceUiTest {
		var taken: String? = null

		setTuIndiceTestContent {
			RecordTermPagerView(
				terms = listOf(
					termItem(termId = "term-a", attemptId = "attempt-a"),
					termItem(termId = "term-b", attemptId = "attempt-b", arrivalAttemptId = "attempt-b")
				),
				texts = recordMapperTexts(),
				selectedTermId = "term-b",
				actions = RecordActions(
					onAttemptArrivalTaken = { attemptId -> taken = attemptId },
					onSelectedTermChange = {},
					onAttemptSelectionChange = { _, _, _, _ -> }
				),
				onScrollInProgressChange = {}
			)
		}

		waitForIdle()
		assertNodeVisible(RecordUiTags.attemptCard("attempt-b"))
		assertEquals("attempt-b", taken)
	}

	@Composable
	private fun TermPager(notice: RecordNotice?) {
		RecordTermPagerView(
			terms = listOf(termItem()),
			texts = recordMapperTexts(),
			notice = notice,
			selectedTermId = "historical-term",
			actions = RecordActions(
				onSelectedTermChange = {},
				onAttemptSelectionChange = { _, _, _, _ -> }
			),
			onScrollInProgressChange = {}
		)
	}

	// The only notice that goes above the pager: a final annulment has no current term to sit on.
	private fun finalAnnulment(): RecordNotice = recordNotice(
		title = UiText.Raw("Tu inscripción aparece anulada"),
		message = UiText.Raw("La universidad la tiene anulada."),
		kind = RecordNoticeKind.AnnulledFinal
	)

	private fun termItem(
		termId: String = "historical-term",
		attemptId: String = "attempt-1",
		arrivalAttemptId: String? = null
	) = TermItem(
		termId = termId,
		periodYear = 2024,
		termOrder = 1,
		shortNameText = "ENE-MAR 2024",
		kind = TermItemKind.HISTORICAL,
		gradeTarget = 4f,
		gradeSumTarget = 4f,
		creditsTarget = 4,
		previousGradeTarget = null,
		previousGradeSumTarget = null,
		previousCreditsTarget = null,
		isCurrent = false,
		canDelete = false,
		canEdit = false,
		attempts = listOf(
			AttemptItem(
				attemptId = attemptId,
				subjectCode = "MA2115",
				grade = 4,
				codeText = "MA2115",
				nameText = "MATEMATICAS 3",
				gradeText = "4 / 5",
				creditsText = "4 UC",
				codeColor = Color(0xFF1E3A5F),
				codeContainerColor = Color(0xFFDCE8F5),
				isReadOnly = true
			)
		),
		arrivalAttemptId = arrivalAttemptId
	)
}
