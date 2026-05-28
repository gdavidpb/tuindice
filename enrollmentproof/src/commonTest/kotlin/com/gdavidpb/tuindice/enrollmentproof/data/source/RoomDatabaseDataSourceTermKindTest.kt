package com.gdavidpb.tuindice.enrollmentproof.data.source

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RoomDatabaseDataSourceTermKindTest {
	@Test
	fun isCurrentTermKind_returnsTrue_onlyForCurrentTerms() {
		assertTrue(RoomDatabaseDataSource.isCurrentTermKind("CURRENT"))
		assertFalse(RoomDatabaseDataSource.isCurrentTermKind("HISTORICAL"))
		assertFalse(RoomDatabaseDataSource.isCurrentTermKind("SYNTHETIC"))
	}
}
