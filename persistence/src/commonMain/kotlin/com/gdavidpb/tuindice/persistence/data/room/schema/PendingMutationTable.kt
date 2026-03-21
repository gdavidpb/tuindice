package com.gdavidpb.tuindice.persistence.data.room.schema

object PendingMutationTable {
	const val TABLE_NAME = "pending_mutations"

	const val MUTATION_ID = "mutation_id"
	const val ENTITY_TYPE = "entity_type"
	const val ENTITY_ID = "entity_id"
	const val REPLACE_KEY = "replace_key"
	const val PAYLOAD = "payload"
	const val EXPECTED_REVISION = "expected_revision"
	const val STATUS = "status"
	const val CREATED_AT = "created_at"
	const val UPDATED_AT = "updated_at"
	const val LAST_ERROR = "last_error"
}

