package com.gdavidpb.tuindice.enrollmentproof.data.source

import com.gdavidpb.tuindice.enrollmentproof.data.repository.DatabaseDataSource
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase

class RoomDatabaseDataSource(
	private val room: TuIndiceDatabase
) : DatabaseDataSource {
	override suspend fun getCurrentQuarterName(uid: String): String? {
		return room.quarters.getCurrentQuarter(uid)
			?.name
	}
}