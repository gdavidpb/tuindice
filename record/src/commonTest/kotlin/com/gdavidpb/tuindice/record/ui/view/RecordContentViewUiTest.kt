package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicScheduleEntry
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.mapper.recordNotice
import com.gdavidpb.tuindice.record.presentation.model.RecordNotice
import com.gdavidpb.tuindice.record.presentation.model.RecordNoticeKind
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.testing.academicTerm
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class RecordContentViewUiTest {
	@Test
	fun when_theCurrentTermNamesSectionAndClassroom_then_eachSubjectCardSaysWhatItHas() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Content(
				state = currentTermState(
					attempts = listOf(
						attempt(
							id = "attempt-1",
							code = "CI5311",
							section = 1,
							schedule = listOf(meeting(ScheduleDay.Monday), meeting(ScheduleDay.Wednesday))
						),
						attempt(id = "attempt-2", code = "CI5437", section = 2)
					)
				)
			)
		}

		assertNodeVisible(RecordUiTags.ContentContainer)
		// Both meetings agree on the room, so it follows the section; a subject with no room named
		// shows its section alone.
		onNodeWithTag(RecordUiTags.attemptDetail("attempt-1")).assertTextEquals("Sección 1 · MYS-116")
		onNodeWithTag(RecordUiTags.attemptDetail("attempt-2")).assertTextEquals("Sección 2")
	}

	@Test
	fun when_aSubjectHasNeitherSectionNorClassroom_then_itsCardShowsNoDetail() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Content(state = currentTermState(attempts = listOf(attempt(id = "attempt-1", code = "EG1114"))))
		}

		assertNodeVisible(RecordUiTags.attemptSubjectChip("attempt-1"))
		assertNodeHidden(RecordUiTags.attemptDetail("attempt-1"))
	}

	@Test
	fun when_theNoticeSpeaksOfTheCurrentTerm_then_itIsDrawnOnThatTermsPage() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Content(
				state = currentTermState(
					attempts = listOf(attempt(id = "attempt-1", code = "CI5311")),
					notice = annulment(RecordNoticeKind.AnnulledProvisional)
				)
			)
		}

		// assertNodeVisible also proves it is drawn once: on the page, and not above the pager too.
		assertNodeVisible(BaseUiTags.NoticeView)
		onNodeWithTag(BaseUiTags.NoticeTitle).assertTextEquals("Tu inscripción aparece anulada")

		val notice = onNodeWithTag(BaseUiTags.NoticeView).getUnclippedBoundsInRoot()
		val pager = onNodeWithTag(RecordUiTags.TermPager).getUnclippedBoundsInRoot()

		assertTrue(notice.top >= pager.top, "the notice belongs to the page of the term")
	}

	@Test
	fun when_theNoticeIsAFinalAnnulment_then_itIsDrawnAboveThePager_forEveryPage() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Content(
				state = Record.State.Content(
					viewMode = RecordViewMode.Historical,
					record = AcademicRecord(
						id = "record",
						terms = listOf(academicTerm(id = "historical-2024", kind = TermKind.HISTORICAL))
					),
					selectedTermId = "historical-2024",
					notice = annulment(RecordNoticeKind.AnnulledFinal)
				)
			)
		}

		assertNodeVisible(BaseUiTags.NoticeView)

		val notice = onNodeWithTag(BaseUiTags.NoticeView).getUnclippedBoundsInRoot()
		val pager = onNodeWithTag(RecordUiTags.TermPager).getUnclippedBoundsInRoot()

		assertTrue(notice.bottom <= pager.top, "the notice sits above the pages")
	}

	@Composable
	private fun Content(state: Record.State.Content) {
		RecordContentView(
			state = state,
			selectedTermId = state.selectedTermId,
			onSelectedTermChange = {},
			onAttemptSelectionChange = { _, _, _, _ -> },
			showTermSelection = false,
			onDismissTermSelection = {}
		)
	}

	private fun currentTermState(
		attempts: List<AcademicAttempt>,
		notice: RecordNotice? = null
	) = Record.State.Content(
		viewMode = RecordViewMode.Projection,
		record = AcademicRecord(
			id = "record",
			terms = listOf(
				academicTerm(
					id = "current-term",
					kind = TermKind.CURRENT,
					periodYear = 2026,
					periodCode = AcademicTermPeriod.SEP_DEC,
					attempts = attempts
				)
			)
		),
		selectedTermId = "current-term",
		notice = notice
	)

	private fun annulment(kind: RecordNoticeKind): RecordNotice = recordNotice(
		title = UiText.Raw("Tu inscripción aparece anulada"),
		message = UiText.Raw("La universidad la tiene anulada."),
		kind = kind
	)

	private fun meeting(day: ScheduleDay) = AcademicScheduleEntry(
		dayOfWeek = day.code,
		startBlock = 1,
		endBlock = 2,
		classroom = "MYS-116"
	)

	private fun attempt(
		id: String,
		code: String,
		section: Int? = null,
		schedule: List<AcademicScheduleEntry>? = null
	) = AcademicAttempt(
		id = id,
		subjectCode = code,
		subjectName = code,
		credits = 3,
		section = section,
		schedule = schedule
	)
}
