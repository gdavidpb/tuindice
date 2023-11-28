package com.gdavidpb.tuindice.enrollmentproof.data.repository.enrollmentproof.source

import com.gdavidpb.tuindice.enrollmentproof.data.repository.enrollmentproof.LocalDataSource
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase

class RoomDataSource(
	private val room: TuIndiceDatabase
) : LocalDataSource {
	override suspend fun getCurrentQuarterName(uid: String): String? {
		return room.quarters.getCurrentQuarter(uid)
			?.name
	}
}