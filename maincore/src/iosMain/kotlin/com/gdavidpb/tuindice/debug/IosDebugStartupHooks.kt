package com.gdavidpb.tuindice.debug

import org.koin.core.Koin

object IosDebugStartupHooks {
	suspend fun run(name: String, koin: Koin, mainSectionName: String) {
		val hook = when (name) {
			IosAuthenticatedWizardCompleteStartupHook.NAME -> IosAuthenticatedWizardCompleteStartupHook
			else -> error("Unsupported debug startup hook: $name")
		}

		hook.run(koin = koin, mainSectionName = mainSectionName)
	}
}
