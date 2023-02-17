package com.gdavidpb.tuindice.record.data.room

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.room.withTransaction
import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.model.Subject
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.record.data.quarter.source.LocalDataSource
import com.gdavidpb.tuindice.record.data.room.mapper.toQuarter
import com.gdavidpb.tuindice.record.data.room.mapper.toQuarterEntity
import com.gdavidpb.tuindice.record.data.room.mapper.toSubject
import com.gdavidpb.tuindice.record.data.room.mapper.toSubjectEntity
import com.gdavidpb.tuindice.record.domain.model.SubjectUpdate
import com.gdavidpb.tuindice.record.utils.CooldownTimes
import com.gdavidpb.tuindice.record.utils.PreferencesKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomDataSource(
	override val room: TuIndiceDatabase,
	private val sharedPreferences: SharedPreferences
) : LocalDataSource(room) {
	override suspend fun isGetQuartersOnCooldown(): Boolean {
		val cooldownTime = sharedPreferences.getLong(PreferencesKeys.COOLDOWN_GET_QUARTERS, 0L)

		return cooldownTime <= System.currentTimeMillis()
	}

	override suspend fun setGetQuartersCooldown() {
		val cooldownTime = System.currentTimeMillis() + CooldownTimes.COOLDOWN_GET_QUARTERS

		sharedPreferences.edit {
			putLong(PreferencesKeys.COOLDOWN_GET_QUARTERS, cooldownTime)
		}
	}

	override suspend fun getQuarters(uid: String): Flow<List<Quarter>> {
		return room.quarters.getQuartersWithSubjects(uid)
			.map { quarters -> quarters.map { quarter -> quarter.toQuarter() } }
	}

	override suspend fun saveQuarters(uid: String, quarters: List<Quarter>) {
		val quarterEntities = quarters
			.map { quarter -> quarter.toQuarterEntity(uid) }

		val subjectEntities = quarters
			.flatMap { quarter -> quarter.subjects }
			.map { subject -> subject.toSubjectEntity(uid) }

		room.withTransaction {
			room.quarters.upsertEntities(quarterEntities)
			room.subjects.upsertEntities(subjectEntities)
		}
	}

	override suspend fun removeQuarter(uid: String, qid: String) {
		room.quarters.deleteQuarter(uid, qid)
	}

	override suspend fun saveSubjects(uid: String, subjects: List<Subject>) {
		val subjectEntities = subjects
			.map { subject -> subject.toSubjectEntity(uid) }

		room.subjects.upsertEntities(subjectEntities)
	}

	override suspend fun updateSubject(uid: String, update: SubjectUpdate): Subject {
		room.subjects.updateSubject(uid = uid, sid = update.subjectId, grade = update.grade)

		val subjectEntity = room.subjects.getSubject(uid = uid, sid = update.subjectId)

		return subjectEntity.toSubject()
	}
}