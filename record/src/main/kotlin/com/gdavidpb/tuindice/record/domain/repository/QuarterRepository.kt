package com.gdavidpb.tuindice.record.domain.repository

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.SubjectUpdate
import kotlinx.coroutines.flow.Flow

interface QuarterRepository {
	suspend fun getQuarters(uid: String): Flow<List<Quarter>>
	suspend fun removeQuarter(uid: String, remove: QuarterRemove)

	suspend fun updateSubject(uid: String, update: SubjectUpdate)
}