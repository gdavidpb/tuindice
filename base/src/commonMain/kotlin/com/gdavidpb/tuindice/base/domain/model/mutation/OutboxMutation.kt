package com.gdavidpb.tuindice.base.domain.model.mutation

interface OutboxMutation {
	val entityType: String
	val entityId: String
	val replaceKey: String
}
