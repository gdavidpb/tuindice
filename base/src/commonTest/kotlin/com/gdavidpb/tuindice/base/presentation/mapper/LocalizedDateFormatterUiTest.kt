package com.gdavidpb.tuindice.base.presentation.mapper

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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlin.test.Test

// The Spanish the names read as. It reads resources, so it runs on the iOS host only
// (androidHostTestExcludedPatterns).
@OptIn(ExperimentalTestApi::class)
class LocalizedDateFormatterUiTest {
	@Test
	fun when_monthNamesAreResolved_then_readTheTwelveMonthsInLowerCase() = runTuIndiceUiTest {
		assertResolvedTexts(
			expected = listOf(
				"enero",
				"febrero",
				"marzo",
				"abril",
				"mayo",
				"junio",
				"julio",
				"agosto",
				"septiembre",
				"octubre",
				"noviembre",
				"diciembre"
			),
			texts = Month.entries.map { month -> month.toNameText() }
		)
	}

	@Test
	fun when_shortMonthNamesAreResolved_then_readTheTwelveMonthsWithoutPeriod() = runTuIndiceUiTest {
		assertResolvedTexts(
			expected = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic"),
			texts = Month.entries.map { month -> month.toShortNameText() }
		)
	}

	@Test
	fun when_weekdayNamesAreResolved_then_readTheWeekFromMondayToSunday() = runTuIndiceUiTest {
		assertResolvedTexts(
			expected = listOf("lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo"),
			texts = DayOfWeek.entries.map { day -> day.toNameText() }
		)
	}

	@Test
	fun when_shortWeekdayNamesAreResolved_then_readTheWeekFromMondayToSunday() = runTuIndiceUiTest {
		assertResolvedTexts(
			expected = listOf("lun", "mar", "mié", "jue", "vie", "sáb", "dom"),
			texts = DayOfWeek.entries.map { day -> day.toShortNameText() }
		)
	}

	@Test
	fun when_monthYearIsResolved_then_readsTheMonthInLowerCaseAndTheYear() = runTuIndiceUiTest {
		assertResolvedTexts(
			expected = listOf("abril 2026", "diciembre 2009"),
			texts = listOf(
				LocalDate(year = 2026, month = Month.APRIL, day = 1).formatLocalizedMonthYear(),
				LocalDate(year = 2009, month = Month.DECEMBER, day = 31).formatLocalizedMonthYear()
			)
		)
	}

	private fun ComposeUiTest.assertResolvedTexts(expected: List<String>, texts: List<UiText>) {
		setTuIndiceTestContent {
			texts.forEachIndexed { index, text ->
				Text(
					modifier = Modifier.testTag(resolvedTextTag(index)),
					text = text.asString()
				)
			}
		}

		expected.forEachIndexed { index, expectedText ->
			// Resources resolve asynchronously off Android: wait for the text, then read the node.
			waitUntil(timeoutMillis = RESOURCE_TIMEOUT_MILLIS) {
				onAllNodesWithText(expectedText).fetchSemanticsNodes().isNotEmpty()
			}

			onNodeWithTag(resolvedTextTag(index)).assertTextEquals(expectedText)
		}
	}

	private companion object {
		const val RESOURCE_TIMEOUT_MILLIS = 5_000L

		fun resolvedTextTag(index: Int) = "localized_date_text_resolved_$index"
	}
}
