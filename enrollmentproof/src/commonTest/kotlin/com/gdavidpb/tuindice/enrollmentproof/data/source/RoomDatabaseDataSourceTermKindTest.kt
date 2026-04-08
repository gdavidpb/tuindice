package com.gdavidpb.tuindice.enrollmentproof.data.source

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RoomDatabaseDataSourceTermKindTest {
	@Test
	fun isOfficialCurrentTermKind_returnsTrue_onlyForOfficialCurrentTerms() {
		assertTrue(RoomDatabaseDataSource.isOfficialCurrentTermKind("OFFICIAL_CURRENT"))
		assertFalse(RoomDatabaseDataSource.isOfficialCurrentTermKind("OFFICIAL_HISTORICAL"))
		assertFalse(RoomDatabaseDataSource.isOfficialCurrentTermKind("SYNTHETIC"))
	}
}
