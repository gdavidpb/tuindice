package com.gdavidpb.tuindice.base.domain.model.transaction

import com.gdavidpb.tuindice.base.domain.model.quarter.QuarterRemoveTransaction
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectUpdateTransaction

class Transaction<T : TransactionOperation> private constructor(
	val reference: String,
	val type: TransactionType,
	val action: TransactionAction,
	val timestamp: Long,
	val dispatchToRemote: Boolean,
	val operation: T
) {
	class Builder<T : TransactionOperation> {
		private var reference: String? = null
		private var timestamp: Long = System.currentTimeMillis()
		private var dispatchToRemote: Boolean = true
		private var operation: T? = null

		fun withReference(value: String) = apply {
			this.reference = value
		}

		fun withTimestamp(value: Long) = apply {
			this.timestamp = value
		}

		fun withDispatchToRemote(value: Boolean) = apply {
			this.dispatchToRemote = value
		}

		fun withOperation(value: T) = apply {
			this.operation = value
		}

		fun build() = Transaction(
			reference = reference ?: error("'reference' is missed"),
			type = operation?.resolveType() ?: error("'type' is missed"),
			action = operation?.resolveAction() ?: error("'action' is missed"),
			timestamp = timestamp,
			dispatchToRemote = dispatchToRemote,
			operation = operation ?: error("'operation' is missed"),
		)

		private fun TransactionOperation.resolveType() = when (this) {
			is SubjectUpdateTransaction -> TransactionType.SUBJECT
			is QuarterRemoveTransaction -> TransactionType.QUARTER
			else -> throw NoWhenBranchMatchedException()
		}

		private fun TransactionOperation.resolveAction() = when (this) {
			is SubjectUpdateTransaction -> TransactionAction.UPDATE
			is QuarterRemoveTransaction -> TransactionAction.DELETE
			else -> throw NoWhenBranchMatchedException()
		}
	}
}