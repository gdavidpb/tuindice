package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecordSyncMeta(
	@SerialName("last_attempted_sync_at") val lastAttemptedSyncAtMillis: Long = 0L,
	@SerialName("last_successful_sync_at") val lastSuccessfulSyncAtMillis: Long = 0L,
	@SerialName("last_error_code") val lastErrorCode: String? = null
)
