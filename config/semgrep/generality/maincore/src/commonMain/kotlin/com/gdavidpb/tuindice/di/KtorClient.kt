package com.gdavidpb.tuindice.di

// Synthetic violation for the generality check (session-recovery-no-noncancellable).
internal fun AuthConfig.installSyntheticBearerAuth() {
	bearer {
		nonCancellableRefresh = true
	}
}
