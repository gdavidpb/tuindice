package com.gdavidpb.tuindice.record.data.quarter.source

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.model.Subject

interface LocalDataSource {
	suspend fun isUpdated(uid: String): Boolean

	suspend fun getQuarters(uid: String): List<Quarter>
	suspend fun saveQuarters(uid: String, quarters: List<Quarter>)
	suspend fun removeQuarter(uid: String, qid: String)

	suspend fun saveSubjects(uid: String, vararg subjects: Subject)
}