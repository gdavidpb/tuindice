package com.gdavidpb.tuindice.login.data.repository.login.source.api.attestation

import com.gdavidpb.tuindice.base.domain.model.attestation.Attestation
import okhttp3.Request

class SignInAttestation : Attestation<SignInAttestationPayload> {
	override fun getPayload(request: Request): SignInAttestationPayload {
		return SignInAttestationPayload(
			basicToken = request.headers["Authorization"].toString(),
			refreshToken = request.headers["Re-Authenticate"].toBoolean()
		)
	}
}