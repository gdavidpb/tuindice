package com.gdavidpb.tuindice.record.data.quarter.source

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.model.Subject
import com.gdavidpb.tuindice.record.domain.model.SubjectUpdate
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
	suspend fun getQuarters(uid: String): Flow<List<Quarter>>
	suspend fun saveQuarters(uid: String, quarters: List<Quarter>)
	suspend fun removeQuarter(uid: String, qid: String)

	suspend fun saveSubjects(uid: String, subjects: List<Subject>)
	suspend fun updateSubject(uid: String, update: SubjectUpdate): Subject
}