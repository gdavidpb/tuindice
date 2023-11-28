package com.gdavidpb.tuindice.base.data.repository.source.api.retrofit

import com.gdavidpb.tuindice.base.domain.model.attestation.AttestationPayload
import okhttp3.Request

interface AttestationParser<T : AttestationPayload> {
	fun parse(request: Request): T
}