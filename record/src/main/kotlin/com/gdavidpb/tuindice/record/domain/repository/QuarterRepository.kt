package com.gdavidpb.tuindice.record.domain.repository

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.model.Subject
import com.gdavidpb.tuindice.record.domain.model.SubjectUpdate

interface QuarterRepository {
	suspend fun getQuarters(uid: String): List<Quarter>
	suspend fun removeQuarter(uid: String, qid: String)

	suspend fun updateSubject(uid: String, update: SubjectUpdate): Subject
}