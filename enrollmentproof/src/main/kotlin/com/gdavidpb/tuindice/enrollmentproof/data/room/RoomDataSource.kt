package com.gdavidpb.tuindice.enrollmentproof.data.room

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.enrollmentproof.data.enrollmentproof.source.LocalDataSource
import com.gdavidpb.tuindice.persistence.data.source.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.source.room.mappers.toQuarter

class RoomDataSource(
	private val room: TuIndiceDatabase
) : LocalDataSource {
	override suspend fun getCurrentQuarter(uid: String): Quarter? {
		return room.quarters.getCurrentQuarter(uid)
			?.toQuarter()
	}
}