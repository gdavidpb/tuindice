package com.gdavidpb.tuindice.enrollmentproof.data.source

import com.gdavidpb.tuindice.enrollmentproof.data.repository.DatabaseDataRepository
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase

private const val CURRENT_ACADEMIC_RECORD_ID = "self"
private const val OFFICIAL_VIEW_MODE = "official"

class RoomDatabaseDataSource(
	private val room: TuIndiceDatabase
) : DatabaseDataRepository {
	override suspend fun getCurrentQuarterName(): String? {
		return room.academicTermProjections
			.getTerms(
				recordId = CURRENT_ACADEMIC_RECORD_ID,
				viewMode = OFFICIAL_VIEW_MODE
			)
			.firstOrNull { term -> !term.closed }
			?.label
	}
}
