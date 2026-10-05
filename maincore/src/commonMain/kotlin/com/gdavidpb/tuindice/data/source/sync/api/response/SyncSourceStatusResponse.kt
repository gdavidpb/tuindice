package com.gdavidpb.tuindice.data.source.sync.api.response

import kotlinx.serialization.Serializable

// Serialized by hand so a value this build does not know decodes to [Unknown] instead of failing
// the whole sync parse.
@Serializable(with = SyncSourceStatusResponseSerializer::class)
enum class SyncSourceStatusResponse(val wireName: String?) {
	Success("success"),
	Unavailable("unavailable"),
	NotAttempted("not_attempted"),
	NotEnrolled("not_enrolled"),
	Unknown(null)
}
