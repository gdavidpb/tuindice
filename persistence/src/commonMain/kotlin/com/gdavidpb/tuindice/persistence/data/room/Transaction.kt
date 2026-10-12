package com.gdavidpb.tuindice.persistence.data.room

import androidx.room.deferredTransaction
import androidx.room.immediateTransaction
import androidx.room.useReaderConnection
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

// A deferred transaction on a reader connection: the reads of [block] (the DAOs called inside join
// this connection) see one committed state, and it never holds up the writer.
suspend fun <R> TuIndiceDatabase.withReadTransaction(
	block: suspend () -> R
): R {
	return useReaderConnection { transactor ->
		transactor.deferredTransaction {
			block()
		}
	}
}
