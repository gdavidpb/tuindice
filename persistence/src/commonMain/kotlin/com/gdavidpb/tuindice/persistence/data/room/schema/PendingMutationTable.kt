package com.gdavidpb.tuindice.persistence.data.room.schema

object PendingMutationTable {
	const val TABLE_NAME = "pending_mutations"
	const val STORE_REPLACE_KEY_INDEX = "idx_pending_mutations_store_replace_key"
	const val STORE_SCOPE_CREATED_AT_INDEX = "idx_pending_mutations_store_scope_created_at"
	const val STORE_ENTITY_TYPE_ENTITY_ID_INDEX = "idx_pending_mutations_store_entity_type_entity_id"

	const val MUTATION_ID = "mutation_id"
	const val STORE_ID = "store_id"
	const val SCOPE_KEY = "scope_key"
	const val ENTITY_TYPE = "entity_type"
	const val ENTITY_ID = "entity_id"
	const val REPLACE_KEY = "replace_key"
	const val PAYLOAD = "payload"
	const val PRECONDITION_TYPE = "precondition_type"
	const val EXPECTED_REVISION = "expected_revision"
	const val STATUS = "status"
	const val CREATED_AT = "created_at"
	const val UPDATED_AT = "updated_at"
	const val LAST_ERROR = "last_error"
}
