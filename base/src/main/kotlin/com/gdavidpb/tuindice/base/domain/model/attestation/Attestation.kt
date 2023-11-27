package com.gdavidpb.tuindice.base.domain.model.attestation

import okhttp3.Request

interface Attestation {
	fun getPayload(request: Request): String
}