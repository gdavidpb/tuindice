package com.gdavidpb.tuindice.record.data.repository.quarter

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.SubjectUpdate

interface RemoteDataSource {
	suspend fun getQuarters(): List<Quarter>
	suspend fun removeQuarter(remove: QuarterRemove)

	suspend fun updateSubject(update: SubjectUpdate): Subject
}