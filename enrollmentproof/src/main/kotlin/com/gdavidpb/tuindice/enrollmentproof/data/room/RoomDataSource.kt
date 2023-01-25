package com.gdavidpb.tuindice.enrollmentproof.data.room

import com.gdavidpb.tuindice.enrollmentproof.data.enrollmentproof.source.LocalDataSource
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase

class RoomDataSource(
	private val room: TuIndiceDatabase
) : LocalDataSource {
	override suspend fun getCurrentQuarterName(uid: String): String? {
		return room.quarters.getCurrentQuarter(uid)
			?.quarter
			?.name
	}
}