package com.gdavidpb.tuindice.record.domain.repository

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.SubjectGradeSet
import kotlinx.coroutines.flow.Flow

interface QuarterRepository {
	suspend fun observeQuartersFlow(): Flow<List<Quarter>>
	suspend fun updateQuarters()
	suspend fun removeQuarter(remove: QuarterRemove)
	suspend fun setSubjectGrade(set: SubjectGradeSet)
}
