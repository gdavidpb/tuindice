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
// this connection) see one committed state. [block] may only call DAOs and must not suspend on
// anything else. That a read does not hold up the writer is demonstrated only with the bundled
// driver of iOS (RoomPersistenceTransactionRunnerIsolationTest); on Android it has no execution in
// this repository's tests (the host has no driver) and the native E2E is its first.
suspend fun <R> TuIndiceDatabase.withReadTransaction(
	block: suspend () -> R
): R {
	return useReaderConnection { transactor ->
		transactor.deferredTransaction {
			block()
		}
	}
}
