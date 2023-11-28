package com.gdavidpb.tuindice.login.data.api.attestation

import com.gdavidpb.tuindice.base.domain.model.attestation.Attestation
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.Request

class SignInAttestation : Attestation {
	override fun getPayload(request: Request): String {
		return buildJsonObject {
			put("basicToken", request.headers["Authorization"])
			put("refreshToken", request.headers["Re-Authenticate"])
		}.toString()
	}
}