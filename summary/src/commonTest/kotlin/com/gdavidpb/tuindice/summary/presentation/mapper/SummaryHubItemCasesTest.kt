package com.gdavidpb.tuindice.summary.presentation.mapper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material.icons.outlined.CalendarMonth
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.domain.model.SummaryHub
import com.gdavidpb.tuindice.summary.domain.model.SummaryTerm
import com.gdavidpb.tuindice.summary.domain.model.SummaryWeek
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCardItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCareerItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryTermItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryWeekItem
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_term_first_message
import tuindice.summary.generated.resources.summary_term_first_title
import tuindice.summary.generated.resources.summary_week_no_term_message
import tuindice.summary.generated.resources.summary_week_no_term_title
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SummaryHubItemCasesTest {
	/* The whole */

	@Test
	fun theFirstTermCaseOfANewStudent_isTheWeekAndTheTermWithTheirCards_andTheCareer() {
		val item = SummaryHub(
			attention = emptyList(),
			week = SummaryWeek.Idle(hasTerms = false),
			term = SummaryTerm.Idle(hasTerms = false, finished = null),
			career = null,
			statistics = null
		).toSummaryHubItem()

		assertEquals(
			SummaryWeekItem(
				classRow = null,
				evaluations = emptyList(),
				hasSchedule = false,
				card = SummaryCardItem(
					icon = Icons.Outlined.CalendarMonth,
					titleText = UiText.Resource(Res.string.summary_week_no_term_title),
					messageText = UiText.Resource(Res.string.summary_week_no_term_message)
				)
			),
			item.week
		)
		assertEquals(
			SummaryTermItem(
				linkText = null,
				week = null,
				summaryText = null,
				subjects = emptyList(),
				finished = null,
				card = SummaryCardItem(
					icon = Icons.AutoMirrored.Outlined.NoteAdd,
					titleText = UiText.Resource(Res.string.summary_term_first_title),
					messageText = UiText.Resource(Res.string.summary_term_first_message)
				)
			),
			item.term
		)
		assertEquals(SummaryCareerItem.Invite, item.career)
	}

	@Test
	fun theHubOfAReadyRecord_hasTheWeekAndTheTerm_andOfANotReadyOneNeitherIsThere() {
		val ready = SummaryHub(
			attention = emptyList(),
			week = SummaryWeek.Idle(hasTerms = false),
			term = SummaryTerm.Idle(hasTerms = false, finished = null),
			career = null,
			statistics = null
		).toSummaryHubItem()

		assertNotNull(ready.week)
		assertNotNull(ready.term)
		assertNull(SummaryHub.Empty.toSummaryHubItem().week)
		assertNull(SummaryHub.Empty.toSummaryHubItem().term)
	}

	@Test
	fun theHubWithNothingToSay_hasOnlyTheInvitationToThePensum() {
		val item = SummaryHub.Empty.toSummaryHubItem()

		assertEquals(SummaryHubItem(career = SummaryCareerItem.Invite), item)
	}
}
