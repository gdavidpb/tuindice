package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.record.presentation.mapper.recordNotice
import com.gdavidpb.tuindice.record.presentation.model.AttemptItem
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

@OptIn(ExperimentalTestApi::class)
class TermItemViewUiTest {
	@Test
	fun when_termIsShown_then_pageShowsOnlyGrades_andTheSummaryStays() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermItemView(
				item = termItem(),
				texts = recordMapperTexts(),
				actions = RecordActions(
					onAttemptSelectionChange = { _, _, _, _ -> }
				)
			)
		}

		assertNodeVisible(RecordUiTags.SelectedTermSummary)
		assertNodeVisible(RecordUiTags.AttemptsList)
		assertNodeHidden(RecordUiTags.ScheduleViewSwitch)
		assertNodeHidden(RecordUiTags.ScheduleContainer)
	}

	@Test
	fun when_thePageCarriesANotice_then_itSitsAboveTheSummaryOfThePage() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermItemView(
				item = termItem().copy(
					notice = recordNotice(
						title = UiText.Raw("Tu inscripción aparece anulada"),
						message = UiText.Raw("La universidad la tiene anulada."),
						kind = RecordNoticeKind.AnnulledProvisional
					)
				),
				texts = recordMapperTexts(),
				actions = RecordActions(
					onAttemptSelectionChange = { _, _, _, _ -> }
				)
			)
		}

		assertNodeVisible(BaseUiTags.NoticeView)
		assertNodeVisible(RecordUiTags.SelectedTermSummary)
		assertNodeHidden(RecordUiTags.ScheduleViewSwitch)
	}

	@Test
	fun when_thePageCarriesNoNotice_then_noneIsDrawn() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermItemView(
				item = termItem(),
				texts = recordMapperTexts(),
				actions = RecordActions(
					onAttemptSelectionChange = { _, _, _, _ -> }
				)
			)
		}

		assertNodeHidden(BaseUiTags.NoticeView)
		assertNodeVisible(RecordUiTags.SelectedTermSummary)
	}

	private fun termItem() = TermItem(
		termId = "current-term",
		periodYear = 2026,
		termOrder = 1,
		shortNameText = "SEP-DIC 2026",
		kind = TermItemKind.CURRENT,
		gradeTarget = 4f,
		gradeSumTarget = 4f,
		creditsTarget = 4,
		previousGradeTarget = null,
		previousGradeSumTarget = null,
		previousCreditsTarget = null,
		isCurrent = true,
		canDelete = false,
		canEdit = false,
		attempts = listOf(
			AttemptItem(
				attemptId = "attempt-1",
				subjectCode = "MA2115",
				grade = 1,
				codeText = "MA2115",
				nameText = "MATEMATICAS 3",
				gradeText = "",
				creditsText = "4 UC",
				codeColor = Color(0xFF1E3A5F),
				codeContainerColor = Color(0xFFDCE8F5),
				isReadOnly = false
			)
		)
	)
}
