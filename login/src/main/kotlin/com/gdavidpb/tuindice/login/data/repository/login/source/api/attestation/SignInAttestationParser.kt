package com.gdavidpb.tuindice.login.data.repository.login.source.api.attestation

import com.gdavidpb.tuindice.base.data.repository.source.api.retrofit.AttestationParser
import okhttp3.Request

class SignInAttestationParser : AttestationParser<SignInAttestationPayload> {
	override fun parse(request: Request): SignInAttestationPayload {
		return SignInAttestationPayload(
			basicToken = request.headers["Authorization"].toString()
		)
	}
}