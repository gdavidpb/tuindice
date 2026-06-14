package com.gdavidpb.tuindice.base.domain.model

data class ObservedSyncedSnapshot<out T>(
	val value: T,
	val hasSynced: Boolean
)
