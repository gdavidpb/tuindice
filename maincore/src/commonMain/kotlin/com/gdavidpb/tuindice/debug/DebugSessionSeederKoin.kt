package com.gdavidpb.tuindice.debug

import org.koin.core.Koin

suspend fun Koin.seedDebugSession(seed: DebugSessionSeed) {
	DebugSessionSeeder(
		sessionRepository = get(),
		settingsRepository = get(),
		credentialsRepository = get(),
		syncStatusRepository = get()
	).seed(seed)
}
