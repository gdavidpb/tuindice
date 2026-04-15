package com.gdavidpb.tuindice.enrollmentproof.data.source

import com.gdavidpb.tuindice.base.utils.extension.academicTermDisplayName
import com.gdavidpb.tuindice.enrollmentproof.data.repository.DatabaseDataRepository
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicTermDao

private const val OFFICIAL_CURRENT_TERM_KIND = "OFFICIAL_CURRENT"

class RoomDatabaseDataSource(
	private val academicTermDao: AcademicTermDao
) : DatabaseDataRepository {
	override suspend fun getCurrentQuarterName(): String? {
		return academicTermDao
			.getTerms()
			.firstOrNull { term -> isOfficialCurrentTermKind(term.kind) }
			?.let { term ->
				academicTermDisplayName(
					startAtMillis = term.startAt,
					endAtMillis = term.endAt
				)
			}
	}

	internal companion object {
		fun isOfficialCurrentTermKind(kind: String): Boolean {
			return kind == OFFICIAL_CURRENT_TERM_KIND
		}
	}
}
