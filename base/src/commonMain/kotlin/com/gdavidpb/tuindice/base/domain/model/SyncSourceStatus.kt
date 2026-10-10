package com.gdavidpb.tuindice.base.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SyncSourceStatus {
	@SerialName("success")
	Success,

	@SerialName("unavailable")
	Unavailable,

	@SerialName("not_attempted")
	NotAttempted,

	// The university reports no enrollment for the current term (never annulled-or-not: the cause,
	// when DST names one, travels in [SyncSourceReport.situation]).
	@SerialName("not_enrolled")
	NotEnrolled,

	// A value this build does not know. It reads as "nothing to report", never as an alert.
	@SerialName("unknown")
	Unknown
}
