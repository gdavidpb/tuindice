package com.gdavidpb.tuindice.base.data.model

import com.gdavidpb.tuindice.base.domain.model.AttestationAuthorization
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AttestationAuthorizationRequest(
	@SerialName("session_id") val sessionId: String? = null,
	@SerialName("refresh_token") val refreshToken: String? = null
)

fun AttestationAuthorization.toRequestAuthorizationOrNull(): AttestationAuthorizationRequest? {
	return when (this) {
		is AttestationAuthorization.Bearer ->
			null

		AttestationAuthorization.CurrentSession ->
			null

		is AttestationAuthorization.Session ->
			AttestationAuthorizationRequest(
				sessionId = sessionId,
				refreshToken = refreshToken
			)
	}
}
