package com.gdavidpb.tuindice.evaluations.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import kotlin.test.Test
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
}
