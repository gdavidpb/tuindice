package com.gdavidpb.tuindice.base.domain.model.attestation

import okhttp3.Request

interface AttestationParser<T : AttestationPayload> {
	fun parse(request: Request): T
}