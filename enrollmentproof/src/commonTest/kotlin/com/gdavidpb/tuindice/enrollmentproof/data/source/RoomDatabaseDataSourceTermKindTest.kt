package com.gdavidpb.tuindice.enrollmentproof.data.source

import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicTermEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RoomDatabaseDataSourceTermKindTest {
	@Test
	fun selectCurrentQuarterName_withTwoCurrentTerms_takesTheHighestTermOrderWhateverTheDaoOrder() {
		val older = academicTerm(id = "b", termOrder = 20261, kind = "CURRENT", label = "Older")
		val newer = academicTerm(id = "a", termOrder = 20262, kind = "CURRENT", label = "Newer")

		assertEquals("Newer", RoomDatabaseDataSource.selectCurrentQuarterName(listOf(older, newer)))
		assertEquals("Newer", RoomDatabaseDataSource.selectCurrentQuarterName(listOf(newer, older)))
	}

	@Test
	fun selectCurrentQuarterName_aTieOnTermOrderIsBrokenByTheHighestId() {
		val low = academicTerm(id = "a", termOrder = 20262, kind = "CURRENT", label = "Low")
		val high = academicTerm(id = "b", termOrder = 20262, kind = "CURRENT", label = "High")

		assertEquals("High", RoomDatabaseDataSource.selectCurrentQuarterName(listOf(high, low)))
		assertEquals("High", RoomDatabaseDataSource.selectCurrentQuarterName(listOf(low, high)))
	}

	@Test
	fun selectCurrentQuarterName_ignoresHistoricalAndSyntheticTerms() {
		val terms = listOf(
			academicTerm(id = "a", termOrder = 20263, kind = "HISTORICAL", label = "Historical"),
			academicTerm(id = "b", termOrder = 20264, kind = "SYNTHETIC", label = "Synthetic"),
			academicTerm(id = "c", termOrder = 20261, kind = "CURRENT", label = "Current")
		)

		assertEquals("Current", RoomDatabaseDataSource.selectCurrentQuarterName(terms))
		assertNull(RoomDatabaseDataSource.selectCurrentQuarterName(terms.take(2)))
	}
}

private fun academicTerm(
	id: String,
	termOrder: Int,
	kind: String,
	label: String
) = AcademicTermEntity(
	id = id,
	periodYear = termOrder / 10,
	periodCode = "JAN_MAR",
	termKey = "$id-$termOrder",
	termOrder = termOrder,
	periodLabel = label,
	kind = kind
)
