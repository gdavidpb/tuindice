package com.gdavidpb.tuindice.record.data.quarter.source

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.model.Subject
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.base.TrackDataSource
import com.gdavidpb.tuindice.record.domain.model.SubjectUpdate
import kotlinx.coroutines.flow.Flow

abstract class LocalDataSource(override val room: TuIndiceDatabase) : TrackDataSource(room) {
	abstract suspend fun isGetQuartersOnCooldown(): Boolean
	abstract suspend fun setGetQuartersCooldown()

	abstract suspend fun getQuarters(uid: String): Flow<List<Quarter>>
	abstract suspend fun saveQuarters(uid: String, quarters: List<Quarter>)
	abstract suspend fun removeQuarter(uid: String, qid: String)

	abstract suspend fun saveSubjects(uid: String, subjects: List<Subject>)
	abstract suspend fun updateSubject(uid: String, update: SubjectUpdate): Subject
}