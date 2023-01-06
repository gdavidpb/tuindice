package com.gdavidpb.tuindice.record.data.room

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.persistence.data.source.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.source.room.mappers.toQuarter
import com.gdavidpb.tuindice.persistence.data.source.room.mappers.toQuarterEntity
import com.gdavidpb.tuindice.record.data.quarter.source.LocalDataSource

class RoomDataSource(
	private val room: TuIndiceDatabase
) : LocalDataSource {
	override suspend fun getQuarters(uid: String): List<Quarter> {
		return room.quarters.getQuarters(uid)
			.map { (quarter, subjects) -> quarter.toQuarter(subjects) }
	}

	override suspend fun saveQuarters(uid: String, quarters: List<Quarter>) {
		val quarterEntities = quarters
			.map { quarter -> quarter.toQuarterEntity(uid) }
			.toTypedArray()

		room.quarters.insert(*quarterEntities)
	}

	override suspend fun removeQuarter(uid: String, qid: String) {
		room.quarters.deleteQuarter(uid, qid)
	}
}