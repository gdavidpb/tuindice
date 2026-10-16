package com.gdavidpb.tuindice.enrollmentproof.data.source

import com.gdavidpb.tuindice.enrollmentproof.data.repository.DatabaseDataRepository
import com.gdavidpb.tuindice.persistence.data.room.currentTermEntityOrNull
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicTermDao
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicTermEntity

class RoomDatabaseDataSource(
	private val academicTermDao: AcademicTermDao
) : DatabaseDataRepository {
	override suspend fun getCurrentQuarterName(): String? {
		return selectCurrentQuarterName(terms = academicTermDao.getTerms())
	}

	internal companion object {
		fun selectCurrentQuarterName(terms: List<AcademicTermEntity>): String? {
			return terms.currentTermEntityOrNull()?.periodLabel
		}
	}
}
