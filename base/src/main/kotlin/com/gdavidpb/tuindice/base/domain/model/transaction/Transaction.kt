package com.gdavidpb.tuindice.base.domain.model.transaction

import com.gdavidpb.tuindice.base.domain.model.quarter.QuarterRemoveTransaction
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectUpdateTransaction

class Transaction<T : TransactionOperation> private constructor(
	val uid: String,
	val reference: String,
	val type: TransactionType,
	val action: TransactionAction,
	val timestamp: Long,
	val dispatchToRemote: Boolean,
	val operation: T
) {
	class Builder<T : TransactionOperation> {
		private var uid: String? = null
		private var reference: String? = null
		private var timestamp: Long = System.currentTimeMillis()
		private var dispatchToRemote: Boolean = true
		private var operation: T? = null

		fun withUid(value: String) = apply {
			uid = value
		}

		fun withReference(value: String) = apply {
			reference = value
		}

		fun withTimestamp(value: Long) = apply {
			timestamp = value
		}

		fun withDispatchToRemote(value: Boolean) = apply {
			dispatchToRemote = value
		}

		fun withOperation(value: T) = apply {
			operation = value
		}

		fun build() = Transaction(
			uid = uid ?: error("'uid' is missed"),
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