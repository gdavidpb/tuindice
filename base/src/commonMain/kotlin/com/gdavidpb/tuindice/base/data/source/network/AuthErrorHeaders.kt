package com.gdavidpb.tuindice.base.data.source.network

object AuthErrorHeaders {
	const val HEADER = "X-Auth-Error"
	const val INSUFFICIENT_SCOPE = "insufficient_scope"
	const val SESSION_SUPERSEDED = "session_superseded"
	const val REFRESH_TOKEN_MISMATCH = "refresh_token_mismatch"
	const val TOKEN_EXPIRED = "token_expired"
}
