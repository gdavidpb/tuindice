package com.gdavidpb.tuindice.base.domain.model.transaction

class Transaction private constructor(
	val reference: String,
	val type: TransactionType,
	val action: TransactionAction,
	val timestamp: Long, // TODO declare it as ordinal (in order to avoid wrong local time)
	val data: String
) {
	class Builder {
		private var reference: String? = null
		private var type: TransactionType? = null
		private var action: TransactionAction? = null
		private var timestamp: Long = System.currentTimeMillis()
		private var data: String? = null

		fun withReference(value: String) = apply {
			reference = value
		}

		fun withType(value: TransactionType) = apply {
			type = value
		}

		fun withAction(value: TransactionAction) = apply {
			action = value
		}

		fun withTimestamp(value: Long) = apply {
			timestamp = value
		}

		fun withData(value: String) = apply {
			data = value
		}

		fun build() = Transaction(
			reference = reference ?: error("'reference' is missed"),
			type = type ?: error("'type' is missed"),
			action = action ?: error("'action' is missed"),
			timestamp = timestamp,
			data = data ?: error("'data' is missed")
		)
	}
}