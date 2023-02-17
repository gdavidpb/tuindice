package com.gdavidpb.tuindice.persistence.data.room.base

import androidx.room.withTransaction
import com.gdavidpb.tuindice.base.utils.extension.noAwait
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.entity.TransactionEntity
import com.gdavidpb.tuindice.persistence.data.room.model.TransactionAction
import com.gdavidpb.tuindice.persistence.data.room.model.TransactionStatus
import com.gdavidpb.tuindice.persistence.data.room.model.TransactionType

abstract class TrackDataSource(
	protected open val room: TuIndiceDatabase
) {
	suspend fun trackTransaction(
		reference: String,
		type: TransactionType,
		action: TransactionAction,
		block: suspend () -> Unit
	) {
		noAwait {
			room.withTransaction {
				val transactionEntity = TransactionEntity(
					reference = reference,
					type = type,
					action = action,
					status = TransactionStatus.IN_PROGRESS
				)

				room.transactions.createTransaction(transactionEntity)

				runCatching {
					block()
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
				}
			}
		}
	}
}