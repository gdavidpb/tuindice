package com.gdavidpb.tuindice.record.data.quarter.source

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.SubjectUpdate
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
	suspend fun getQuarters(uid: String): Flow<List<Quarter>>
	suspend fun removeQuarter(uid: String, remove: QuarterRemove)
	suspend fun updateSubject(uid: String, update: SubjectUpdate)

	suspend fun saveSubjects(uid: String, subjects: List<Subject>)
	suspend fun saveQuarters(uid: String, quarters: List<Quarter>)
}