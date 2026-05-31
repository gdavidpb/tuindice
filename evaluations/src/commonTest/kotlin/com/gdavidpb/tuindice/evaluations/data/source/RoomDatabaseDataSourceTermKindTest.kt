package com.gdavidpb.tuindice.evaluations.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicTermEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RoomDatabaseDataSourceTermKindTest {
	@Test
	fun selectOfficialCurrentTermId_prefersCurrentTerm() {
		val terms = listOf(
			academicTerm(
				id = "11111111111111111111111111111111",
				termOrder = 20261,
				kind = TermKind.CURRENT.name
			),
			academicTerm(
				id = "22222222222222222222222222222222",
				termOrder = 20262,
				kind = TermKind.SYNTHETIC.name
			),
			academicTerm(
				id = "33333333333333333333333333333333",
				termOrder = 20263,
				kind = TermKind.SYNTHETIC.name
			)
		)

		assertEquals(
			"11111111111111111111111111111111",
			RoomDatabaseDataSource.selectOfficialCurrentTermId(
				terms = terms,
				nowMillis = 1_776_124_800_000L
			)
		)
	}

	@Test
	fun selectOfficialCurrentTermId_returnsNull_whenNoCurrentExists() {
		val terms = listOf(
			academicTerm(
				id = "11111111111111111111111111111111",
				termOrder = 20261,
				kind = TermKind.HISTORICAL.name
			),
			academicTerm(
				id = "22222222222222222222222222222222",
				termOrder = 20262,
				kind = TermKind.SYNTHETIC.name
			),
			academicTerm(
				id = "33333333333333333333333333333333",
				termOrder = 20263,
				kind = TermKind.SYNTHETIC.name
			)
		)

		assertNull(
			RoomDatabaseDataSource.selectOfficialCurrentTermId(
				terms = terms,
				nowMillis = 1_787_000_000_000L
			)
		)
	}

	@Test
	fun selectOfficialCurrentTermId_ignoresHistoricalAndSyntheticTerms() {
		val terms = listOf(
			academicTerm(
				id = "44444444444444444444444444444444",
				termOrder = 20261,
				kind = TermKind.HISTORICAL.name
			),
			academicTerm(
				id = "22222222222222222222222222222222",
				termOrder = 20262,
				kind = TermKind.SYNTHETIC.name
			)
		)

		assertNull(
			RoomDatabaseDataSource.selectOfficialCurrentTermId(
				terms = terms,
				nowMillis = 1_774_000_000_000L
			)
		)
	}

	@Test
	fun selectOfficialCurrentTermId_returnsLatestCurrentTerm_whenMultipleCurrentTermsExist() {
		val terms = listOf(
			academicTerm(
				id = "11111111111111111111111111111111",
				termOrder = 20261,
				kind = TermKind.CURRENT.name
			),
			academicTerm(
				id = "22222222222222222222222222222222",
				termOrder = 20262,
				kind = TermKind.CURRENT.name
			)
		)

		assertEquals(
			"22222222222222222222222222222222",
			RoomDatabaseDataSource.selectOfficialCurrentTermId(
				terms = terms,
				nowMillis = 1_787_000_000_000L
			)
		)
	}
}

private fun academicTerm(
	id: String,
	termOrder: Int,
	kind: String
) = AcademicTermEntity(
	id = id,
	periodYear = termOrder / 10,
	periodCode = "JAN_MAR",
	termKey = "${termOrder / 10}-JAN_MAR",
	termOrder = termOrder,
	periodLabel = "Enero - Marzo ${termOrder / 10}",
	kind = kind
)
