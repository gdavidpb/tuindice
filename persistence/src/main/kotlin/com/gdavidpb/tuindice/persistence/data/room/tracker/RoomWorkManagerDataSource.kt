package com.gdavidpb.tuindice.persistence.data.room.tracker

import androidx.work.WorkManager
import com.gdavidpb.tuindice.base.utils.extension.noAwait
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.entity.TransactionEntity
import com.gdavidpb.tuindice.persistence.data.room.model.TransactionAction
import com.gdavidpb.tuindice.persistence.data.room.model.TransactionStatus
import com.gdavidpb.tuindice.persistence.data.room.model.TransactionType

class RoomWorkManagerDataSource(
	private val room: TuIndiceDatabase,
	private val workManager: WorkManager
) : TransactionDataSource {
	override suspend fun trackTransaction(
		reference: String,
		type: TransactionType,
		action: TransactionAction,
		transaction: suspend () -> Unit
	) {
		noAwait {
			val transactionEntity = TransactionEntity(
				reference = reference,
				type = type,
				action = action,
				status = TransactionStatus.IN_PROGRESS
			)

			room.transactions.createTransaction(transactionEntity)

			runCatching {
				transaction()
			}.onSuccess {
				room.transactions.updateTransactionStatus(
					transactionId = transactionEntity.id,
					status = TransactionStatus.COMPLETED
				)
			}.onFailure {
				room.transactions.updateTransactionStatus(
					transactionId = transactionEntity.id,
					status = TransactionStatus.PENDING
				)

				// TODO enqueue work manager task
			}
		}
	}
}