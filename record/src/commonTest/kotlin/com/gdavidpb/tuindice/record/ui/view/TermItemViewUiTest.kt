package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.text.AnnotatedString
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.record.presentation.model.AttemptItem
import com.gdavidpb.tuindice.record.presentation.model.RecordNotice
import com.gdavidpb.tuindice.record.presentation.model.RecordNoticeKind
import com.gdavidpb.tuindice.record.presentation.model.TermItem
import com.gdavidpb.tuindice.record.presentation.model.TermItemKind
import com.gdavidpb.tuindice.record.ui.RecordUiTags
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
				onAttemptSelectionChange = { _, _, _, _ -> }
			)
		}

		assertNodeVisible(RecordUiTags.SelectedTermSummary)
		assertNodeVisible(RecordUiTags.AttemptsList)
		assertNodeHidden(RecordUiTags.ScheduleViewSwitch)
		assertNodeHidden(RecordUiTags.ScheduleContainer)
	}

	@Test
	fun when_currentTermHasANotice_then_itSitsAboveTheSummaryOfThePage() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermItemView(
				item = termItem(),
				notice = RecordNotice(
					title = UiText.Raw("Tu inscripción aparece anulada"),
					message = UiText.Raw("La universidad la tiene anulada."),
					kind = RecordNoticeKind.AnnulledProvisional
				),
				onAttemptSelectionChange = { _, _, _, _ -> }
			)
		}

		assertNodeVisible(BaseUiTags.NoticeView)
		assertNodeVisible(RecordUiTags.SelectedTermSummary)
		assertNodeHidden(RecordUiTags.ScheduleViewSwitch)
	}

	private fun termItem() = TermItem(
		termId = "current-term",
		periodYear = 2026,
		termOrder = 1,
		shortNameText = "SEP-DIC 2026",
		kind = TermItemKind.CURRENT,
		gradeText = AnnotatedString("Δx 4.0000"),
		gradeDelta = null,
		gradeSumText = AnnotatedString("∑x 4.0000"),
		gradeSumDelta = null,
		creditsText = AnnotatedString("⦿ 4"),
		creditsDelta = null,
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
