package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.testing.assertDpEquals
import com.gdavidpb.tuindice.record.testing.textLayout
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@OptIn(ExperimentalTestApi::class)
class ScheduleDayHeaderViewUiTest {
	@Test
	fun when_theDayIsToday_then_itsNameIsBold_tagged_andReadAsToday() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleDayHeaderView(day = ScheduleDay.Monday, isToday = true)
		}

		assertNodeVisible(RecordUiTags.ScheduleTodayHeader)
		onNodeWithTag(RecordUiTags.ScheduleTodayHeader)
			.assertTextEquals("Lun")
			.assertContentDescriptionEquals("lunes, hoy")
		assertEquals(
			expected = FontWeight.Bold,
			actual = onNodeWithTag(RecordUiTags.ScheduleTodayHeader).textLayout().layoutInput.style.fontWeight
		)
	}

	@Test
	fun when_theDayIsNotToday_then_itsNameIsPlain_andNothingMarksIt() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleDayHeaderView(day = ScheduleDay.Monday, isToday = false)
		}

		onNodeWithText("Lun").assertIsDisplayed()
		assertNotEquals(FontWeight.Bold, onNodeWithText("Lun").textLayout().layoutInput.style.fontWeight)
		assertNodeHidden(RecordUiTags.ScheduleTodayHeader)
		// The short name is all it says: "hoy" is only said of today.
		onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.ContentDescription)).assertCountEquals(0)
	}

	@Test
	fun when_anyDayOfTheWeekIsToday_then_itIsReadByItsFullName() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Column {
				ScheduleDay.entries.forEach { day ->
					ScheduleDayHeaderView(day = day, isToday = true)
				}
			}
		}

		val headers = onAllNodesWithTag(RecordUiTags.ScheduleTodayHeader)

		headers.assertCountEquals(TodayDescriptions.size)
		// ScheduleDay lists the week from Monday, the order the columns are drawn in.
		TodayDescriptions.forEachIndexed { index, description ->
			headers[index].assertContentDescriptionEquals(description)
		}
	}

	@Test
	fun when_todayStandsNextToAnotherDay_then_bothHeadersAreAsTall_soTheNamesStayLevel() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Row {
				ScheduleDayHeaderView(modifier = Modifier.testTag(TodayTag), day = ScheduleDay.Monday, isToday = true)
				ScheduleDayHeaderView(modifier = Modifier.testTag(OtherTag), day = ScheduleDay.Tuesday, isToday = false)
			}
		}

		val today = onNodeWithTag(TodayTag).getUnclippedBoundsInRoot()
		val other = onNodeWithTag(OtherTag).getUnclippedBoundsInRoot()

		// Every header keeps the room of the accent dot, whether it draws it or not.
		assertDpEquals(expected = today.bottom - today.top, actual = other.bottom - other.top, what = "header height")
		assertDpEquals(
			expected = onNodeWithText("Lun").getUnclippedBoundsInRoot().top,
			actual = onNodeWithText("Mar").getUnclippedBoundsInRoot().top,
			what = "top of the day names"
		)
	}

	private companion object {
		const val TodayTag = "today_header"
		const val OtherTag = "other_header"

		val TodayDescriptions = listOf(
			"lunes, hoy",
			"martes, hoy",
			"miércoles, hoy",
			"jueves, hoy",
			"viernes, hoy",
			"sábado, hoy",
			"domingo, hoy"
		)
	}
}
