package com.gdavidpb.tuindice.base.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncSourceReport(
	@SerialName("status") val status: SyncSourceStatus,
	@SerialName("situation") val situation: EnrollmentSituation? = null
)
