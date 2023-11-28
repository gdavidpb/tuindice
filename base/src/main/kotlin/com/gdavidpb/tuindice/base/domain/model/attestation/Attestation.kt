package com.gdavidpb.tuindice.base.domain.model.attestation

import okhttp3.Request

interface Attestation<T : AttestationPayload> {
	fun getPayload(request: Request): T
}