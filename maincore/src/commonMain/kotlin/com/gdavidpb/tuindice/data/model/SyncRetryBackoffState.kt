package com.gdavidpb.tuindice.data.model

data class SyncRetryBackoffState(
	val retryCount: Int,
	val lastRetryAt: Long,
	val retryBackoffUntil: Long
)
