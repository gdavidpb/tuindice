package com.gdavidpb.tuindice.persistence.data.room.schema

object TransactionTable {
	const val TABLE_NAME = "transactions"

	const val ID = "transaction_id"
	const val ACCOUNT_ID = "transaction_account_id"
	const val REFERENCE = "transaction_ref"
	const val TYPE = "transaction_type"
	const val ACTION = "transaction_action"
	const val TIMESTAMP = "transaction_timestamp"
	const val DATA = "transaction_data"
}