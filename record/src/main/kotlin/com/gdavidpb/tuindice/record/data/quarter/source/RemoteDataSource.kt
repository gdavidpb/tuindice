package com.gdavidpb.tuindice.record.data.quarter.source

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.model.Subject
import com.gdavidpb.tuindice.record.domain.model.SubjectUpdate

interface RemoteDataSource {
	suspend fun getQuarters(): List<Quarter>
	suspend fun removeQuarter(qid: String)

	suspend fun updateSubject(update: SubjectUpdate): Subject
}