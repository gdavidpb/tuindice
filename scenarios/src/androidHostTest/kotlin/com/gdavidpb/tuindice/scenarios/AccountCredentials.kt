package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenarios.fixture.E2eAccount
import java.util.Base64

internal object AccountCredentials {
	/** The base64 of `identifier:password` as the app sends it after `Basic `. */
	fun basic(account: E2eAccount): String =
		Base64.getEncoder().encodeToString("${account.backendIdentifier}:${account.password}".toByteArray())
}
