package com.gdavidpb.tuindice.enrollmentproof.data.source

import com.gdavidpb.tuindice.enrollmentproof.data.repository.DatabaseDataRepository
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicTermDao

private const val CURRENT_TERM_KIND = "CURRENT"

class RoomDatabaseDataSource(
	private val academicTermDao: AcademicTermDao
) : DatabaseDataRepository {
	override suspend fun getCurrentQuarterName(): String? {
		return academicTermDao
			.getTerms()
			.firstOrNull { term -> isCurrentTermKind(term.kind) }
			?.periodLabel
	}

	internal companion object {
		fun isCurrentTermKind(kind: String): Boolean {
			return kind == CURRENT_TERM_KIND
		}
	}
}
