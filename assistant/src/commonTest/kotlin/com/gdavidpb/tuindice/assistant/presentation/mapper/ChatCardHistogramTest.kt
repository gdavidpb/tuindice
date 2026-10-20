package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_count_subject_many
import tuindice.assistant.generated.resources.assistant_card_b_count_subject_one
import tuindice.assistant.generated.resources.assistant_card_b_histogram_grade
import kotlin.test.Test
import kotlin.test.assertEquals

// A grade histogram is one bar per grade, from the first grade up, and each row says its grade and its subjects.
class ChatCardHistogramTest {
	@Test
	fun when_theCountsAreGiven_then_eachBarFillsItsShareOfTheLongestOne() {
		val bars = listOf(0, 2, 4, 8).toHistogramBarItems()

		assertEquals(listOf("1", "2", "3", "4"), bars.map { bar -> bar.grade })
		assertEquals(listOf("0", "2", "4", "8"), bars.map { bar -> bar.count })
		assertEquals(listOf(0f, 0.25f, 0.5f, 1f), bars.map { bar -> bar.fraction })
	}

	@Test
	fun when_everyCountIsZero_then_noBarFillsAndNothingDividesByZero() {
		assertEquals(listOf(0f, 0f), listOf(0, 0).toHistogramBarItems().map { bar -> bar.fraction })
		assertEquals(emptyList(), emptyList<Int>().toHistogramBarItems())
	}

	@Test
	fun when_aRowIsDescribed_then_itNamesTheGradeAndTheSubjectsInTheRightNumber() {
		val bars = listOf(1, 16).toHistogramBarItems()

		assertEquals(
			UiText.Resource(
				Res.string.assistant_card_b_histogram_grade,
				listOf(
					"1",
					UiText.Resource(Res.string.assistant_card_b_count_subject_one, listOf(1))
				)
			),
			bars[0].description
		)
		assertEquals(
			UiText.Resource(
				Res.string.assistant_card_b_histogram_grade,
				listOf(
					"2",
					UiText.Resource(Res.string.assistant_card_b_count_subject_many, listOf(16))
				)
			),
			bars[1].description
		)
	}
}
