package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.datetime.LocalDate
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class CalendarDayClassDotUiTest {
	@Test
	fun when_theDayHasClass_then_theDotIsDrawn() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CalendarDayClassDot(date = LocalDate(2026, 10, 12), hasClass = true, isSelectable = true)
		}

		assertNodeVisible(EvaluationsUiTags.calendarDayClassDot(12))
	}

	@Test
	fun when_theDayIsOutsideTheRange_then_theDotIsStillDrawn() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CalendarDayClassDot(date = LocalDate(2026, 10, 12), hasClass = true, isSelectable = false)
		}

		assertNodeVisible(EvaluationsUiTags.calendarDayClassDot(12))
	}

	@Test
	fun when_theDayHasNoClassOrIsEmpty_then_nothingIsDrawn() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CalendarDayClassDot(date = LocalDate(2026, 10, 13), hasClass = false, isSelectable = true)
			CalendarDayClassDot(date = null, hasClass = true, isSelectable = true)
		}

		onNodeWithTag(EvaluationsUiTags.calendarDayClassDot(13)).assertDoesNotExist()
	}
}
