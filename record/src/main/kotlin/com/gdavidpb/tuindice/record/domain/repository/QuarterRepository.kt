package com.gdavidpb.tuindice.record.domain.repository

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.SubjectGradeSet
import kotlinx.coroutines.flow.Flow

interface QuarterRepository {
	suspend fun getQuartersFlow(uid: String): Flow<List<Quarter>>
	suspend fun getQuarters(uid: String): List<Quarter>
	suspend fun removeQuarter(uid: String, remove: QuarterRemove)
	suspend fun setSubjectGrade(uid: String, set: SubjectGradeSet)
}