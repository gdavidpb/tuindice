@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.summary.presentation.mapper

import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

// The Spanish the sync line reads as, resolved the way the screen does it: asString() inside a
// composable.
@OptIn(ExperimentalTestApi::class)
class DateUiTest {
	@Test
	fun when_thereHasBeenNoSync_then_lineReadsNever() = runTuIndiceUiTest {
		assertResolvedTexts(
			"Última sincronización: Nunca" to (null as Long?).toSyncStatusText(Clock.System),
			"Última sincronización: Nunca" to 0L.toSyncStatusText(Clock.System),
			"Nunca" to (null as Long?).formatSyncTimestamp(Clock.System)
		)
	}

	@Test
	fun when_syncWasToday_then_lineReadsTodayAndTheTime() = runTuIndiceUiTest {
		assertResolvedTexts(
			"Última sincronización: Hoy, 03:05 p. m." to millisDaysAgo(0, hour = 15, minute = 5).toSyncStatusText(Clock.System),
			"Última sincronización: Hoy, 12:00 p. m." to millisDaysAgo(0, hour = 12, minute = 0).toSyncStatusText(Clock.System)
		)
	}

	@Test
	fun when_syncWasYesterday_then_lineReadsYesterdayAndTheTime() = runTuIndiceUiTest {
		assertResolvedTexts(
			"Última sincronización: Ayer, 09:07 a. m." to millisDaysAgo(1, hour = 9, minute = 7).toSyncStatusText(Clock.System)
		)
	}

	@Test
	fun when_syncWasWithinTheLastWeek_then_lineReadsTheCapitalizedWeekdayAndTheTime() = runTuIndiceUiTest {
		val threeDaysAgo = millisDaysAgo(3, hour = 15, minute = 5)

		assertResolvedTexts(
			"Última sincronización: ${threeDaysAgo.weekdayName()}, 03:05 p. m." to threeDaysAgo.toSyncStatusText(Clock.System)
		)
	}

	@Test
	fun when_syncWasAMonthAgo_then_lineReadsThePaddedDayTheMonthAndTheYear() = runTuIndiceUiTest {
		val aMonthAgo = millisDaysAgo(30)
		val date = Instant.fromEpochMilliseconds(aMonthAgo).toLocalDateTime(TimeZone.currentSystemDefault()).date
		val day = date.day.toString().padStart(2, '0')

		assertResolvedTexts(
			"Última sincronización: $day de ${date.month.monthName()} ${date.year}" to aMonthAgo.toSyncStatusText(Clock.System)
		)
	}

	private fun Long.weekdayName(): String {
		val dayOfWeek = Instant
			.fromEpochMilliseconds(this)
			.toLocalDateTime(TimeZone.currentSystemDefault())
			.dayOfWeek

		return when (dayOfWeek) {
			DayOfWeek.MONDAY -> "Lunes"
			DayOfWeek.TUESDAY -> "Martes"
			DayOfWeek.WEDNESDAY -> "Miércoles"
			DayOfWeek.THURSDAY -> "Jueves"
			DayOfWeek.FRIDAY -> "Viernes"
			DayOfWeek.SATURDAY -> "Sábado"
			DayOfWeek.SUNDAY -> "Domingo"
		}
	}

	private fun Month.monthName(): String {
		return when (this) {
			Month.JANUARY -> "enero"
			Month.FEBRUARY -> "febrero"
			Month.MARCH -> "marzo"
			Month.APRIL -> "abril"
			Month.MAY -> "mayo"
			Month.JUNE -> "junio"
			Month.JULY -> "julio"
			Month.AUGUST -> "agosto"
			Month.SEPTEMBER -> "septiembre"
			Month.OCTOBER -> "octubre"
			Month.NOVEMBER -> "noviembre"
			Month.DECEMBER -> "diciembre"
		}
	}

	private fun ComposeUiTest.assertResolvedTexts(vararg cases: Pair<String, UiText>) {
		setTuIndiceTestContent {
			cases.forEachIndexed { index, (_, text) ->
				Text(
					modifier = Modifier.testTag(resolvedTextTag(index)),
					text = text.asString()
				)
			}
		}

		cases.forEachIndexed { index, (expected, _) ->
			// Resources resolve asynchronously off Android: wait for the text, then read the node.
			waitUntil(timeoutMillis = RESOURCE_TIMEOUT_MILLIS) {
				onAllNodesWithText(expected).fetchSemanticsNodes().isNotEmpty()
			}

			onNodeWithTag(resolvedTextTag(index)).assertTextEquals(expected)
		}
	}

	private companion object {
		const val RESOURCE_TIMEOUT_MILLIS = 5_000L

		fun resolvedTextTag(index: Int) = "sync_text_resolved_$index"
	}
}
