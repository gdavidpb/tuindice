package com.gdavidpb.tuindice.login.data.repository.login.source.api.attestation

import com.gdavidpb.tuindice.base.domain.model.attestation.AttestationParser
import okhttp3.Request

class SignInAttestationParser : AttestationParser<SignInAttestationPayload> {
	override fun parse(request: Request): SignInAttestationPayload {
		return SignInAttestationPayload(
			basicToken = request.headers["Authorization"].toString(),
			refreshToken = request.headers["Re-Authenticate"].toBoolean()
		)
	}
}