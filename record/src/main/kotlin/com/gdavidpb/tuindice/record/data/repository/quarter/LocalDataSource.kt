package com.gdavidpb.tuindice.record.data.repository.quarter

import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalSubject
import com.gdavidpb.tuindice.record.data.repository.quarter.model.SetSubjectGradeResult
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
	fun getQuartersFlow(): Flow<List<LocalQuarter>>
	suspend fun getQuarter(qid: String): LocalQuarter?
	suspend fun removeQuarter(qid: String)
	suspend fun saveQuarters(quarters: List<LocalQuarter>)
	suspend fun saveSubjects(subjects: List<LocalSubject>)
	suspend fun setSubjectGradeAndRecompute(
		qid: String,
		sid: String,
		grade: Int,
		commit: Boolean
	): SetSubjectGradeResult
}