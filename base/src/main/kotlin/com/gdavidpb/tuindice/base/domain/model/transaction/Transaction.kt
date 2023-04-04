package com.gdavidpb.tuindice.base.domain.model.transaction

import com.gdavidpb.tuindice.base.domain.model.quarter.QuarterRemoveTransaction
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectUpdateTransaction

class Transaction<T : TransactionData> private constructor(
	val uid: String,
	val reference: String,
	val type: TransactionType,
	val action: TransactionAction,
	val timestamp: Long, // TODO declare it as ordinal (in order to avoid wrong local time)
	val dispatchToRemote: Boolean,
	val data: T
) {
	class Builder<T : TransactionData> {
		private var uid: String? = null
		private var reference: String? = null
		private var timestamp: Long = System.currentTimeMillis()
		private var dispatchToRemote: Boolean = true
		private var data: T? = null

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

		fun withData(value: T) = apply {
			data = value
		}

		fun build() = Transaction(
			uid = uid ?: error("'uid' is missed"),
			reference = reference ?: error("'reference' is missed"),
			type = data?.resolveType() ?: error("'type' is missed"),
			action = data?.resolveAction() ?: error("'action' is missed"),
			timestamp = timestamp,
			dispatchToRemote = dispatchToRemote,
			data = data ?: error("'data' is missed")
		)

		private fun TransactionData.resolveType() = when (this) {
			is SubjectUpdateTransaction -> TransactionType.SUBJECT
			is QuarterRemoveTransaction -> TransactionType.QUARTER
			else -> throw NoWhenBranchMatchedException()
		}

		private fun TransactionData.resolveAction() = when (this) {
			is SubjectUpdateTransaction -> TransactionAction.UPDATE
			is QuarterRemoveTransaction -> TransactionAction.DELETE
			else -> throw NoWhenBranchMatchedException()
		}
	}
}