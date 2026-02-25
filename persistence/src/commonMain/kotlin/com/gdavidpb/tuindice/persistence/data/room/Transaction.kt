package com.gdavidpb.tuindice.persistence.data.room

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection

suspend fun <R> TuIndiceDatabase.withImmediateTransaction(
	block: suspend () -> R
): R {
	return useWriterConnection { transactor ->
		transactor.immediateTransaction {
			block()
		}
	}
}
