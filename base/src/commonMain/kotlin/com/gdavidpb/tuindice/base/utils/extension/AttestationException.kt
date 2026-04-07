package com.gdavidpb.tuindice.base.utils.extension

import com.gdavidpb.tuindice.base.data.model.ATTESTATION_KEY_USER_MISMATCH_CODE
import com.gdavidpb.tuindice.base.data.model.AttestationKeyUserMismatchResponse
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException

suspend fun Throwable.isAttestationKeyUserMismatch(): Boolean {
	if (!isConflict()) return false

	val exception = this as? ClientRequestException ?: return false
	return runCatching {
		exception.response.body<AttestationKeyUserMismatchResponse>().code == ATTESTATION_KEY_USER_MISMATCH_CODE
	}.getOrDefault(false)
}
