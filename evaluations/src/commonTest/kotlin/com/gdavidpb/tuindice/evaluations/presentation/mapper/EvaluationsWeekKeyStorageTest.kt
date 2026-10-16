package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EvaluationsWeekKeyStorageTest {
	private val anchor = LocalDate(2026, 9, 28)

	@Test
	fun toStorageValue_savesAnAcademicWeekWithTheIsoDateOfItsAnchor() {
		assertEquals(
			"academic:2026-09-28:7",
			EvaluationsWeekKey.Academic(7).toStorageValue(weekOneStart = anchor)
		)
	}

	@Test
	fun toStorageValue_savesContinuousWithoutAnAnchor() {
		assertEquals("continuous", EvaluationsWeekKey.Continuous.toStorageValue(weekOneStart = null))
		assertEquals("continuous", EvaluationsWeekKey.Continuous.toStorageValue(weekOneStart = anchor))
	}

	@Test
	fun toStorageValue_cannotSaveAnAcademicWeekWithoutItsAnchor() {
		assertNull(EvaluationsWeekKey.Academic(7).toStorageValue(weekOneStart = null))
	}

	@Test
	fun toEvaluationsWeekKeyOrNull_readsBackWhatWasSavedUnderTheSameAnchor() {
		assertEquals(
			EvaluationsWeekKey.Academic(7),
			"academic:2026-09-28:7".toEvaluationsWeekKeyOrNull(weekOneStart = anchor)
		)
	}

	@Test
	fun toEvaluationsWeekKeyOrNull_dropsAWeekSavedUnderAnotherAnchor() {
		assertNull("academic:2026-08-31:7".toEvaluationsWeekKeyOrNull(weekOneStart = anchor))
	}

	@Test
	fun toEvaluationsWeekKeyOrNull_dropsAWeekInTheOldFormatWithoutAnAnchor() {
		assertNull("academic:7".toEvaluationsWeekKeyOrNull(weekOneStart = anchor))
	}

	@Test
	fun toEvaluationsWeekKeyOrNull_dropsMalformedTexts() {
		assertNull("academic:".toEvaluationsWeekKeyOrNull(weekOneStart = anchor))
		assertNull("academic:2026-09-28:".toEvaluationsWeekKeyOrNull(weekOneStart = anchor))
		assertNull("academic:2026-09-28:x".toEvaluationsWeekKeyOrNull(weekOneStart = anchor))
		assertNull("academic:not-a-date:7".toEvaluationsWeekKeyOrNull(weekOneStart = anchor))
		assertNull("academic:2026-09-28:7:1".toEvaluationsWeekKeyOrNull(weekOneStart = anchor))
		assertNull("week_7".toEvaluationsWeekKeyOrNull(weekOneStart = anchor))
	}

	@Test
	fun toEvaluationsWeekKeyOrNull_keepsContinuousWhateverTheAnchor() {
		assertEquals(
			EvaluationsWeekKey.Continuous,
			"continuous".toEvaluationsWeekKeyOrNull(weekOneStart = anchor)
		)
		assertEquals(
			EvaluationsWeekKey.Continuous,
			"continuous".toEvaluationsWeekKeyOrNull(weekOneStart = LocalDate(2026, 8, 31))
		)
	}
}
