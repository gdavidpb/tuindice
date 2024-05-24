package com.gdavidpb.tuindice.record.data.repository.quarter

import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalSubject
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.SubjectGradeSet
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
	fun getQuartersFlow(uid: String): Flow<List<LocalQuarter>>
	suspend fun getQuarter(uid: String, qid: String): LocalQuarter?
	suspend fun removeQuarter(uid: String, qid: String)
	suspend fun saveQuarters(uid: String, quarters: List<LocalQuarter>)
	suspend fun saveSubjects(uid: String, subjects: List<LocalSubject>)
	suspend fun computeSetSubjectGrade(uid: String, qid: String, sid: String, grade: Int): List<LocalQuarter>
}