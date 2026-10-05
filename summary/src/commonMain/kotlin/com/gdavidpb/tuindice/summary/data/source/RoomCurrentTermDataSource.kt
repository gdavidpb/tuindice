package com.gdavidpb.tuindice.summary.data.source

import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicTermDao
import com.gdavidpb.tuindice.summary.domain.repository.CurrentTermRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private const val CURRENT_TERM_KIND = "CURRENT"

class RoomCurrentTermDataSource(
	private val academicTermDao: AcademicTermDao
) : CurrentTermRepository {
	override fun observeHasCurrentTerm(): Flow<Boolean> {
		return academicTermDao
			.observeTermsFlow()
			.map { terms -> terms.any { term -> term.kind == CURRENT_TERM_KIND } }
			.distinctUntilChanged()
	}
}
