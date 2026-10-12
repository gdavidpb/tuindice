// Fixture de semgrep --test para session-recovery.yaml.
// No es código del proyecto: cada línea anotada valida una regla.
package com.gdavidpb.tuindice.di

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

fun installAuthSample(config: AuthConfig) {
	config.bearer {
		// ruleid: session-recovery-no-noncancellable
		nonCancellableRefresh = true
	}
}

fun installAuthDefaultsSample(config: AuthConfig) {
	config.bearer {
		// ok: session-recovery-no-noncancellable
		nonCancellableRefresh = false
		// ok: session-recovery-no-noncancellable
		refreshTokens { null }
	}
}

suspend fun recoverSample(): Int {
	// ruleid: session-recovery-no-noncancellable
	return withContext(NonCancellable) { 1 }
}

suspend fun recoverWithoutShieldSample(): Int {
	// ok: session-recovery-no-noncancellable
	return withContext(Unconfined) { 1 }
}

suspend fun requestFromTheScopeSample(client: HttpClient): HttpResponse {
	// ruleid: session-recovery-no-noncancellable
	return withContext(NonCancellable) { client.get("https://api.tuindice.app/record/v5/sync") }
}
