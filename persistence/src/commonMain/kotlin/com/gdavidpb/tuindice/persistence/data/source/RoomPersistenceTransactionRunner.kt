package com.gdavidpb.tuindice.persistence.data.source

import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.withImmediateTransaction
import com.gdavidpb.tuindice.persistence.domain.repository.PersistenceTransactionRunner

class RoomPersistenceTransactionRunner(
	private val room: TuIndiceDatabase
) : PersistenceTransactionRunner {
	override suspend fun <R> immediate(block: suspend () -> R): R {
		return room.withImmediateTransaction(block)
	}
}
