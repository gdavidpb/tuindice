package com.gdavidpb.tuindice.enrollmentproof.data.source

import com.gdavidpb.tuindice.enrollmentproof.data.repository.DatabaseDataRepository
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase

class RoomDatabaseDataSource(
	private val room: TuIndiceDatabase
) : DatabaseDataRepository {
	override suspend fun getCurrentQuarterName(): String? {
		return room.quarters.getCurrentQuarter()
			?.name
	}
}