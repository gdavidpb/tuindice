package com.gdavidpb.tuindice.record.data.repository.quarter

import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalSubject
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.QuarterUpdate
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
	fun getQuartersFlow(uid: String): Flow<List<LocalQuarter>>
	suspend fun getQuarters(uid: String): List<LocalQuarter>
	suspend fun getQuarter(uid: String, qid: String): LocalQuarter?
	suspend fun removeQuarter(uid: String, remove: QuarterRemove)
	suspend fun updateQuarter(uid: String, update: QuarterUpdate)
	suspend fun saveSubjects(uid: String, subjects: List<LocalSubject>)
	suspend fun saveQuarters(uid: String, quarters: List<LocalQuarter>)
}