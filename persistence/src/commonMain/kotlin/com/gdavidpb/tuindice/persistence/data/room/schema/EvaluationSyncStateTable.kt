package com.gdavidpb.tuindice.persistence.data.room.schema

object EvaluationSyncStateTable {
	const val TABLE_NAME = "evaluation_sync_state"

	const val KEY = "evaluation_sync_state_key"
	const val ANCHOR_REVISION = "evaluation_anchor_revision"

	const val DEFAULT_KEY = "evaluations"
}
