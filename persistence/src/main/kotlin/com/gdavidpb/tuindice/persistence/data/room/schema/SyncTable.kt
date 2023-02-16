package com.gdavidpb.tuindice.persistence.data.room.schema

object SyncTable {
	const val TABLE_NAME = "syncs"

	const val ID = "sync_id"
	const val ENTITY_ID = "sync_entity_id"
	const val ENTITY_TYPE = "sync_entity_type"
	const val ENTITY_ACTION = "sync_entity_action"
	const val ENTITY_DATA = "sync_entity_data"
	const val TIMESTAMP = "sync_timestamp"
}