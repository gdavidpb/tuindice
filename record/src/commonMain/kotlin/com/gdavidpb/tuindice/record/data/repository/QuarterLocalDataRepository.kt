package com.gdavidpb.tuindice.record.data.repository

import com.gdavidpb.tuindice.record.data.model.quarter.LocalQuarter
import com.gdavidpb.tuindice.record.data.model.quarter.LocalSubject
import com.gdavidpb.tuindice.record.data.model.quarter.SetSubjectGradeResult
import kotlinx.coroutines.flow.Flow

interface QuarterLocalDataRepository {
	fun getQuartersFlow(): Flow<List<LocalQuarter>>
	suspend fun getConfirmedQuarters(): List<LocalQuarter>
	suspend fun getQuarter(qid: String): LocalQuarter?
	suspend fun confirmQuarterAddition(addedQuarter: LocalQuarter, affectedQuarters: List<LocalQuarter>)
	suspend fun removeQuarter(qid: String)
	suspend fun confirmQuarterRemoval(qid: String, affectedQuarters: List<LocalQuarter>)
	suspend fun confirmSubjectGradeMutation(affectedQuarters: List<LocalQuarter>)
	suspend fun saveQuarters(quarters: List<LocalQuarter>)
	suspend fun saveSubjects(subjects: List<LocalSubject>)
	suspend fun clearSubjectGradePreview(qid: String, sid: String)
	suspend fun setSubjectGradeAndRecompute(
		qid: String,
		sid: String,
		grade: Int,
		commit: Boolean
	): SetSubjectGradeResult
}
