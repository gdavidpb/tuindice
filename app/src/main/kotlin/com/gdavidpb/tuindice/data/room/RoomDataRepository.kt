package com.gdavidpb.tuindice.data.room

import com.gdavidpb.tuindice.base.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.domain.model.TransactionStatus

class RoomDataRepository(
	private val room: TuIndiceDatabase
) : DatabaseRepository {
	override suspend fun prune() {
		room.transactions.prune(status = TransactionStatus.COMPLETED)
	}
}