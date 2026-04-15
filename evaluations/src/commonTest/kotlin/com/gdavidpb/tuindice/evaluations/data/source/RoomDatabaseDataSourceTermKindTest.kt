package com.gdavidpb.tuindice.evaluations.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicTermEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RoomDatabaseDataSourceTermKindTest {
	@Test
	fun isEditableTermKind_returnsTrue_forCurrentAndSyntheticTerms() {
		assertTrue(RoomDatabaseDataSource.isEditableTermKind(TermKind.OFFICIAL_CURRENT.name))
		assertTrue(RoomDatabaseDataSource.isEditableTermKind(TermKind.SYNTHETIC.name))
	}

	@Test
	fun isEditableTermKind_returnsFalse_forHistoricalOfficialTerms() {
		assertFalse(RoomDatabaseDataSource.isEditableTermKind(TermKind.OFFICIAL_HISTORICAL.name))
	}

	@Test
	fun selectCurrentEditableTermId_prefersTermActiveOnCurrentDate() {
		val terms = listOf(
			academicTerm(
				id = "11111111111111111111111111111111",
				startAt = 1_767_236_400_000L,
				endAt = 1_774_926_000_000L,
				kind = TermKind.OFFICIAL_CURRENT.name
			),
			academicTerm(
				id = "22222222222222222222222222222222",
				startAt = 1_775_012_400_000L,
				endAt = 1_785_470_400_000L,
				kind = TermKind.SYNTHETIC.name
			),
			academicTerm(
				id = "33333333333333333333333333333333",
				startAt = 1_788_235_200_000L,
				endAt = 1_798_686_000_000L,
				kind = TermKind.SYNTHETIC.name
			)
		)

		assertEquals(
			"22222222222222222222222222222222",
			RoomDatabaseDataSource.selectCurrentEditableTermId(
				terms = terms,
				nowMillis = 1_776_124_800_000L
			)
		)
	}

	@Test
	fun selectCurrentEditableTermId_fallsBackToLatestStartedEditableTerm_whenNoTermIsCurrentlyActive() {
		val terms = listOf(
			academicTerm(
				id = "11111111111111111111111111111111",
				startAt = 1_767_236_400_000L,
				endAt = 1_774_926_000_000L,
				kind = TermKind.OFFICIAL_CURRENT.name
			),
			academicTerm(
				id = "22222222222222222222222222222222",
				startAt = 1_775_012_400_000L,
				endAt = 1_785_470_400_000L,
				kind = TermKind.SYNTHETIC.name
			),
			academicTerm(
				id = "33333333333333333333333333333333",
				startAt = 1_788_235_200_000L,
				endAt = 1_798_686_000_000L,
				kind = TermKind.SYNTHETIC.name
			)
		)

		assertEquals(
			"22222222222222222222222222222222",
			RoomDatabaseDataSource.selectCurrentEditableTermId(
				terms = terms,
				nowMillis = 1_787_000_000_000L
			)
		)
	}

	@Test
	fun selectCurrentEditableTermId_ignoresHistoricalTerms_whenChoosingFallback() {
		val terms = listOf(
			academicTerm(
				id = "44444444444444444444444444444444",
				startAt = 1_756_699_200_000L,
				endAt = 1_767_150_000_000L,
				kind = TermKind.OFFICIAL_HISTORICAL.name
			),
			academicTerm(
				id = "22222222222222222222222222222222",
				startAt = 1_775_012_400_000L,
				endAt = 1_785_470_400_000L,
				kind = TermKind.SYNTHETIC.name
			)
		)

		assertEquals(
			"22222222222222222222222222222222",
			RoomDatabaseDataSource.selectCurrentEditableTermId(
				terms = terms,
				nowMillis = 1_774_000_000_000L
			)
		)
	}
}

private fun academicTerm(
	id: String,
	startAt: Long,
	endAt: Long,
	kind: String
) = AcademicTermEntity(
	id = id,
	startAt = startAt,
	endAt = endAt,
	kind = kind
)
