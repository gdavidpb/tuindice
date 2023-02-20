package com.gdavidpb.tuindice.persistence.data.room.schema

object TransactionTable {
	const val TABLE_NAME = "transactions"

	const val ID = "transaction_id"
	const val REFERENCE = "transaction_ref"
	const val TYPE = "transaction_type"
	const val ACTION = "transaction_action"
	const val STATUS = "transaction_status"
	const val TIMESTAMP = "transaction_timestamp"
}