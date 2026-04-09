package com.gdavidpb.tuindice.enrollmentproof.data.source

import com.gdavidpb.tuindice.enrollmentproof.data.repository.DatabaseDataRepository
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase

private const val CURRENT_ACADEMIC_RECORD_ID = "self"
private const val OFFICIAL_CURRENT_TERM_KIND = "OFFICIAL_CURRENT"

class RoomDatabaseDataSource(
	private val room: TuIndiceDatabase
) : DatabaseDataRepository {
	override suspend fun getCurrentQuarterName(): String? {
		return room.academicTerms
			.getTerms(CURRENT_ACADEMIC_RECORD_ID)
			.firstOrNull { term -> isOfficialCurrentTermKind(term.kind) }
			?.label
	}

	internal companion object {
		fun isOfficialCurrentTermKind(kind: String): Boolean {
			return kind == OFFICIAL_CURRENT_TERM_KIND
		}
	}
}
